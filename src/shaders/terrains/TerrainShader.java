package shaders.terrains;

import entities.Camera;
import entities.EmptyLightSource;
import entities.LightSource;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import shaders.ShaderProgram;
import toolbox.Maths;

import java.io.File;
import java.util.List;

public class TerrainShader extends ShaderProgram {

    private static final String BASE_PATH = "src" + File.separator + "shaders" + File.separator + "terrains" + File.separator;
    private static final String VERTEX_FILE = BASE_PATH + "terrainVertexShader.glsl";
    private static final String FRAGMENT_FILE = BASE_PATH + "terrainFragmentShader.glsl";

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

    public TerrainShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_skyColour = super.getUniformLocation("skyColour");
        location_backgroundTexture = super.getUniformLocation("backgroundTexture");
        location_rTexture = super.getUniformLocation("rTexture");
        location_gTexture = super.getUniformLocation("gTexture");
        location_bTexture = super.getUniformLocation("bTexture");
        location_blendMap = super.getUniformLocation("blendMap");

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
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
    }

    public void connectTextureUnits() {
        super.loadInt(location_backgroundTexture, 0);
        super.loadInt(location_rTexture, 1);
        super.loadInt(location_gTexture, 2);
        super.loadInt(location_bTexture, 3);
        super.loadInt(location_blendMap, 4);
    }

    public void loadSkyColour(Vector3f colour) {
        super.load3DVector(location_skyColour, colour);
    }

    public void loadLights(List<LightSource> lightSources) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            LightSource lightSource = i < lightSources.size() ? lightSources.get(i) : new EmptyLightSource();
            super.load3DVector(location_lightPosition[i], lightSource.getPosition());
            super.load3DVector(location_lightColour[i], lightSource.getColour());
            super.load3DVector(location_lightAttenuation[i], lightSource.getAttenuation());
        }
    }

    public void loadTextureProperties(float damper, float reflectivity) {
        super.loadFloat(location_shineDamper, damper);
        super.loadFloat(location_reflectivity, reflectivity);
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