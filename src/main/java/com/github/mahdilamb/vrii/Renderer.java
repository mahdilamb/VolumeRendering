package com.github.mahdilamb.vrii;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;

public class Renderer {

    private static final GLProfile profile = GLProfile.getDefault();
    private static final GLCapabilities capabilities = new GLCapabilities(profile);
    private static final GLCanvas canvas = new GLCanvas(capabilities);
    private static final Controls controls = new Controls();
    static Program program = Program.maxIntensity;
    static VolumeWithCube volume;
    static ColorMap colorMap;
    private static int sampleCount = 512;
    private static float brightness = 1;

    static {
        try {
            colorMap = new ColorMap(new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\colorMappings\\colors1.png"));
            volume = new VolumeWithCube(new MosaicVolumeSource(
                    "Brain - Water",
                    new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\volumes\\sagittal.png"),
                    2,
                    176,
                    .7f
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        canvas.setSize(800, 640);
        canvas.addGLEventListener(new GLEventListener() {
            public void init(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                gl.glClearColor(0, 0, 0.2f, 1);
                IntBuffer intBuffer = IntBuffer.allocate(1);

                program.use(gl);
                program.allocateUniform(gl, "zScale", (gl2, loc) -> {
                    gl.glUniform1f(loc, volume.getScale(2)
                    );
                });
                program.allocateUniform(gl, "aspect", (gl2, loc) -> {
                    gl.glUniform1f(loc, getAspectRatio()
                    );
                });
                program.allocateUniform(gl, "depthSampleCount", (gl2, loc) -> {
                    gl.glUniform1i(loc, sampleCount
                    );
                });

                program.allocateUniform(gl, "tex", (gl2, loc) -> {
                    gl.glUniform1i(loc, 0);
                });
                program.allocateUniform(gl, "colorMap", (gl2, loc) -> {
                    gl.glUniform1i(loc, 1);
                });

                program.allocateUniform(gl, "transform", (gl2, loc) -> {
                    gl.glUniformMatrix4fv(loc, 1, false, controls.transformMatrix.get(Buffers.newDirectFloatBuffer(16)));
                });

                colorMap.init(gl);

                volume.init(gl);

            }

            public void display(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                gl.glEnable(GL_BLEND);
                gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                program.use(gl);
                colorMap.render(gl);
                program.setUniforms(gl);
                volume.render(gl);
                program.unUse(gl);
            }

            public void dispose(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                Program.destroyAllPrograms(gl);
                Texture.destroyAllTextures(gl);
            }

            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                drawable.getGL().glViewport(x, y, width, height);
            }
        });
        canvas.addMouseWheelListener(controls);
        canvas.addMouseMotionListener(controls);
        canvas.addMouseListener(controls);
    }

    private Renderer() {
    }

    public static void redraw() {
        Renderer.canvas.display();
    }


    public static GLCanvas getCanvas() {
        return canvas;
    }

    public static void setCanvasSize(int width, int height) {
        canvas.setSize(width, height);
    }

    public static int getWidth() {
        return canvas.getSurfaceWidth();
    }

    public static int getHeight() {
        return canvas.getSurfaceHeight();
    }

    public static float getAspectRatio() {
        return ((float) getWidth()) / getHeight();
    }


    public static void setShader(Program program) {
        Renderer.program = program;
        redraw();
    }


    public static synchronized void setVolume(MosaicVolumeSource volume) throws IOException {
        setVolume(new VolumeWithCube(volume));

    }

    public static void setVolume(VolumeWithCube volume) {
        Renderer.volume = volume;
        Renderer.volume.hasChanges = true;
        redraw();
    }

    public static void setColorMap(ColorMap colorMap) {
        Renderer.colorMap = colorMap;
        ColorMap.hasChanges = true;
        redraw();
    }

    public static void setOpacityMin(float i) {
        ColorMap.opacityNodes[0] = i;
        ColorMap.hasChanges = true;

        ColorMap.update(null);
        redraw();

    }


    public static void setOpacityMax(float i) {
        ColorMap.opacityNodes[1] = i;
        ColorMap.hasChanges = true;

        ColorMap.update(null);
        redraw();
    }

    public static void setSampleCount(int value) {
        Renderer.sampleCount = value;
        redraw();
    }


}
