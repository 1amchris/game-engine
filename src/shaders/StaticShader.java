package shaders;

import entities.Camera;
import org.lwjgl.util.vector.Matrix4f;
import toolbox.Maths;

import java.io.File;

public class StaticShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src" + File.separator + "shaders" + File.separator + "vertexShader.glsl";
    private static final String FRAGMENT_FILE = "src" + File.separator + "shaders" + File.separator + "fragmentShader.glsl";

    private int location_transformationMatrix;
    private int projection_transformationMatrix;
    private int view_transformationMatrix;

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        projection_transformationMatrix = super.getUniformLocation("projectionMatrix");
        view_transformationMatrix = super.getUniformLocation("viewMatrix");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
    }

    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(projection_transformationMatrix, matrix);
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        super.loadMatrix(view_transformationMatrix, matrix);
    }
}
