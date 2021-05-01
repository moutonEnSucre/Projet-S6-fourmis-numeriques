package entity.logic.action;

import entity.Ant;
import openGL.world.World;

public class ActionCondLoaded extends Action {

    public ActionCondLoaded() {
        isConditional = true;
    }

    /**
     * Do nothing, the action is conditional
     * @param a the ant that will be executing the action
     * @param world the world in which the action will be executed
     */
    @Override
    public void execute(Ant a, World world) {
        return;
    }

    /**
     * Return whether or not the ant is carrying food
     * @param a the ant testing the condition
     * @param world the world used to verify the condition
     * @return true if satified, false otherwise
     */
    @Override
    public boolean isConditionSatisfied(Ant a, World world) {
        return a.isCarryingFood();
    }

    /**
     * Return the action identifier as a string
     * @return action's identifier
     */
    @Override
    public String toString() {
        return "isLoaded";
    }
}
