package openGL.world;

import engineTester.MainGameLoop;
import openGL.models.Model;
import openGL.renderEngine.Loader;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Chunk {

	public static final int VERTEX_COUNT = 3;

	private int x;
	private int z;
	private float[][] height;
	private HeightsGenerator generator;
	private Model model;

	public Chunk(int gridX, int gridZ, HeightsGenerator generator){
		this.x = gridX;
		this.z = gridZ;
		this.generator = generator;
		height = new float[VERTEX_COUNT][VERTEX_COUNT];
		this.model = generateTerrain(MainGameLoop.loader);
	}

	/**
	 * Get the X coord of the chunk
	 * @return X coord of the chunk
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the Z coord of the chunk
	 * @return Z coord of the chunk
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Get the chunk's model
	 * @return the chunk's model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Generate a model for the chunk
	 * @param loader the loader used to generate the VAO
	 * @return a Model for the chunk
	 */
	private Model generateTerrain(Loader loader){
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count*2];
		int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
		int vertexPointer = 0;
		for(int i=0;i<VERTEX_COUNT;i++){
			for(int j=0;j<VERTEX_COUNT;j++){
				vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1);
				vertices[vertexPointer*3+1] = getHeight(j, i);
				vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1);
				height[i][j] = vertices[vertexPointer*3+1];
				Vector3f normal = getNormal(j, i);
				normals[vertexPointer*3] = normal.x;
				normals[vertexPointer*3+1] = normal.y;
				normals[vertexPointer*3+2] = normal.z;
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<VERTEX_COUNT-1;gz++){
			for(int gx=0;gx<VERTEX_COUNT-1;gx++){
				int topLeft = (gz*VERTEX_COUNT)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}

	/**
	 * Get the height of the chunk at coords (x, z)
	 * @param x the x coord
	 * @param z the z coord
	 * @return the height at coords (x, z)
	 */
	private float getHeight(float x, float z) {
		return generator.getHeightInChunk(this.x, this.z, x, z);
	}

	/**
	 * Get the normal of the chunk at coords (x, z)
	 * @param x the x coord
	 * @param z the z coord
	 * @return the normal at coords (x, z)
	 */
	public Vector3f getNormal(float x, float z) {
		float hL = getHeight(x - 1, z);
		float hR = getHeight(x + 1, z);
		float hD = getHeight(x, z - 1);
		float hU = getHeight(x, z + 1);
		Vector3f normal = new Vector3f(hL - hR, 2f, hD - hU);
		normal.normalise();
		return normal;
	}
}
