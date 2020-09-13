package com.github.mahdilamb.vrii;


import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import java.io.File;
import java.io.IOException;

public abstract class Renderer {

    private static final GLProfile profile = GLProfile.getDefault();
    private static final GLCapabilities capabilities = new GLCapabilities(profile);
    private static final GLCanvas canvas = new GLCanvas(capabilities);
    protected final Camera camera = new Camera(this);
    final Controls controls = new Controls(this);
    protected Volume volume;
    protected ColorMap colorMap;
    public static int sampleCount = 512;

    public Renderer(final Volume volume, final ColorMap colorMap) {

        this.colorMap = colorMap;
        this.volume = volume;

        canvas.setSize(800, 640);

        canvas.addMouseWheelListener(controls);
        canvas.addMouseMotionListener(controls);
        canvas.addMouseListener(controls);
    }
    public Renderer() throws IOException {
        this(new Volume(new MosaicVolumeSource(
                        "Brain - Water",
                        new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\volumes\\sagittal.png"),
                        2,
                        176,
                        .7f
                )),
                new ColorMap( new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\colorMappings\\colors1.png"))
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
