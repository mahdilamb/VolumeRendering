package com.github.mahdilamb.vrii.test.arcball;

import java.awt.event.*;

import static org.joml.Math.abs;

public class Controls implements MouseMotionListener, MouseWheelListener, MouseListener {
    private final iRenderer renderer;
    int mouseButton = 0;
    
    public Controls(iRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseButton == MouseEvent.BUTTON1) {
            renderer.getCamera().rotate(e);

        } else if (mouseButton == MouseEvent.BUTTON3) {
            renderer.getCamera().translate(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        float delta = (float) e.getPreciseWheelRotation();
        if (abs(delta) < 1e-2f) {
            return;
        }
        renderer.getCamera().zoom(-delta);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        mouseButton = e.getButton();

        renderer.getCamera().init(e);

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseButton = 0;

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
