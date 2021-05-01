package openGL.entities;

import openGL.models.TexturedModel;

import org.lwjgl.util.vector.Vector3f;

public class RenderableObject {

	private TexturedModel model;
	protected Vector3f position;
	protected float rotX, rotY, rotZ;
	private float scale;
	private float boundingSphereRadius;
	private Vector3f boundingSphereOffset;

	private boolean canBeRendered = true;

	/**
	 * Create a new RenderableObject, set hit model and set his position in the world
	 * @param model TexturedModel of the entity
	 * @param position position in the world
	 * @param rotX rotation around x-axis in degrees
	 * @param rotY rotation around y-axis in degrees
	 * @param rotZ rotation around z-axis in degrees
	 * @param scale scale of the Model
	 */
	public RenderableObject(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
		boundingSphereRadius = scale;
		this.boundingSphereOffset = new Vector3f(0, 0, 0);
	}

	/**
	 * Return the offset of the sphere hitbox from the model origin
	 * @return hitbox offset
	 */
	public Vector3f getBoundingSphereOffset() {
		return boundingSphereOffset;
	}

	/**
	 * Return the Textured Model associated with the entity
	 * @return TexturedModel of the entity
	 */
	public TexturedModel getModel() {
		return model;
	}

	/**
	 * Return the position of the entity in the world
	 * @return entity's position
	 */
	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Set the position of the entity in the world
	 * @param position entity's new position
	 */
	public void setPosition(Vector3f position) {
		this.position = position;
	}

	/**
	 * Return the entity's angle around the x-axis in degrees
	 * @return angle around x-axis in degrees
	 */
	public float getRotX() {
		return rotX;
	}

	/**
	 * Set the entity's angle around the x-axis in degrees
	 * @param rotX new angle around x-axis in degrees
	 */
	public void setRotX(float rotX) {
		this.rotX = rotX;
	}

	/**
	 * Return the entity's angle around the y-axis in degrees
	 * @return angle around y-axis in degrees
	 */
	public float getRotY() {
		return rotY;
	}

	/**
	 * Set the entity's angle around the y-axis in degrees
	 * @param rotY new angle around y-axis in degrees
	 */
	public void setRotY(float rotY) {
		this.rotY = rotY;
	}

	/**
	 * Return the entity's angle around the z-axis in degrees
	 * @return angle around z-axis in degrees
	 */
	public float getRotZ() {
		return rotZ;
	}

	/**
	 * Set the entity's angle around the z-axis in degrees
	 * @param rotZ new angle around z-axis in degrees
	 */
	public void setRotZ(float rotZ) {
		this.rotZ = rotZ;
	}

	/**
	 * Get the scale of the entity
	 * @return entity's scaling factor
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * Set the scale of the entity
	 * @param scale entity's new scaling factor
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * Return the spherical hitbox's radius
	 * @return hitbox radius
	 */
	public float getBoundingSphereRadius() {
		return boundingSphereRadius;
	}

	/**
	 * Return whether or not the entity has to be rendered
	 * @return can the entity be renderer
	 */
	public boolean canBeRendered() {
		return canBeRendered;
	}

	/**
	 * Set if the entity can be rendered
	 * @param canBeRendered can the entity be rendered
	 */
	public void setCanBeRendered(boolean canBeRendered) {
		this.canBeRendered = canBeRendered;
	}

	/**
	 * Get the square of the distance between the entity and the camera
	 * @param camera the camera used for the rendering
	 * @return the squared distance between the camera and the entity
	 */
	public Float getDistanceSquaredFromCamera(Camera camera) {
		return Vector3f.sub(position, camera.getPosition(), null).lengthSquared();
	}

}
