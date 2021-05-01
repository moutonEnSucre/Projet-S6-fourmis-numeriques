package entity;

import openGL.entities.RenderableObject;
import openGL.world.Chunk;
import openGL.world.HeightsGenerator;

import java.util.ArrayList;
import java.util.List;

public class Tile extends Chunk {

    /** List of the entities in the tile */
    private List<RenderableObject> entities;

    public Tile(int gridX, int gridZ, HeightsGenerator generator) {
        super(gridX, gridZ, generator);
        entities = new ArrayList<RenderableObject>();
    }

    /**
     * Add an entity into the tile
     * @param entity the entity to be added
     */
    public void addEntity(RenderableObject entity) {
        entities.add(entity);
    }

    /**
     * Get an entity of a type EntityType e if the tile contains one, the entity is removed from the tile
     * return null otherwise
     * @param e the type of entity
     * @return an entity of type e or null
     */
    public RenderableObject getEntity(EntityTypes e) {
        switch (e) {
            case ANT:
                for (RenderableObject ro : entities) {
                    if (ro instanceof Ant) {
                        entities.remove(ro);
                        return ro;
                    }
                }
                break;
            case FOOD:
                for (RenderableObject ro : entities) {
                    if (ro instanceof Food) {
                        entities.remove(ro);
                        return ro;
                    }
                }
                break;
            case ANTHILL:
                for (RenderableObject ro : entities)
                    if (ro instanceof AntHill) {
                        entities.remove(ro);
                        return ro;
                    }
                    break;
        }
        return null;
    }

    /**
     * Return true if the tile contains an entity of type e
     * @param e the type of the entity
     * @return true if the tile contains an entity of type e, false otherwise
     */
    public boolean contains(EntityTypes e) {
        switch (e) {
            case ANT:
                for (RenderableObject ro : entities) {
                    if (ro instanceof Ant)
                        return true;
                }
                break;
            case FOOD:
                for (RenderableObject ro : entities) {
                    if (ro instanceof Food)
                        return true;
                }
                break;
            case ANTHILL:
                for (RenderableObject ro : entities) {
                    if (ro instanceof AntHill)
                        return true;
                }
                break;
        }
        return false;
    }

    /**
     * Get all the entities contained in the tile
     * @return a list of the tile's entities
     */
    public List<RenderableObject> getEntities() {
        return entities;
    }

    /**
     * Get all the entities contained in the tile of a certain type
     * @param type the type of entity you want to extract
     * @return a list of the tile's entities
     */
    public List<RenderableObject> getEntities(EntityTypes type) {
        List<RenderableObject> toReturn = new ArrayList<>();
        for (RenderableObject obj : entities) {
            if (type == EntityTypes.FOOD && obj instanceof Food)
                toReturn.add(obj);
            if (type == EntityTypes.ANTHILL && obj instanceof AntHill)
                toReturn.add(obj);
        }
        return toReturn;
    }

    /**
     * Clear the chunk by deleting its entities
     */
    public void clear() {
        entities.clear();
    }
}
