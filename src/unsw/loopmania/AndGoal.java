package unsw.loopmania;

import org.json.JSONArray;
import org.json.JSONObject;
public class AndGoal extends Goal{
    public JSONArray goals;

    /**
     * constructor
     * @param condition
     */
    public AndGoal(JSONObject condition) {
        super(condition);
        this.goals = conditions.getJSONArray("subgoals");
    }

    /**
     * sub goal check
     * @return check result
     */
    public boolean subgoalcheck() {
        Goal goal1 = new Goal(goals.getJSONObject(0));
        Goal goal2 = new Goal(goals.getJSONObject(1));
        goal1.setCurrentStatus(gold, exp, turns, bosses);
        goal2.setCurrentStatus(gold, exp, turns, bosses);


        return goal1.goalCheck() && goal2.goalCheck();
    }

}