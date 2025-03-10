#version 150

in vec4 a_lighting;
uniform vec3 u_batchPosition;
uniform mat4 u_projViewTrans;
uniform mat4 u_modelMat;

uniform float u_time;
uniform vec3 cameraPosition;

out vec2 v_texCoord0;
out vec4 blocklight;
out float waveStrength;
out vec3 worldPos;
out vec3 toCameraVector;

#import "base:shaders/common/bitUnpacker.glsl"

void main() {
    worldPos = GET_BLOCK_VERT_POSITION + u_batchPosition;
    blocklight = a_lighting;
    v_texCoord0 = GET_TEX_COORDS;
    toCameraVector = cameraPosition - worldPos;
    vec3 normal = GET_VERT_NORMAL;
    float xOff = 0.0;
    float zOff = 0.0;
    if(normal.y < 0.0) {
        xOff = normal.x * -0.001;
        zOff = normal.z * -0.001;
    }
    float waveSpeed = 0.25;
    float waveTime = waveSpeed * u_time;
    float scale = 5.0;
    float wavePositionA = 10.0 * (cos(worldPos.x * scale) + sin(worldPos.z * scale));
    float wavePositionB = 10.0 * (sin(worldPos.x * scale) + cos(worldPos.z * scale));
    waveStrength = 0.1 * (sin(waveTime + wavePositionA) * cos(waveTime + wavePositionB));
    vec4 modelPosition = u_modelMat * vec4(worldPos.x + xOff, worldPos.y + waveStrength - 0.2, worldPos.z + zOff, 1.0);
    worldPos = modelPosition.xyz;
    gl_Position = u_projViewTrans * modelPosition;
}
