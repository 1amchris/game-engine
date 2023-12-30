#version 400 core

in vec3 textureCoords;
out vec4 out_Color;

uniform samplerCube cubeMap;
uniform samplerCube cubeMap2;
uniform float blendFactor;
uniform vec3 fogColour;

const float lowerFogLimit = 0.0;
const float upperFogLimit = 60.0;

void main(void) {
    vec4 texture1 = texture(cubeMap, textureCoords);
    vec4 texture2 = texture(cubeMap2, textureCoords);
    vec4 finalColour = mix(texture1, texture2, blendFactor);
    float visibilityFactor = clamp((textureCoords.y - lowerFogLimit) / (upperFogLimit - lowerFogLimit), 0.0, 1.0);
    out_Color = mix(vec4(fogColour, 1.0), finalColour, visibilityFactor);
}