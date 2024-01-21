package guis.shaders;

import org.lwjgl.util.vector.Matrix4f;
import shared.shaders.ShaderProgram;
import shared.toolbox.Directories;

public class GuiShader extends ShaderProgram {

    private static final String BASE_PATH = Directories.fromPath("src", "guis", "shaders");
    private static final String VERTEX_FILE = BASE_PATH + "GuiVertexShader.glsl";
    private static final String FRAGMENT_FILE = BASE_PATH + "GuiFragmentShader.glsl";

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
