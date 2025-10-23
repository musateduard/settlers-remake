#version 330 core

layout (location = 0) in vec3 vertex_position;
uniform mat4 transform_matrix;


void main() {

    // todo: calculate gl_Position = projection_matrix * transform_matrix * model_matrix

    gl_Position = transform_matrix * vec4(vertex_position, 1.0);
}