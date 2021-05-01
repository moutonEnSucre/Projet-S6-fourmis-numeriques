package openGL.shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import openGL.shaders.Uniforms.Uniform;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public abstract class ShaderProgram {

	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;

	/**
	 * Create a new ShaderProgram and load the appropriate programs in the GPU's RAM
	 * before allocating the memory for VAO attributes
	 * @param vertex Path to the vertex shader file
	 * @param fragment Path to the fragment shader file
	 */
	public ShaderProgram(String vertex, String fragment) {
		vertexShaderID = loadShader(vertex, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragment, GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
	}

	/**
	 * Used to bind all of the Vertex Array Object attributes to the Vertex Shader
	 */
	protected abstract void bindAttributes();

	/**
	 * Bind a VAO Attribute to a location
	 * @param attribute attribute location
	 * @param varibleName attribute name
	 */
	protected void bindAttribute(int attribute, String varibleName) {
		GL20.glBindAttribLocation(programID, attribute, varibleName);
	}

	/**
	 * Start the shader on the GPU
	 */
	public void start() {
		GL20.glUseProgram(programID);
	}

	/**
	 * Stop the shader on the GPU
	 */
	public void stop() {
		GL20.glUseProgram(0);
	}

	/**
	 * Clear the GPU memory used by the shaders
	 */
	public void clear() {
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}

	/**
	 * Load a shader from a file
	 * @param file path of the shader file
	 * @param type indicate the type of shader (Vertex, Geometry, Tesselation, Fragment ...)
	 * @return the shader ID given by OpenGL
	 */
	private static int loadShader(String file, int type) {
		StringBuilder shaderSource = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Impossible de trouver le shader \"" + file + "\"");
			System.exit(-1);
		}
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Impossible de compiler le shader \"" + file + "\"(" + shaderID + ")");
			System.exit(-1);
		} else {
			System.out.println("Shader (" + shaderID + ") " + "\"" + file + "\" complilé avec succes");
		}
		return shaderID;
	}

	/**
	 * Allocate the memory on the GPU's RAM for all the Uniforms variables of this shader
	 * @param uniforms list of all the Uniforms to be used by the shader
	 */
	protected void storeAllUniformLocations(Uniform... uniforms){
		for(Uniform uniform : uniforms){
			uniform.storeUniformLocation(programID);
		}
		GL20.glValidateProgram(programID);
	}
}
