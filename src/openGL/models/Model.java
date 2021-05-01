package openGL.models;

import engineTester.MainGameLoop;

public class Model {
	
	private int vaoID;
	private int vertexCount;

	/**
	 * Create a new Model and link a VAO to it
	 * @param vaoID the ID of the Model's VAO
	 * @param vertexCount the number of vertices in the Model
	 */
	public Model(int vaoID, int vertexCount){
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
	}

	/**
	 * Return the model's VAO ID
	 * @return VAO ID of the model
	 */
	public int getVaoID() {
		return vaoID;
	}

	/**
	 * Return the number of vertices in the model
	 * @return number of vertices
	 */
	public int getVertexCount() {
		return vertexCount;
	}


}
