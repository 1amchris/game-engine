#version 400 core

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in vec3 tangent;

out vec2 pass_textureCoordinates;
out vec3 toLightVector[4];
out vec3 toCameraVector;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPositionEyeSpace[4];

uniform float numberOfRows;
uniform vec2 offset;

const float density = 0;
const float gradient = 5.0;

uniform vec4 plane;

void main(void) {
	vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
	gl_ClipDistance[0] = dot(worldPosition, plane);
	mat4 modelViewMatrix = viewMatrix * transformationMatrix;
	vec4 positionRelativeToCam = modelViewMatrix * vec4(position, 1.0);
	gl_Position = projectionMatrix * positionRelativeToCam;
	
	pass_textureCoordinates = (textureCoordinates / numberOfRows) + offset;
	
	vec3 surfaceNormal = normalize((modelViewMatrix * vec4(normal, 0.0)).xyz);
	vec3 surfaceTangent = normalize((modelViewMatrix * vec4(tangent, 0.0)).xyz);
	vec3 surfaceBitangent = normalize(cross(surfaceNormal, surfaceTangent));

	mat3 toTangentSpace = mat3(surfaceTangent, surfaceBitangent, surfaceNormal);

	for (int i = 0; i < 4; i++) {
		toLightVector[i] = toTangentSpace * (lightPositionEyeSpace[i] - positionRelativeToCam.xyz);
	}
	toCameraVector = toTangentSpace * -positionRelativeToCam.xyz;
	
	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance * density), gradient));
	visibility = clamp(visibility, 0.0, 1.0);
}