#version 400 core
const float PI = 3.14159265;
in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec2 pass_textureCoordinates;
flat out vec3 surfaceNormal;
out vec3 color;
out vec3 toLightVector;
out float visibility;
out vec3 toCameraVector;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 worldCenter;
uniform vec3 lightPosition;
uniform float density = 0.043;
uniform float gradient = 30;

void main(void){

	vec4 worldPosition = transformationMatrix * vec4(position,1.0);
	gl_Position = projectionMatrix * viewMatrix * worldPosition;
	pass_textureCoordinates = textureCoordinates;
	
	surfaceNormal = (transformationMatrix * vec4(normal,0.0)).xyz;
	surfaceNormal = normalize(surfaceNormal);
	float angle = asin(abs(dot(vec3(0, 1, 0), surfaceNormal)));

	if (angle > PI/2.5) {
		color = mix(vec3(0, 1, 0), vec3(0.5, 0.4, 0.12), clamp(1-(angle - PI/2.4)/(PI/2 - PI/2.4), 0, 1));
	} else if (angle > PI/3) {
		color = mix(vec3(0.5, 0.4, 0.12), vec3(0.7), clamp(1-(angle - PI/3)/(PI/2.5 - PI/3), 0, 1));
	} else {
		color = vec3(0.7);
	}
	toLightVector = lightPosition - worldPosition.xyz;
	toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;
	float distance = length(worldCenter.xz - worldPosition.xz);
	visibility = exp(-pow((distance * density), gradient));
	visibility = clamp(visibility, 0.0, 1.0);
}