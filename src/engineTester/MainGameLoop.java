package engineTester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Terrain;
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
        Mouse.setGrabbed(true);

        Loader loader = new Loader();
        MasterRenderer renderer = new MasterRenderer();
        Light light = new Light(new Vector3f(1500, 2000, 1500), new Vector3f(1, 1, 1));
        Camera camera = new Camera();

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
            recenterMouse();

            renderer.processTerrain(terrain1);
            renderer.processTerrain(terrain2);
            renderer.processTerrain(terrain3);
            renderer.processTerrain(terrain4);

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

        addFern(loader, entities, 200);
        addGrass(loader, entities, 100);
        addFlowers(loader, entities, 100);
        addTrees(loader, entities, 100);
        addBunnies(loader, entities, 20);

        return entities;
    }

    private static void addFern(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel fernModel = OBJLoader.loadObjModel("fern", loader);
        ModelTexture fernTexture = new ModelTexture(loader.loadTexture("fern"));
        fernTexture.setTransparency(true);
        entities.put(fernModel, generateRandomEntities(fernModel, fernTexture, 1, count));
    }

    private static void addGrass(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel plantModel = OBJLoader.loadObjModel("grassModel", loader);
        ModelTexture grassTexture = new ModelTexture(loader.loadTexture("grassTexture"));
        grassTexture.setTransparency(true);
        grassTexture.setUseFakeLighting(true);
        entities.put(plantModel, generateRandomEntities(plantModel, grassTexture, 2, count));
    }

    private static void addFlowers(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel plantModel = OBJLoader.loadObjModel("grassModel", loader);
        ModelTexture flowerTexture = new ModelTexture(loader.loadTexture("flower"));
        flowerTexture.setTransparency(true);
        flowerTexture.setUseFakeLighting(true);
        entities.put(plantModel, generateRandomEntities(plantModel, flowerTexture, 3, count));
    }

    private static void addTrees(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel treeModel = OBJLoader.loadObjModel("lowPolyTree", loader);
        ModelTexture treeTexture = new ModelTexture(loader.loadTexture("lowPolyTree"));
        entities.put(treeModel, generateRandomEntities(treeModel, treeTexture, 1, count));
    }

    private static void addBunnies(Loader loader, Map<RawModel, List<Entity>> entities, int count) {
        RawModel bunnyModel = OBJLoader.loadObjModel("bunny", loader);
        ModelTexture bunnyTexture = new ModelTexture(loader.loadTexture("white"));
        entities.put(bunnyModel, generateRandomEntities(bunnyModel, bunnyTexture, 0.25f, count));
    }

    private static List<Entity> generateRandomEntities(RawModel model, ModelTexture texture, float scale, int count) {
        Random random = new Random();

        List<Entity> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Entity fern = new Entity(
                    new TexturedModel(model, texture),
                    new Vector3f(random.nextFloat() * 500 - 250, 0, random.nextFloat() * 500 - 250),
                    new Vector3f(0, random.nextFloat() * 360, 0), scale);
            result.add(fern);
        }

        return result;
    }
}
