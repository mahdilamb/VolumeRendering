#version 330 core

uniform float aspect;

layout (location=0) in vec2 coordinates;
out vec2 texCoord;
uniform mat4 transform;


void main() {
	texCoord = coordinates;
	texCoord.x *= aspect;


	gl_Position = vec4(coordinates, 1.0, 1.0);
}
