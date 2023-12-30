#version 400 core

in vec2 pass_textureCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4 out_Colour;

uniform sampler2D textureSampler;
uniform vec3 lightColour[4];
uniform vec3 lightAttenuation[4];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;

void main(void) {
    vec3 unitNormalVector = normalize(surfaceNormal);
    vec3 unitCameraVector = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for (int i = 0; i < 4; i++) {
        float distanceToLight = length(toLightVector[i]);
        float attenuationFactor = lightAttenuation[i].x
                                + lightAttenuation[i].y * distanceToLight
                                + lightAttenuation[i].z * distanceToLight * distanceToLight;
        vec3 unitLightVector = normalize(toLightVector[i]);
        float brightness = max(0.0, dot(unitNormalVector, unitLightVector));
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormalVector);
        float specularFactor = max(0.0, dot(reflectedLightDirection, unitCameraVector));
        float dampedFactor = pow(specularFactor, shineDamper);
        totalDiffuse = totalDiffuse + brightness * lightColour[i] / attenuationFactor;
        totalSpecular = totalSpecular + dampedFactor * reflectivity * lightColour[i] / attenuationFactor;
    }

    totalDiffuse = max(vec3(0.2), totalDiffuse);

    vec4 textureColour = texture(textureSampler, pass_textureCoords);
    if (textureColour.a < 0.5) {
        discard;
    }

    out_Colour = vec4(totalDiffuse, 1.0) * textureColour + vec4(totalSpecular, 1.0);
    out_Colour = mix(vec4(skyColour, 1.0), out_Colour, visibility);
}
