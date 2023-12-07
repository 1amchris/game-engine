#version 400 core

in vec3 colour;
in vec2 pass_textureCoords;

out vec4 out_Colour;

uniform sampler2D textureSampler;

void main(void) {

//    out_Colour = vec4(colour, 1.0);
    out_Colour = texture(textureSampler, pass_textureCoords);

}
