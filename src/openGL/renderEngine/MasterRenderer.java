package openGL.renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configs.Configs;
import entity.Ant;
import entity.Food;
import openGL.models.TexturedModel;

import openGL.shaders.HitboxShader;
import openGL.utils.Maths;
import openGL.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import openGL.shaders.StaticShader;
import openGL.shaders.WorldShader;
import openGL.entities.Camera;
import openGL.entities.RenderableObject;
import openGL.entities.Light;

public class MasterRenderer {
	
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 200;

	private boolean isWireframe = false;
	private boolean canToogleWireframe = true;

	private Matrix4f projectionMatrix;
	
	private StaticShader entityShader = new StaticShader();
	private HitboxShader hitboxShader = new HitboxShader();
	private EntityRenderer entityRenderer;
	
	private WorldRenderer worldRenderer;
	private WorldShader worldShader = new WorldShader();
	
	
	private Map<TexturedModel,List<RenderableObject>> entities = new HashMap<TexturedModel,List<RenderableObject>>();
	private World world;

	/**
	 * Create a new MasterRenderer and create sub renderer and projection matrix
	 * @param world the world in wich the scene takes place
	 */
	public MasterRenderer(World world){
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		createProjectionMatrix();
		entityRenderer = new EntityRenderer(entityShader, hitboxShader, projectionMatrix);
		worldRenderer = new WorldRenderer(worldShader, projectionMatrix);
		this.world = world;
	}

	/**
	 * Render the scene to the screen
	 * @param sun the main light source of the scene
	 * @param camera the camera used to render, it represent the screen
	 * @param hitbox do we need to render the hitboxes or not
	 */
	public void render(Light sun,Camera camera, boolean hitbox){
		prepare();
		if (hitbox) {
			hitboxShader.start();
			hitboxShader.viewMatrix.loadMatrix(Maths.createViewMatrix(camera));
			entityRenderer.renderHitbox(entities);
			hitboxShader.stop();
		}
		entityShader.start();
		entityShader.lightColour.loadVec3(sun.getColour());
		entityShader.lightPosition.loadVec3(sun.getPosition());
		entityShader.viewMatrix.loadMatrix(Maths.createViewMatrix(camera));
		entityShader.skyColor.loadVec3(Configs.SKY_COLOR);
		entityShader.density.loadFloat(Configs.FOG_DENSITY);
		entityShader.gradient.loadFloat(Configs.FOG_GRADIENT);
		entityShader.worldCenter.loadVec3(world.getSizeX()/2f, 0,  world.getSizeZ()/2f);
		entityRenderer.render(entities);
		entityShader.stop();
		worldShader.start();
		worldShader.lightColour.loadVec3(sun.getColour());
		worldShader.lightPosition.loadVec3(sun.getPosition());
		worldShader.viewMatrix.loadMatrix(Maths.createViewMatrix(camera));
		worldShader.skyColor.loadVec3(Configs.SKY_COLOR);
		worldShader.density.loadFloat(Configs.FOG_DENSITY);
		worldShader.gradient.loadFloat(Configs.FOG_GRADIENT);
		worldShader.worldCenter.loadVec3(world.getSizeX()/2f, 0,  world.getSizeZ()/2f);
		worldRenderer.render(world);
		worldShader.stop();
		entities.clear();
	}

	/**
	 * Register a list of entities to be rendered in the next frame
	 * @param renderableObjects list of object to be rendered
	 */
	public void registerRenderableObjects(List<RenderableObject> renderableObjects){
		for (RenderableObject renderableObject : renderableObjects) {
			if (renderableObject != null && renderableObject.canBeRendered()) {
				TexturedModel entityModel = renderableObject.getModel();
				List<RenderableObject> batch = entities.get(entityModel);
				if (batch != null) {
					batch.add(renderableObject);
				} else {
					List<RenderableObject> newBatch = new ArrayList<RenderableObject>();
					newBatch.add(renderableObject);
					entities.put(entityModel, newBatch);
				}
			}
		}
	}

	/**
	 * Register a list of ants to be rendered in the next frame
	 * @param ants list of ants to be rendered
	 */
	public void registerAnts(List<Ant> ants){
		for (Ant ant : ants) {
			if (ant != null && ant.canBeRendered()) {
				TexturedModel entityModel = ant.getModel();
				List<RenderableObject> batch = entities.get(entityModel);
				if (batch != null) {
					batch.add(ant);
				} else {
					List<RenderableObject> newBatch = new ArrayList<RenderableObject>();
					newBatch.add(ant);
					entities.put(entityModel, newBatch);
				}
			}
		}
	}

	/**
	 * Register an ant to be rendered in the next frame
	 * @param ant ant to be rendered
	 */
	public void registerAnt(Ant ant){
		if (ant != null && ant.canBeRendered()) {
			TexturedModel entityModel = ant.getModel();
			List<RenderableObject> batch = entities.get(entityModel);
			if (batch != null) {
				batch.add(ant);
			} else {
				List<RenderableObject> newBatch = new ArrayList<RenderableObject>();
				newBatch.add(ant);
				entities.put(entityModel, newBatch);
			}
		}
	}

	/**
	 * Register a food to be rendered in the next frame
	 * @param food food to be rendered
	 */
	public void registerFood(Food food){
		if (food != null && food.canBeRendered()) {
			TexturedModel entityModel = food.getModel();
			List<RenderableObject> batch = entities.get(entityModel);
			if (batch != null) {
				batch.add(food);
			} else {
				List<RenderableObject> newBatch = new ArrayList<RenderableObject>();
				newBatch.add(food);
				entities.put(entityModel, newBatch);
			}
		}
	}

	/**
	 * Clear the renderer by removing the shader from GPU RAM
	 */
	public void clear(){
		entityShader.clear();
		worldShader.clear();
	}


	/**
	 * Prepare the renderer for the rendering of the next frame
	 */
	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(Configs.SKY_COLOR.x, Configs.SKY_COLOR.y, Configs.SKY_COLOR.z, 1);
		if (!Keyboard.isKeyDown(Keyboard.KEY_W))
			canToogleWireframe = true;
	}

	/**
	 * Calculate the projection matrix using the FOV, and the clip planes
	 * it will be used to simulate perspective on the screen
	 */
	private void createProjectionMatrix() {
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
	}

	/**
	 * Return the projection matrix used to render the scene
	 * @return projection matrix
	 */
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	/**
	 * Switch between wireframe and filled rendering
	 */
	public void toogleWireframe() {
		canToogleWireframe = false;
		isWireframe = !isWireframe;
		if (isWireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		} else {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
	}

	/**
	 * Return whether or not the Renderer is able to switch between wireframe and filled rendering
	 * @return can switch between rendering mode
	 */
	public boolean canToogleWireframe() {
		return canToogleWireframe;
	}

	/**
	 * Set the world to be rendered
	 * @param world the world to render
	 */
	public void setWorld(World world) {
		this.world = world;
	}
}
