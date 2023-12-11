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

import java.io.File;
import java.lang.reflect.Field;

public class MainGameLoop {

    public static void main(String[] main) {

        setupNatives();
        DisplayManager.createDisplay();
        Mouse.setGrabbed(true);

        Loader loader = new Loader();
        MasterRenderer renderer = new MasterRenderer();

        RawModel model = OBJLoader.loadObjModel("TheStanfordDragon", loader);
        ModelTexture grass = new ModelTexture(loader.loadTexture("grass"));
        ModelTexture purple = new ModelTexture(loader.loadTexture("purple"));
        purple.setReflectivity(2);
        purple.setShineDamper(15);

        Light light = new Light(new Vector3f(3000, 2000, 2000), new Vector3f(1, 1, 1));
        Camera camera = new Camera();

        Terrain terrain1 = new Terrain(0, 0, loader, grass);
        Terrain terrain2 = new Terrain(1, 0, loader, grass);
        Entity entity = new Entity(new TexturedModel(model, purple), new Vector3f(0, 0, -25),
                new Vector3f(0, 0, 0), 25f);

        while (!Display.isCloseRequested()) {
            entity.increaseRotation(new Vector3f(0, 0.5f, 0));
            camera.move();
            recenterMouse();

            renderer.processTerrain(terrain1);
            renderer.processTerrain(terrain2);
            renderer.processEntity(entity);

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
}
