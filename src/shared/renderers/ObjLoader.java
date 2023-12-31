package shared.renderers;

import shared.models.RawModel;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjLoader {
    public static RawModel loadObjModel(String fileName, Loader loader) {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("res" + File.separator + fileName + ".obj");
        } catch (IOException e) {
            System.err.println("Failed to load file!");
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(fileReader);
        String line;
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        float[] verticesArray = null;
        float[] normalsArray = null;
        float[] texturesArray = null;
        int[] indicesArray = null;

        try {
            while(true) {
                line = reader.readLine();
                String[] currentLine = line.split(" ");
                if (line.startsWith("v ")) {
                    Vector3f vertex = parseVector3f(currentLine);
                    vertices.add(vertex);

                } else if (line.startsWith("vt ")) {
                    Vector2f texture = parseVector2f(currentLine);
                    textures.add(texture);

                } else if (line.startsWith("vn ")) {
                    Vector3f normal = parseVector3f(currentLine);
                    normals.add(normal);

                } else if (line.startsWith("f ")) {
                    texturesArray = new float[vertices.size() * 2];
                    normalsArray = new float[vertices.size() * 3];
                    break;
                }
            }

            while(line != null) {
                if (!line.startsWith("f ")) {
                    line = reader.readLine();
                    continue;
                }

                String[] currentLine = line.split(" ");
                for (int i = 0; i < 3; i++) {
                    String[] vertex = currentLine[i + 1].split("/");
                    processVertex(vertex, indices, textures, normals, texturesArray, normalsArray);
                }

                line = reader.readLine();
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        verticesArray = new float[vertices.size() * 3];
        indicesArray = new int[indices.size()];

        int vertexPointer = 0;
        for (Vector3f vertex: vertices) {
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }

        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        return loader.loadToVAO(verticesArray, texturesArray, normalsArray, indicesArray);
    }

    private static Vector3f parseVector3f(String[] elements) {
        return new Vector3f(
                Float.parseFloat(elements[1]),
                Float.parseFloat(elements[2]),
                Float.parseFloat(elements[3]));
    }

    private static Vector2f parseVector2f(String[] elements) {
        return new Vector2f(
                Float.parseFloat(elements[1]),
                Float.parseFloat(elements[2]));
    }

    private static void processVertex(String[] vertexData, List<Integer> indices,
                                      List<Vector2f> textures, List<Vector3f> normals,
                                      float[] texturesArray, float[] normalsArray) {

        int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
        indices.add(currentVertexPointer);

        Vector2f currentTexture = textures.get(Integer.parseInt(vertexData[1]) - 1);
        texturesArray[currentVertexPointer * 2] = currentTexture.x;
        texturesArray[currentVertexPointer * 2 + 1] = 1 - currentTexture.y;

        Vector3f currentNormal = normals.get(Integer.parseInt(vertexData[2]) - 1);
        normalsArray[currentVertexPointer * 3] = currentNormal.x;
        normalsArray[currentVertexPointer * 3 + 1] = currentNormal.y;
        normalsArray[currentVertexPointer * 3 + 2] = currentNormal.z;
    }
}
