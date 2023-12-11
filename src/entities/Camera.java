package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

    private static final float MOVEMENT_SPEED = 0.2f;
    private static final float LOOK_SPEED = 0.5f;

    private static final float MAX_PITCH = 90f;
    private static final float MIN_PITCH = -90f;

    private Vector3f position = new Vector3f(0, 5, 0);
    private float pitch = 10;
    private float yaw = 0;
    private float roll = 0;

    public Camera() { }

    public void move() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            position.x += (float) (Math.sin(Math.toRadians(yaw)) * MOVEMENT_SPEED);
            position.z -= (float) (Math.cos(Math.toRadians(yaw)) * MOVEMENT_SPEED);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            position.x -= (float) (Math.sin(Math.toRadians(yaw)) * MOVEMENT_SPEED);
            position.z += (float) (Math.cos(Math.toRadians(yaw)) * MOVEMENT_SPEED);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            position.x -= (float) (Math.cos(Math.toRadians(yaw)) * MOVEMENT_SPEED);
            position.z -= (float) (Math.sin(Math.toRadians(yaw)) * MOVEMENT_SPEED);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            position.x += (float) (Math.cos(Math.toRadians(yaw)) * MOVEMENT_SPEED);
            position.z += (float) (Math.sin(Math.toRadians(yaw)) * MOVEMENT_SPEED);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            position.y += MOVEMENT_SPEED;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            position.y -= MOVEMENT_SPEED;
        }

        yaw = (yaw + Mouse.getDX() * LOOK_SPEED) % 360;
        pitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, pitch - Mouse.getDY() * LOOK_SPEED));
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
