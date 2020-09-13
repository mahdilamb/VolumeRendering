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

public class Quad extends Renderer {
    final Program program = new Program(new File("resources\\shaders\\quad"));

    static {
        ColorMap.opacityNodes[0] = 0;
        ColorMap.opacityNodes[1] = 1;
    }

    long lastTime = System.nanoTime();
    int vertexBuffer;
    final float[] vertexBufferData = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
    };

    public Quad() throws IOException {
        super(new Volume(new MosaicVolumeSource(
                        "Brain - Water",
                        new File("resources\\volumes\\sagittal.png"),
                        2,
                        176,
                        .7f
                )),
                new ColorMap(new File("resources\\colorMappings\\colors1.png"))
        );
        colorMap.setRenderer(this);
        getCanvas().addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                gl.glClearColor(0, 0, 0.4f, 1);

                gl.glEnable(GL_BLEND);
                gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                program.init(gl);
                IntBuffer intBuffer = IntBuffer.allocate(1);
                gl.glGenBuffers(1, intBuffer);
                vertexBuffer = intBuffer.get(0);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
                gl.glBufferData(GL_ARRAY_BUFFER, vertexBufferData.length * Float.BYTES, Buffers.newDirectFloatBuffer(vertexBufferData), GL_STATIC_DRAW);
                program.allocateUniform(gl, "iV", (gl2, loc) -> {
                    gl2.glUniformMatrix4fv(loc, 1, false, getCamera().getViewMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
                });
                program.allocateUniform(gl, "iP", (gl2, loc) -> {
                    gl2.glUniformMatrix4fv(loc, 1, false, getCamera().getProjectionMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
                });

                program.allocateUniform(gl, "viewSize", (gl2, loc) -> {
                    gl2.glUniform2f(loc, getWidth(), getHeight());
                });
                program.allocateUniform(gl, "depthSampleCount", (gl2, loc) -> {
                    gl2.glUniform1i(loc, sampleCount);
                });
                program.allocateUniform(gl, "tex", (gl2, loc) -> {
                    gl2.glUniform1i(loc, 0);
                });
                program.allocateUniform(gl, "colorMap", (gl2, loc) -> {
                    gl2.glUniform1i(loc, 1);
                });
                colorMap.init(gl);
                volume.init(gl);

            }


            @Override
            public void display(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                program.use(gl);
                colorMap.render(gl);
                volume.render(gl);
                program.setUniforms(gl);


                // 1st attribute buffer : vertices
                gl.glEnableVertexAttribArray(0);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
                gl.glVertexAttribPointer(
                        0,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
                        2,                  // size
                        GL_FLOAT,           // type
                        false,           // normalized?
                        0,                  // stride
                        0            // array buffer offset
                );
                // Draw the triangle !
                gl.glDrawArrays(GL_TRIANGLES, 0, vertexBufferData.length / 2); // Starting from vertex 0; 3 vertices total -> 1 triangle
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
        new RenderingWindow(new Quad()).run();
    }
}
