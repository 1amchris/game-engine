package water.shaders;

import gameObjects.entities.LightSource;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import shared.shaders.ShaderProgram;
import shared.toolbox.Maths;
import gameObjects.entities.Camera;

import java.io.File;

public class WaterShader extends ShaderProgram {

	private static final String BASE_PATH = String.join(File.separator, new String[] { "src", "water", "shaders" }) + File.separator;
	private final static String VERTEX_FILE = BASE_PATH + "WaterVertexShader.glsl";
	private final static String FRAGMENT_FILE = BASE_PATH + "WaterFragmentShader.glsl";

	private int location_modelMatrix;
	private int location_viewMatrix;
	private int location_projectionMatrix;
	private int location_reflectionTexture;
	private int location_refractionTexture;

	private int location_dudvMap;
	private int location_normalMap;
	private int location_depthMap;

	private int location_cameraPosition;
	private int location_waveDisplacement;
	private int location_refractivity;
	private int location_reflectivity;
	private int location_shineDamper;
	private int location_colour;
	private int location_ripplesStrength;
	private int location_lightColour;
	private int location_lightPosition;

	private int location_nearPlane;
	private int location_farPlane;

	public WaterShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		bindAttribute(0, "position");
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = getUniformLocation("projectionMatrix");
		location_viewMatrix = getUniformLocation("viewMatrix");
		location_modelMatrix = getUniformLocation("modelMatrix");
		location_reflectionTexture = getUniformLocation("reflectionTexture");
		location_refractionTexture = getUniformLocation("refractionTexture");

		location_dudvMap = getUniformLocation("dudvMap");
		location_normalMap = getUniformLocation("normalMap");
		location_depthMap = getUniformLocation("depthMap");

		location_cameraPosition = getUniformLocation("cameraPosition");
		location_colour = getUniformLocation("colour");
		location_waveDisplacement = getUniformLocation("waveDisplacement");
		location_ripplesStrength = getUniformLocation("ripplesStrength");
		location_refractivity = getUniformLocation("refractivity");
		location_reflectivity = getUniformLocation("reflectivity");
		location_shineDamper = getUniformLocation("shineDamper");
		location_lightColour = getUniformLocation("lightColour");
		location_lightPosition = getUniformLocation("lightPosition");

		location_nearPlane = getUniformLocation("nearPlane");
		location_farPlane = getUniformLocation("farPlane");
	}

	public void connectTextureUnits() {
		loadInt(location_reflectionTexture, 0);
		loadInt(location_refractionTexture, 1);
		loadInt(location_dudvMap, 2);
		loadInt(location_normalMap, 3);
		loadInt(location_depthMap, 4);
	}

	public void loadLightSource(LightSource lightSource) {
		loadVector(location_lightColour, lightSource.getColour());
		loadVector(location_lightPosition, lightSource.getPosition());
	}

	public void loadMotionProperties(float waveDisplacement, float ripplesStrength) {
		loadFloat(location_waveDisplacement, waveDisplacement);
		loadFloat(location_ripplesStrength, ripplesStrength);
	}

	public void loadProperties(Vector3f colour,  float refractivity, float reflectivity, float shineDamper) {
		loadVector(location_colour, colour);
		loadFloat(location_refractivity, refractivity);
		loadFloat(location_reflectivity, reflectivity);
		loadFloat(location_shineDamper, shineDamper);
	}

	public void setupShader(Matrix4f projection) {
		loadMatrix(location_projectionMatrix, projection);
	}
	
	public void loadViewProperties(Camera camera, float nearPlane, float farPlane){
		loadVector(location_cameraPosition, camera.getPosition());
		loadMatrix(location_viewMatrix, Maths.createViewMatrix(camera));
		loadFloat(location_nearPlane, nearPlane);
		loadFloat(location_farPlane, farPlane);
	}

	public void loadModelMatrix(Matrix4f modelMatrix){
		loadMatrix(location_modelMatrix, modelMatrix);
	}

}
