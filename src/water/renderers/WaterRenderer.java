package water.renderers;

import java.io.File;
import java.util.List;

import gameObjects.entities.LightSource;
import org.lwjgl.opengl.GL13;
import shared.models.RawModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import shared.renderers.DisplayManager;
import shared.renderers.Loader;
import shared.toolbox.Directories;
import shared.toolbox.Maths;
import gameObjects.entities.Camera;
import water.entities.WaterTile;
import water.shaders.WaterShader;

public class WaterRenderer {

	private static final String BASE_DIR = Directories.fromPath("water");
	private static final String DUDV_MAP = BASE_DIR + "dudvMap";
	private static final String NORMAL_MAP = BASE_DIR + "normalMap";

	private static final float WAVE_SPEED = 0.03f;
	private static final float RIPPLES_STRENGTH = 0.04f;
	private static final float REFRACTIVITY = 2.0f;
	private static final float REFLECTIVITY = 0.5f;
	private static final float SHINE_DAMPER = 20.0f;
	private static final Vector3f WATER_COLOUR = new Vector3f(0.0f, 0.3f, 0.5f);

	private final RawModel quad;
	private final WaterShader shader;
	private final WaterFrameBuffers frameBuffers;

	private final int dudvMapId;
	private final int normalMapId;

	private float waveDisplacement = 0;

	public WaterRenderer(WaterShader shader, WaterFrameBuffers frameBuffers, Loader loader) {
		this.shader = shader;
		this.frameBuffers = frameBuffers;
		this.quad = loadQuad(loader);
		this.dudvMapId = loader.loadTexture(DUDV_MAP);
		this.normalMapId = loader.loadTexture(NORMAL_MAP);
	}

	public void setupShader(Matrix4f projectionMatrix) {
		shader.start();
		shader.connectTextureUnits();
		shader.setupShader(projectionMatrix);
		shader.stop();
	}

	public void render(List<WaterTile> water, LightSource lightSource, Camera camera, float nearPlane, float farPlane) {
		prepareRender(lightSource, camera, nearPlane, farPlane);
		for (WaterTile tile : water) {
			Matrix4f modelMatrix = Maths.createTransformationMatrix(
					tile.getPosition(), new Vector3f(0, 0, 0), WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
		}
		unbind();
	}
	
	private void prepareRender(LightSource lightSource, Camera camera, float nearPlane, float farPlane){
		shader.start();
		shader.loadViewProperties(camera, nearPlane, farPlane);
		shader.loadLightSource(lightSource);

		waveDisplacement += WAVE_SPEED * DisplayManager.getDeltaTime();
		waveDisplacement %= 1;
		shader.loadMotionProperties(waveDisplacement, RIPPLES_STRENGTH);
		shader.loadProperties(WATER_COLOUR, REFRACTIVITY, REFLECTIVITY, SHINE_DAMPER);

		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffers.getReflectionTextureId());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffers.getRefractionTextureId());
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvMapId);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMapId);
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffers.getRefractionDepthTexture());

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void unbind(){
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	private RawModel loadQuad(Loader loader) {
		// Just x and z vertices positions here, y is always 0 in the shader
		return loader.loadToVAO(new float[] {
			-1, -1,  -1, 1,  1, -1,  1, -1,  -1, 1,  1, 1
		}, 2);
	}
}
