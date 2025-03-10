#version 150

in vec4 a_lighting;
uniform vec3 u_batchPosition;
uniform mat4 u_projViewTrans;
uniform mat4 u_modelMat;

out vec2 v_texCoord0;
out vec3 worldPos;
out vec4 blocklight;
out vec3 faceNormal;

#import "base:shaders/common/bitUnpacker.glsl"

void main() {
	worldPos = GET_BLOCK_VERT_POSITION + u_batchPosition;
	blocklight = a_lighting;
	v_texCoord0 = GET_TEX_COORDS;
	faceNormal = GET_FACE_NORMAL;
	vec4 modelPosition = u_modelMat * vec4(worldPos, 1.0);
	worldPos = modelPosition.xyz;
	gl_Position = u_projViewTrans * modelPosition;
}
