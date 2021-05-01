package entity.logic.action;

import entity.Ant;
import openGL.world.World;
import org.lwjgl.util.vector.Vector3f;

public class ActionForward extends Action {

    /**
     * Set the ant's target position to one tile to it's front update it's target rotation and update it's fitness score
     * @param a the ant executing the action
     * @param world the world in which the action is executed
     */
    @Override
    public void execute(Ant a, World world) {
        a.setTargetPosition(new Vector3f((float)(a.getPosition().x + Math.cos(Math.toRadians(a.getTargetRot()))), 0, (float)(a.getPosition().z - Math.sin(Math.toRadians(a.getTargetRot())))));
    }

    /**
     * Return the action identifier as a string
     * @return action's identifier
     */
    @Override
    public String toString() {
        return "forward";
    }
}
