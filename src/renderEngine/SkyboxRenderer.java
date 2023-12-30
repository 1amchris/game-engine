package renderEngine;

import entities.Camera;
import models.RawModel;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import shaders.skyboxes.SkyboxShader;

import java.io.File;

public class SkyboxRenderer {

    private static final float SIZE = 500f;

    private static final float[] VERTICES = {
            -SIZE,  SIZE, -SIZE,
            -SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

            -SIZE,  SIZE, -SIZE,
            SIZE,  SIZE, -SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
            SIZE, -SIZE,  SIZE
    };

    private static final String SKYBOX_DIR = "skybox" + File.separator;
    private static final String SKYBOX_DAY_TEXTURES_DIR = SKYBOX_DIR + "beautifulSky" + File.separator;
    private static final String SKYBOX_NIGHT_TEXTURES_DIR = SKYBOX_DIR + "skyFullOfStars" + File.separator;

    private static final int DAYTIME_SPEED = 500;

    private static final String[] DAY_TEXTURE_FILES = {
        SKYBOX_DAY_TEXTURES_DIR + "right",
        SKYBOX_DAY_TEXTURES_DIR + "left",
        SKYBOX_DAY_TEXTURES_DIR + "top",
        SKYBOX_DAY_TEXTURES_DIR + "bottom",
        SKYBOX_DAY_TEXTURES_DIR + "back",
        SKYBOX_DAY_TEXTURES_DIR + "front"
    };
    private static final String[] NIGHT_TEXTURE_FILES = {
        SKYBOX_NIGHT_TEXTURES_DIR + "right",
        SKYBOX_NIGHT_TEXTURES_DIR + "left",
        SKYBOX_NIGHT_TEXTURES_DIR + "top",
        SKYBOX_NIGHT_TEXTURES_DIR + "bottom",
        SKYBOX_NIGHT_TEXTURES_DIR + "back",
        SKYBOX_NIGHT_TEXTURES_DIR + "front"
    };

    private final RawModel cube;
    private final int dayTextureID;
    private final int nightTextureID;
    private final SkyboxShader shader;
    private float time = 0;

    public SkyboxRenderer(SkyboxShader shader, Loader loader) {
        this.cube = loader.loadToVAO(VERTICES, 3);
        this.dayTextureID = loader.loadCubeMap(DAY_TEXTURE_FILES);
        this.nightTextureID = loader.loadCubeMap(NIGHT_TEXTURE_FILES);
        this.shader = shader;

        shader.start();
        shader.connectTextureUnits();
        shader.stop();
    }

    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    public void render(Camera camera, Vector3f fogColour) {
        shader.start();
        shader.loadViewMatrix(camera);
        shader.loadFogColour(fogColour);
        GL30.glBindVertexArray(cube.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        bindTextures();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    private void bindTextures() {
        time += DisplayManager.getDeltaTime() * DAYTIME_SPEED;
        time %= 24_000;

        int texture0;
        int texture1;
        float blendFactor;

        if (time >= 0 && time < 5_000) {
            texture0 = nightTextureID;
            texture1 = nightTextureID;
            blendFactor = time / 5_000;
        } else if (time >= 5_000 && time < 8_000) {
            texture0 = nightTextureID;
            texture1 = dayTextureID;
            blendFactor = (time - 5_000) / 3_000; // 3_000 = 8_000 - 5_000
        } else if (time >= 8_000 && time < 21_000) {
            texture0 = dayTextureID;
            texture1 = dayTextureID;
            blendFactor = (time - 8_000) / 13_000; // 13_000 = 21_000 - 8_000
        } else {
            texture0 = dayTextureID;
            texture1 = nightTextureID;
            blendFactor = (time - 21_000) / 3_000; // 3_000 = 24_000 - 21_000
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture0);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture1);
        shader.loadBlendFactor(blendFactor);
    }
}
