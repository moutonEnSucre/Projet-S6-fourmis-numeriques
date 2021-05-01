package openGL.shaders.Uniforms;

import org.lwjgl.opengl.GL20;

public class UniformSampler extends Uniform {

	private int currentValue;

	/**
	 * Create a new Uniform of type sampler2D
	 * @param name name of the uniform, must be the same as in the Shader program
	 */
	public UniformSampler(String name) {
		super(name);
	}

	public void loadTexUnit(int texUnit) {
		if (currentValue != texUnit) {
			GL20.glUniform1i(super.getLocation(), texUnit);
			currentValue = texUnit;
		}
	}

}
