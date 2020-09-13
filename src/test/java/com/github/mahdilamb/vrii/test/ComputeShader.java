package com.github.mahdilamb.vrii.test;

import com.github.mahdilamb.vrii.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;


public class ComputeShader extends Renderer {

    private final Program program = new Program(new File("resources\\shaders\\compute\\"));
    private final Program quadProgram = new Program(new File("resources\\shaders\\compute\\quad\\"));

    private final ComputeTexture texture = new ComputeTexture(this, 16);

    int vertexBuffer;
    long lastTime = System.nanoTime();

    final float[] vertexBufferData = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
    };

    public ComputeShader() throws IOException {
        super();
        colorMap.setRenderer(this);
        getCanvas().addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                gl.glClearColor(0, 0, 0.4f, 1);

                gl.glEnable(GL_BLEND);
                gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                program.init(gl);
                program.allocateUniform(gl, "iV", (gl2, loc) -> gl2.glUniformMatrix4fv(loc, 1, false, camera.getViewMatrix().invert().get(Buffers.newDirectFloatBuffer(16))));
                program.allocateUniform(gl, "iP", (gl2, loc) -> gl2.glUniformMatrix4fv(loc, 1, false, camera.getProjectionMatrix().invert().get(Buffers.newDirectFloatBuffer(16))));
                program.allocateUniform(gl, "depthSampleCount", (gl2, loc) -> {
                    gl2.glUniform1i(loc, sampleCount);
                });
                program.allocateUniform(gl, "tex", (gl2, loc) -> {
                    gl2.glUniform1i(loc, 0);
                });
                program.allocateUniform(gl, "colorMap", (gl2, loc) -> {
                    gl2.glUniform1i(loc, 1);
                });
                program.allocateUniform(gl, "zScale", (gl2, loc) -> {
                    gl2.glUniform1f(loc, volume.getScale(2));
                });
                final IntBuffer intBuffer = IntBuffer.allocate(1);

                texture.init(gl);
                volume.init(gl);
                colorMap.init(gl);
                quadProgram.init(gl);
                gl.glGenBuffers(1, intBuffer);
                vertexBuffer = intBuffer.get(0);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
                gl.glBufferData(GL_ARRAY_BUFFER, vertexBufferData.length * Float.BYTES, Buffers.newDirectFloatBuffer(vertexBufferData), GL_STATIC_DRAW);

            }


            @Override
            public void display(GLAutoDrawable drawable) {

                final GL2 gl = drawable.getGL().getGL2();
                gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                program.use(gl);
                program.setUniforms(gl);
                volume.render(gl);
                colorMap.render(gl);
                texture.render(gl);
                quadProgram.use(gl);

                // 1st attribute buffer : vertices
                gl.glEnableVertexAttribArray(0);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
                gl.glVertexAttribPointer(
                        0,
                        2,
                        GL_FLOAT,
                        false,
                        0,
                        0
                );
                gl.glActiveTexture(GL_TEXTURE0);
                gl.glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
                gl.glDrawArrays(GL_TRIANGLES, 0, vertexBufferData.length / 2);
                gl.glDisableVertexAttribArray(0);
                final long thisTime = System.nanoTime();
                System.out.println(1 / (((float) thisTime - lastTime) / 1_000_000_000));
                lastTime = thisTime;
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                Program.destroyAllPrograms(gl);
                Texture.destroyAllTextures(gl);
            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                final GL2 gl = drawable.getGL().getGL2();
                gl.glViewport(x, y, width, height);
            }
        });
    }


    public static void main(String... args) throws IOException {
        new RenderingWindow(new ComputeShader()).run();


    }
}
