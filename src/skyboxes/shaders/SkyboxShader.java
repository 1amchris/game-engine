package skyboxes.shaders;

import gameObjects.entities.Camera;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import shared.renderers.DisplayManager;
import shared.shaders.ShaderProgram;
import shared.toolbox.Directories;
import shared.toolbox.Maths;

public class SkyboxShader extends ShaderProgram {

    private static final String BASE_PATH = Directories.fromPath("src", "skyboxes", "shaders");
    private static final String VERTEX_FILE = BASE_PATH + "SkyboxVertexShader.glsl";
    private static final String FRAGMENT_FILE = BASE_PATH + "SkyboxFragmentShader.glsl";

    private static final float ROTATION_SPEED = 0.3f;

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_fogColour;
    private int location_cubeMap;
    private int location_cubeMap2;
    private int location_blendFactor;

    private float skyboxRotation = 0;

    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        skyboxRotation = skyboxRotation + ROTATION_SPEED * DisplayManager.getDeltaTime();
        skyboxRotation %= 360;
        Matrix4f.rotate((float) Math.toRadians(skyboxRotation), new Vector3f(0, 1, 0), matrix, matrix);
        loadMatrix(location_viewMatrix, matrix);
    }

    public void connectTextureUnits() {
        super.loadInt(location_cubeMap, 0);
        super.loadInt(location_cubeMap2, 1);
    }

    public void loadFogColour(Vector3f fogColour) {
        loadVector(location_fogColour, fogColour);
    }

    public void loadBlendFactor(float blendFactor) {
        loadFloat(location_blendFactor, blendFactor);
    }

    @Override
    public void getAllUniformLocations() {
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_fogColour = getUniformLocation("fogColour");
        location_cubeMap = getUniformLocation("cubeMap");
        location_cubeMap2 = getUniformLocation("cubeMap2");
        location_blendFactor = getUniformLocation("blendFactor");
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
    }
}
