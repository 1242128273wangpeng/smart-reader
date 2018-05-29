precision lowp float;

varying vec2 v_texCoord;
uniform sampler2D u_sampler2d;
void main()
{
    gl_FragColor = texture2D(u_sampler2d, v_texCoord);
}