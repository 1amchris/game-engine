package engineTester;

import gameObjects.entities.*;
import gameObjects.renderers.EntityRenderer;
import gameObjects.renderers.NMEntityRenderer;
import gameObjects.shaders.EntityShader;
import gameObjects.shaders.NMEntityShader;
import guis.entities.*;
import guis.renderers.GuiRenderer;
import guis.shaders.GuiShader;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import shared.disposers.Disposer;
import shared.models.*;
import shared.renderers.*;
import shared.textures.ModelTexture;
import shared.toolbox.Directories;
import skyboxes.renderers.SkyboxRenderer;
import skyboxes.shaders.SkyboxShader;
import terrains.entities.*;
import terrains.renderers.TerrainRenderer;
import terrains.shaders.TerrainShader;
import water.entities.WaterTile;
import water.renderers.WaterFrameBuffers;
import water.renderers.WaterRenderer;
import water.shaders.WaterShader;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainGameLoop {

    private static final String GUI_DIR = Directories.fromPath("guis");
    private static final String OBJECT_DIR = Directories.fromPath("objects");
    private static final String NM_OBJECT_DIR = Directories.fromPath("objects", "normaled");
    private static final String TERRAIN_DIR = Directories.fromPath("terrain");

    private static final String TERRAIN_BLEND_MAP_FILENAME = TERRAIN_DIR + "blendMap";
    private static final String TERRAIN_HEIGHT_MAP_FILENAME = TERRAIN_DIR + "heightMap";

    private static final Integer WORLD_SIZE = 4;

    public static void main(String[] main) {

        /* Setting up */
        setupNatives();
        DisplayManager.createDisplay();

        Disposer disposer = new Disposer();
        Loader loader = disposer.create(new Loader());

        List<LightSource> lightSources = new ArrayList<>();
        LightSource sun = new LightSource(new Vector3f(100, 2000, -700), new Vector3f(1, 1, 1));
        lightSources.add(sun);
        MasterRenderer renderer = new MasterRenderer();

        /* World elements */
        SkyboxShader skyboxShader = disposer.create(new SkyboxShader());
        renderer.setupRenderer(new SkyboxRenderer(skyboxShader, loader));

        TerrainShader terrainShader = disposer.create(new TerrainShader());
        renderer.setupRenderer(new TerrainRenderer(terrainShader));

//        WaterShader waterShader = disposer.create(new WaterShader());
//        WaterFrameBuffers waterFbos = disposer.create(new WaterFrameBuffers());
//        renderer.setupRenderer(new WaterRenderer(waterShader, waterFbos, loader));

        Map<Integer, Map<Integer, Terrain>> world = createWorld(loader);
//        List<WaterTile> waterBodies = createWater();

        /*  Game elements */
        EntityShader entityShader = disposer.create(new EntityShader());
        renderer.setupRenderer(new EntityRenderer(entityShader));
        NMEntityShader nmEntityShader = disposer.create(new NMEntityShader());
        renderer.setupRenderer(new NMEntityRenderer(nmEntityShader));

        Map<RawModel, List<Entity>> entities = createStaticEntities(loader, world);
        Map<RawModel, List<Entity>> nmEntities = createStaticNMEntities(loader, world);
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

        /* Gui elements */
        GuiShader guiShader = disposer.create(new GuiShader());
        renderer.setupRenderer(new GuiRenderer(guiShader, loader));

        List<GuiTexture> guis = new ArrayList<>();
        GuiTexture healthBar = new GuiTexture(loader.loadTexture(GUI_DIR + "health"),
                new Vector2f(-.8f, .95f), new Vector2f(0.2f, 0.2f));
        guis.add(healthBar);

        /*  Game loop */
        while (!Display.isCloseRequested()) {
            camera.move();
            player.move(getTerrainAtPosition(player.getPosition(), world));

            GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

            List<Entity> flattenedEntities = flatten(player, entities);
            List<Entity> flattenedNMEntities = flatten(null, nmEntities);
            List<Terrain> flattenedWorld = flatten(world);

//            float waterHeight = waterBodies.stream()
//                    .map(WaterTile::getPosition)
//                    .min(Comparator.comparingDouble(Vector3f::getY))
//                    .get().y;
//
//            waterFbos.bindReflectionFrameBuffer();
//            float cameraHeightFromWater = 2 * (camera.getPosition().y - waterHeight);
//            camera.getPosition().y -= cameraHeightFromWater;
//            camera.invertPitch();
//            renderer.renderScene(flattenedEntities, flattenedNMEntities, flattenedWorld, lightSources, camera, new Vector4f(0, 1, 0, -waterHeight));
//            camera.getPosition().y += cameraHeightFromWater;
//            camera.invertPitch();
//
//            waterFbos.bindRefractionFrameBuffer();
//            renderer.renderScene(flattenedEntities, flattenedNMEntities, flattenedWorld, lightSources, camera, new Vector4f(0, -1, 0, waterHeight + 0.1f));
//            waterFbos.unbindCurrentFrameBuffer();

            GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
            renderer.renderScene(flattenedEntities, flattenedNMEntities, flattenedWorld, lightSources, camera, new Vector4f(0, 0, 0, 0));
//            renderer.renderWater(waterBodies, sun, camera);
            renderer.renderGui(guis);

            DisplayManager.updateDisplay();
        }

        /*  Clean up */
        disposer.dispose();
        DisplayManager.closeDisplay();
    }

    private static List<Terrain> flatten(Map<Integer, Map<Integer, Terrain>> world) {
        return world.values().stream().flatMap(terrains -> terrains.values().stream()).collect(Collectors.toList());
    }

    private static List<Entity> flatten(Player player, Map<RawModel, List<Entity>> entities) {
        if (player != null) {
            return Stream.concat(Stream.of(player), entities.values().stream().flatMap(List::stream)).collect(Collectors.toList());
        } else {
            return entities.values().stream().flatMap(List::stream).collect(Collectors.toList());
        }
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

    private static List<WaterTile> createWater() {
        List<WaterTile> waterBodies = new ArrayList<>();
        waterBodies.add(new WaterTile(new Vector3f(-105, -0.5f, 55)));
        return waterBodies;
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
            String nativesPath = Directories.fromPath(workingDir, "lib", "natives");
            System.setProperty("java.library.path", nativesPath);

            // Need to reinitialize the native library path (hack)
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);

        } catch (Exception e) {
            e.printStackTrace();
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

    private static Map<RawModel, List<Entity>> createStaticNMEntities(Loader loader, Map<Integer, Map<Integer, Terrain>> world) {
        Map<RawModel, List<Entity>> entities = new Hashtable<>();

        createBarrels(loader, entities, world, 1);

        return entities;
    }

    private static List<Entity> createBarrels(Loader loader, Map<RawModel, List<Entity>> entities, Map<Integer, Map<Integer, Terrain>> world, int count) {
        RawModel barrelModel = NMObjLoader.loadObjModel(NM_OBJECT_DIR + "barrel", loader);
        ModelTexture barrelTexture = new ModelTexture(loader.loadTexture(NM_OBJECT_DIR + "barrel"));
        barrelTexture.setNormalMap(loader.loadTexture(NM_OBJECT_DIR + "barrelNormal"));
        barrelTexture.setShineDamper(10);
        barrelTexture.setReflectivity(0.5f);
//        List<Entity> barrels = generateRandomEntities(barrelModel, barrelTexture, 1f, world, count);
        List<Entity> barrels = Arrays.stream(new Entity[]{
            new Entity(new TexturedModel(barrelModel, barrelTexture), new Vector3f(0, 4, 0), new Vector3f(0, 0, 0), 0.5f),
        }).collect(Collectors.toList());
        entities.put(barrelModel, barrels);
        return barrels;
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
