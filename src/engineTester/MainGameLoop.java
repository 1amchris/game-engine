package engineTester;

import entities.*;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class MainGameLoop {

    private static final String OBJECT_DIR = "objects" + File.separator;
    private static final String TERRAIN_DIR = "terrain" + File.separator;

    private static final String TERRAIN_BLEND_MAP_FILENAME =  TERRAIN_DIR + "blendMap";
    private static final String TERRAIN_HEIGHT_MAP_FILENAME = TERRAIN_DIR + "heightMap";

    private static final Integer WORLD_SIZE = 4;

    public static void main(String[] main) {

        setupNatives();
        DisplayManager.createDisplay();

        Loader loader = new Loader();
        MasterRenderer renderer = new MasterRenderer();
        Light light = new Light(new Vector3f(500, 2000, 800), new Vector3f(1, 1, 1));
        Player player = createPlayer(loader);
        Camera camera = new Camera(player);

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

        Map<RawModel, List<Entity>> entities = generateStaticEntities(loader, world);

        while (!Display.isCloseRequested()) {
            camera.move();
            player.move(getTerrainPlayerIsStandingOn(player, world));
//            recenterMouse();

            for (Map<Integer, Terrain> terrains : world.values()) {
                for (Terrain terrain : terrains.values()) {
                    renderer.processTerrain(terrain);
                }
            }

            renderer.processEntity(player);
            for (List<Entity> values : entities.values()) {
                for (Entity entity : values) {
                    renderer.processEntity(entity);
                }
            }

            renderer.render(light, camera);
            DisplayManager.updateDisplay();
        }

        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
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

    private static Terrain getTerrainPlayerIsStandingOn(Player player, Map<Integer, Map<Integer, Terrain>> world) {
        Vector3f playerPosition = player.getPosition();
        int terrainX = (int) Math.floor(playerPosition.x / Terrain.SIZE);
        int terrainZ = (int) Math.floor(playerPosition.z / Terrain.SIZE);
        return world.get(terrainX).get(terrainZ);
    }

    private static Map<RawModel, List<Entity>> generateStaticEntities(Loader loader, Map<Integer, Map<Integer, Terrain>> world) {
        Map<RawModel, List<Entity>> entities = new Hashtable<>();

        createFern(loader, entities, world, 2000);
        createGreeneries(loader, entities, world, 4000);
        createTrees(loader, entities, world, 1000);
        createBunnies(loader, entities, world, 200);

        return entities;
    }

    private static List<Entity> createFern(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel fernModel = OBJLoader.loadObjModel(OBJECT_DIR + "fern", loader);
        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture(OBJECT_DIR + "fern"));
        fernTextureAtlas.setAtlasProperties(2, 4);
        fernTextureAtlas.setTransparency(true);
        List<Entity> ferns = generateRandomEntities(fernModel, fernTextureAtlas, 1, world, count);
        entities.put(fernModel, ferns);
        return ferns;
    }

    private static List<Entity> createGreeneries(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel model = OBJLoader.loadObjModel(OBJECT_DIR + "grassModel", loader);
        ModelTexture texture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "greeneries"));
        texture.setAtlasProperties(4, 9);
        texture.setTransparency(true);
        texture.setUseFakeLighting(true);
        List<Entity> grasses = generateRandomEntities(model, texture, 3, world, count);
        entities.put(model, grasses);
        return grasses;
    }

    private static List<Entity> createTrees(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel treeModel = OBJLoader.loadObjModel(OBJECT_DIR + "lowPolyTree", loader);
        ModelTexture treeTexture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "lowPolyTree"));
        List<Entity> trees = generateRandomEntities(treeModel, treeTexture, 1, world, count);
        entities.put(treeModel, trees);
        return trees;
    }

    private static List<Entity> createBunnies(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel bunnyModel = OBJLoader.loadObjModel(OBJECT_DIR + "bunny", loader);
        ModelTexture bunnyTexture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "white"));
        List<Entity> bunnies = generateRandomEntities(bunnyModel, bunnyTexture, 0.25f, world, count);
        entities.put(bunnyModel, bunnies);
        return bunnies;
    }

    private static Player createPlayer(Loader loader) {
        RawModel playerModel = OBJLoader.loadObjModel(OBJECT_DIR + "bunny", loader);
        ModelTexture playerTexture = new ModelTexture(loader.loadTexture(OBJECT_DIR + "lightBlue"));
        return new Player(
                new TexturedModel(playerModel, playerTexture),
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0), 0.25f);
    }

    private static List<Entity> generateRandomEntities(RawModel model, ModelTexture texture, float scale, Map<Integer, Map<Integer, Terrain>> world, int count) {
        Random random = new Random();

        List<Entity> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<Integer, Terrain> randomTerrainStrip = new ArrayList<>(world.values()).get(random.nextInt(world.size()));
            Terrain randomTerrain = new ArrayList<>(randomTerrainStrip.values()).get(random.nextInt(randomTerrainStrip.size()));

            float worldX = randomTerrain.getX() + random.nextFloat() * Terrain.SIZE;
            float worldZ = randomTerrain.getZ() + random.nextFloat() * Terrain.SIZE;

            Entity entity = new Entity(
                    new TexturedModel(model, texture, random.nextInt(texture.getTexturesCount())),
                    new Vector3f(worldX, randomTerrain.getHeightOfTerrain(worldX, worldZ), worldZ),
                    new Vector3f(0, random.nextFloat() * 360, 0), scale);
            result.add(entity);
        }

        return result;
    }
}
