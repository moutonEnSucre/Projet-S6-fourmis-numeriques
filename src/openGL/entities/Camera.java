package openGL.entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

	private float distanceFromFocus = 5;
	private float angleAroundFocus = 90;
	private static final float Y_OFFSET = .3f;

	private Vector3f position = new Vector3f(5,5,25);
	private float pitch = 10;
	private float yaw ;

	private RenderableObject focus;

	/**
	 * Create a new Camera
	 * @param focus the targeted entity
	 */
	public Camera(RenderableObject focus) {
		this.focus = focus;
	}

	/**
	 * Update the camera's position and angles
	 * to follow the targeted entity
	 */
	public void update(){
		calculateAngleAroundFocus();
		calculatePitch();
		calculateZoom();
		float hDist = calculateHorizontalDistance();
		float vDist = calculateVerticalDistance();
		calculateCameraPosition(hDist, vDist);
		this.yaw = 180 - (angleAroundFocus);
	}

	/**
	 * Return the camera's targeted entity
	 * @return camera's targeted entity
	 */
	public RenderableObject getFocus() {
		return focus;
	}

	/**
	 * Set the entity focused by the camera
	 * @param focus new camera's targeted entity
	 */
	public void setFocus(RenderableObject focus) {
		angleAroundFocus = 90;
		this.focus = focus;
	}

	/**
	 * Return the camera position in world space
	 * @return camera position
	 */
	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Return the camera's Pitch angle
	 * @return pitch angle
	 */
	public float getPitch() {
		return pitch;
	}

	/**
	 * Return the camera's Yaw angle
	 * @return yaw angle
	 */
	public float getYaw() {
		return yaw;
	}

	/**
	 * Calculate the zoom factor of the camera regarding the focused entity
	 */
	private void calculateZoom() {
		float zoomLevel = Mouse.getDWheel() * 0.01f;
		distanceFromFocus -= zoomLevel;
		if (distanceFromFocus < 0.5f)
			distanceFromFocus = 0.5f;
		if (distanceFromFocus > 60f)
			distanceFromFocus = 60f;
	}

	/**
	 * Calculate the pitch angle of the camera
	 */
	private void calculatePitch() {
		if (Mouse.isButtonDown(1)) {
			float pitchChange = Mouse.getDY() * 0.1f;
			pitch -= pitchChange;
			if (pitch < 10)
				pitch = 10;
			if (pitch > 90)
				pitch = 90;
		}
	}

	/**
	 * Calculate the angle around the focused entity
	 */
	private void calculateAngleAroundFocus() {
		if (Mouse.isButtonDown(1)) {
			float angleChange = Mouse.getDX() * 0.3f;
			angleAroundFocus -= angleChange;
			angleAroundFocus %= 360;
		}
	}

	/**
	 * Calculate the horizontal distance between the camera and the entity
	 * @return horizontal distance between the camera and the entity
	 */
	private float calculateHorizontalDistance() {
		return (float) (distanceFromFocus * Math.cos(Math.toRadians(pitch)));
	}

	/**
	 * Calculate the vertical distance between the camera and the entity
	 * @return vertical distance between the camera and the entity
	 */
	private float calculateVerticalDistance() {
		return (float) (distanceFromFocus * Math.sin(Math.toRadians(pitch)));
	}

	/**
	 * Calculate the camera's position around the focused entity
	 * @param hDist horizontal distance between the camera and the entity
	 * @param vDist vertical distance between the camera and the entity
	 */
	private void calculateCameraPosition(float hDist, float vDist) {
		position.y = focus.getPosition().y + vDist + Y_OFFSET;
		float xOffset = (float) (hDist * Math.sin(Math.toRadians(angleAroundFocus)));
		float zOffset = (float) (hDist * Math.cos(Math.toRadians(angleAroundFocus)));
		position.x = focus.getPosition().x - xOffset;
		position.z = focus.getPosition().z - zOffset;
	}
}
