package shared.toolbox;

import gameObjects.entities.Camera;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.*;

public class MousePicker {

    private Vector3f currentRay;

    private final Matrix4f projectionMatrix;
    private final Camera camera;
    private Matrix4f viewMatrix;

    public MousePicker(Camera camera, Matrix4f projectionMatrix) {
        this.camera = camera;
        this.projectionMatrix = projectionMatrix;
        this.viewMatrix = Maths.createViewMatrix(camera);
    }

    public Vector3f getCurrentRay() {
        return currentRay;
    }

    public void update() {
        viewMatrix = Maths.createViewMatrix(this.camera);
        currentRay = calculateMouseRay();
    }

    private Vector3f calculateMouseRay() {
        Vector2f normalizedCoords = getNormalizedDeviceCoords(Mouse.getX(), Mouse.getY());
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1 /* (in the screen) */, 1);
        Vector4f eyeCoords = toEyeSpaceCoords(clipCoords);
        Vector4f worldCoords = toWorldSpaceCoords(eyeCoords);
        Vector3f mouseRay = new Vector3f(worldCoords.x, worldCoords.y, worldCoords.z);
        mouseRay.normalise();
        return mouseRay;
    }

    private Vector4f toWorldSpaceCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
        return Matrix4f.transform(invertedView, eyeCoords, null);
    }

    private Vector4f toEyeSpaceCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
        Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1 /* (in the screen) */, 0);
    }

    private Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY) {
        float x = 2 * mouseX / Display.getWidth() - 1;
        float y = 2 * mouseY / Display.getHeight() - 1;

        /*
         * Note to self:
         *  LWJGL is kind of weird in that it will use bottom left as origin,
         *  Where other graphics libraries will usually use top left.
         *  return "new Vector2f(x, -y)" if this is the case.
         */
        return new Vector2f(x, y);
    }
}
