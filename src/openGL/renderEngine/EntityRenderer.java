package openGL.renderEngine;

import java.util.List;
import java.util.Map;

import configs.Configs;
import openGL.models.Model;
import openGL.models.TexturedModel;

import openGL.shaders.HitboxShader;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import openGL.shaders.StaticShader;
import openGL.textures.ModelTexture;
import openGL.utils.Maths;
import openGL.entities.RenderableObject;

public class EntityRenderer {

	private StaticShader shader;
	private HitboxShader hitboxShader;

	/**
	 * Create a new EntityRenderer and initialize everything needed
	 * @param shader the shader used to render entities
	 * @param hitboxShader the shader used to render hitboxes
	 * @param projectionMatrix the projection matrix used by the MasterRenderer
	 */
	public EntityRenderer(StaticShader shader, HitboxShader hitboxShader ,Matrix4f projectionMatrix) {
		this.shader = shader;
		this.hitboxShader = hitboxShader;
		shader.start();
		shader.projectionMatrix.loadMatrix(projectionMatrix);
		shader.stop();
		hitboxShader.start();
		hitboxShader.projectionMatrix.loadMatrix(projectionMatrix);
		hitboxShader.stop();
	}

	/**
	 * Render a set of entities to the screen sorted by model,
	 * allowing us to render every entity using a model without reloading it
	 * @param entities map of Model and Entities to render
	 */
	public void render(Map<TexturedModel, List<RenderableObject>> entities) {
		for (TexturedModel model : entities.keySet()) {
			prepareTexturedModel(model);
			List<RenderableObject> batch = entities.get(model);
			for (RenderableObject renderableObject : batch) {
				prepareInstance(renderableObject);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(),
						GL11.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel();
		}
	}

	/**
	 * Render the hitboxes of the entities to the screen
	 * @param entities map of entities to render a hitbox for
	 */
	public void renderHitbox(Map<TexturedModel, List<RenderableObject>> entities) {
		prepareHitboxModel(Configs.sphereTexturedModel);
		for (TexturedModel model : entities.keySet()) {
			List<RenderableObject> batch = entities.get(model);
			for (RenderableObject renderableObject : batch) {
				prepareInstanceHitbox(renderableObject);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(),
						GL11.GL_UNSIGNED_INT, 0);
			}
		}
		unbindTexturedModel();
	}

	/**
	 * Bind the VAO attributes, load uniforms to the shader and set the active texture
	 * @param model chunk currently being processed
	 */
	private void prepareTexturedModel(TexturedModel model) {
		Model rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		ModelTexture texture = model.getTexture();
		shader.currentTime.loadInteger((int) (DisplayManager.getCurrentTime() % Configs.ANT_ANIMATION_DURATION));

		if (model == Configs.antTexturedModel) {
			shader.animated.loadBoolean(true);
			shader.animationDuration.loadInteger(Configs.ANT_ANIMATION_DURATION);
		} else {
			shader.animated.loadBoolean(false);
		}
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
	}

	/**
	 * Bind the VAO attributes, load uniforms to the shader and set the active texture
	 * @param model chunk currently being processed
	 */
	private void prepareHitboxModel(TexturedModel model) {
		Model rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
	}

	/**
	 * unbind all the VAO attributes of the current model
	 */
	private void unbindTexturedModel() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	/**
	 * load the transformation matrix to the entity shader
	 * @param renderableObject entity to be rendered
	 */
	private void prepareInstance(RenderableObject renderableObject) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrixAnt(renderableObject.getPosition(), renderableObject.getRotX(), renderableObject.getRotY(), renderableObject.getRotZ(), renderableObject.getScale());
		shader.transformationMatrix.loadMatrix(transformationMatrix);
	}

	/**
	 * load the transformation matrix and other Uniforms to the hitbox shader
	 * @param renderableObject entity to render a hitbox for
	 */
	private void prepareInstanceHitbox(RenderableObject renderableObject) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(renderableObject.getPosition(), renderableObject.getRotX(), renderableObject.getRotY(), renderableObject.getRotZ(), 1);
		hitboxShader.transformationMatrix.loadMatrix(transformationMatrix);
		hitboxShader.radius.loadFloat(renderableObject.getBoundingSphereRadius());
		hitboxShader.positionOffset.loadVec3(renderableObject.getBoundingSphereOffset());
	}

}
