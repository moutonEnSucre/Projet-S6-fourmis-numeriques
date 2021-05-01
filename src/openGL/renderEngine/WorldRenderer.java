package openGL.renderEngine;

import openGL.models.Model;

import openGL.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import openGL.shaders.WorldShader;
import openGL.world.Chunk;
import openGL.textures.ModelTexture;
import openGL.utils.Maths;

public class WorldRenderer {

	private WorldShader shader;

	/**
	 * Create a new WorldRendered used to handle the world rendering
	 * @param shader shader to be used for rendering
	 * @param projectionMatrix projection matrix used for the field of view
	 */
	public WorldRenderer(WorldShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.projectionMatrix.loadMatrix(projectionMatrix);
		shader.stop();
	}

	/**
	 * Render the whole world onto the screen
	 * @param world world to be rendered
	 */
	public void render(World world) {
		for (Chunk chunk : world.getChunks()) {
			prepareTerrain(chunk);
			loadModelMatrix(chunk);
			GL11.glDrawElements(GL11.GL_TRIANGLES, chunk.getModel().getVertexCount(),
					GL11.GL_UNSIGNED_INT, 0);
			unbindTexturedModel();
		}
	}

	/**
	 * Bind the VAO attributes, load uniforms to the shader and set the active texture
	 * @param chunk chunk currently being processed
	 */
	private void prepareTerrain(Chunk chunk) {
		Model rawModel = chunk.getModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
	}

	/**
	 * Unbind all the VAO attributes of the current model
	 */
	private void unbindTexturedModel() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Load the Transformation matrix of the current chunk to the shader
	 * @param chunk chunk currently being processed
	 */
	private void loadModelMatrix(Chunk chunk) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(
				new Vector3f(chunk.getX(), 0, chunk.getZ()), 0, 0, 0, 1);
		shader.transformationMatrix.loadMatrix(transformationMatrix);
	}

}
