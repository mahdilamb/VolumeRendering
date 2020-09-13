#version 330 core
uniform sampler2D compute;
in vec2 vUV;
void main() {
    vec2 UV = (vUV+1)/2;
    gl_FragColor = texture(compute,UV);
}
