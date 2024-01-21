package terrains.entities;

import shared.models.RawModel;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import shared.renderers.Loader;
import shared.toolbox.Directories;
import shared.toolbox.Maths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Terrain {

    public static final float SIZE = 1024;
    private static final float MAX_HEIGHT = 20;
    private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;

    private final float x;
    private final float z;
    private final RawModel model;
    private final TerrainTexturePack texturePack;
    private final TerrainTexture blendMap;

    private float[][] heights;

    public Terrain(int gridX, int gridZ, Loader loader, TerrainTexturePack texturePack,
                   TerrainTexture blendMap, String heightMapFileName) {
        this.texturePack = texturePack;
        this.blendMap = blendMap;
        this.x = gridX * SIZE;
        this.z = gridZ * SIZE;
        this.model = generateTerrain(loader, heightMapFileName);
    }

    public float getHeightOfTerrain(float worldX, float worldZ) {
        if (!isInTerrainBounds(worldX, worldZ)) {
            return 0;
        }

        float terrainX = worldX - this.x;
        float terrainZ = worldZ - this.z;
        float gridSquareSize = SIZE / ((float) heights.length - 1);
        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);

        float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
        float answer;
        if (xCoord <= (1 - zCoord)) {
            answer = Maths.barryCentric(
                    new Vector3f(0, heights[gridX][gridZ], 0),
                    new Vector3f(1, heights[gridX + 1][gridZ], 0),
                    new Vector3f(0, heights[gridX][gridZ + 1], 1),
                    new Vector2f(xCoord, zCoord)
            );
        } else {
            answer = Maths.barryCentric(
                    new Vector3f(1, heights[gridX + 1][gridZ], 0),
                    new Vector3f(1, heights[gridX + 1][gridZ + 1], 1),
                    new Vector3f(0, heights[gridX][gridZ + 1], 1),
                    new Vector2f(xCoord, zCoord)
            );
        }

        return answer;
    }

    public boolean isInTerrainBounds(float worldX, float worldZ) {
        float terrainX = worldX - this.x;
        float terrainZ = worldZ - this.z;

        return 0 <= terrainX && terrainX < SIZE
                && 0 <= terrainZ && terrainZ < SIZE;
    }

    private RawModel generateTerrain(Loader loader, String heightMapFileName) {

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Directories.fromPath("res") + heightMapFileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int vertexCount = image.getHeight();
        heights = new float[vertexCount][vertexCount];
        int count = vertexCount * vertexCount;
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (vertexCount - 1) * (vertexCount)];

        int vertexPointer = 0;
        for (int i = 0; i < vertexCount; i++) {
            for (int j = 0; j < vertexCount; j++) {
                float height = getHeightFromImage(j, i, image);
                heights[j][i] = height;
                vertices[vertexPointer * 3] = j / ((float) vertexCount - 1) * SIZE;
                vertices[vertexPointer * 3 + 1] = height;
                vertices[vertexPointer * 3 + 2] = i / ((float) vertexCount - 1) * SIZE;

                Vector3f normal = calculateNormal(j, i, image);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;

                textureCoords[vertexPointer * 2] = (float) j / ((float) vertexCount - 1);
                textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) vertexCount - 1);
                vertexPointer++;
            }
        }

        int indexPointer = 0;
        for (int gz = 0; gz < vertexCount - 1; gz++) {
            for (int gx = 0; gx < vertexCount - 1; gx++) {
                int topLeft = (gz * vertexCount) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = (gz + 1) * vertexCount + gx;
                int bottomRight = bottomLeft + 1;
                indices[indexPointer++] = topLeft;
                indices[indexPointer++] = bottomLeft;
                indices[indexPointer++] = topRight;
                indices[indexPointer++] = topRight;
                indices[indexPointer++] = bottomLeft;
                indices[indexPointer++] = bottomRight;
            }
        }

        return loader.loadToVAO(vertices, textureCoords, normals, indices);
    }

    private Vector3f calculateNormal(int x, int z, BufferedImage heightMap) {
        float heightL = getHeightFromImage(x - 1, z, heightMap);
        float heightR = getHeightFromImage(x + 1, z, heightMap);
        float heightD = getHeightFromImage(x, z - 1, heightMap);
        float heightU = getHeightFromImage(x, z + 1, heightMap);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalise();
        return normal;
    }

    private float getHeightFromImage(int x, int z, BufferedImage image) {
        if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) {
            return 0;
        }

        float height = image.getRGB(x, z);
        height += MAX_PIXEL_COLOUR / 2f;
        height /= MAX_PIXEL_COLOUR / 2f;
        height *= MAX_HEIGHT;
        return height;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public RawModel getModel() {
        return model;
    }

    public TerrainTexturePack getTexturePack() {
        return texturePack;
    }

    public TerrainTexture getBlendMap() {
        return blendMap;
    }
}
