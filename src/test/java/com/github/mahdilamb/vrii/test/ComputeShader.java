package com.github.mahdilamb.vrii.test;

import com.github.mahdilamb.vrii.ComputeTexture;
import com.github.mahdilamb.vrii.Program;
import com.github.mahdilamb.vrii.Texture;
import com.github.mahdilamb.vrii.test.arcball.Camera;
import com.github.mahdilamb.vrii.test.arcball.Controls;
import com.github.mahdilamb.vrii.test.arcball.iRenderer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL3ES3.*;


public class ComputeShader extends JFrame implements iRenderer {
    private final GLProfile profile = GLProfile.getDefault();
    private final GLCapabilities capabilities = new GLCapabilities(profile);
    private final GLCanvas canvas = new GLCanvas(capabilities);
    final Camera camera = new Camera(this);
    final Controls controls = new Controls(this);
    private final Program program = new Program(new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\shaders\\compute\\"));
    private final Program quadProgram = new Program(new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\shaders\\compute\\quad\\"));

    private final ComputeTexture texture = new ComputeTexture();
    final int[] workGroupCount = new int[3];
    final int[] workGroupSize = new int[3];
    int workGroupInvocations;
    int vertexBuffer;
    final float[] vertexBufferData = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
    };

    {
        canvas.setSize(800, 640);
        canvas.addMouseListener(controls);
        canvas.addMouseMotionListener(controls);
        canvas.addMouseWheelListener(controls);
        canvas.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                program.init(gl);
                program.allocateUniform(gl, "iV", (gl2, loc) -> {
                    gl2.glUniformMatrix4fv(loc, 1, false, camera.getViewMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
                });
                program.allocateUniform(gl, "iP", (gl2, loc) -> {
                    gl2.glUniformMatrix4fv(loc, 1, false, camera.getProjectionMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
                });
                program.allocateUniform(gl, "depthSampleCount", (gl2, loc) -> {
                    gl2.glUniform1i(loc, 512);
                });
                final IntBuffer intBuffer = IntBuffer.allocate(1);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, intBuffer);
                workGroupCount[0] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1, intBuffer);
                workGroupCount[1] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2, intBuffer);
                workGroupCount[2] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, intBuffer);
                workGroupSize[0] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1, intBuffer);
                workGroupSize[1] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2, intBuffer);
                workGroupSize[2] = intBuffer.get(0);
                gl.glGetIntegerv(GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, intBuffer);
                workGroupInvocations = intBuffer.get(0);
                texture.init(gl);
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
                //TODO update compute shader
            }
        });
    }

    public ComputeShader() throws IOException {

        add(canvas);
        pack();
    }

    public int getCanvasHeight() {
        return canvas.getSurfaceHeight();
    }

    @Override
    public void redraw() {
        canvas.display();
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    public int getCanvasWidth() {
        return canvas.getSurfaceWidth();
    }


    public static void main(String... args) throws IOException {
        final ComputeShader cube = new ComputeShader();

        cube.setVisible(true);

    }
}
