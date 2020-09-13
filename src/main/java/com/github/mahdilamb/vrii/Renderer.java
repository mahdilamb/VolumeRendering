package com.github.mahdilamb.vrii;


import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL3ES3.*;

public abstract class Renderer {

    private static final GLProfile profile = GLProfile.getDefault();
    private static final GLCapabilities capabilities = new GLCapabilities(profile);
    private static final GLCanvas canvas = new GLCanvas(capabilities);
    protected final Camera camera = new Camera(this);
    final Controls controls = new Controls(this);
    protected Volume volume;
    protected ColorMap colorMap;
    public static int sampleCount = 512;
    public final int[] workGroupCount = new int[3];
    public final int[] maxWorkGroupSize = new int[3];
    public final int[] workGroupSize = new int[3];

    public int workGroupInvocations;
    public Renderer(final Volume volume, final ColorMap colorMap) {

        this.colorMap = colorMap;
        this.volume = volume;

        canvas.setSize(800, 640);

        canvas.addMouseWheelListener(controls);
        canvas.addMouseMotionListener(controls);
        canvas.addMouseListener(controls);
        canvas.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {
                final GL2 gl = drawable.getGL().getGL2();
                System.out.println(gl.glGetString(GL_RENDERER));
                System.out.println(gl.glGetString(GL_VENDOR));
                System.out.println(gl.glGetString(GL_VERSION));
                System.out.println(gl.glGetString(GL_EXTENSIONS));
                final IntBuffer intBuffer = IntBuffer.allocate(1);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, intBuffer);
                workGroupCount[0] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1, intBuffer);
                workGroupCount[1] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2, intBuffer);
                workGroupCount[2] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, intBuffer);
                maxWorkGroupSize[0] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1, intBuffer);
                maxWorkGroupSize[1] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2, intBuffer);
                maxWorkGroupSize[2] = intBuffer.get(0);
                gl.glGetIntegerv(GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, intBuffer);
                workGroupInvocations = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_COMPUTE_WORK_GROUP_SIZE, 0, intBuffer);
                workGroupSize[0] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_COMPUTE_WORK_GROUP_SIZE, 1, intBuffer);
                workGroupSize[1] = intBuffer.get(0);
                gl.glGetIntegeri_v(GL_COMPUTE_WORK_GROUP_SIZE, 2, intBuffer);
                workGroupSize[2] = intBuffer.get(0);
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {

            }

            @Override
            public void display(GLAutoDrawable drawable) {

            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

            }
        });
    }
    public Renderer() throws IOException {
        this(new Volume(new MosaicVolumeSource(
                        "Brain - Water",
                        new File("resources\\volumes\\sagittal.png"),
                        2,
                        176,
                        .7f
                )),
                new ColorMap( new File("resources\\colorMappings\\colors1.png"))
        );


    }
    public void redraw() {
        canvas.display();
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


    public synchronized void setVolume(MosaicVolumeSource volume) throws IOException {
        setVolume(new VolumeWithCube(volume));

    }

    public void setVolume(VolumeWithCube volume) {
        this.volume = volume;
        this.volume.hasChanges = true;
        redraw();
    }

    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
        colorMap.hasChanges = true;
        redraw();
    }

    public void setOpacityMin(float i) {
        ColorMap.opacityNodes[0] = i;
        colorMap.hasChanges = true;

        colorMap.update(null);
        redraw();

    }


    public void setOpacityMax(float i) {
        ColorMap.opacityNodes[1] = i;
        ColorMap.hasChanges = true;

        colorMap.update(null);
        redraw();
    }

    public void setSampleCount(int value) {
        sampleCount = value;
        redraw();
    }


    public Camera getCamera() {
        return camera;
    }
}
