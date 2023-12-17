package renderEngine;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Terrain;
import models.TexturedModel;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import shaders.StaticShader;
import shaders.TerrainShader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterRenderer {

    private static final float FOV = 90;
    private static final float NEAR_PLANE = 0.01f;
    private static final float FAR_PLANE = 800;

    private static final Vector3f SKY_COLOUR = new Vector3f(192/255f, 240/255f, 240/255f);

    private Matrix4f projectionMatrix;

    private StaticShader entityShader;
    private EntityRenderer entityRenderer;
    private TerrainRenderer terrainRenderer;
    private TerrainShader terrainShader;

    private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
    private List<Terrain> terrains = new ArrayList<>();

    public MasterRenderer() {
        enableCulling();
        createProjectionMatrix();
        this.entityShader = new StaticShader();
        this.entityRenderer = new EntityRenderer(entityShader, projectionMatrix);
        this.terrainShader = new TerrainShader();
        this.terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
    }

    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }

    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public void render(Light sun, Camera camera) {
        prepare();

        entityShader.start();
        entityShader.loadLight(sun);
        entityShader.loadViewMatrix(camera);
        entityShader.loadSkyColour(SKY_COLOUR);
        entityRenderer.render(entities);
        entityShader.stop();

        terrainShader.start();
        terrainShader.loadLight(sun);
        terrainShader.loadViewMatrix(camera);
        terrainShader.loadSkyColour(SKY_COLOUR);
        terrainRenderer.render(terrains);
        terrainShader.stop();

        terrains.clear();
        entities.clear();
    }

    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    public void processEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);
        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }
    }

    public void prepare() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(SKY_COLOUR.x, SKY_COLOUR.y, SKY_COLOUR.z, 1);
    }

    public void cleanUp() {
        entityShader.cleanUp();
        terrainShader.cleanUp();
    }

    private void createProjectionMatrix() {
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float x_scale = 1f / (float) Math.tan(Math.toRadians(FOV / 2f));
        float y_scale = x_scale * aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
    }
}
