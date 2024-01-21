package shared.renderers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import shared.models.RawModel;
import shared.models.NMVertex;
import shared.toolbox.Directories;
import shared.toolbox.Vectors;

public class NMObjLoader {

	private static final String RES_LOC = Directories.fromPath("res");

	public static RawModel loadObjModel(String fileName, Loader loader) {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(RES_LOC + fileName + ".obj");
		} catch (FileNotFoundException e) {
			System.err.println("Failed to load file!");
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(fileReader);
		String line;
		List<NMVertex> vertices = new ArrayList<>();
		List<Vector2f> textures = new ArrayList<>();
		List<Vector3f> normals = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		try {
			while (true) {
				line = reader.readLine();
				String[] currentLine = line.split(" ");
				if (line.startsWith("v ")) {
					Vector3f vertex = Vectors.parseVector3f(currentLine, 1, 4);
					NMVertex newVertex = new NMVertex(vertices.size(), vertex);
					vertices.add(newVertex);

				} else if (line.startsWith("vt ")) {
					Vector2f texture = Vectors.parseVector2f(currentLine, 1, 3);
					textures.add(texture);

				} else if (line.startsWith("vn ")) {
					Vector3f normal = Vectors.parseVector3f(currentLine, 1, 4);
					normals.add(normal);

				} else if (line.startsWith("f ")) {
					break;
				}
			}

			while (line != null && line.startsWith("f ")) {
				String[] currentLine = line.split(" ");
				String[] vertex1 = currentLine[1].split("/");
				String[] vertex2 = currentLine[2].split("/");
				String[] vertex3 = currentLine[3].split("/");
				NMVertex v0 = processVertex(vertex1, vertices, indices);
				NMVertex v1 = processVertex(vertex2, vertices, indices);
				NMVertex v2 = processVertex(vertex3, vertices, indices);
				calculateTangents(v0, v1, v2, textures);
				line = reader.readLine();
			}

			reader.close();

		} catch (IOException e) {
            e.printStackTrace();
		}

		removeUnusedVertices(vertices);

		float[] verticesArray = new float[vertices.size() * 3];
		float[] texturesArray = new float[vertices.size() * 2];
		float[] normalsArray = new float[vertices.size() * 3];
		float[] tangentsArray = new float[vertices.size() * 3];
		convertDataToArrays(vertices, textures, normals, verticesArray,
				texturesArray, normalsArray, tangentsArray);
		int[] indicesArray = convertIndicesListToArray(indices);

		return loader.loadToVAO(verticesArray, texturesArray, normalsArray, tangentsArray, indicesArray);
	}

	private static void calculateTangents(NMVertex v0, NMVertex v1, NMVertex v2,
                                          List<Vector2f> textures) {
		Vector3f deltaPos1 = Vector3f.sub(v1.getPosition(), v0.getPosition(), null);
		Vector3f deltaPos2 = Vector3f.sub(v2.getPosition(), v0.getPosition(), null);
		Vector2f uv0 = textures.get(v0.getTextureIndex());
		Vector2f uv1 = textures.get(v1.getTextureIndex());
		Vector2f uv2 = textures.get(v2.getTextureIndex());
		Vector2f deltaUv1 = Vector2f.sub(uv1, uv0, null);
		Vector2f deltaUv2 = Vector2f.sub(uv2, uv0, null);

		float r = 1.0f / (deltaUv1.x * deltaUv2.y - deltaUv1.y * deltaUv2.x);
		deltaPos1.scale(deltaUv2.y);
		deltaPos2.scale(deltaUv1.y);
		Vector3f tangent = Vector3f.sub(deltaPos1, deltaPos2, null);
		tangent.scale(r);
		v0.addTangent(tangent);
		v1.addTangent(tangent);
		v2.addTangent(tangent);
	}

	private static NMVertex processVertex(String[] vertex, List<NMVertex> vertices,
                                          List<Integer> indices) {
		int index = Integer.parseInt(vertex[0]) - 1;
		NMVertex currentVertex = vertices.get(index);
		int textureIndex = Integer.parseInt(vertex[1]) - 1;
		int normalIndex = Integer.parseInt(vertex[2]) - 1;
		if (!currentVertex.isSet()) {
			currentVertex.setTextureIndex(textureIndex);
			currentVertex.setNormalIndex(normalIndex);
			indices.add(index);
			return currentVertex;
		} else {
			return dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices,
					vertices);
		}
	}

	private static int[] convertIndicesListToArray(List<Integer> indices) {
		int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}

	private static float convertDataToArrays(List<NMVertex> vertices, List<Vector2f> textures,
                                             List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
                                             float[] normalsArray, float[] tangentsArray) {

        float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			NMVertex currentVertex = vertices.get(i);
			if (currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			Vector3f position = currentVertex.getPosition();
			Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
			Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
			Vector3f tangent = currentVertex.getAverageTangent();
			verticesArray[i * 3] = position.x;
			verticesArray[i * 3 + 1] = position.y;
			verticesArray[i * 3 + 2] = position.z;
			texturesArray[i * 2] = textureCoord.x;
			texturesArray[i * 2 + 1] = 1 - textureCoord.y;
			normalsArray[i * 3] = normalVector.x;
			normalsArray[i * 3 + 1] = normalVector.y;
			normalsArray[i * 3 + 2] = normalVector.z;
			tangentsArray[i * 3] = tangent.x;
			tangentsArray[i * 3 + 1] = tangent.y;
			tangentsArray[i * 3 + 2] = tangent.z;

		}

		return furthestPoint;
	}

	private static NMVertex dealWithAlreadyProcessedVertex(NMVertex previousVertex, int newTextureIndex,
                                                           int newNormalIndex, List<Integer> indices, List<NMVertex> vertices) {
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			indices.add(previousVertex.getIndex());
			return previousVertex;
		} else {
			NMVertex anotherVertex = previousVertex.getDuplicateVertex();
			if (anotherVertex != null) {
				return dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex,
						newNormalIndex, indices, vertices);
			} else {
				NMVertex duplicateVertex = previousVertex.duplicate(vertices.size());//NEW
				duplicateVertex.setTextureIndex(newTextureIndex);
				duplicateVertex.setNormalIndex(newNormalIndex);
				previousVertex.setDuplicateVertex(duplicateVertex);
				vertices.add(duplicateVertex);
				indices.add(duplicateVertex.getIndex());
				return duplicateVertex;
			}
		}
	}

	private static void removeUnusedVertices(List<NMVertex> vertices) {
		for (NMVertex vertex : vertices) {
			vertex.averageTangents();
			if (!vertex.isSet()) {
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	}
}