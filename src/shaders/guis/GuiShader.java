package shaders.guis;

import org.lwjgl.util.vector.Matrix4f;
import shaders.ShaderProgram;

import java.io.File;

public class GuiShader extends ShaderProgram {

    private static final String BASE_PATH = "src" + File.separator + "shaders" + File.separator + "guis" + File.separator;
    private static final String VERTEX_FILE = BASE_PATH + "guiVertexShader.glsl";
    private static final String FRAGMENT_FILE = BASE_PATH + "guiFragmentShader.glsl";

    private int location_transformationMatrix;

    public GuiShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    public void loadTransformation(Matrix4f matrix) {
        loadMatrix(location_transformationMatrix, matrix);
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = getUniformLocation("transformationMatrix");
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
    }
}
