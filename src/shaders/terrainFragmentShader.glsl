#version 400 core

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4 out_Colour;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;

uniform vec3 lightColour[4];
uniform vec3 lightAttenuation[4];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;

void main(void) {

    vec4 blendMapColour = texture(blendMap, pass_textureCoordinates);
    float backgroundTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
    vec2 tiledCoordinates = pass_textureCoordinates * 40.0;
    vec4 backgroundTextureColour = texture(backgroundTexture, tiledCoordinates) * backgroundTextureAmount;
    vec4 rTextureColour = texture(rTexture, tiledCoordinates) * blendMapColour.r;
    vec4 gTextureColour = texture(gTexture, tiledCoordinates) * blendMapColour.g;
    vec4 bTextureColour = texture(bTexture, tiledCoordinates) * blendMapColour.b;
    vec4 terrainColour = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;

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

    out_Colour = vec4(totalDiffuse, 1.0) * terrainColour + vec4(totalSpecular, 1.0);
    out_Colour = mix(vec4(skyColour, 1.0), out_Colour, visibility);
}
