package entity;

import configs.Configs;
import openGL.entities.RenderableObject;
import openGL.models.TexturedModel;
import org.lwjgl.util.vector.Vector3f;

public class Food extends RenderableObject {

    public Food(Vector3f position, float rotX, float rotY, float rotZ) {
        super(Configs.foodTexturedModel, position, rotX, rotY, rotZ, .2f);
    }
}
