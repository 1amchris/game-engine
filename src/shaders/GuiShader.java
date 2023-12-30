package shaders;

import org.lwjgl.util.vector.Matrix4f;

import java.io.File;

public class GuiShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src" + File.separator + "shaders" + File.separator + "guiVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src" + File.separator + "shaders" + File.separator + "guiFragmentShader.glsl";

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
