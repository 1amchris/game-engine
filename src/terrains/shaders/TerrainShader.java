package terrains.shaders;

import gameObjects.entities.Camera;
import gameObjects.entities.EmptyLightSource;
import gameObjects.entities.LightSource;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import shared.shaders.ShaderProgram;
import shared.toolbox.Maths;

import java.io.File;
import java.util.List;

public class TerrainShader extends ShaderProgram {

    private static final String BASE_PATH = String.join(File.separator, new String[] { "src", "terrains", "shaders" }) + File.separator;
    private static final String VERTEX_FILE = BASE_PATH + "TerrainVertexShader.glsl";
    private static final String FRAGMENT_FILE = BASE_PATH + "TerrainFragmentShader.glsl";

    private static final int MAX_LIGHTS = 4;

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int[] location_lightPosition;
    private int[] location_lightColour;
    private int[] location_lightAttenuation;
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_skyColour;
    private int location_backgroundTexture;
    private int location_rTexture;
    private int location_gTexture;
    private int location_bTexture;
    private int location_blendMap;
    private int location_clipPlane;

    public TerrainShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = getUniformLocation("transformationMatrix");
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_shineDamper = getUniformLocation("shineDamper");
        location_reflectivity = getUniformLocation("reflectivity");
        location_skyColour = getUniformLocation("skyColour");
        location_backgroundTexture = getUniformLocation("backgroundTexture");
        location_rTexture = getUniformLocation("rTexture");
        location_gTexture = getUniformLocation("gTexture");
        location_bTexture = getUniformLocation("bTexture");
        location_blendMap = getUniformLocation("blendMap");
        location_clipPlane = getUniformLocation("clipPlane");

        location_lightPosition = new int[MAX_LIGHTS];
        location_lightColour = new int[MAX_LIGHTS];
        location_lightAttenuation = new int[MAX_LIGHTS];

        for (int i = 0; i < MAX_LIGHTS; i++) {
            location_lightPosition[i] = getUniformLocation("lightPosition[" + i + "]");
            location_lightColour[i] = getUniformLocation("lightColour[" + i + "]");
            location_lightAttenuation[i] = getUniformLocation("lightAttenuation[" + i + "]");
        }
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
        bindAttribute(1, "textureCoordinates");
        bindAttribute(2, "normal");
    }

    public void connectTextureUnits() {
        loadInt(location_backgroundTexture, 0);
        loadInt(location_rTexture, 1);
        loadInt(location_gTexture, 2);
        loadInt(location_bTexture, 3);
        loadInt(location_blendMap, 4);
    }

    public void loadClipPlane(Vector4f plane) {
        loadVector(location_clipPlane, plane);
    }

    public void loadSkyColour(Vector3f colour) {
        loadVector(location_skyColour, colour);
    }

    public void loadLights(List<LightSource> lightSources) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            LightSource lightSource = i < lightSources.size() ? lightSources.get(i) : new EmptyLightSource();
            loadVector(location_lightPosition[i], lightSource.getPosition());
            loadVector(location_lightColour[i], lightSource.getColour());
            loadVector(location_lightAttenuation[i], lightSource.getAttenuation());
        }
    }

    public void loadTextureProperties(float damper, float reflectivity) {
        loadFloat(location_shineDamper, damper);
        loadFloat(location_reflectivity, reflectivity);
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
