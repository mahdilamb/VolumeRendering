package com.github.mahdilamb.vrii.test;

import com.github.mahdilamb.vrii.*;
import com.github.mahdilamb.vrii.test.arcball.Camera;
import com.github.mahdilamb.vrii.test.arcball.Controls;
import com.github.mahdilamb.vrii.test.arcball.iRenderer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import org.joml.Vector3f;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;


public class Cube extends JFrame implements iRenderer {
    private final GLProfile profile = GLProfile.getDefault();
    private final GLCapabilities capabilities = new GLCapabilities(profile);
    private final GLCanvas canvas = new GLCanvas(capabilities);
    final Camera camera = new Camera(this);
    final Controls controls = new Controls(this);
    private final Program program = new Program(new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\shaders\\cube"));
    final Volume volume = new Volume(new MosaicVolumeSource(
            "Brain - Water",
            new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\volumes\\sagittal.png"),
            2,
            176,
            .7f
    ));
    final ColorMap colorMap = new ColorMap(new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\colorMappings\\colors1.png"));
    static {
        ColorMap.opacityNodes[0] = 0;
        ColorMap.opacityNodes[1] = 1;
    }
    int vertexBuffer;
    final float[] vertexBufferData = new float[]{
            -1.0f, -1.0f, -1.0f, // triangle 1 : begin
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f, // triangle 1 : end
            1.0f, 1.0f, -1.0f, // triangle 1 : begin
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f, // triangle 1 : end
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f
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
                // Enable depth test
                gl.glEnable(GL_DEPTH_TEST);
                // Accept fragment if it closer to the camera than the former one
                gl.glDepthFunc(GL_LESS);
                program.init(gl);
                IntBuffer intBuffer = IntBuffer.allocate(1);
                gl.glGenBuffers(1, intBuffer);
                vertexBuffer = intBuffer.get(0);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
                gl.glBufferData(GL_ARRAY_BUFFER, vertexBufferData.length * Float.BYTES, Buffers.newDirectFloatBuffer(vertexBufferData), GL_STATIC_DRAW);
                program.allocateUniform(gl, "MVP", (gl2, loc) -> {
                    gl2.glUniformMatrix4fv(loc, 1, false, camera.getViewProjectionMatrix().get(Buffers.newDirectFloatBuffer(16)));
                });
                program.allocateUniform(gl, "iV", (gl2, loc) -> {
                    gl2.glUniformMatrix4fv(loc, 1, false, camera.getViewMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
                });
                program.allocateUniform(gl, "iP", (gl2, loc) -> {
                    gl2.glUniformMatrix4fv(loc, 1, false, camera.getProjectionMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
                });

                program.allocateUniform(gl, "viewSize", (gl2, loc) -> {
                    gl2.glUniform2f(loc, getCanvasWidth(), getCanvasHeight());
                });
                program.allocateUniform(gl, "depthSampleCount", (gl2, loc) -> {
                    gl2.glUniform1i(loc, 512);
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
                program.setUniforms(gl);
                colorMap.render(gl);
                volume.render(gl);
                // 1st attribute buffer : vertices
                gl.glEnableVertexAttribArray(0);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
                gl.glVertexAttribPointer(
                        0,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
                        3,                  // size
                        GL_FLOAT,           // type
                        false,           // normalized?
                        0,                  // stride
                        0            // array buffer offset
                );
                // Draw the triangle !
                gl.glDrawArrays(GL_TRIANGLES, 0, vertexBufferData.length / 3); // Starting from vertex 0; 3 vertices total -> 1 triangle
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
            }
        });
    }

    public Cube() throws IOException {

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
        final Cube cube = new Cube();

        cube.setVisible(true);

    }
}
