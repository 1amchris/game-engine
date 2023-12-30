package water.renderers;

import java.util.List;

import shared.models.RawModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import shared.renderers.Loader;
import shared.toolbox.Maths;
import gameObjects.entities.Camera;
import water.entities.WaterTile;
import water.shaders.WaterShader;

public class WaterRenderer {

	private final RawModel quad;
	private final WaterShader shader;

	public WaterRenderer(WaterShader shader, Loader loader) {
		this.shader = shader;
		this.quad = loadQuad(loader);
	}

	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void render(List<WaterTile> water, Camera camera) {
		prepareRender(camera);	
		for (WaterTile tile : water) {
			Matrix4f modelMatrix = Maths.createTransformationMatrix(
					tile.getPosition(), new Vector3f(0, 0, 0), WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
		}
		unbind();
	}
	
	private void prepareRender(Camera camera){
		shader.start();
		shader.loadViewMatrix(camera);
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
	}
	
	private void unbind(){
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
