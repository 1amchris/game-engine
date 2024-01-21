package shared.toolbox;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;

public class Vectors {

    public static Vector3f parseVector3f(String[] elements, int from, int to) {
        return Vectors.parseVector3f(Arrays.copyOfRange(elements, from, to));
    }

    public static Vector3f parseVector3f(String[] elements) {
        return new Vector3f(
                Float.parseFloat(elements[0]),
                Float.parseFloat(elements[1]),
                Float.parseFloat(elements[2]));
    }

    public static Vector2f parseVector2f(String[] elements, int from, int to) {
        return Vectors.parseVector2f(Arrays.copyOfRange(elements, from, to));
    }

    public static Vector2f parseVector2f(String[] elements) {
        return new Vector2f(
                Float.parseFloat(elements[0]),
                Float.parseFloat(elements[1]));
    }
}
