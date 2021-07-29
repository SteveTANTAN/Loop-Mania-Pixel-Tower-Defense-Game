package unsw.loopmania;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.control.Label;
import java.util.EnumMap;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.IOException;

/**
 * the draggable types.
 * If you add more draggable types, add an enum value here.
 * This is so we can see what type is being dragged.
 */
enum DRAGGABLE_TYPE{
    CARD,
    ITEM
}




/**
 * equiment type
 */ 
enum ITEMS_TYPE{
    SWORD, STAKE, STAFF, ARMOUR, SHIELD, HELMET, THEONERING, HEALTHPOTION
}

/**
 * A JavaFX controller for the world.
 * 
 * All event handlers and the timeline in JavaFX run on the JavaFX application thread:
 *     https://examples.javacodegeeks.com/desktop-java/javafx/javafx-concurrency-example/
 *     Note in https://openjfx.io/javadoc/11/javafx.graphics/javafx/application/Application.html under heading "Threading", it specifies animation timelines are run in the application thread.
 * This means that the starter code does not need locks (mutexes) for resources shared between the timeline KeyFrame, and all of the  event handlers (including between different event handlers).
 * This will make the game easier for you to implement. However, if you add time-consuming processes to this, the game may lag or become choppy.
 * 
 * If you need to implement time-consuming processes, we recommend:
 *     using Task https://openjfx.io/javadoc/11/javafx.graphics/javafx/concurrent/Task.html by itself or within a Service https://openjfx.io/javadoc/11/javafx.graphics/javafx/concurrent/Service.html
 * 
 *     Tasks ensure that any changes to public properties, change notifications for errors or cancellation, event handlers, and states occur on the JavaFX Application thread,
 *         so is a better alternative to using a basic Java Thread: https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm
 *     The Service class is used for executing/reusing tasks. You can run tasks without Service, however, if you don't need to reuse it.
 *
 * If you implement time-consuming processes in a Task or thread, you may need to implement locks on resources shared with the application thread (i.e. Timeline KeyFrame and drag Event handlers).
 * You can check whether code is running on the JavaFX application thread by running the helper method printThreadingNotes in this class.
 * 
 * NOTE: http://tutorials.jenkov.com/javafx/concurrency.html and https://www.developer.com/design/multithreading-in-javafx/#:~:text=JavaFX%20has%20a%20unique%20set,in%20the%20JavaFX%20Application%20Thread.
 * 
 * If you need to delay some code but it is not long-running, consider using Platform.runLater https://openjfx.io/javadoc/11/javafx.graphics/javafx/application/Platform.html#runLater(java.lang.Runnable)
 *     This is run on the JavaFX application thread when it has enough time.
 */
public class LoopManiaWorldController {

    /**
     * squares gridpane includes path images, enemies, character, empty grass, buildings
     */
    @FXML
    private GridPane squares;

    /**
     * cards gridpane includes cards and the ground underneath the cards
     */
    @FXML
    private GridPane cards;

    /**
     * anchorPaneRoot is the "background". It is useful since anchorPaneRoot stretches over the entire game world,
     * so we can detect dragging of cards/items over this and accordingly update DragIcon coordinates
     */
    @FXML
    private AnchorPane anchorPaneRoot;

    /**
     * equippedItems gridpane is for equipped items (e.g. swords, shield, axe)
     */
    @FXML
    private GridPane equippedItems;

    @FXML
    private GridPane unequippedInventory;

    @FXML
    private GridPane characterInfo;
    
    @FXML
    private AnchorPane stats;

    @FXML
    private Label roundsNumLabel;

    @FXML
    private Label winningCondition;

    @FXML
    private HBox saveGame;

    @FXML
    private Label gameStatus;

    @FXML
    private HBox exitMenu;

    // all image views including tiles, character, enemies, cards... even though cards in separate gridpane...
    private List<ImageView> entityImages;

    /**
     * when we drag a card/item, the picture for whatever we're dragging is set here and we actually drag this node
     */
    private DragIcon draggedEntity;

    private boolean isPaused;
    private LoopManiaWorld world;

    /**
     * runs the periodic game logic - second-by-second moving of character through maze, as well as enemies, and running of battles
     */
    private Timeline timeline;

    private Image towerCardImage;
    private Image zombiePitCardImage;
    private Image vampireCastleCardImage;
    private Image barrackCardImage;
    private Image villageCardImage;
    private Image trapCardImage;
    private Image campfireCardImage;
    private Image basicEnemyImage;
    private Image zombieImage;
    private Image vampireImage;
    private Image swordImage;

    private Image towerBuildingImage;
    private Image zombiePitBuildingImage;
    private Image vampireCastleBuildingImage;
    private Image barrackBuildingImage;
    private Image villageBuildingImage;
    private Image trapBuildingImage;
    private Image campfireBuildingImage;
    private Image stakeImage;
    private Image staffImage;
    private Image armourImage;
    private Image shieldImage;
    private Image helmetImage;
    private Image theOneRingImage;
    private Image emptyTheOneRingImage;
    
    private Image heroCastlImage;

    private Image heartImage;
    private Image goldPileslImage;
    /**
     * the image currently being dragged, if there is one, otherwise null.
     * Holding the ImageView being dragged allows us to spawn it again in the drop location if appropriate.
     */
    private ImageView currentlyDraggedImage;
    
