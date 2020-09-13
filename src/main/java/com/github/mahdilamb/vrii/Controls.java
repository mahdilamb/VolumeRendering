package com.github.mahdilamb.vrii;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.event.*;


public class Controls implements MouseListener, MouseWheelListener, MouseMotionListener {
    float zoom = .8f;
    float startAngle = 0;
    final Vector2f angle = new Vector2f(.9f, -.25f);
    final Vector3f translation = new Vector3f();
    int mouseButton;
    final Vector2f moveStart = new Vector2f();
    final Vector2f last = new Vector2f();
    final Vector2f pan = new Vector2f();
    public Matrix4f transformMatrix;
    public Controls(){
        updateTransformation();
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        mouseButton = e.getButton();
        moveStart.x = e.getX();
        moveStart.y = e.getY();
    }

    public void mouseReleased(MouseEvent e) {
        mouseButton = 0;
        moveStart.zero();
        last.zero();
        pan.zero();

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        if(mouseButton == MouseEvent.BUTTON1 || mouseButton == MouseEvent.BUTTON3){
            final float deltaX = e.getX() - moveStart.x();
            final float deltaY = e.getY() - moveStart.y();
            if(mouseButton == MouseEvent.BUTTON1){
                pan.x += deltaX-last.x;
                pan.y+= deltaY-last.y;
            }else if(mouseButton == MouseEvent.BUTTON3){
                translation.x -= (deltaX -last.x)*.003;
                translation.y -= (deltaY -last.y)*.003;
            }
            last.x = deltaX;
            last.y = deltaY;
        }
        updateTransformation();
    }

    public void mouseMoved(MouseEvent e) {

    }


    public void mouseWheelMoved(MouseWheelEvent e) {
        zoom += zoom * .001 * e.getPreciseWheelRotation();
        updateTransformation();
    }

    private void updateTransformation() {
        angle.x -= pan.x * .0025;
        pan.x = 0;
        float cx = (float) Math.cos(angle.x);
        float sx = (float) Math.sin(angle.x);

        final Matrix4f xRotationMatrix = new Matrix4f(
                cx, 0, sx, 0,
                0, 1, 0, 0,
                -sx, 0, cx, 0,
                0, 0, 0, 1
        );

        angle.y -= pan.y * 0.0025;
        pan.y = 0;
        float c = (float) Math.cos(angle.y);
        float s = (float) Math.sin(angle.y);

        final Matrix4f rotationMatrix = new Matrix4f(
                1, 0, 0, 0,
                0, c, -s, 0,
                0, s, c, 0,
                0, 0, 0, 1
        )
                .mul(
                        xRotationMatrix
                );

        final Matrix4f translationMatrix = new Matrix4f()
                .translate(translation);

        final Matrix4f scaleMatrix = new Matrix4f()
                .scale(zoom);
        transformMatrix = translationMatrix
                .mul(rotationMatrix)
                .mul(scaleMatrix);
        Renderer.redraw();
    }


}
