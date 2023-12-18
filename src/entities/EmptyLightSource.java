package entities;

import org.lwjgl.util.vector.Vector3f;

public class EmptyLightSource extends LightSource {
    public EmptyLightSource() {
        super(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
    }
}
