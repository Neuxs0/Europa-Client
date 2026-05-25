#ifdef GL_ES
precision highp float;
#endif

varying vec2 v_local;

uniform vec4 u_color;
uniform vec2 u_size;
uniform vec2 u_lineStart;
uniform vec2 u_lineEnd;
uniform float u_radius;
uniform float u_halfWidth;
uniform int u_shape;

float roundedRectSdf(vec2 point, vec2 halfSize, float radius) {
    vec2 q = abs(point - halfSize) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

float circleSdf(vec2 point, vec2 size) {
    return length(point - size * 0.5) - min(size.x, size.y) * 0.5;
}

float capsuleSdf(vec2 point, vec2 start, vec2 end, float radius) {
    vec2 segment = end - start;
    float segmentLengthSq = dot(segment, segment);
    float h = segmentLengthSq <= 0.0001 ? 0.0 : clamp(dot(point - start, segment) / segmentLengthSq, 0.0, 1.0);
    return length(point - start - segment * h) - radius;
}

void main() {
    float distanceToEdge = u_shape == 1
        ? circleSdf(v_local, u_size)
        : u_shape == 2
            ? capsuleSdf(v_local, u_lineStart, u_lineEnd, u_halfWidth)
            : roundedRectSdf(v_local, u_size * 0.5, u_radius);
    float aa = max(fwidth(distanceToEdge) * 0.5, 0.0001);
    float alpha = 1.0 - smoothstep(-aa, aa, distanceToEdge);
    gl_FragColor = vec4(u_color.rgb, u_color.a * alpha);
}
