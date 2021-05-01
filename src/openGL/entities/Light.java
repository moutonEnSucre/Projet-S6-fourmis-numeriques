package openGL.entities;

import org.lwjgl.util.vector.Vector3f;

public class Light {
	
	private Vector3f position;
	private Vector3f colour;

	/**
	 * Create a new Light source
	 * @param position position in the world
	 * @param colour Color of the light source
	 */
	public Light(Vector3f position, Vector3f colour) {
		this.position = position;
		this.colour = colour;
	}

	/**
	 * Return the position of the light in the world space
	 * @return light's position
	 */
	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Set the position of the light in the world space
	 * @param position light's new position
	 */
	public void setPosition(Vector3f position) {
		this.position = position;
	}

	/**
	 * Return the light color
	 * @return light's color
	 */
	public Vector3f getColour() {
		return colour;
	}

	/**
	 * Set the light color
	 * @param colour light's new color
	 */
	public void setColour(Vector3f colour) {
		this.colour = colour;
	}
	

}
