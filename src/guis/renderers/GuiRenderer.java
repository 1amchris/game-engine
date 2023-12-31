package guis.renderers;

import guis.entities.GuiTexture;
import guis.shaders.GuiShader;
import shared.models.RawModel;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import shared.renderers.Loader;
import shared.toolbox.Maths;

import java.util.List;

public class GuiRenderer {

    private final RawModel quadModel;
    private final GuiShader shader;

    public GuiRenderer(GuiShader shader, Loader loader) {
        float[] positions = { -1, 1,   -1, -1,    1, 1,    1, -1 };
        this.quadModel = loader.loadToVAO(positions, 2);
        this.shader = shader;
    }

    public void render(List<GuiTexture> guis) {
        shader.start();
        GL30.glBindVertexArray(quadModel.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (GuiTexture gui: guis) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, gui.getTexture());
            Matrix4f transformationMatrix = Maths.createTransformationMatrix(gui.getPosition(), gui.getScale());
            shader.loadTransformation(transformationMatrix);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quadModel.getVertexCount());
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }
}
