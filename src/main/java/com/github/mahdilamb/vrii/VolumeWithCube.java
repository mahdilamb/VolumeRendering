package com.github.mahdilamb.vrii;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;

public class VolumeWithCube extends Volume {

    static float[] vertices = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
    };
    static FloatBuffer verticesBuffer = Buffers.newDirectFloatBuffer(vertices);
    Integer verticesVB0;

    public VolumeWithCube(File file, int width, int height, int depth) throws IOException {
        super(file, width, height, depth);
    }

    public VolumeWithCube(MosaicVolumeSource source) throws IOException {
        super(source);
    }

    @Override
    public void init(GL2 gl) {
        super.init(gl);
        if (verticesVB0 == null) {
            IntBuffer intBuffer = IntBuffer.allocate(1);
            gl.glGenBuffers(1, intBuffer);
            verticesVB0 = intBuffer.get(0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, verticesVB0);
            gl.glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, verticesBuffer.rewind(), GL_STATIC_DRAW);
        }
    }

    @Override
    public void render(GL2 gl) {
        super.render(gl);
        // 1st attribute buffer : vertices
        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL_ARRAY_BUFFER, verticesVB0);
        gl.glVertexAttribPointer(
                0,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
                2,                  // size
                GL_FLOAT,           // type
                false,           // normalized?
                0,                  // stride
                0            // array buffer offset
        );
        // Draw the triangle !
        gl.glDrawArrays(GL_TRIANGLES, 0, 6); // Starting from vertex 0; 3 vertices total -> 1 triangle
        gl.glDisableVertexAttribArray(0);
    }

    @Override
    public void destroy(GL2 gl) {
        gl.glDeleteBuffers(1, Buffers.newDirectIntBuffer(new int[]{verticesVB0}));
        super.destroy(gl);
    }
}
