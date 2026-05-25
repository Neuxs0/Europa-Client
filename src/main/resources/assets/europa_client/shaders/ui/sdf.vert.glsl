attribute vec2 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec2 v_local;

void main() {
    v_local = a_texCoord0;
    gl_Position = u_projTrans * vec4(a_position, 0.0, 1.0);
}
