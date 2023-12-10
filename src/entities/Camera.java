package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

    private static final float MOVEMENT_SPEED = 0.02f;

    private Vector3f position = new Vector3f(0, 0, 0);
    private float pitch;
    private float yaw;
    private float roll;

    public Camera() { }

    public void move() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            position.z -= MOVEMENT_SPEED;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            position.x -= MOVEMENT_SPEED;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            position.z += MOVEMENT_SPEED;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            position.x += MOVEMENT_SPEED;
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }
}
