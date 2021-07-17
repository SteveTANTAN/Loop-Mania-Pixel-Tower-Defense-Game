package unsw.loopmania;

import javafx.beans.property.SimpleIntegerProperty;

/**
 * a basic form of building in the world
 */
public class CampfireBuilding extends Building {
    // DONE = add more types of building, and make sure buildings have effects on entities as required by the spec
    public CampfireBuilding(SimpleIntegerProperty x, SimpleIntegerProperty y) {
        super(x, y);
    }

    /**
     * use the function of the builidng
     */
    public boolean work(Character character){
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        boolean hasAttackEnhance = false;
        for(int i = 0; i < 4; i++){
            if(getX() + dx[i] == character.getX() && 
            getY() + dy[i] == character.getY()){
                hasAttackEnhance = true;
            }
        }
        return hasAttackEnhance;
    }
}
