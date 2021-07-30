package unsw.loopmania;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * the main application
 * run main method from this class
 */
public class LoopManiaApplication extends Application {
    // DONE = possibly add other menus?

    /**
     * the controller for the game. Stored as a field so can terminate it when click exit button
     */
    private LoopManiaWorldController mainController;
    private  Parent gameRoot;
    @Override
    public void start(Stage primaryStage) throws IOException {
        // set title on top of window bar
        primaryStage.setTitle("Loop Mania");
        
        // prevent human player resizing game window (since otherwise would see white space)
        // alternatively, you could allow rescaling of the game (you'd have to program resizing of the JavaFX nodes)
        primaryStage.setResizable(false);

        // load home page
        HomePageController homePageController = new HomePageController();
        FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("HomePageView.fxml"));
        homeLoader.setController(homePageController);
        Parent homeRoot = homeLoader.load();
        homePageController.init();

        // load the main menu
        MainMenuController mainMenuController = new MainMenuController();
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("MainMenuView.fxml"));
        menuLoader.setController(mainMenuController);
        Parent mainMenuRoot = menuLoader.load();
        mainMenuController.init();

        // load victory interface
        VictoryPageController victoryPageController = new VictoryPageController();
        FXMLLoader victoryLoader = new FXMLLoader(getClass().getResource("VictoryPageView.fxml"));
        victoryLoader.setController(victoryPageController);
        Parent victoryRoot = victoryLoader.load();
        victoryPageController.init();

        // load defeat interface
        DefeatPageController defeatPageController = new DefeatPageController();
        FXMLLoader defeatLoader = new FXMLLoader(getClass().getResource("DefeatPageView.fxml"));
        defeatLoader.setController(defeatPageController);
        Parent defeatRoot = defeatLoader.load();
        defeatPageController.init();

        // create new scene with the main menu (so we start with the main menu)
        Scene scene = new Scene(homeRoot);
        
        // set functions which are activated when button click to switch menu is pressed
        // e.g. from main menu to start the game, or from the game to return to main menu
        homePageController.setMainMenuSwitcher(() -> {
            // build up a new game
            try {
                LoopManiaWorldControllerLoader loopManiaLoader = new LoopManiaWorldControllerLoader("world_with_twists_and_turns.json");
                mainController = loopManiaLoader.loadController();
                FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("LoopManiaView.fxml"));
                gameLoader.setController(mainController);
                gameRoot = gameLoader.load();
                mainController.setPrimaryStage(primaryStage);
                mainController.setMainMenuController(mainMenuController);

                mainController.setMainMenuSwitcher(() -> {
                    switchToRoot(scene, homeRoot, primaryStage);
                });
                mainController.setDefeatSwitcher(() -> {
                    switchToRoot(scene, defeatRoot, primaryStage);
                },defeatPageController);
                mainController.setVictorySwitcher(() -> {
                    switchToRoot(scene, victoryRoot, primaryStage);
                },victoryPageController);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switchToRoot(scene, mainMenuRoot, primaryStage);
        });
        mainMenuController.setGameSwitcher(() -> {
            switchToRoot(scene, gameRoot, primaryStage);
            mainController.startTimer();
        });
        
        // deploy the main onto the stage
        switchToRoot(scene, homeRoot, primaryStage);
    }

    @Override
    public void stop(){
        if(mainController == null) return;
        // wrap up activities when exit program
        mainController.terminate();
    }

    /**
     * switch to a different Root
     */
    private void switchToRoot(Scene scene, Parent root, Stage stage){
        scene.setRoot(root);
        root.requestFocus();
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
