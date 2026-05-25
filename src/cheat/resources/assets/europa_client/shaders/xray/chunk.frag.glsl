#version 150
#ifdef GL_ES
precision mediump float;
#endif

uniform float u_time;
uniform vec3 cameraPosition;
uniform vec3 skyAmbientColor;
uniform vec4 tintColor;
uniform vec3 worldAmbientColor;

#import "base:shaders/common/renderDistance.glsl"

in vec2 texCoordDiffuse;
in vec2 texCoordEmission;
in vec3 worldPos;
in vec4 blocklight;
in vec3 faceNormal;
flat in int frameCount;
in float frameDuration;

uniform sampler2D texDiffuse;
uniform sampler2D texEmission;
uniform vec3 u_sunDirection;

out vec4 outColor;
uniform float u_fogDensity;
uniform float u_xray;

#import "base:shaders/common/fog.glsl"

bool isFaceBorder()
{
    vec3 n = abs(normalize(faceNormal));
    vec2 facePos;

    if (n.x > n.y && n.x > n.z) {
        facePos = worldPos.yz;
    } else if (n.y > n.z) {
        facePos = worldPos.xz;
    } else {
        facePos = worldPos.xy;
    }

    vec2 blockPos = fract(facePos);
    blockPos = min(blockPos, 1.0 - blockPos);
    return blockPos.x <= 0.0625 || blockPos.y <= 0.0625;
}

void main()
{
    vec2 tilingTexCoords = texCoordDiffuse;

    if (frameCount >= 2)
    {
        float tileWidth = 16.0 / textureSize(texDiffuse, 0).x;
        float animTime = mod(floor(u_time / frameDuration), frameCount);
        animTime *= tileWidth;
        tilingTexCoords.x += animTime;
    }

    vec4 texColor = texture(texDiffuse, tilingTexCoords);
    vec4 texColorEmission = texture(texEmission, texCoordEmission);

    float fadeOutDistance = (u_renderDistanceInChunks - 1) * 16;
    float fadeOutFactor = clamp((fadeOutDistance - length(worldPos.xz - cameraPosition.xz))/16.0, 0, 1);
    texColor.a = texColor.a * pow(fadeOutFactor, 0.5);

    if(texColor.a == 0)
    {
        discard;
    }

    bool xrayEnabled = u_xray > 0.5;
    bool oreBlock = blocklight.r > 0.9 && blocklight.g < 0.1 && blocklight.b < 0.1;

    if (xrayEnabled && !oreBlock && !isFaceBorder()) {
        discard;
    }

    float noonDot = dot(u_sunDirection, faceNormal);
    noonDot = sign(noonDot) * sqrt(abs(noonDot));
    vec3 blockAmbientColor = skyAmbientColor * max(noonDot, 0.5);

    vec3 it =  pow(15*blocklight.rgb / 25.0, vec3(2));
    vec3 t = 30.0/(1.0 + exp(-15.0 * it)) - 15;
    vec3 lightTint = max(t/15, blocklight.a * blockAmbientColor);

    if (xrayEnabled) {
        lightTint = oreBlock ? vec3(1.0) : vec3(0.72);
    }

    outColor = vec4(texColor.rgb * lightTint, texColor.a);
    outColor.rgb = max(outColor.rgb, texColorEmission.rgb * texColorEmission.a);
    outColor = tintColor * outColor;

    vec3 fogColor = skyAmbientColor;
    fogColor = getFogColor(fogColor, blocklight.rgb, u_fogDensity, worldPos, cameraPosition);
    outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, worldPos, cameraPosition);

    outColor.rgb = max(outColor.rgb, texColor.rgb * worldAmbientColor);

    float gamma = 1.1;
    outColor.rgb = pow(outColor.rgb, vec3(1.0/gamma));
}
