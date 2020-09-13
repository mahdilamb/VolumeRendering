#version 330 core

layout(location = 0) in vec2 coordinates;


void main() {

    gl_Position = vec4(coordinates, 1.0, 1.0);

}
