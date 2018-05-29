uniform mat4 u_MVPMatrix;

attribute vec4 av4_position;
attribute vec2 av2_texCoord;
varying vec2 v_texCoord;
void main()
{
    gl_Position = u_MVPMatrix * av4_position;
    v_texCoord = av2_texCoord;
}