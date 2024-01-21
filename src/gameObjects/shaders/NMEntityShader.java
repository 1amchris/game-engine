package gameObjects.shaders;

import java.util.List;

import gameObjects.entities.EmptyLightSource;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import gameObjects.entities.LightSource;
import shared.shaders.ShaderProgram;
import shared.textures.ModelTexture;
import shared.toolbox.Directories;

public class NMEntityShader extends ShaderProgram {
	
	private static final int MAX_LIGHTS = 4;

	private static final String BASE_PATH = Directories.fromPath("src", "gameObjects", "shaders");
	private static final String VERTEX_FILE = BASE_PATH + "NMEntityVertexShader.glsl";
	private static final String FRAGMENT_FILE = BASE_PATH + "NMEntityFragmentShader.glsl";
	
	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int[] location_lightPositionEyeSpace;
	private int[] location_lightColour;
	private int[] location_lightAttenuation;
	private int location_shineDamper;
	private int location_reflectivity;
	private int location_skyColour;
	private int location_atlasGridSize;
	private int location_atlasTextureOffset;
	private int location_clipPlane;
	private int location_modelTexture;
	private int location_normalMap;

	public NMEntityShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoordinates");
		super.bindAttribute(2, "normal");
		super.bindAttribute(3, "tangent");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = getUniformLocation("transformationMatrix");
		location_projectionMatrix = getUniformLocation("projectionMatrix");
		location_viewMatrix = getUniformLocation("viewMatrix");
		location_shineDamper = getUniformLocation("shineDamper");
		location_reflectivity = getUniformLocation("reflectivity");
		location_skyColour = getUniformLocation("skyColour");
		location_atlasGridSize = getUniformLocation("numberOfRows");
		location_atlasTextureOffset = getUniformLocation("offset");
		location_clipPlane = getUniformLocation("plane");
		location_modelTexture = getUniformLocation("modelTexture");
		location_normalMap = getUniformLocation("normalMap");

		location_lightPositionEyeSpace = new int[MAX_LIGHTS];
		location_lightColour = new int[MAX_LIGHTS];
		location_lightAttenuation = new int[MAX_LIGHTS];

		for (int i = 0; i < MAX_LIGHTS; i++) {
			location_lightPositionEyeSpace[i] = getUniformLocation("lightPositionEyeSpace[" + i + "]");
			location_lightColour[i] = getUniformLocation("lightColour[" + i + "]");
			location_lightAttenuation[i] = getUniformLocation("attenuation[" + i + "]");
		}
	}
	
	public void connectTextureUnits(){
		loadInt(location_modelTexture, 0);
		loadInt(location_normalMap, 1);
	}
	
	public void loadClipPlane(Vector4f plane){
		loadVector(location_clipPlane, plane);
	}
	
	public void loadAtlasGridSize(int atlasGridSize) {
		loadFloat(location_atlasGridSize, atlasGridSize);
	}
	
	public void loadAtlasTextureOffset(Vector2f offset){
		loadVector(location_atlasTextureOffset, offset);
	}
	
	public void loadSkyColour(Vector3f colour) {
		loadVector(location_skyColour, colour);
	}
	
	public void loadTexture(ModelTexture texture){
		super.loadFloat(location_shineDamper, texture.getShineDamper());
		super.loadFloat(location_reflectivity, texture.getReflectivity());
	}
	
	public void loadTransformationMatrix(Matrix4f matrix){
		loadMatrix(location_transformationMatrix, matrix);
	}
	
	public void loadLights(List<LightSource> lightSources, Matrix4f viewMatrix){
		for (int i = 0; i < MAX_LIGHTS; i++) {
			LightSource lightSource = i < lightSources.size() ? lightSources.get(i) : new EmptyLightSource();
			loadVector(location_lightPositionEyeSpace[i], getEyeSpacePosition(lightSources.get(i).getPosition(), viewMatrix));
			loadVector(location_lightColour[i], lightSource.getColour());
			loadVector(location_lightAttenuation[i], lightSource.getAttenuation());
		}
	}
	
	public void loadViewMatrix(Matrix4f viewMatrix){
		loadMatrix(location_viewMatrix, viewMatrix);
	}
	
	public void loadProjectionMatrix(Matrix4f projection){
		loadMatrix(location_projectionMatrix, projection);
	}
	
	private Vector3f getEyeSpacePosition(Vector3f position, Matrix4f viewMatrix){
		Vector4f eyeSpacePos = new Vector4f(position.x, position.y, position.z, 1f);
		Matrix4f.transform(viewMatrix, eyeSpacePos, eyeSpacePos);
		return new Vector3f(eyeSpacePos);
	}
}
