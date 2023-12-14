package entities;

import models.TexturedModel;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;

public class Player extends Entity {

    private static final float RUN_SPEED = 20;
    private static final float TURN_SPEED = 160;
    private static final float GRAVITY = -50;
    private static final float JUMP_POWER = 20;
    private static final float TERRAIN_HEIGHT = 0;

    private float horizontalVelocity = 0;
    private float verticalVelocity = 0;
    private float turnSpeed = 0;
    private Boolean canJump = true;

    public Player(TexturedModel model, Vector3f position, Vector3f rotation, float scale) {
        super(model, position, rotation, scale);
    }

    public void move() {
        checkInputs();
        increaseRotation(new Vector3f(0, turnSpeed * DisplayManager.getDeltaTime(), 0));
        float distance = horizontalVelocity * DisplayManager.getDeltaTime();
        float distanceX = (float) Math.sin(Math.toRadians(getRotation().y)) * distance;
        float distanceZ = (float) Math.cos(Math.toRadians(getRotation().y)) * distance;
        verticalVelocity += GRAVITY * DisplayManager.getDeltaTime();
        float distanceY = verticalVelocity * DisplayManager.getDeltaTime();
        increasePosition(new Vector3f(distanceX, distanceY, distanceZ));

        if (getPosition().y < TERRAIN_HEIGHT) {
            verticalVelocity = 0;
            getPosition().y = TERRAIN_HEIGHT;
            canJump = true;
        }
    }

    private void jump() {
        if (!canJump) {
            return;
        }

        verticalVelocity = JUMP_POWER;
        canJump = false;
    }

    private void checkInputs() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            horizontalVelocity = RUN_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            horizontalVelocity = -RUN_SPEED;
        } else {
            horizontalVelocity = 0;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            turnSpeed = TURN_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            turnSpeed = -TURN_SPEED;
        } else {
            turnSpeed = 0;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            jump();
        }
    }
}
