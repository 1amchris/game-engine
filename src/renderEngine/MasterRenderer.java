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
import textures.GuiTexture;

import java.util.*;

public class MasterRenderer {

    private static final float FOV = 90;
    private static final float NEAR_PLANE = 0.01f;
    private static final float FAR_PLANE = 800;

    private static final Vector3f SKY_COLOUR = new Vector3f(192/255f, 240/255f, 240/255f);

    private Matrix4f projectionMatrix;

    private EntityRenderer entityRenderer;
    private TerrainRenderer terrainRenderer;
    private GuiRenderer guiRenderer;

    private final Map<TexturedModel, List<Entity>> entities = new HashMap<>();
    private final List<Terrain> terrains = new ArrayList<>();
    private final List<GuiTexture> guis = new ArrayList<>();

    public MasterRenderer(EntityRenderer entityRenderer, GuiRenderer guiRenderer, TerrainRenderer terrainRenderer) {
        enableCulling();
        createProjectionMatrix();
        setupRenderer(entityRenderer);
        setupRenderer(guiRenderer);
        setupRenderer(terrainRenderer);
    }

    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }

    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public void render(Light sun, Camera camera) {
        beforeRender();

        entityRenderer.start(sun, camera, SKY_COLOUR);
        entityRenderer.render(entities);
        entityRenderer.stop();

        terrainRenderer.start(sun, camera, SKY_COLOUR);
        terrainRenderer.render(terrains);
        terrainRenderer.stop();

        guiRenderer.render(guis);

        afterRender();
    }

    public void processGui(GuiTexture gui) {
        guis.add(gui);
    }

    public void processGuis(Collection<GuiTexture> guis) {
        for (GuiTexture gui: guis) {
            processGui(gui);
        }
    }

    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    public void processTerrains(Collection<Terrain> terrains) {
        for (Terrain terrain: terrains) {
            processTerrain(terrain);
        }
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

    public void processEntities(Collection<Entity> entities) {
        for (Entity entity: entities) {
            processEntity(entity);
        }
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

    private void setupRenderer(EntityRenderer renderer) {
        renderer.setProjectionMatrix(projectionMatrix);
        this.entityRenderer = renderer;
    }

    private void setupRenderer(GuiRenderer renderer) {
        this.guiRenderer = renderer;
    }

    private void setupRenderer(TerrainRenderer renderer) {
        renderer.setProjectionMatrix(projectionMatrix);
        this.terrainRenderer = renderer;
    }

    private void beforeRender() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(SKY_COLOUR.x, SKY_COLOUR.y, SKY_COLOUR.z, 1);
    }

    private void afterRender() {
        entities.clear();
        guis.clear();
        terrains.clear();
    }
}
