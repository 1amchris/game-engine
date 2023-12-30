package shared.models;

import org.lwjgl.util.vector.Vector2f;
import shared.textures.ModelTexture;

public class TexturedModel {

    private final RawModel rawModel;
    private final ModelTexture texture;

    private Integer textureIndex = 0;

    public TexturedModel(RawModel model, ModelTexture texture) {
        this.rawModel = model;
        this.texture = texture;
    }

    public TexturedModel(RawModel model, ModelTexture texture, Integer textureIndex) {
        this.rawModel = model;
        this.texture = texture;
        this.textureIndex = textureIndex;
    }

    public Vector2f getTextureOffset() {
        int column = textureIndex % getTexture().getAtlasGridSize();
        int row = textureIndex / getTexture().getAtlasGridSize();

        float xOffset = (float) column / getTexture().getAtlasGridSize();
        float yOffset = (float) row / getTexture().getAtlasGridSize();

        return new Vector2f(xOffset, yOffset);
    }

    public RawModel getRawModel() {
        return rawModel;
    }

    public ModelTexture getTexture() {
        return texture;
    }
}
