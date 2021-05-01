package openGL.shaders;

import openGL.shaders.Uniforms.Uniform;
import openGL.shaders.Uniforms.UniformFloat;
import openGL.shaders.Uniforms.UniformMatrix;
import openGL.shaders.Uniforms.UniformVec3;

public class HitboxShader extends ShaderProgram{

    private static final String VERTEX_SHADER = "res/shaders/hitbox/vertexShader.glsl";
    private static final String FRAGMENT_SHADER = "res/shaders/hitbox/fragmentShader.glsl";

    public UniformMatrix transformationMatrix = new UniformMatrix("transformationMatrix");
    public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
    public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
    public UniformVec3 positionOffset = new UniformVec3("positionOffset");
    public UniformFloat radius = new UniformFloat("radius");

    /**
     * Create a new HitboxShader and store all of the Uniforms variables
     */
    public HitboxShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
        super.storeAllUniformLocations(transformationMatrix, projectionMatrix, viewMatrix, positionOffset, positionOffset, radius);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
    }
}
