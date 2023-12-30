package shared.renderers;

import gameObjects.entities.Camera;
import gameObjects.entities.Entity;
import gameObjects.entities.LightSource;
import terrains.entities.Terrain;
import guis.renderers.GuiRenderer;
import shared.models.TexturedModel;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import guis.entities.GuiTexture;
import gameObjects.renderers.EntityRenderer;
import skyboxes.renderers.SkyboxRenderer;
import terrains.renderers.TerrainRenderer;
import water.entities.WaterTile;
import water.renderers.WaterRenderer;

import java.util.*;

public class MasterRenderer {

    private static final float FOV = 90;
    private static final float NEAR_PLANE = 0.01f;
    private static final float FAR_PLANE = 800;

    private static final Vector3f SKY_COLOUR = new Vector3f(145/255f, 170/255f, 190/255f);

    private Matrix4f projectionMatrix;

    private EntityRenderer entityRenderer;
    private GuiRenderer guiRenderer;
    private SkyboxRenderer skyboxRenderer;
    private TerrainRenderer terrainRenderer;
    private WaterRenderer waterRenderer;

    private final Map<TexturedModel, List<Entity>> entities = new HashMap<>();
    private final List<Terrain> terrains = new ArrayList<>();
    private final List<WaterTile> water = new ArrayList<>();

    public MasterRenderer() {
        enableCulling();
        createProjectionMatrix();
    }

    public void setupRenderer(EntityRenderer renderer) {
        renderer.setProjectionMatrix(projectionMatrix);
        this.entityRenderer = renderer;
    }

    public void setupRenderer(WaterRenderer renderer) {
        renderer.setProjectionMatrix(projectionMatrix);
        this.waterRenderer = renderer;
    }

    public void setupRenderer(GuiRenderer renderer) {
        this.guiRenderer = renderer;
    }

    public void setupRenderer(SkyboxRenderer renderer) {
        renderer.setProjectionMatrix(projectionMatrix);
        this.skyboxRenderer = renderer;
    }

    public void setupRenderer(TerrainRenderer renderer) {
        renderer.setProjectionMatrix(projectionMatrix);
        this.terrainRenderer = renderer;
    }

    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }

    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public void renderScene(List<Entity> entities, List<Terrain> terrains, List<WaterTile> waterTiles,
                            List<GuiTexture> guis, List<LightSource> lightSources, Camera camera) {
        processEntities(entities);
        processTerrains(terrains);
        processWater(waterTiles);
        render(lightSources, camera);
        render(guis);
    }
    private void render(List<GuiTexture> guis) {
        if (guiRenderer != null) {
            guiRenderer.render(guis);
        }
    }

    private void render(List<LightSource> lightSources, Camera camera) {
        beforeRender();

        if (entityRenderer != null) {
            entityRenderer.start(lightSources, camera, SKY_COLOUR);
            entityRenderer.render(entities);
            entityRenderer.stop();
        }

        if (terrainRenderer != null) {
            terrainRenderer.start(lightSources, camera, SKY_COLOUR);
            terrainRenderer.render(terrains);
            terrainRenderer.stop();
        }

        if (skyboxRenderer != null) {
            skyboxRenderer.render(camera, SKY_COLOUR);
        }

        if (waterRenderer != null) {
            waterRenderer.render(water, camera);
        }

        afterRender();
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

    public void processWater(Collection<WaterTile> waterTiles) {
        for (WaterTile tile: waterTiles) {
            processWater(tile);
        }
    }

    public void processWater(WaterTile tile) {
        water.add(tile);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
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

    private void beforeRender() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(SKY_COLOUR.x, SKY_COLOUR.y, SKY_COLOUR.z, 1);
    }

    private void afterRender() {
        entities.clear();
        terrains.clear();
    }
}