    /**
     * null if nothing being dragged, or the type of item being dragged
     */
    private DRAGGABLE_TYPE currentlyDraggedType;

    /**
     * mapping from draggable type enum CARD/TYPE to the event handler triggered when the draggable type is dropped over its appropriate gridpane
     */
    private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> gridPaneSetOnDragDropped;
    /**
     * mapping from draggable type enum CARD/TYPE to the event handler triggered when the draggable type is dragged over the background
     */
    private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> anchorPaneRootSetOnDragOver;
    /**
     * mapping from draggable type enum CARD/TYPE to the event handler triggered when the draggable type is dropped in the background
     */
    private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> anchorPaneRootSetOnDragDropped;
    /**
     * mapping from draggable type enum CARD/TYPE to the event handler triggered when the draggable type is dragged into the boundaries of its appropriate gridpane
     */
    private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> gridPaneNodeSetOnDragEntered;
    /**
     * mapping from draggable type enum CARD/TYPE to the event handler triggered when the draggable type is dragged outside of the boundaries of its appropriate gridpane
     */
    private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> gridPaneNodeSetOnDragExited;

    /**
     * object handling switching to the main menu
     */
    private MenuSwitcher mainMenuSwitcher;

    private HeroCastleBuilding heroCastleBuilding;


    private CardDescription cardDescription;

    private Stage primaryStage;

    private boolean isSurvivalMode;

    private Label healthPointText;
    private Label goldText;
    private Label expText;
    
    private MainMenuController mainMenuController;

    /**
     * @param world world object loaded from file
     * @param initialEntities the initial JavaFX nodes (ImageViews) which should be loaded into the GUI
     */
    public LoopManiaWorldController(LoopManiaWorld world, List<ImageView> initialEntities) {
        this.world = world;
        entityImages = new ArrayList<>(initialEntities);
        towerCardImage = new Image((new File("src/images/tower_card.png")).toURI().toString());
        zombiePitCardImage = new Image((new File("src/images/zombie_pit_card.png")).toURI().toString());
        vampireCastleCardImage = new Image((new File("src/images/vampire_castle_card.png")).toURI().toString());
        barrackCardImage = new Image((new File("src/images/barracks_card.png")).toURI().toString());
        villageCardImage = new Image((new File("src/images/village_card.png")).toURI().toString());
        trapCardImage = new Image((new File("src/images/trap_card.png")).toURI().toString());
        campfireCardImage = new Image((new File("src/images/campfire_card.png")).toURI().toString());
        basicEnemyImage = new Image((new File("src/images/slug.png")).toURI().toString());
        zombieImage = new Image((new File("src/images/zombie.png")).toURI().toString());
        vampireImage = new Image((new File("src/images/vampire.png")).toURI().toString());
        swordImage = new Image((new File("src/images/basic_sword.png")).toURI().toString());

        towerBuildingImage = new Image((new File("src/images/tower.png")).toURI().toString());
        zombiePitBuildingImage = new Image((new File("src/images/zombie_pit.png")).toURI().toString());
        vampireCastleBuildingImage = new Image((new File("src/images/vampire_castle_building_purple_background.png")).toURI().toString());
        barrackBuildingImage = new Image((new File("src/images/barracks.png")).toURI().toString());
        villageBuildingImage = new Image((new File("src/images/village.png")).toURI().toString());
        trapBuildingImage = new Image((new File("src/images/trap.png")).toURI().toString());
        campfireBuildingImage = new Image((new File("src/images/campfire.png")).toURI().toString());
        heroCastlImage = new Image((new File("src/images/heros_castle.png")).toURI().toString());

        stakeImage = new Image((new File("src/images/stake.png")).toURI().toString());
        staffImage = new Image((new File("src/images/staff.png")).toURI().toString());
        armourImage = new Image((new File("src/images/armour.png")).toURI().toString());
        shieldImage = new Image((new File("src/images/shield.png")).toURI().toString());
        helmetImage = new Image((new File("src/images/helmet.png")).toURI().toString());
        theOneRingImage = new Image((new File("src/images/the_one_ring.png")).toURI().toString());
        emptyTheOneRingImage = new Image((new File("src/images/src_images_the_one_ring.png")).toURI().toString());
        heartImage= new Image((new File("src/images/heart.png")).toURI().toString());
        goldPileslImage = new Image((new File("src/images/gold_pile.png")).toURI().toString());
        currentlyDraggedImage = null;
        currentlyDraggedType = null;

        // initialize them all...
        gridPaneSetOnDragDropped = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
        anchorPaneRootSetOnDragOver = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
        anchorPaneRootSetOnDragDropped = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
        gridPaneNodeSetOnDragEntered = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
        gridPaneNodeSetOnDragExited = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
    }

