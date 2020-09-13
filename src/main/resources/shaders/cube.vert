#version 330 core

layout(location = 0) in vec3 coordinates;
uniform mat4 MVP;
out vec4 vPosition;

void main() {
    gl_Position = MVP*vec4(coordinates,1);

    vPosition = vec4(coordinates,1);


}
