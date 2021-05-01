package entity.logic.action;

import entity.Ant;
import entity.EntityTypes;
import entity.Tile;
import openGL.world.World;

public class ActionCondAntHill extends Action {

    public ActionCondAntHill() {
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
     * Return whether or not the ant is on an anthill
     * @param a the ant testing the condition
     * @param world the world used to verify the condition
     * @return true if satified, false otherwise
     */
    @Override
    public boolean isConditionSatisfied(Ant a, World world) {
        Tile tile = (Tile) world.getChunkInWorldCoords(a.getPosition().x, a.getPosition().z);
        if (tile == null)
            return false;
        else
            return tile.contains(EntityTypes.ANTHILL);
    }

    /**
     * Return the action identifier as a string
     * @return action's identifier
     */
    @Override
    public String toString() {
        return "anthill";
    }
}
