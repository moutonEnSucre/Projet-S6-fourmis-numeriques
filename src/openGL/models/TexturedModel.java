package openGL.models;

import openGL.textures.ModelTexture;

public class TexturedModel {
	
	private Model rawModel;
	private ModelTexture texture;

	/**
	 * Create a new Model with a texture
	 * @param model the raw Model
	 * @param texture the Texture to be attached
	 */
	public TexturedModel(Model model, ModelTexture texture){
		this.rawModel = model;
		this.texture = texture;
	}

	/**
	 * Return the raw Model
	 * @return a Model without texture
	 */
	public Model getRawModel() {
		return rawModel;
	}

	/**
	 * Return the texture of the model
	 * @return texture of the model
	 */
	public ModelTexture getTexture() {
		return texture;
	}

}
