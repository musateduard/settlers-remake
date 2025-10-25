#version 330 core

layout (location = 0) in vec3 vertex_position;

uniform mat4 transform_matrix;
uniform mat4 projection_matrix;
uniform mat4 view_matrix;
uniform mat4 model_matrix;


void main() {

    // todo: calculate gl_Position = projection_matrix * transform_matrix * model_matrix

    // gl_Position = transform_matrix * vec4(vertex_position, 1.0);
    gl_Position = projection_matrix * view_matrix * model_matrix * vec4(vertex_position, 1.0);
}