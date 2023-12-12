package textures;

public class ModelTexture {

    private int textureID;

    private float shineDamper = 1;
    private float reflectivity = 0;

    private Boolean hasTransparency = false;
    private Boolean useFakeLighting = false;

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
}
