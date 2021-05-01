package openGL.utils;

import openGL.world.Chunk;
import openGL.world.World;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import openGL.entities.Camera;

public class Maths {

	/**
	 * Create the transformation matrix, it enable us to use the same model to render different instance in different places in the world
	 * @param translation position of the object in the world
	 * @param rx rotation of the object around the x-axis
	 * @param ry rotation of the object around the y-axis
	 * @param rz rotation of the object around the z-axis
	 * @param scale scale of the object
	 * @return The transformation matrix used to transform the object's local coordinates to coordinates in the world
	 */
	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), matrix, matrix);
		return matrix;
	}

	/**
	 * Create the transformation matrix, it enable us to use the same model to render different instance in different places in the world
	 * @param translation position of the object in the world
	 * @param rx rotation of the object around the x-axis
	 * @param ry rotation of the object around the y-axis
	 * @param rz rotation of the object around the z-axis
	 * @param scale scale of the object
	 * @return The transformation matrix used to transform the object's local coordinates to coordinates in the world
	 */
	public static Matrix4f createTransformationMatrixAnt(Vector3f translation, float rx, float ry, float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.translate(new Vector3f(0, .15f, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.translate(new Vector3f(0, -.15f, 0), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), matrix, matrix);
		return matrix;
	}

	/**
	 * Create the view matrix, which is used to transform coordinates from absolute world space to camera space
	 * @param camera the camera used for rendering
	 * @return The view matrix used to transform the object's world coordinates to coordinates in the camera space
	 */
	public static Matrix4f createViewMatrix(Camera camera) {
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x,-cameraPos.y,-cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
	}

	/**
	 * Make the input vector coordinates loop around the size vector
	 * it's effectively a modulus
	 * @param input the input vector to loop around
	 * @param size vector representing the upper limit of each coordinates
	 * @return a looped 3D Vector with it's input included in [0; size]
	 */
	public static Vector3f loopVector(Vector3f input, Vector3f size) {
		if (input.x < 0)
			input.x += size.x;
		if (input.x > size.x)
			input.x -= size.x;
		if (input.y < 0)
			input.y += size.y;
		if (input.y > size.y)
			input.y -= size.y;
		if (input.z < 0)
			input.z += size.z;
		if (input.z > size.z)
			input.z -= size.z;
		return input;
	}

	/**
	 * Make the input vector coordinates loop around the size vector
	 * it's effectively a modulus
	 * @param input the input vector to loop around
	 * @param size vector representing the upper limit of each coordinates
	 * @return a looped 2D Vector with it's input included in [0; size]
	 */
	public static Vector2f loopVector(Vector2f input, Vector2f size) {
		if (input.x < 0)
			input.x += size.x;
		if (input.x > size.x)
			input.x -= size.x;
		if (input.y < 0)
			input.y += size.y;
		if (input.y > size.y)
			input.y -= size.y;
		return input;
	}

	/**
	 * Clamp a value between two bounds
	 * @param val value to clamp
	 * @param min lower bound (included)
	 * @param max upper bound (included)
	 * @return the clamped value
	 */
	public static float clamp(float val, float min, float max) {
		if (val > max)
			val = max;
		else if (val < min)
			val = min;
		return val;
	}

	/**
	 * Return the signed angle from u to v in radians
	 * @param u base vector
	 * @param v result vector
	 * @return angle from u to v in radians
	 */
	public static float signedAngle(Vector2f u, Vector2f v)
	{
		Vector2f tmp = new Vector2f(-u.y, u.x);
		return (float) Math.atan2(Vector2f.dot(tmp, v), Vector2f.dot(u, v));
	}

	public static Vector2f calculateXZRotations(World world, float x, float z) {
		Vector3f normal = world.getNormal(x, z);
		float rotX = 1.618f * (float) Math.toDegrees(Maths.signedAngle(new Vector2f(normal.z, normal.y), new Vector2f(0, 1)));
		float rotZ = 1.618f * (float) Math.toDegrees(Maths.signedAngle(new Vector2f(0, 1), new Vector2f(normal.x, normal.y)));

		return new Vector2f(rotX, rotZ);
	}
}
