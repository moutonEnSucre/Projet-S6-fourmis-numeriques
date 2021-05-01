package openGL.utils;
import entity.Ant;
import openGL.entities.RenderableObject;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import openGL.entities.Camera;

import java.util.Comparator;
import java.util.List;

public class MousePicker {

    private static final float RAY_RANGE = 600;
    private static final float RAY_RANGE_SQR = RAY_RANGE * RAY_RANGE;

    private Vector3f currentRay = new Vector3f();

    private boolean canSelectEntity = true;

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Camera camera;

    private List<Ant> entities;

    /**
     * Create a new instance of MousePicker, able to select entities from the passed list of entities
     * in regard to the camera position
     * @param cam Camera used for rendering
     * @param projection projection matrix used for rendering
     * @param entities list of selectable entities
     */
    public MousePicker(Camera cam, Matrix4f projection, List<Ant> entities) {
        camera = cam;
        projectionMatrix = projection;
        viewMatrix = Maths.createViewMatrix(camera);
        this.entities = entities;
    }

    /**
     * Return the direction of the casted ray
     * @return the casted ray direction
     */
    public Vector3f getCurrentRay() {
        return currentRay;
    }

    /**
     * Update the view matrix using the camera position,
     * recalculate the ray
     * and update the ability of the picker to select an entity
     */
    public void update() {
        viewMatrix = Maths.createViewMatrix(camera);
        currentRay = calculateMouseRay();
        if (!Mouse.isButtonDown(0))
            canSelectEntity = true;
    }

    /**
     * Return whether or not the picker is able to select an entity
     * @return can the picker select an entity
     */
    public boolean canSelectEntity() {
        return canSelectEntity;
    }

    /**
     * Return the closest entity in range of the current casted ray
     * @param exclude the currently selected entity
     * @return the closest entity hitted by the casted ray, null if no entity was hit
     */
    public RenderableObject getClickedEntity(RenderableObject exclude) {
        canSelectEntity = false;
        entities.sort(distanceFromCamComp);
        for (RenderableObject e : entities) {
            if (e == exclude) continue;
            if (e.getDistanceSquaredFromCamera(camera) < RAY_RANGE_SQR) {
                Vector3f oc = Vector3f.sub(camera.getPosition(), Vector3f.add(e.getPosition(), e.getBoundingSphereOffset(), null), null);
                float a = Vector3f.dot(currentRay, currentRay);
                float b = (float) (2.0 * Vector3f.dot(oc, currentRay));
                float c = Vector3f.dot(oc,oc) - e.getBoundingSphereRadius()*e.getBoundingSphereRadius();
                float discriminant = b*b - 4*a*c;
                if (discriminant>0)
                    return e;
            } else {
                break;
            }
        }
        return null;
    }

    /**
     * Calculate the ray representing the direction pointed by the mouse cursor
     * @return 3D Vector representing the direction from the camera to the mouse in regard to the world's position
     */
    private Vector3f calculateMouseRay() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        Vector2f normalizedCoords = getNormalisedDeviceCoordinates(mouseX, mouseY);
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        return toWorldCoords(eyeCoords);
    }

    /**
     * Return a vector representing the direction pointed by the mouse cursor in the coordinates system of the world
     * @param eyeCoords vector representing the direction from the camera to the mouse in regard to the camera's position
     * @return 3D Vector representing the direction from the camera to the mouse in regard to the world's position
     */
    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
        Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay.normalise();
        return mouseRay;
    }

    /**
     * Return a vector representing the direction from the camera to the mouse
     * in regard to the camera's position,
     * the fixed z component means a vector pointing inside the screen
     * @param clipCoords normalized mouse position (between -1 and 1) in an orthogonal view field
     * @return 4D Vector representing the direction from the camera to the mouse
     */
    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
        Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    /**
     * Return the mouse coords from screen coordinates to normalized coords
     * between -1 and 1 usable by OpenGL
     * @param mouseX x position of the mouse on screen
     * @param mouseY y position of the mouse on screen
     * @return mouse position on screen normalized between -1 and 1
     */
    private Vector2f getNormalisedDeviceCoordinates(float mouseX, float mouseY) {
        float x = (2.0f * mouseX) / Display.getWidth() - 1f;
        float y = (2.0f * mouseY) / Display.getHeight() - 1f;
        return new Vector2f(x, y);
    }

    /**
     * Comparator used to sort entities by distance from the camera
     */
    private Comparator<RenderableObject> distanceFromCamComp = new Comparator<RenderableObject>() {
        public int compare(RenderableObject e1, RenderableObject e2) {
            return e1.getDistanceSquaredFromCamera(camera).compareTo(e2.getDistanceSquaredFromCamera(camera));
        }};

}