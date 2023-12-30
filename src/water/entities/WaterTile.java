package water.entities;

import org.lwjgl.util.vector.Vector3f;

public class WaterTile {
	
	public static final float TILE_SIZE = 60;

	private final Vector3f position;

	public WaterTile(Vector3f position){
		this.position = position;
	}

	public Vector3f getPosition() { return position; }
}
