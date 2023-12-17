package shaders;

import entities.Camera;
import entities.Light;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import textures.ModelTexture;
import toolbox.Maths;

import java.io.File;

public class StaticShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src" + File.separator + "shaders" + File.separator + "vertexShader.glsl";
    private static final String FRAGMENT_FILE = "src" + File.separator + "shaders" + File.separator + "fragmentShader.glsl";

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_lightPosition;
    private int location_lightColour;
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_useFakeLighting;
    private int location_skyColour;
    private int location_atlasGridSize;
    private int location_atlasTextureOffset;

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_lightPosition = super.getUniformLocation("lightPosition");
        location_lightColour = super.getUniformLocation("lightColour");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_useFakeLighting = super.getUniformLocation("useFakeLighting");
        location_skyColour = super.getUniformLocation("skyColour");
        location_atlasGridSize = super.getUniformLocation("atlasGridSize");
        location_atlasTextureOffset = super.getUniformLocation("atlasTextureOffset");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    public void loadAtlasGridSize(int atlasGridSize) {
        loadFloat(location_atlasGridSize, atlasGridSize);
    }

    public void loadAtlasTextureOffset(Vector2f offset) {
        load2DVector(location_atlasTextureOffset, offset);
    }

    public void loadSkyColour(Vector3f colour) {
        super.load3DVector(location_skyColour, colour);
    }

    public void loadLight(Light light) {
        super.load3DVector(location_lightPosition, light.getPosition());
        super.load3DVector(location_lightColour, light.getColour());
    }

    public void loadTextureProperties(ModelTexture texture) {
        super.loadFloat(location_shineDamper, texture.getShineDamper());
        super.loadFloat(location_reflectivity, texture.getReflectivity());
        super.loadBoolean(location_useFakeLighting, texture.getUseFakeLighting());
    }

    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        super.loadMatrix(location_viewMatrix, matrix);
    }
}
