package entity;

import configs.Configs;
import openGL.entities.RenderableObject;
import org.lwjgl.util.vector.Vector3f;

public class AntHill extends RenderableObject {

    private static int lastID = -1;
    private int id;

    public AntHill(Vector3f position, float rotX, float rotY, float rotZ) {
        super(Configs.antHilTexturedModel, position, rotX, rotY, rotZ, 1f);
        id = ++lastID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
