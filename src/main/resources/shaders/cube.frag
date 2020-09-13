#version 330 core
in vec4 vPosition;

out vec4 color;
uniform vec2 viewSize;
uniform mat4 MVP;
uniform mat4 MV;

uniform mat4 iV;
uniform mat4 iP;

uniform vec3 rayOrigin;
uniform float focalLength;
vec3 aabb[2] = vec3[2](
vec3(-1.0, -1.0, -1.0),
vec3(1.0, 1.0, 1.0)
);

struct Ray {
    vec3 origin;
    vec3 direction;
    vec3 inv_direction;
    int sign[3];
};

Ray makeRay(vec3 origin, vec3 direction) {
    vec3 inv_direction = vec3(1.0) / direction;

    return Ray(
    origin,
    direction,
    inv_direction,
    int[3](
    ((inv_direction.x < 0.0) ? 1 : 0),
    ((inv_direction.y < 0.0) ? 1 : 0),
    ((inv_direction.z < 0.0) ? 1 : 0)
    )
    );
}

/*
	From: https://github.com/hpicgs/cgsee/wiki/Ray-Box-Intersection-on-the-GPU
*/
void intersect(
in Ray ray, in vec3 aabb[2],
out float tmin, out float tmax
){
    float tymin, tymax, tzmin, tzmax;
    tmin = (aabb[ray.sign[0]].x - ray.origin.x) * ray.inv_direction.x;
    tmax = (aabb[1-ray.sign[0]].x - ray.origin.x) * ray.inv_direction.x;
    tymin = (aabb[ray.sign[1]].y - ray.origin.y) * ray.inv_direction.y;
    tymax = (aabb[1-ray.sign[1]].y - ray.origin.y) * ray.inv_direction.y;
    tzmin = (aabb[ray.sign[2]].z - ray.origin.z) * ray.inv_direction.z;
    tzmax = (aabb[1-ray.sign[2]].z - ray.origin.z) * ray.inv_direction.z;
    tmin = max(max(tmin, tymin), tzmin);
    tmax = min(min(tmax, tymax), tzmax);
}

void main(){
    vec2 texCoord = ((gl_FragCoord.xy/viewSize)*2)-1;

    vec3 rayDirection;
    rayDirection.xy = 2.0 * gl_FragCoord.xy / viewSize - 1.0;
    rayDirection.z = -focalLength;
    rayDirection = (vec4(rayDirection, 0) * MV).xyz;

    Ray ray = makeRay(rayOrigin.xyz, rayDirection.xyz);
    float tmin = 0.0;
    float tmax = 0.0;
    intersect(ray, aabb, tmin, tmax);
    vec3 rayStart = ray.origin + ray.direction * tmin;
    vec3 rayStop = ray.origin + ray.direction * tmax;
    rayStart = 0.5 * (rayStart + 1.0);
    rayStop = 0.5 * (rayStop + 1.0);

    color =vec4(rayStart,1);
}