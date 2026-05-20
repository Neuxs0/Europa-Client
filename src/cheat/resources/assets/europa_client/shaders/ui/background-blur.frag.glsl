#ifdef GL_ES
precision highp float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_direction;
uniform float u_sigma;

void main() {
    vec4 color = vec4(0.0);
    float totalWeight = 0.0;
    float twoSigmaSq = 2.0 * u_sigma * u_sigma;
    float radius = min(24.0, ceil(u_sigma * 3.0));

    for (int i = -24; i <= 24; i++) {
        float sampleIndex = float(i);
        if (abs(sampleIndex) <= radius) {
            float weight = exp(-(sampleIndex * sampleIndex) / twoSigmaSq);
            color += texture2D(u_texture, v_texCoords + u_direction * sampleIndex) * weight;
            totalWeight += weight;
        }
    }

    gl_FragColor = (color / totalWeight) * v_color;
}
