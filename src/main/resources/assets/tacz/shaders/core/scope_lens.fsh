#version 330

// Same as position_tex_color, but ignores the alpha of the sampled frame: shader packs leave it at 0.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 frame = texture(Sampler0, texCoord0);
    fragColor = vec4(frame.rgb * vertexColor.rgb * ColorModulator.rgb, vertexColor.a * ColorModulator.a);
}
