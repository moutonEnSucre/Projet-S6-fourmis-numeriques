#version 400 core

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec2 pass_textureCoordinates;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float radius;
uniform vec3 positionOffset;

void main(void){

	vec4 worldPosition = transformationMatrix * vec4(position*radius,1.0);
	gl_Position = projectionMatrix * viewMatrix * worldPosition + vec4(2*positionOffset, 0.0);
	pass_textureCoordinates = textureCoordinates;
}