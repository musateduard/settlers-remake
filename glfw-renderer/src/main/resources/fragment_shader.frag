#version 330 core

uniform vec4 uniform_color;
layout (location = 0) out vec4 fragment_color;


void main() {
    fragment_color = uniform_color;
}