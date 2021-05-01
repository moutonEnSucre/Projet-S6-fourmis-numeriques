#version 400 core

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec2 pass_textureCoordinates;
out vec3 surfaceNormal;
out vec3 toLightVector;
out vec3 toCameraVector;
flat out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition;
uniform int currentTime;
uniform bool animated;
uniform vec3 worldCenter;
uniform int animationDuration;
uniform float density = 0.043;
uniform float gradient = 30;


const float rotationCap = 15;

void main(void){

	vec3 rotatedPosition = position;
	if (animated && position.y <= 0.00001) {
		float theta = rotationCap * sin(2*3.14159265 * currentTime / animationDuration) * 3.14159265 / 180;
		mat3 rotate = mat3(cos(theta), 0, sin(theta), 0, 1, 0, -sin(theta), 0, cos(theta));
		rotatedPosition = rotate * position;
	}
	vec4 worldPosition = transformationMatrix * vec4(rotatedPosition,1.0);
	gl_Position = projectionMatrix * viewMatrix * worldPosition;
	pass_textureCoordinates = textureCoordinates;
	surfaceNormal = (transformationMatrix * vec4(normal,0.0)).xyz;
	toLightVector = lightPosition - worldPosition.xyz;
	float distance = length(worldCenter.xz - worldPosition.xz);
	toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;
	visibility = exp(-pow(distance * density, gradient));
	visibility = clamp(visibility, 0.0, 1.0);
}