    @FXML
    public void initialize() {
        
        Image pathTilesImage = new Image((new File("src/images/32x32GrassAndDirtPath.png")).toURI().toString());
        Image inventorySlotImage = new Image((new File("src/images/empty_slot.png")).toURI().toString());
        Rectangle2D imagePart = new Rectangle2D(0, 0, 32, 32);

        // Add the ground first so it is below all other entities (inculding all the twists and turns)
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                ImageView groundView = new ImageView(pathTilesImage);
                groundView.setViewport(imagePart);
                squares.add(groundView, x, y);
            }
        }

        // load entities loaded from the file in the loader into the squares gridpane
        for (ImageView entity : entityImages){
            squares.getChildren().add(entity);
        }

        // add the ground underneath the cards
        for (int x=0; x<world.getWidth(); x++){
            ImageView groundView = new ImageView(pathTilesImage);
            groundView.setViewport(imagePart);
            cards.add(groundView, x, 0);
        }

        // add the empty slot images for the unequipped inventory
        for (int x=0; x<LoopManiaWorld.unequippedInventoryWidth; x++){
            for (int y=0; y<LoopManiaWorld.unequippedInventoryHeight; y++){
                ImageView emptySlotView = new ImageView(inventorySlotImage);
                unequippedInventory.add(emptySlotView, x, y);
            }
        }

        // create the draggable icon
        draggedEntity = new DragIcon();
        draggedEntity.setVisible(false);
        draggedEntity.setOpacity(0.7);
        anchorPaneRoot.getChildren().add(draggedEntity);

    }

    /**
     * create and run the timer
     */
    public void startTimer(){
        System.out.println("starting timer");
        isPaused = false;
        gameStatus.setText("");
        // trigger adding code to process main game logic to queue. JavaFX will target framerate of 0.3 seconds
        if(timeline == null){
            timeline = new Timeline(new KeyFrame(Duration.seconds(0.3), event -> {
                // check whether the position is in the Hero's Castle
                if(world.getRoundsNum()== 0){
                    world.addRoundsNum();
                }else{
                    if(heroCastleBuilding.work(world.getCharacter(),this)) return;
                }
                world.runTickMoves();

                Item item = world.getLastUnequippedInventoryItem();
                if (item != null) {
                    onLoad(item);
                }
                Card card = world.getLastCardEntity();
                if (card != null) {
                    onLoad(card);
                }

                if (world.GetUsedTheOneRing()) {
                    setUsedTheOneRingImage();
                }
                if (world.getIsDead()) {
                    pause();
                }

                List<BasicEnemy> deadEnemies = world.buildingFunction();
                for (BasicEnemy e: deadEnemies){
                    reactToEnemyDefeat(e);
                }

                List<BasicEnemy> defeatedEnemies = world.runBattles();
                for (BasicEnemy e: defeatedEnemies){
                    reactToEnemyDefeat(e);
                }
                List<BasicEnemy> newEnemies = world.possiblySpawnEnemies();
                for (BasicEnemy newEnemy: newEnemies){
                    onLoad(newEnemy);
                }
                // update the display
                updateDisplay();
                printThreadingNotes("HANDLED TIMER");
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
 
            // build up the hero castle building
            heroCastleBuilding = new HeroCastleBuilding(new SimpleIntegerProperty(0), 
             new SimpleIntegerProperty(0), this);
            addHeroCastle(heroCastleBuilding);

            // build up the card descripton
            cardDescription = new CardDescription(this);

            roundsNumLabel.setPadding(new Insets(0,0,0,10));
            winningCondition.setWrapText(true);
            winningCondition.setText("Winning Conditions:Looping reaches 100 & Gold reaches 600 & EXP reaches 1000");

            saveGame.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent arg0) {
                    // save game
                    System.out.print("save game");
                }
            });

            exitMenu.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent arg0) {
                    // exit to main menu
                    switchToMainMenu();
                }
            });
            // init the display of heath point, gold and exp
            characterInfo.getChildren().clear();
            characterInfo.setPadding(new Insets(10));
            characterInfo.setVgap(10);
            HBox hBox = new HBox();
            hBox.setSpacing(10);
            hBox.setAlignment(Pos.CENTER_LEFT);
            ImageView imageView = new ImageView();
            imageView.setImage(heartImage);
            healthPointText = new Label();
            healthPointText.setFont(Font.font("Microsoft YaHei",FontWeight.BOLD,16));
            healthPointText.setTextFill(new Color(0.9,0,0,1));
            hBox.getChildren().addAll(imageView,healthPointText);
            characterInfo.add(hBox, 0,0);
            hBox = new HBox();
            hBox.setSpacing(10);
            hBox.setAlignment(Pos.CENTER_LEFT);
            imageView = new ImageView();
            imageView.setImage(goldPileslImage);
            goldText = new Label();
            goldText.setFont(Font.font("Microsoft YaHei",FontWeight.BOLD,16));
            goldText.setTextFill(new Color(0,0.9,0,1));
            hBox.getChildren().addAll(imageView,goldText);
            characterInfo.add(hBox, 0,1);
            hBox = new HBox();
            hBox.setSpacing(10);
            hBox.setAlignment(Pos.CENTER_LEFT);
            Label label = new Label("EXP");
            label.setFont(Font.font("Microsoft YaHei",FontWeight.BOLD,16));
            label.setTextFill(new Color(0.64,0.28,0.64,1));
            expText = new Label();
            expText.setFont(Font.font("Microsoft YaHei",FontWeight.BOLD,16));
            expText.setTextFill(new Color(0.64,0.28,0.64,1));
            hBox.getChildren().addAll(label,expText);
            characterInfo.add(hBox, 0,2);
        }
        timeline.play();
    }

    public void updateDisplay(){
        // update the dispaly of number of the round
        roundsNumLabel.setText(String.format("ROUND: %d/100", world.getRoundsNum()));
        healthPointText.setText(String.format("%d/100", world.getCharacter().getHealth()));
        goldText.setText(String.format("%d", world.getCharacter().getGold()));
        expText.setText(String.format("%d", world.getCharacter().getEXP()));
    }

    /**
     * pause the execution of the game loop
     * the human player can still drag and drop items during the game pause
     */
    public void pause(){
        isPaused = true;
        System.out.println("pausing");
        gameStatus.setText("PAUSE");
        if(timeline == null) return;
        timeline.stop();
    }

    /**
     *  terminate pause
     */
    public void terminate(){
        pause();
    }

    /**
     * pair the entity an view so that the view copies the movements of the entity.
     * add view to list of entity images
     * @param entity backend entity to be paired with view
     * @param view frontend imageview to be paired with backend entity
     */
    private void addEntity(Entity entity, ImageView view) {
        trackPosition(entity, view);
        entityImages.add(view);
    }
    
    /**
     * load a card from the world by name, and pair it with an image in the GUI
     */
    private void loadCardByType(CARDS_TYPE cards_TYPE) {
        // DONE = load more types of card
        Card card = world.loadCard(cards_TYPE);
        onLoad(card);
    }

    /**
     * load a item from the world, and pair it with an image in the GUI
     */
    public void loadItemByType(ITEMS_TYPE itemType) {
        // start by getting first available coordinates
        Item item = world.addUnequippedItem(itemType);
        onLoad(item);
    }


    /**
     * run GUI events after an enemy is defeated, such as spawning items/experience/gold
     * @param enemy defeated enemy for which we should react to the death of
     */
    private void reactToEnemyDefeat(BasicEnemy enemy){
        // react to character defeating an enemy
        // in starter code, spawning extra card/weapon...
        // provide different benefits to defeating the enemy based on the type of enemy

        // a type of card is looted from the enemies with a specified probability
        int index = new Random().nextInt(100);
        if(index < 42){
            index = index / 6;
            loadCardByType(CARDS_TYPE.values()[index]);
        }

        int rd = new Random().nextInt(100);
        if (rd < 20) {
            loadItemByType(ITEMS_TYPE.SWORD);    
        } else if (rd >= 20 && rd < 35) {
            loadItemByType(ITEMS_TYPE.STAKE);  
        } else if (rd >= 35 && rd < 45) {
            loadItemByType(ITEMS_TYPE.STAFF);  
        } else if (rd >= 45 && rd < 55) {
            loadItemByType(ITEMS_TYPE.ARMOUR);  
        } else if (rd >= 55 && rd < 70) {
            loadItemByType(ITEMS_TYPE.SHIELD);  
        } else if (rd >= 70 && rd < 85) {
            loadItemByType(ITEMS_TYPE.HELMET);  
        } else if (rd >= 85 && rd < 90) {
            loadItemByType(ITEMS_TYPE.THEONERING);  
        } else if (rd >= 90 && rd <= 100) {
            world.getCharacter().setHealth(world.getCharacter().getHealth() + 50); // health potion
        }
        world.getCharacter().setGold(world.getCharacter().getGold() + enemy.getGoldDefeated());
        world.getCharacter().setEXP(world.getCharacter().getEXP() + enemy.getEXP());
    }

    /**
     * load a card into the GUI.
     * Particularly, we must connect to the drag detection event handler,
     * and load the image into the cards GridPane.
     * @param vampireCastleCard
     */
    private void onLoad(Card card) {
        ImageView view = null;
        if(card instanceof TowerCard){
            view = new ImageView(towerCardImage);
        }
        else if(card instanceof ZombiePitCard){
            view = new ImageView(zombiePitCardImage);
        }
        else if(card instanceof VampireCastleCard){
            view = new ImageView(vampireCastleCardImage);
        }
        else if(card instanceof BarrackCard){
            view = new ImageView(barrackCardImage);
        }
        else if(card instanceof VillageCard){
            view = new ImageView(villageCardImage);
        }
        else if(card instanceof TrapCard){
            view = new ImageView(trapCardImage);
        }
        else if(card instanceof CampfireCard){
            view = new ImageView(campfireCardImage);
        }

        // FROM https://stackoverflow.com/questions/41088095/javafx-drag-and-drop-to-gridpane
        // note target setOnDragOver and setOnDragEntered defined in initialize method
        addDragEventHandlers(view, DRAGGABLE_TYPE.CARD, cards, squares);

        addEntity(card, view);
        cards.getChildren().add(view);
        view.setOnMouseEntered(new CardMouseHoverHandler(card.getName(),card.getDescription()));
        view.setOnMouseExited(new CardMouseLeaveHandler());
    }

    /**
     * load a item into the GUI.
     * Particularly, we must connect to the drag detection event handler,
     * and load the image into the unequippedInventory GridPane.
     * @param item
     */
    private void onLoad(Item item) {
        ImageView view = null;
        if (item instanceof Sword) {
            view = new ImageView(swordImage);
        } else if (item instanceof Stake) {
            view = new ImageView(stakeImage);
        } else if (item instanceof Staff) {
            view = new ImageView(staffImage);
        } else if (item instanceof Armour) {
            view = new ImageView(armourImage);
        } else if (item instanceof Shield) {
            view = new ImageView(shieldImage);
        } else if (item instanceof Helmet) {
            view = new ImageView(helmetImage);
        } else if (item instanceof TheOneRing) {
            view = new ImageView(theOneRingImage);
        }

        addDragEventHandlers(view, DRAGGABLE_TYPE.ITEM, unequippedInventory, equippedItems);
        addEntity(item, view);
        unequippedInventory.getChildren().add(view);
    }

    /**
     * load an enemy into the GUI
     * @param enemy
     */
    public void onLoad(BasicEnemy enemy) {
        ImageView view = null;
        if(enemy instanceof Zombie){
            view = new ImageView(zombieImage);
        }
        else if(enemy instanceof Vampire){
            view = new ImageView(vampireImage);
        }
        else if(enemy instanceof BasicEnemy){
            view = new ImageView(basicEnemyImage);
        }
        addEntity(enemy, view);
        squares.getChildren().add(view);
    }

    /**
     * load a hero castle into the GUI
     */
    public void addHeroCastle(Entity heroEntity) {
        ImageView view = new ImageView(heroCastlImage);
        addEntity(heroEntity, view);
        squares.getChildren().add(view);
    }

    /**
     * load a building into the GUI
     * @param building
     */
    private void onLoad(Building building){
        ImageView view = null;
        if(building instanceof TowerBuilding){
            view = new ImageView(towerBuildingImage);
        }
        else if(building instanceof ZombiePitBuilding){
            view = new ImageView(zombiePitBuildingImage);
        }
        else if(building instanceof VampireCastleBuilding){
            view = new ImageView(vampireCastleBuildingImage);
        }
        else if(building instanceof BarrackBuilding){
            view = new ImageView(barrackBuildingImage);
        }
        else if(building instanceof VillageBuilding){
            view = new ImageView(villageBuildingImage);
        }
        else if(building instanceof TrapBuilding){
            view = new ImageView(trapBuildingImage);
        }
        else if(building instanceof CampfireBuilding){
            view = new ImageView(campfireBuildingImage);
        }
        addEntity(building, view);
        squares.getChildren().add(view);
    }

    /**
     * add drag event handlers for dropping into gridpanes, dragging over the background, dropping over the background.
     * These are not attached to invidual items such as swords/cards.
     * @param draggableType the type being dragged - card or item
     * @param sourceGridPane the gridpane being dragged from
     * @param targetGridPane the gridpane the human player should be dragging to (but we of course cannot guarantee they will do so)
     */
    private void buildNonEntityDragHandlers(DRAGGABLE_TYPE draggableType, GridPane sourceGridPane, GridPane targetGridPane){
        // TODO = be more selective about where something can be dropped
        // for example, in the specification, villages can only be dropped on path, whilst vampire castles cannot go on the path

        gridPaneSetOnDragDropped.put(draggableType, new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                // TODO = for being more selective about where something can be dropped, consider applying additional if-statement logic
                /*
                 *you might want to design the application so dropping at an invalid location drops at the most recent valid location hovered over,
                 * or simply allow the card/item to return to its slot (the latter is easier, as you won't have to store the last valid drop location!)
                 */
                boolean isValid = true;  // it means whether the position is valid
                if (currentlyDraggedType == draggableType){
                    // problem = event is drop completed is false when should be true...
                    // https://bugs.openjdk.java.net/browse/JDK-8117019
                    // putting drop completed at start not making complete on VLAB...

                    //Data dropped
                    //If there is an image on the dragboard, read it and use it
                    Dragboard db = event.getDragboard();
                    Node node = event.getPickResult().getIntersectedNode();
                    if(node != targetGridPane && db.hasImage()){

                        Integer cIndex = GridPane.getColumnIndex(node);
                        Integer rIndex = GridPane.getRowIndex(node);
                        int x = cIndex == null ? 0 : cIndex;
                        int y = rIndex == null ? 0 : rIndex;
                        //Places at 0,0 - will need to take coordinates once that is implemented
                        ImageView image = new ImageView(db.getImage());

                        int nodeX = GridPane.getColumnIndex(currentlyDraggedImage);
                        int nodeY = GridPane.getRowIndex(currentlyDraggedImage);

                        switch (draggableType){
                            case CARD:
                                // check whether the position is valid
                                if(currentlyDraggedImage.getImage() == towerCardImage ||
                                currentlyDraggedImage.getImage() == zombiePitCardImage ||
                                currentlyDraggedImage.getImage() == vampireCastleCardImage ||
                                currentlyDraggedImage.getImage() == campfireCardImage){
                                    if(!world.checkAdjacentPath(x, y)){
                                        isValid = false;
                                    }
                                }
                                else{
                                    if(!world.checkInPath(x, y)){
                                        isValid = false;
                                    }
                                }
                                
                                if(isValid){
                                    removeDraggableDragEventHandlers(draggableType, targetGridPane);
                                    // DONE = spawn a building here of different types
                                    Building newBuilding = convertCardToBuildingByCoordinates(nodeX, nodeY, x, y);
                                    onLoad(newBuilding);
                                }else{
                                    for (Node mNode: targetGridPane.getChildren()){
                                        if(mNode.getOpacity() < 1){
                                            mNode.setOpacity(1);
                                        }
                                    }
                                }
                                break;
                            case ITEM:
                                removeDraggableDragEventHandlers(draggableType, targetGridPane);
                                // spawn an item in the new location. 
                                Item currentItem = world.GetEquippedFromUnequippedByCoordinates(nodeX, nodeY, x, y);
                                if (currentItem != null) {
                                    targetGridPane.add(image, x, y, 1, 1);
                                    world.setCharacterEquipment(world.getCharacter(), currentItem);
                                } else {
                                    for (Node mNode: targetGridPane.getChildren()){
                                        if(mNode.getOpacity() < 1){
                                            mNode.setOpacity(1);
                                        }
                                    }
                                    sourceGridPane.add(image, nodeX, nodeY);
                                    addDragEventHandlers(image, DRAGGABLE_TYPE.ITEM, unequippedInventory, equippedItems);
                                }
                                break;
                            default:
                                break;
                        }
                        
                        if(isValid){
                            draggedEntity.setVisible(false);
                            draggedEntity.setMouseTransparent(false);
                            // remove drag event handlers before setting currently dragged image to null
                            currentlyDraggedImage = null;
                            currentlyDraggedType = null;
                            printThreadingNotes("DRAG DROPPED ON GRIDPANE HANDLED");
                        }
                    }
                }
                if(isValid){
                    event.setDropCompleted(true);
                    // consuming prevents the propagation of the event to the anchorPaneRoot (as a sub-node of anchorPaneRoot, GridPane is prioritized)
                    // https://openjfx.io/javadoc/11/javafx.base/javafx/event/Event.html#consume()
                    // to understand this in full detail, ask your tutor or read https://docs.oracle.com/javase/8/javafx/events-tutorial/processing.htm
                    event.consume();
                }
            }
        });

        // this doesn't fire when we drag over GridPane because in the event handler for dragging over GridPanes, we consume the event
        anchorPaneRootSetOnDragOver.put(draggableType, new EventHandler<DragEvent>(){
            // https://github.com/joelgraff/java_fx_node_link_demo/blob/master/Draggable_Node/DraggableNodeDemo/src/application/RootLayout.java#L110
            @Override
            public void handle(DragEvent event) {
                if (currentlyDraggedType == draggableType){
                    if(event.getGestureSource() != anchorPaneRoot && event.getDragboard().hasImage()){
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                if (currentlyDraggedType != null){
                    draggedEntity.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                }
                event.consume();
            }
        });

        // this doesn't fire when we drop over GridPane because in the event handler for dropping over GridPanes, we consume the event
        anchorPaneRootSetOnDragDropped.put(draggableType, new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (currentlyDraggedType == draggableType){
                    //Data dropped
                    //If there is an image on the dragboard, read it and use it
                    Dragboard db = event.getDragboard();
                    Node node = event.getPickResult().getIntersectedNode();
                    if(node != anchorPaneRoot && db.hasImage()){
                        //Places at 0,0 - will need to take coordinates once that is implemented
                        currentlyDraggedImage.setVisible(true);
                        draggedEntity.setVisible(false);
                        draggedEntity.setMouseTransparent(false);
                        // remove drag event handlers before setting currently dragged image to null
                        removeDraggableDragEventHandlers(draggableType, targetGridPane);
                        
                        currentlyDraggedImage = null;
                        currentlyDraggedType = null;
                    }
                }
                //let the source know whether the image was successfully transferred and used
                event.setDropCompleted(true);
                event.consume();
            }
        });
    }

    /**
     * remove the card from the world, and spawn and return a building instead where the card was dropped
     * @param cardNodeX the x coordinate of the card which was dragged, from 0 to width-1
     * @param cardNodeY the y coordinate of the card which was dragged (in starter code this is 0 as only 1 row of cards)
     * @param buildingNodeX the x coordinate of the drop location for the card, where the building will spawn, from 0 to width-1
     * @param buildingNodeY the y coordinate of the drop location for the card, where the building will spawn, from 0 to height-1
     * @return building entity returned from the world
     */
    private Building convertCardToBuildingByCoordinates(int cardNodeX, int cardNodeY, int buildingNodeX, int buildingNodeY) {
        return world.convertCardToBuildingByCoordinates(cardNodeX, cardNodeY, buildingNodeX, buildingNodeY);
    }

    /**
     * remove an item from the unequipped inventory by its x and y coordinates in the unequipped inventory gridpane
     * @param nodeX x coordinate from 0 to unequippedInventoryWidth-1
     * @param nodeY y coordinate from 0 to unequippedInventoryHeight-1
     */
    //private void removeItemByCoordinates(int nodeX, int nodeY) {
    //   world.removeUnequippedInventoryItemByCoordinates(nodeX, nodeY);
    //}

    /**
     * add drag event handlers to an ImageView
     * @param view the view to attach drag event handlers to
     * @param draggableType the type of item being dragged - card or item
     * @param sourceGridPane the relevant gridpane from which the entity would be dragged
     * @param targetGridPane the relevant gridpane to which the entity would be dragged to
     */
    private void addDragEventHandlers(ImageView view, DRAGGABLE_TYPE draggableType, GridPane sourceGridPane, GridPane targetGridPane){
        view.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                currentlyDraggedImage = view; // set image currently being dragged, so squares setOnDragEntered can detect it...
                currentlyDraggedType = draggableType;
                //Drag was detected, start drap-and-drop gesture
                //Allow any transfer node
                Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
    
                //Put ImageView on dragboard
                ClipboardContent cbContent = new ClipboardContent();
                cbContent.putImage(view.getImage());
                db.setContent(cbContent);
                view.setVisible(false);

                buildNonEntityDragHandlers(draggableType, sourceGridPane, targetGridPane);

                draggedEntity.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                switch (draggableType){
                    case CARD:
                        draggedEntity.setImage(view.getImage());
                        break;
                    case ITEM:
                        draggedEntity.setImage(view.getImage());
                        break;
                    default:
                        break;
                }
                
                draggedEntity.setVisible(true);
                draggedEntity.setMouseTransparent(true);
                draggedEntity.toFront();

                // IMPORTANT!!!
                // to be able to remove event handlers, need to use addEventHandler
                // https://stackoverflow.com/a/67283792
                targetGridPane.addEventHandler(DragEvent.DRAG_DROPPED, gridPaneSetOnDragDropped.get(draggableType));
                anchorPaneRoot.addEventHandler(DragEvent.DRAG_OVER, anchorPaneRootSetOnDragOver.get(draggableType));
                anchorPaneRoot.addEventHandler(DragEvent.DRAG_DROPPED, anchorPaneRootSetOnDragDropped.get(draggableType));

                for (Node n: targetGridPane.getChildren()){
                    // events for entering and exiting are attached to squares children because that impacts opacity change
                    // these do not affect visibility of original image...
                    // https://stackoverflow.com/questions/41088095/javafx-drag-and-drop-to-gridpane
                    gridPaneNodeSetOnDragEntered.put(draggableType, new EventHandler<DragEvent>() {
                        // TODO = be more selective about whether highlighting changes - if it cannot be dropped in the location, the location shouldn't be highlighted!
                        public void handle(DragEvent event) {
                            if (currentlyDraggedType == draggableType){
                            //The drag-and-drop gesture entered the target
                            //show the user that it is an actual gesture target
                                if(event.getGestureSource() != n && event.getDragboard().hasImage()){
                                    n.setOpacity(0.7);
                                }
                            }
                            event.consume();
                        }
                    });
                    gridPaneNodeSetOnDragExited.put(draggableType, new EventHandler<DragEvent>() {
                        // TODO = since being more selective about whether highlighting changes, you could program the game so if the new highlight location is invalid the highlighting doesn't change, or leave this as-is
                        public void handle(DragEvent event) {
                            if (currentlyDraggedType == draggableType){
                                n.setOpacity(1);
                            }
                
                            event.consume();
                        }
                    });
                    n.addEventHandler(DragEvent.DRAG_ENTERED, gridPaneNodeSetOnDragEntered.get(draggableType));
                    n.addEventHandler(DragEvent.DRAG_EXITED, gridPaneNodeSetOnDragExited.get(draggableType));
                }
                event.consume();
            }
            
        });
    }

    /**
     * remove drag event handlers so that we don't process redundant events
     * this is particularly important for slower machines such as over VLAB.
     * @param draggableType either cards, or items in unequipped inventory
     * @param targetGridPane the gridpane to remove the drag event handlers from
     */
    private void removeDraggableDragEventHandlers(DRAGGABLE_TYPE draggableType, GridPane targetGridPane){
        // remove event handlers from nodes in children squares, from anchorPaneRoot, and squares
        targetGridPane.removeEventHandler(DragEvent.DRAG_DROPPED, gridPaneSetOnDragDropped.get(draggableType));

        anchorPaneRoot.removeEventHandler(DragEvent.DRAG_OVER, anchorPaneRootSetOnDragOver.get(draggableType));
        anchorPaneRoot.removeEventHandler(DragEvent.DRAG_DROPPED, anchorPaneRootSetOnDragDropped.get(draggableType));

        for (Node n: targetGridPane.getChildren()){
            n.removeEventHandler(DragEvent.DRAG_ENTERED, gridPaneNodeSetOnDragEntered.get(draggableType));
            n.removeEventHandler(DragEvent.DRAG_EXITED, gridPaneNodeSetOnDragExited.get(draggableType));
        }
    }

    /**
     * handle the pressing of keyboard keys.
     * Specifically, we should pause when pressing SPACE
     * @param event some keyboard key press
     */
    @FXML
    public void handleKeyPress(KeyEvent event) {
        // TODO = handle additional key presses, e.g. for consuming a health potion
        switch (event.getCode()) {
        case SPACE:
            if (isPaused){
                startTimer();
            }
            else{
                pause();
            }
            break;
        default:
            break;
        }
    }

    public void setMainMenuSwitcher(MenuSwitcher mainMenuSwitcher){
        // TODO = possibly set other menu switchers
        this.mainMenuSwitcher = mainMenuSwitcher;
    }

    @FXML
    private void switchToMainMenu(){
        // DONE = possibly set other menu switchers
        pause();
        heroCastleBuilding.closeStore();
        if (world.goalCheck()) {
            // game end
            System.out.print("you have meet the conditio!");
        } else {
            System.out.print("you have not meet the condition!");

        }
        mainMenuSwitcher.switchMenu();
    }

    /**
     * Set a node in a GridPane to have its position track the position of an
     * entity in the world.
     *
     * By connecting the model with the view in this way, the model requires no
     * knowledge of the view and changes to the position of entities in the
     * model will automatically be reflected in the view.
     * 
     * note that this is put in the controller rather than the loader because we need to track positions of spawned entities such as enemy
     * or items which might need to be removed should be tracked here
     * 
     * NOTE teardown functions setup here also remove nodes from their GridPane. So it is vital this is handled in this Controller class
     * @param entity
     * @param node
     */
    private void trackPosition(Entity entity, Node node) {
        // TODO = tweak this slightly to remove items from the equipped inventory?
        GridPane.setColumnIndex(node, entity.getX());
        GridPane.setRowIndex(node, entity.getY());

        ChangeListener<Number> xListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
                GridPane.setColumnIndex(node, newValue.intValue());
            }
        };
        ChangeListener<Number> yListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
                GridPane.setRowIndex(node, newValue.intValue());
            }
        };

        // if need to remove items from the equipped inventory, add code to remove from equipped inventory gridpane in the .onDetach part
        ListenerHandle handleX = ListenerHandles.createFor(entity.x(), node)
                                               .onAttach((o, l) -> o.addListener(xListener))
                                               .onDetach((o, l) -> {
                                                    o.removeListener(xListener);
                                                    entityImages.remove(node);
                                                    squares.getChildren().remove(node);
                                                    cards.getChildren().remove(node);
                                                    equippedItems.getChildren().remove(node);
                                                    unequippedInventory.getChildren().remove(node);
                                                })
                                               .buildAttached();
        ListenerHandle handleY = ListenerHandles.createFor(entity.y(), node)
                                               .onAttach((o, l) -> o.addListener(yListener))
                                               .onDetach((o, l) -> {
                                                   o.removeListener(yListener);
                                                   entityImages.remove(node);
                                                   squares.getChildren().remove(node);
                                                   cards.getChildren().remove(node);
                                                   equippedItems.getChildren().remove(node);
                                                   unequippedInventory.getChildren().remove(node);
                                                })
                                               .buildAttached();
        handleX.attach();
        handleY.attach();

        // this means that if we change boolean property in an entity tracked from here, position will stop being tracked
        // this wont work on character/path entities loaded from loader classes
        entity.shouldExist().addListener(new ChangeListener<Boolean>(){
            @Override
            public void changed(ObservableValue<? extends Boolean> obervable, Boolean oldValue, Boolean newValue) {
                handleX.detach();
                handleY.detach();
            }
        });
    }
    
    /**
     * getter of timeline
     * @return timeline
     */
    public Timeline getTimeLine(){
        return timeline;
    }

    /**
     * getter of loopmaniaword
     * @return loopmaniaword
     */
    public LoopManiaWorld getLoopManiaWorld(){
        return world;
    }
    
    /**
     * eventhandler used to respond to view the description of the card
     */
    private  class CardMouseHoverHandler implements EventHandler<MouseEvent>{
        private String name;
        private String description;
        public CardMouseHoverHandler(String name, String description){
            this.name = name;
            this.description = description;
        }
        @Override
        public void handle(MouseEvent e) {
            cardDescription.show(name, description);
        } 
    }

    /**
     * eventhandler used to respond to close the description of the card
     */
    private  class CardMouseLeaveHandler implements EventHandler<MouseEvent>{
        @Override
        public void handle(MouseEvent e) {
            cardDescription.close();
        }
        
    }


    /**
     * set primary stage
     * @param primaryStage
     */
    public void setPrimaryStage(Stage stage){
        primaryStage = stage;
    }

    /**
     * get primary stage
     * @return primary stage
     */
    public Stage getPrimayStage(){
        return primaryStage;
    }


    public boolean isSurvivalMode() {
        return isSurvivalMode;
    }

    public AnchorPane getStats() {
        return stats;
    }

    public void closeStore(){
        if(heroCastleBuilding == null) return;
        heroCastleBuilding.closeStore();
    }

    public void setMainMenuController(MainMenuController mainMenuController){
        this.mainMenuController = mainMenuController;
    }

    public Mode getModeReq(){
        return mainMenuController.getMode_req();
    }

    /**
     * we added this method to help with debugging so you could check your code is running on the application thread.
     * By running everything on the application thread, you will not need to worry about implementing locks, which is outside the scope of the course.
     * Always writing code running on the application thread will make the project easier, as long as you are not running time-consuming tasks.
     * We recommend only running code on the application thread, by using Timelines when you want to run multiple processes at once.
     * EventHandlers will run on the application thread.
     */
    private void printThreadingNotes(String currentMethodLabel){
        // System.out.println("\n###########################################");
        // System.out.println("current method = "+currentMethodLabel);
        // System.out.println("In application thread? = "+Platform.isFxApplicationThread());
        // System.out.println("Current system time = "+java.time.LocalDateTime.now().toString().replace('T', ' '));
    }

    /**
     * set the one ring image to balck tiny when used it 
     */
    public void setUsedTheOneRingImage() {
        if (world.getCharacter().getTheOneRing() == null) {
            ImageView image = new ImageView(emptyTheOneRingImage);
            equippedItems.add(image, 3, 0, 1, 1);
            world.setUsedTheOneRing(false);
        }
    }
}
