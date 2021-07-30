package unsw.loopmania;

import java.util.Random;

public class Vampire extends BasicEnemy{
    private Building building;
    /**
     * constructor of vampire
     * @param position position
     */
    public Vampire(PathPosition position,Building building) {
        super(position);
        super.setHealth(32);
        super.setAggressivity(15);
        super.setSupportRange(2);
        super.setAttackRange(2);
        super.setGoldDefeated(5);
        super.setEXP(8);
        super.setName("Vampire");
        this.building = building;
    }

    /**
     * move the vampire
     */
    @Override
    public void move(){
        // this basic enemy moves in a random direction... 25% chance up or down, 50% chance not at all...
        int directionChoice = (new Random()).nextInt(6);
        if (directionChoice < 2){
            moveUpPath();
        }
        else{
            moveDownPath();
        }
    }
    public Building getVampireCastleBuilding(){
        return building;
    }
}
