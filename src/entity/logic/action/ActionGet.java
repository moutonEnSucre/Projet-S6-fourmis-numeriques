package entity.logic.action;

import entity.Ant;
import entity.EntityTypes;
import entity.Food;
import entity.Tile;
import openGL.world.World;

public class ActionGet extends Action {

    /**
     * take a food under the ant if possible update it's fitness score
     * @param a the ant executing the action
     * @param world the world in which the action is executed
     */
    @Override
    public void execute(Ant a, World world) {
        if (a.isCarryingFood()) {
            a.addFitness(-.2f);
            return;
        }
        Tile tile = (Tile) world.getChunkInWorldCoords(a.getPosition().x, a.getPosition().z);
        if (tile != null && tile.contains(EntityTypes.FOOD)) {
            a.setFood((Food) tile.getEntity(EntityTypes.FOOD));
            a.addFitness(.1f);
        } else {
            a.addFitness(-.1f);
        }
    }

    /**
     * Return the action identifier as a string
     * @return action's identifier
     */
    @Override
    public String toString() {
        return "get";
    }
}
