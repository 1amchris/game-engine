package entities;

import org.lwjgl.util.vector.Vector3f;

public class LightSource {

    private Vector3f position;
    private Vector3f colour;
    private Vector3f attenuation = new Vector3f(1, 0,0);

    public LightSource(Vector3f position, Vector3f colour) {
        this.position = position;
        this.colour = colour;
    }

    public LightSource(Vector3f position, Vector3f colour, Vector3f attenuation) {
        this.position = position;
        this.colour = colour;
        this.attenuation = attenuation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getColour() {
        return colour;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }

    public Vector3f getAttenuation() {
        return attenuation;
    }
}

