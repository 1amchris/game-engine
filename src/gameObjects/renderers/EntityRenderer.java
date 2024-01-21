package gameObjects.renderers;

import gameObjects.entities.Camera;
import gameObjects.entities.Entity;
import gameObjects.entities.LightSource;
import gameObjects.shaders.EntityShader;
import shared.models.RawModel;
import shared.models.TexturedModel;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import shared.renderers.MasterRenderer;
import shared.textures.ModelTexture;
import shared.toolbox.Maths;

import java.util.List;
import java.util.Map;

public class EntityRenderer {

    private final EntityShader shader;

    public EntityRenderer(EntityShader shader) {
        this.shader = shader;
    }

    public void setupShader(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    public void start(List<LightSource> lightSources, Camera camera, Vector4f clipPlane, Vector3f skyColour) {
        shader.start();
        shader.loadClipPlane(clipPlane);
        shader.loadLights(lightSources);
        shader.loadViewMatrix(camera);
        shader.loadSkyColour(skyColour);
    }

    public void stop() {
        shader.stop();
    }

    public void render(Map<TexturedModel, List<Entity>> entities) {
        for (TexturedModel model: entities.keySet()) {
            prepareTexturedModel(model);
            List<Entity> batch = entities.get(model);
            for (Entity entity: batch) {
                prepareInstance(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(),
                        GL11.GL_UNSIGNED_INT, 0);
            }
            unbindTexturedModel();
        }
    }

    private void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        GL30.glBindVertexArray(rawModel.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);

        ModelTexture texture = model.getTexture();
        shader.loadAtlasGridSize(texture.getAtlasGridSize());
        shader.loadTexture(texture);

        if (texture.isTransparent()) {
            MasterRenderer.disableCulling();
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getID());
    }

    private void unbindTexturedModel() {
        MasterRenderer.enableCulling();
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    private void prepareInstance(Entity entity) {
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(
                entity.getPosition(), entity.getRotation(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix);
        shader.loadAtlasTextureOffset(entity.getModel().getTextureOffset());
    }
}
