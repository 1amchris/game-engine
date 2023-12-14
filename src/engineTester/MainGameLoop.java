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

    public static void main(String[] main) {

        setupNatives();
        DisplayManager.createDisplay();

        Loader loader = new Loader();
        MasterRenderer renderer = new MasterRenderer();
        Light light = new Light(new Vector3f(1500, 2000, 1500), new Vector3f(1, 1, 1));
        Player player = createPlayer(loader);
        Camera camera = new Camera(player);

        TerrainTexturePack terrainTexturePack = new TerrainTexturePack(
            new TerrainTexture(loader.loadTexture("terrain" + File.separator + "grassy")),
            new TerrainTexture(loader.loadTexture("terrain" + File.separator + "mud")),
            new TerrainTexture(loader.loadTexture("terrain" + File.separator + "flowers")),
            new TerrainTexture(loader.loadTexture("terrain" + File.separator + "path"))
        );
        TerrainTexture terrainBlendMap = new TerrainTexture(loader.loadTexture("terrain" + File.separator + "blendMap"));

        Terrain terrain1 = new Terrain(0, 0, loader, terrainTexturePack, terrainBlendMap);
        Terrain terrain2 = new Terrain(1, 0, loader, terrainTexturePack, terrainBlendMap);
        Terrain terrain3 = new Terrain(0, 1, loader, terrainTexturePack, terrainBlendMap);
        Terrain terrain4 = new Terrain(1, 1, loader, terrainTexturePack, terrainBlendMap);

        Map<RawModel, List<Entity>> entities = generateStaticEntities(loader);

        while (!Display.isCloseRequested()) {
            camera.move();
            player.move();
//            recenterMouse();

            renderer.processTerrain(terrain1);
            renderer.processTerrain(terrain2);
            renderer.processTerrain(terrain3);
            renderer.processTerrain(terrain4);

            renderer.processEntity(player);
            for (List<Entity> values: entities.values()) {
                for (Entity entity: values) {
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

    private static Map<RawModel, List<Entity>> generateStaticEntities(Loader loader) {
        Map<RawModel, List<Entity>> entities = new Hashtable<>();

        createFern(loader, entities, 200);
        createGrass(loader, entities, 100);
        createFlowers(loader, entities, 100);
        createTrees(loader, entities, 100);
        createBunnies(loader, entities, 20);

        return entities;
    }

    private static List<Entity> createFern(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel fernModel = OBJLoader.loadObjModel("fern", loader);
        ModelTexture fernTexture = new ModelTexture(loader.loadTexture("fern"));
        fernTexture.setTransparency(true);
        List<Entity> ferns = generateRandomEntities(fernModel, fernTexture, 1, count);
        entities.put(fernModel, ferns);
        return ferns;
    }

    private static List<Entity> createGrass(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel grassModel = OBJLoader.loadObjModel("grassModel", loader);
        ModelTexture grassTexture = new ModelTexture(loader.loadTexture("grassTexture"));
        grassTexture.setTransparency(true);
        grassTexture.setUseFakeLighting(true);
        List<Entity> grasses = generateRandomEntities(grassModel, grassTexture, 2, count);
        entities.put(grassModel, grasses);
        return grasses;
    }

    private static List<Entity> createFlowers(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel plantModel = OBJLoader.loadObjModel("grassModel", loader);
        ModelTexture flowerTexture = new ModelTexture(loader.loadTexture("flower"));
        flowerTexture.setTransparency(true);
        flowerTexture.setUseFakeLighting(true);
        List<Entity> flowers = generateRandomEntities(plantModel, flowerTexture, 3, count);
        entities.put(plantModel, flowers);
        return flowers;
    }

    private static List<Entity> createTrees(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel treeModel = OBJLoader.loadObjModel("lowPolyTree", loader);
        ModelTexture treeTexture = new ModelTexture(loader.loadTexture("lowPolyTree"));
        List<Entity> trees = generateRandomEntities(treeModel, treeTexture, 1, count);
        entities.put(treeModel, trees);
        return trees;
    }

    private static List<Entity> createBunnies(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel bunnyModel = OBJLoader.loadObjModel("bunny", loader);
        ModelTexture bunnyTexture = new ModelTexture(loader.loadTexture("white"));
        List<Entity> bunnies = generateRandomEntities(bunnyModel, bunnyTexture, 0.25f, count);
        entities.put(bunnyModel, bunnies);
        return bunnies;
    }

    private static Player createPlayer(Loader loader) {
        RawModel playerModel = OBJLoader.loadObjModel("bunny", loader);
        ModelTexture playerTexture = new ModelTexture(loader.loadTexture("lightBlue"));
        Player player = new Player(
                new TexturedModel(playerModel, playerTexture),
                new Vector3f(0, 0, -25),
                new Vector3f(0, 0, 0), 0.25f);
        return player;
    }

    private static List<Entity> generateRandomEntities(RawModel model, ModelTexture texture, float scale, int count) {
        Random random = new Random();

        List<Entity> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Entity entity = new Entity(
                    new TexturedModel(model, texture),
                    new Vector3f(random.nextFloat() * 500 - 250, 0, random.nextFloat() * 500 - 250),
                    new Vector3f(0, random.nextFloat() * 360, 0), scale);
            result.add(entity);
        }

        return result;
    }
}
