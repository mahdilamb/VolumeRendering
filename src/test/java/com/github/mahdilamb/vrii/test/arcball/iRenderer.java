package com.github.mahdilamb.vrii.test.arcball;


public interface iRenderer {


    Camera getCamera();
    void redraw();

    int getCanvasWidth();

    int getCanvasHeight();

    default float getAspectRatio(){
        return ((float) getCanvasWidth()) / getCanvasHeight();
    }
}
