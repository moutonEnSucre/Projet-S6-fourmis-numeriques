package openGL.shaders.Uniforms;

import org.lwjgl.opengl.GL20;

public class UniformBoolean extends Uniform{

	private boolean currentBool;

	/**
	 * Create a new Uniform of type boolean
	 * @param name name of the uniform, must be the same as in the Shader program
	 */
	public UniformBoolean(String name){
		super(name);
	}
	
	public void loadBoolean(boolean bool){
		if(currentBool != bool){
			GL20.glUniform1f(super.getLocation(), bool ? 1f : 0f);
			currentBool = bool;
		}
	}
	
}
