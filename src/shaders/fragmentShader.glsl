#version 400 core

in vec2 pass_textureCoords;
in vec3 surfaceNormal;
in vec3 toLightVector;
in vec3 toCameraVector;
in float visibility;

out vec4 out_Colour;

uniform sampler2D textureSampler;
uniform vec3 lightColour;
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;

void main(void) {
    vec3 unitNormalVector = normalize(surfaceNormal);
    vec3 unitLightVector = normalize(toLightVector);
    vec3 unitCameraVector = normalize(toCameraVector);

    float brightness = max(0.2, dot(unitNormalVector, unitLightVector));
    vec3 diffuse = brightness * lightColour;

    vec3 lightDirection = -unitLightVector;
    vec3 reflectedLightDirection = reflect(lightDirection, unitNormalVector);
    float specularFactor = max(0.0, dot(reflectedLightDirection, unitCameraVector));
    float dampedFactor = pow(specularFactor, shineDamper);
    vec3 finalSpecular = dampedFactor * reflectivity * lightColour;

    vec4 textureColour = texture(textureSampler, pass_textureCoords);
    if (textureColour.a < 0.5) {
        discard;
    }

    out_Colour = vec4(diffuse, 1.0) * textureColour + vec4(finalSpecular, 1.0);
    out_Colour = mix(vec4(skyColour, 1.0), out_Colour, visibility);
}
