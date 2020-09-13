#version 330 core
out vec4 color;
in vec2 vUV;
void main(){
    color =vec4(vUV,0,1);
}