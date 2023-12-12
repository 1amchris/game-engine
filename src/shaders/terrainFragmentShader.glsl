#version 400 core

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector;
in vec3 toCameraVector;
in float visibility;

out vec4 out_Colour;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;

uniform vec3 lightColour;
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
    vec3 unitLightVector = normalize(toLightVector);
    vec3 unitCameraVector = normalize(toCameraVector);

    float brightness = max(0.2, dot(unitNormalVector, unitLightVector));
    vec3 diffuse = brightness * lightColour;

    vec3 lightDirection = -unitLightVector;
    vec3 reflectedLightDirection = reflect(lightDirection, unitNormalVector);
    float specularFactor = max(0.0, dot(reflectedLightDirection, unitCameraVector));
    float dampedFactor = pow(specularFactor, shineDamper);
    vec3 finalSpecular = dampedFactor * reflectivity * lightColour;

    out_Colour = vec4(diffuse, 1.0) * terrainColour + vec4(finalSpecular, 1.0);
    out_Colour = mix(vec4(skyColour, 1.0), out_Colour, visibility);
}
