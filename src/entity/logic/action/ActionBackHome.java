package entity.logic.action;

import entity.Ant;
import entity.EntityTypes;
import openGL.entities.RenderableObject;
import openGL.utils.Maths;
import openGL.world.World;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

public class ActionBackHome extends Action {

    private static final int A_STAR_WEIGHT_MULTIPLIER = 5;

    /**
     * Set the target position of the ant to the optimal adjacent tile onto the nearest path to the closest anthill entrance
     * @param a the ant executing the action
     * @param world the world in which the action is executed
     */
    @Override
    public void execute(Ant a, World world) {
        List<RenderableObject> entrances = world.extractEntities(EntityTypes.ANTHILL);
        if (entrances.size() > 0) {
            Vector2f bestEnd = new Vector2f(entrances.get(0).getPosition().x, entrances.get(0).getPosition().z);
            float bestDist = Float.MAX_VALUE;
            for (RenderableObject entrance : entrances) {
                Vector2f ant2DPos = new Vector2f(a.getPosition().x, a.getPosition().z);
                Vector2f entrance2DPos = new Vector2f(entrance.getPosition().x, entrance.getPosition().z);
                float distSqrt = Vector2f.sub(ant2DPos, entrance2DPos, null).lengthSquared();
                if (distSqrt < bestDist) {
                    bestDist = distSqrt;
                    bestEnd = new Vector2f(entrance2DPos);
                }
            }
            bestEnd.x -= .5f;
            bestEnd.y -= .5f;
            Vector2f path = aStar(new Vector2f(a.getPosition().x - .5f, a.getPosition().z - .5f), bestEnd, world);

            if (path != null) {
                float xForward = (float) (int) (Math.cos(Math.toRadians(a.getTargetRot())));
                float zForward = (float) (int) (-Math.sin(Math.toRadians(a.getTargetRot())));
                Vector2f forward = new Vector2f(xForward, zForward);

                float xPath = path.x - a.getPosition().x + .5f;
                float zPath = path.y - a.getPosition().z + .5f;
                Vector2f relativePath = new Vector2f(xPath, zPath);

                float theta = (float) Math.toDegrees(Maths.signedAngle(forward, relativePath));

                a.setTargetRot(a.getTargetRot() - theta);
                a.setTargetPosition(new Vector3f(path.x + .5f, 0, path.y + .5f));
            }
        }
    }

    /**
     * Return the heuristic cost of a position.
     * the heuristic cost take into consideration if it's faster to cross any world boundaries by effectively creating a 3*3 grid of world to find the closest end point
     * @param start the position to be tested
     * @param end the end position
     * @param world the world into which the search is made
     * @return the heuristic cost of the node
     */
    private float h(Vector2f start, Vector2f end, World world) {
        float dist = Float.MAX_VALUE;
        Vector2f[] loopedEnd = {
                new Vector2f(end.x, end.y),
                new Vector2f(end.x + world.getSizeX(), end.y),
                new Vector2f(end.x - world.getSizeX(), end.y),
                new Vector2f(end.x, end.y + world.getSizeZ()),
                new Vector2f(end.x, end.y - world.getSizeZ()),
                new Vector2f(end.x + world.getSizeX(), end.y + world.getSizeZ()),
                new Vector2f(end.x + world.getSizeX(), end.y - world.getSizeZ()),
                new Vector2f(end.x - world.getSizeX(), end.y + world.getSizeZ()),
                new Vector2f(end.x - world.getSizeX(), end.y - world.getSizeZ())
        };
        for(Vector2f potentialEnd : loopedEnd) {
            float endDist = (float) Math.sqrt(Math.pow(start.x - potentialEnd.x, 2) + Math.pow(start.y - potentialEnd.y, 2));
            if (endDist < dist)
                dist = endDist;
        }
        return A_STAR_WEIGHT_MULTIPLIER * dist;
    }

    /**
     * Used to get the value from a Map, return +INFINITY if the key isn't found
     * @param map the map to search in
     * @param vec the key to find
     * @return the value of the key if present, +INFINITY if not
     */
    private float getValue(Map<Vector2f, Float> map, Vector2f vec) {
        return (map.get(vec) == null ? Float.MAX_VALUE - 1 : map.get(vec));
    }

    /**
     * Calculate an optimal path from to an end position
     * @param start the start position
     * @param end the end position
     * @param world the world in which the search is performed
     * @return the position to move to to advance in the path, null if impossible
     */
    private Vector2f aStar(Vector2f start, Vector2f end, World world) {
        List<Vector2f> openList = new ArrayList<>();
        openList.add(start);

        Map<Vector2f, Vector2f> cameFrom = new HashMap<>();
        Map<Vector2f, Float> gScore = new HashMap<>();
        Map<Vector2f, Float> fScore = new HashMap<>();

        gScore.put(start, 0f);
        fScore.put(start, (h(start, end, world)));

        while(openList.size() > 0) {
            Vector2f current = openList.get(0);
            for (Vector2f v : openList) {
                if (getValue(fScore, v) < getValue(fScore, current))
                    current = v;
            }

            if (current.equals(end)) {
                Vector2f last = null;
                while (cameFrom.containsKey(current)) {
                    last = current;
                    current = cameFrom.get(current);
                }
                return last;
            }
            openList.remove(current);
            Vector2f worldSize = new Vector2f(world.getSizeX(), world.getSizeZ());
            Vector2f[] neighbors = {
                    Maths.loopVector(new Vector2f(current.x - 1, current.y), worldSize),
                    Maths.loopVector(new Vector2f(current.x + 1, current.y), worldSize),
                    Maths.loopVector(new Vector2f(current.x, current.y - 1), worldSize),
                    Maths.loopVector(new Vector2f(current.x, current.y + 1), worldSize)
            };

            for (Vector2f vec : neighbors) {
                boolean navigable = true;
                if (navigable) {
                    float newGScore = getValue(gScore, current) + 1;
                    if (newGScore < getValue(gScore, vec)) {

                        cameFrom.put(vec, current);
                        gScore.put(vec, newGScore);
                        fScore.put(vec, newGScore + h(vec, end, world));
                        if (!openList.contains(vec)) {
                            openList.add(vec);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return the action identifier as a string
     * @return action's identifier
     */
    @Override
    public String toString() {
        return "return";
    }
}
