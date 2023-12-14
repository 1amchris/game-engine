package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

//    private static final float MOVEMENT_SPEED = 0.2f;
//    private static final float LOOK_SPEED = 0.5f;

//    private static final float MAX_PITCH = 90f;
//    private static final float MIN_PITCH = -90f;

    private Vector3f position = new Vector3f(0, 0, 0);
    private float pitch = 15;
    private float yaw = 0;
    private float roll = 0;

    private Player player;
    private float distanceFromPlayer = 30;
    private float angleAroundPlayer = 0;

    public Camera(Player player) {
        this.player = player;
    }

    public void move() {
        calculateZoom();
        calculatePitch();
        calculateAngleAroundPlayer();
        float horizontalDistance = calculateHorizontalDistance();
        float verticalDistance = calculateVerticalDistance();
        calculateCameraPosition(horizontalDistance, verticalDistance);
        this.yaw = 180 - (player.getRotation().y  + angleAroundPlayer);
    }

    private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
        float theta = player.getRotation().y + angleAroundPlayer;
        float offsetX = horizontalDistance * (float) Math.sin(Math.toRadians(theta));
        float offsetZ = horizontalDistance * (float) Math.cos(Math.toRadians(theta));
        position.x = player.getPosition().x - offsetX;
        position.y = player.getPosition().y + verticalDistance;
        position.z = player.getPosition().z - offsetZ;
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

    private float calculateHorizontalDistance() {
        return distanceFromPlayer * (float) Math.cos(Math.toRadians(pitch));
    }

    private float calculateVerticalDistance() {
        return distanceFromPlayer * (float) Math.sin(Math.toRadians(pitch));
    }

    private void calculateZoom() {
        float zoomLevel = Mouse.getDWheel() * 0.1f;
        distanceFromPlayer -= zoomLevel;
    }

    private void calculatePitch() {
        if (Mouse.isButtonDown(1 /* right button */)) {
            float pitchChange = Mouse.getDY() * 0.1f;
            pitch -= pitchChange;
        }
    }

    private void calculateAngleAroundPlayer() {
        if (Mouse.isButtonDown(0 /* left button */)) {
            float angleChange = Mouse.getDX() * 0.3f;
            angleAroundPlayer -= angleChange;
        }
    }
}
