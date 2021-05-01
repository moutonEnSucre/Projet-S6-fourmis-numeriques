package openGL.textures;

public class ModelTexture {
	
	private int textureID;
	
	private float shineDamper = 1;
	private float reflectivity = 0;

	/**
	 * Create a new texture using the OpenGL provided texture location
	 * @param texture OpenGl texture location
	 */
	public ModelTexture(int texture){
		this.textureID = texture;
	}

	/**
	 * Return the texture location
	 * @return the texture location
	 */
	public int getID(){
		return textureID;
	}

	/**
	 * Return the shine damper of the texture, used by the shader
	 * @return shine damper attribute
	 */
	public float getShineDamper() {
		return shineDamper;
	}

	/**
	 * Set the shine damper of the texture, used by the shader
	 * @param shineDamper new shine damper attribute
	 */
	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	/**
	 * Return the reflectivity of the texture, used by the shader
	 * @return reflectivity attribute
	 */
	public float getReflectivity() {
		return reflectivity;
	}

	/**
	 * set the reflectivity of the texture, used by the shader
	 * @param reflectivity new reflectivity attribute
	 */
	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

}
