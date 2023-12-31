#version 400 core

in vec4 clipSpace;
in vec2 textureCoords;
in vec3 toCameraVector;
in vec3 fromLightVector;

out vec4 out_Color;

uniform sampler2D reflectionTexture;
uniform sampler2D refractionTexture;
uniform sampler2D dudvMap;
uniform sampler2D normalMap;
uniform sampler2D depthMap;
uniform vec3 lightColour;

uniform float waveDisplacement;
uniform float refractivity;
uniform float reflectivity;
uniform float shineDamper;
uniform float ripplesStrength;
uniform vec3 colour;

uniform float nearPlane;
uniform float farPlane;

void main(void) {
	vec2 normalizedDeviceSpace = clipSpace.xy / (clipSpace.w * 2.0) + 0.5;
	vec2 reflectionTextureCoords = vec2(normalizedDeviceSpace.x, -normalizedDeviceSpace.y);
	vec2 refractionTextureCoords = normalizedDeviceSpace;

	float depth = texture(depthMap, refractionTextureCoords).r;
	float floorDistance = 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));

	depth = gl_FragCoord.z;
	float waterDistance = 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
	float waterDepth = floorDistance - waterDistance;

	vec2 distortedTextureCoords = texture(dudvMap, vec2(textureCoords.x + waveDisplacement, textureCoords.y)).rg * 0.1;
	distortedTextureCoords = textureCoords + vec2(distortedTextureCoords.x, distortedTextureCoords.y + waveDisplacement);
	vec2 distortion = (texture(dudvMap, distortedTextureCoords).rg * 2.0 - 1.0) * ripplesStrength * clamp(waterDepth / 20, 0.0, 1.0);

	reflectionTextureCoords += distortion;
	reflectionTextureCoords.x = clamp(reflectionTextureCoords.x, 0.01, 0.99);
	reflectionTextureCoords.y = clamp(reflectionTextureCoords.y, -0.99, -0.01);
	vec4 reflectionColour = texture(reflectionTexture, reflectionTextureCoords);

	refractionTextureCoords += distortion;
	refractionTextureCoords = clamp(refractionTextureCoords, 0.01, 0.99);
	vec4 refractionColour = texture(refractionTexture, refractionTextureCoords);

	vec4 normalMapColour = texture(normalMap, distortedTextureCoords);
	vec3 normal = normalize(vec3(normalMapColour.r * 2.0 - 1.0, normalMapColour.b * 6.0, normalMapColour.g * 2.0 - 1));

	vec3 viewVector = normalize(toCameraVector);
	float refractiveFactor = pow(dot(viewVector, normal), refractivity);

	vec3 reflectedLight = reflect(normalize(fromLightVector), normal);
	float specularFactor = pow(max(dot(reflectedLight, viewVector), 0.0), shineDamper);
	vec3 specularHighlights = lightColour * specularFactor * reflectivity * clamp(waterDepth / 10.0, 0.0, 1.0);

	out_Color = mix(reflectionColour, refractionColour, refractiveFactor);
	out_Color = mix(out_Color, vec4(colour, 1.0), 0.2) + vec4(specularHighlights, 0.0);
	out_Color.a = clamp(waterDepth / 5.0, 0.0, 1.0);
}