package water.shaders;

import org.lwjgl.util.vector.Matrix4f;
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
	}

	public void loadProjectionMatrix(Matrix4f projection) {
		loadMatrix(location_projectionMatrix, projection);
	}
	
	public void loadViewMatrix(Camera camera){
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		loadMatrix(location_viewMatrix, viewMatrix);
	}

	public void loadModelMatrix(Matrix4f modelMatrix){
		loadMatrix(location_modelMatrix, modelMatrix);
	}

}
