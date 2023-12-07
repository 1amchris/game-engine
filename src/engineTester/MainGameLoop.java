package engineTester;

import models.TexturedModel;
import org.lwjgl.opengl.Display;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import models.RawModel;
import renderEngine.Renderer;
import shaders.ShaderProgram;
import shaders.StaticShader;
import textures.ModelTexture;

import java.io.File;
import java.lang.reflect.Field;

public class MainGameLoop {

    public static void main(String[] main) {

        setupNatives();
        DisplayManager.createDisplay();

        Loader loader = new Loader();
        Renderer renderer = new Renderer();
        ShaderProgram shader = new StaticShader();

        float[] vertices = {
                -0.5f, 0.5f, 0,     // V0
                -0.5f, -0.5f, 0,    // V1
                0.5f, -0.5f, 0,     // V2
                0.5f, 0.5f, 0,      // V3
        };

        int[] indices = {
                1, 3, 0, // Top left triangle
                3, 1, 2  // Bottom right triangle
        };

        float[] textureCoords = {
                0, 0, // V0
                0, 1, // V1
                1, 1, // V2
                1, 0, // V3
        };

        RawModel model = loader.loadToVAO(vertices, textureCoords, indices);
        ModelTexture texture = new ModelTexture(loader.loadTexture("image"));
        TexturedModel texturedModel = new TexturedModel(model, texture);

        while (!Display.isCloseRequested()) {
            renderer.prepare();
            shader.start();
            renderer.render(texturedModel);
            shader.stop();
            DisplayManager.updateDisplay();
        }

        shader.cleanUp();
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
}
