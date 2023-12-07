package shaders;

import java.io.File;

public class StaticShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src" + File.separator + "shaders" + File.separator + "vertexShader.glsl";
    private static final String FRAGMENT_FILE = "src" + File.separator + "shaders" + File.separator + "fragmentShader.glsl";

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
    }
}
