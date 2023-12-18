package shaders;

import entities.Camera;
import entities.EmptyLightSource;
import entities.LightSource;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import textures.ModelTexture;
import toolbox.Maths;

import java.io.File;
import java.util.List;

public class StaticShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src" + File.separator + "shaders" + File.separator + "entityVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src" + File.separator + "shaders" + File.separator + "entityFragmentShader.glsl";

    private static final int MAX_LIGHTS = 4;

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int[] location_lightPosition;
    private int[] location_lightColour;
    private int[] location_lightAttenuation;
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
        location_transformationMatrix = getUniformLocation("transformationMatrix");
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_shineDamper = getUniformLocation("shineDamper");
        location_reflectivity = getUniformLocation("reflectivity");
        location_useFakeLighting = getUniformLocation("useFakeLighting");
        location_skyColour = getUniformLocation("skyColour");
        location_atlasGridSize = getUniformLocation("atlasGridSize");
        location_atlasTextureOffset = getUniformLocation("atlasTextureOffset");

        location_lightPosition = new int[MAX_LIGHTS];
        location_lightColour = new int[MAX_LIGHTS];
        location_lightAttenuation = new int [MAX_LIGHTS];

        for (int i = 0; i < MAX_LIGHTS; i++) {
            location_lightPosition[i] = getUniformLocation("lightPosition[" + i + "]");
            location_lightColour[i] = getUniformLocation("lightColour[" + i + "]");
            location_lightAttenuation[i] = getUniformLocation("lightAttenuation[" + i + "]");
        }
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
        bindAttribute(1, "textureCoords");
        bindAttribute(2, "normal");
    }

    public void loadAtlasGridSize(int atlasGridSize) {
        loadFloat(location_atlasGridSize, atlasGridSize);
    }

    public void loadAtlasTextureOffset(Vector2f offset) {
        load2DVector(location_atlasTextureOffset, offset);
    }

    public void loadSkyColour(Vector3f colour) {
        load3DVector(location_skyColour, colour);
    }

    public void loadLights(List<LightSource> lightSources) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            LightSource lightSource = i < lightSources.size() ? lightSources.get(i) : new EmptyLightSource();
            load3DVector(location_lightPosition[i], lightSource.getPosition());
            load3DVector(location_lightColour[i], lightSource.getColour());
            load3DVector(location_lightAttenuation[i], lightSource.getAttenuation());
        }
    }

    public void loadTextureProperties(ModelTexture texture) {
        loadFloat(location_shineDamper, texture.getShineDamper());
        loadFloat(location_reflectivity, texture.getReflectivity());
        loadBoolean(location_useFakeLighting, texture.getUseFakeLighting());
    }

    public void loadTransformationMatrix(Matrix4f matrix) {
        loadMatrix(location_transformationMatrix, matrix);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        loadMatrix(location_viewMatrix, matrix);
    }
}
