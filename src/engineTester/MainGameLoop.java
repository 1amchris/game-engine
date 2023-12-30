package engineTester;

import entities.*;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.*;
import shaders.GuiShader;
import shaders.SkyboxShader;
import shaders.StaticShader;
import shaders.TerrainShader;
import textures.GuiTexture;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class MainGameLoop {

    private static final String GUI_DIR = "guis" + File.separator;
    private static final String OBJECT_DIR = "objects" + File.separator;
    private static final String TERRAIN_DIR = "terrain" + File.separator;

    private static final String TERRAIN_BLEND_MAP_FILENAME = TERRAIN_DIR + "blendMap";
    private static final String TERRAIN_HEIGHT_MAP_FILENAME = TERRAIN_DIR + "heightMap";

    private static final Integer WORLD_SIZE = 4;

    public static void main(String[] main) {

        setupNatives();
        DisplayManager.createDisplay();

        Loader loader = new Loader();
        List<LightSource> lightSources = new ArrayList<>();
//        lightSources.add(new LightSource(new Vector3f(0, 1000, -7000), new Vector3f(0.4f, 0.4f, 0.4f)));
        lightSources.add(new LightSource(new Vector3f(100, 2000, -700), new Vector3f(1, 1, 1)));

        StaticShader entityShader = new StaticShader();
        EntityRenderer entityRenderer = new EntityRenderer(entityShader);
        GuiShader guiShader = new GuiShader();
        GuiRenderer guiRenderer = new GuiRenderer(guiShader, loader);
        SkyboxShader skyboxShader = new SkyboxShader();
        SkyboxRenderer skyboxRenderer = new SkyboxRenderer(skyboxShader, loader);
        TerrainShader terrainShader = new TerrainShader();
        TerrainRenderer terrainRenderer = new TerrainRenderer(terrainShader);
        MasterRenderer renderer = new MasterRenderer(entityRenderer, guiRenderer, skyboxRenderer, terrainRenderer);

        Map<Integer, Map<Integer, Terrain>> world = createWorld(loader);
        Map<RawModel, List<Entity>> entities = createStaticEntities(loader, world);
        Player player = createPlayer(loader);
        Camera camera = new Camera(player);

        List<Entity> lamps = new ArrayList<>();
        TexturedModel lampModel = new TexturedModel(
            ObjLoader.loadObjModel(OBJECT_DIR + "lamp", loader),
            new ModelTexture(loader.loadTexture(OBJECT_DIR + "lamp"))
        );
        createLamp(85, -53, lampModel, new Vector3f(2, 0, 2), lamps, lightSources, world);
        createLamp(70, -150, lampModel, new Vector3f(0, 2, 2), lamps, lightSources, world);
        createLamp(93, -115, lampModel, new Vector3f(2, 2, 0), lamps, lightSources, world);
        entities.put(lampModel.getRawModel(), lamps);

        List<GuiTexture> guis = new ArrayList<>();
        GuiTexture healthBar = new GuiTexture(loader.loadTexture(GUI_DIR + "health"),
                new Vector2f(-.8f, .95f), new Vector2f(0.2f, 0.2f));
        guis.add(healthBar);

        MousePicker mousePicker = new MousePicker(camera, renderer.getProjectionMatrix());

        while (!Display.isCloseRequested()) {
            camera.move();
            player.move(getTerrainAtPosition(player.getPosition(), world));
//            recenterMouse();

            mousePicker.update();
            System.out.println(mousePicker.getCurrentRay());

            for (Map<Integer, Terrain> terrains : world.values()) {
                renderer.processTerrains(terrains.values());
            }

            renderer.processEntity(player);
            for (List<Entity> values : entities.values()) {
                renderer.processEntities(values);
            }

            renderer.processGuis(guis);

            renderer.render(lightSources, camera);
            DisplayManager.updateDisplay();
        }

        entityShader.cleanUp();
        guiShader.cleanUp();
        terrainShader.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

    private static Map<Integer, Map<Integer, Terrain>> createWorld(Loader loader) {
        TerrainTexturePack terrainTexturePack = new TerrainTexturePack(
                new TerrainTexture(loader.loadTexture(TERRAIN_DIR + "grassy")),
                new TerrainTexture(loader.loadTexture(TERRAIN_DIR + "mud")),
                new TerrainTexture(loader.loadTexture(TERRAIN_DIR + "flowers")),
                new TerrainTexture(loader.loadTexture(TERRAIN_DIR + "path"))
        );
        TerrainTexture terrainBlendMap = new TerrainTexture(loader.loadTexture(TERRAIN_BLEND_MAP_FILENAME));

        Map<Integer, Map<Integer, Terrain>> world = new HashMap<>();
        final int HALF_WORLD_SIZE = WORLD_SIZE / 2;
        for (int i = 0; i < WORLD_SIZE; i++) {
            int gridX = i - HALF_WORLD_SIZE;
            world.put(gridX, new HashMap<>());
            for (int j = 0; j < WORLD_SIZE; j++) {
                int gridZ = j - HALF_WORLD_SIZE;
                world.get(gridX).put(gridZ, new Terrain(gridX, gridZ, loader, terrainTexturePack, terrainBlendMap, TERRAIN_HEIGHT_MAP_FILENAME));
            }
        }

        return world;
    }

    private static Entity createLamp(float x, float z, TexturedModel model, Vector3f lightColour,
                                   List<Entity> lamps, List<LightSource> lightSources,
                                   Map<Integer, Map<Integer, Terrain>> world) {
        Terrain terrainAtPosition = getTerrainAtPosition(new Vector3f(x, 0, z), world);
        float terrainHeightAtPosition = terrainAtPosition.getHeightOfTerrain(x, z);
        LightSource lightSource = new LightSource(
                new Vector3f(x, terrainHeightAtPosition + 10, z),
                lightColour, new Vector3f(1, 0.01f, 0.002f));
        lightSources.add(lightSource);

        Entity lamp = new Entity(
                model,
                new Vector3f(x, terrainHeightAtPosition, z),
                new Vector3f(0, new Random().nextFloat() * 360, 0), 1);
        lamps.add(lamp);

        return lamp;
    }

    private static void setupNatives() {
        try {
            String workingDir = System.getProperty("user.dir");
            String nativesPath = workingDir + File.separator + "lib" + File.separator + "natives";
            System.setProperty("java.library.path", nativesPath);

            // Need to reinitialize the native library path (hack)
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void recenterMouse() {
        if (Display.isActive()) {
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
        }
    }

    private static Terrain getTerrainAtPosition(Vector3f position, Map<Integer, Map<Integer, Terrain>> world) {
        int terrainX = (int) Math.floor(position.x / Terrain.SIZE);
        int terrainZ = (int) Math.floor(position.z / Terrain.SIZE);
        return world.get(terrainX).get(terrainZ);
    }

    private static Map<RawModel, List<Entity>> createStaticEntities(Loader loader, Map<Integer, Map<Integer, Terrain>> world) {
        Map<RawModel, List<Entity>> entities = new Hashtable<>();

        createFern(loader, entities, world, 2000);
        createGreeneries(loader, entities, world, 4000);
        createTrees(loader, entities, world, 1000);
        createBunnies(loader, entities, world, 200);

        return entities;
    }

    private static List<Entity> createFern(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel fernModel = ObjLoader.loadObjModel(OBJECT_DIR + "fern", loader);
        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture(OBJECT_DIR + "fern"));
        fernTextureAtlas.setAtlasProperties(2, 4);
        fernTextureAtlas.setTransparency(true);
        List<Entity> ferns = generateRandomEntities(fernModel, fernTextureAtlas, 1, world, count);
        entities.put(fernModel, ferns);
        return ferns;
    }

    private static List<Entity> createGreeneries(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel model = ObjLoader.loadObjModel(OBJECT_DIR + "grassModel", loader);
        ModelTexture texture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "greeneries"));
        texture.setAtlasProperties(4, 9);
        texture.setTransparency(true);
        texture.setUseFakeLighting(true);
        List<Entity> grasses = generateRandomEntities(model, texture, 3, world, count);
        entities.put(model, grasses);
        return grasses;
    }

    private static List<Entity> createTrees(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel treeModel = ObjLoader.loadObjModel(OBJECT_DIR + "lowPolyTree", loader);
        ModelTexture treeTexture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "lowPolyTree"));
        List<Entity> trees = generateRandomEntities(treeModel, treeTexture, 1, world, count);
        entities.put(treeModel, trees);
        return trees;
    }

    private static List<Entity> createBunnies(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel bunnyModel = ObjLoader.loadObjModel(OBJECT_DIR + "bunny", loader);
        ModelTexture bunnyTexture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "white"));
        List<Entity> bunnies = generateRandomEntities(bunnyModel, bunnyTexture, 0.25f, world, count);
        entities.put(bunnyModel, bunnies);
        return bunnies;
    }

    private static Player createPlayer(Loader loader) {
        RawModel playerModel = ObjLoader.loadObjModel(OBJECT_DIR + "bunny", loader);
        ModelTexture playerTexture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "lightBlue"));
        return new Player(
                new TexturedModel(playerModel, playerTexture),
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0), 0.25f);
    }

    private static List<Entity> generateRandomEntities(RawModel model, ModelTexture texture, float scale,
                                                       Map<Integer, Map<Integer, Terrain>> world, int count) {
        Random random = new Random();

        List<Entity> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<Integer, Terrain> randomTerrainStrip = new ArrayList<>(world.values()).get(random.nextInt(world.size()));
            Terrain randomTerrain = new ArrayList<>(randomTerrainStrip.values()).get(random.nextInt(randomTerrainStrip.size()));

            float worldX = randomTerrain.getX() + random.nextFloat() * Terrain.SIZE;
            float worldZ = randomTerrain.getZ() + random.nextFloat() * Terrain.SIZE;
            float worldY = randomTerrain.getHeightOfTerrain(worldX, worldZ);

            Entity entity = new Entity(
                    new TexturedModel(model, texture, random.nextInt(texture.getTexturesCount())),
                    new Vector3f(worldX, worldY, worldZ),
                    new Vector3f(0, random.nextFloat() * 360, 0), scale);
            result.add(entity);
        }

        return result;
    }
}
