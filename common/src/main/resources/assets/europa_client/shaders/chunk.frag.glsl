#version 150
#ifdef GL_ES
precision mediump float;
#endif

uniform vec3 cameraPosition;
uniform vec3 skyAmbientColor;
uniform vec4 tintColor;
uniform vec3 worldAmbientColor;
uniform vec3 u_sunDirection;

#import "base:shaders/common/renderDistance.glsl"

in vec2 v_texCoord0;
in vec3 worldPos;
in vec4 blocklight;
in vec3 faceNormal;

uniform sampler2D texDiffuse;

out vec4 outColor;
uniform float u_fogDensity;

#import "base:shaders/common/fog.glsl"

void main() {
    vec4 texColor = texture(texDiffuse, v_texCoord0);
    float fadeOutDistance = (u_renderDistanceInChunks - 1.0) * 16.0;
    float fadeOutFactor = clamp((fadeOutDistance - length(worldPos.xz - cameraPosition.xz)) / 16.0, 0.0, 1.0);
    texColor.a *= pow(fadeOutFactor, 0.5);
    if(texColor.a == 0.0) {
        discard;
    }
    vec3 lightTint = vec3(1.0);
    outColor = tintColor * vec4(texColor.rgb * lightTint, texColor.a);
    vec3 fogColor = skyAmbientColor;
    fogColor = getFogColor(fogColor, blocklight.rgb, u_fogDensity, worldPos, cameraPosition);
    outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, worldPos, cameraPosition);
    outColor.rgb = max(outColor.rgb, texColor.rgb * worldAmbientColor);
    float gamma = 1.1;
    outColor.rgb = pow(outColor.rgb, vec3(1.0 / gamma));
}
