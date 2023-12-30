package shared.textures;

public class ModelTexture {

    private final int textureID;

    private float shineDamper = 1;
    private float reflectivity = 0;

    private Boolean hasTransparency = false;
    private Boolean useFakeLighting = false;

    private int atlasGridSize = 1;
    private int texturesCount = 1;

    public ModelTexture(int id) {
        this.textureID = id;
    }

    public int getID() {
        return this.textureID;
    }

    public float getShineDamper() {
        return shineDamper;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

    public Boolean isTransparent() {
        return hasTransparency;
    }

    public void setTransparency(Boolean hasTransparency) {
        this.hasTransparency = hasTransparency;
    }

    public Boolean getUseFakeLighting() {
        return useFakeLighting;
    }

    public void setUseFakeLighting(Boolean useFakeLighting) {
        this.useFakeLighting = useFakeLighting;
    }

    public int getAtlasGridSize() {
        return atlasGridSize;
    }

    public int getTexturesCount() {
        return texturesCount;
    }

    public void setAtlasProperties(int atlasGridSize, int texturesCount) {
        this.atlasGridSize = atlasGridSize;
        this.texturesCount = texturesCount;
    }
}
