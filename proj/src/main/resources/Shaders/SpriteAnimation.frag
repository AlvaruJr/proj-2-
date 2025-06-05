uniform sampler2D ColorMap;
uniform int SpriteCols;
uniform int SpriteRows;
uniform vec2 Offset;
uniform vec2 Scale;

varying vec2 texCoord;

void main() {
    vec2 scaledCoord = texCoord * Scale + Offset;
    gl_FragColor = texture2D(ColorMap, scaledCoord);
}