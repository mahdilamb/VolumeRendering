package com.github.mahdilamb.vrii.test.arcball;

import org.joml.*;

import java.awt.event.MouseEvent;

import static org.joml.Math.*;

class CameraView {
    final Vector3f position;
    final Quaternionf rotation;
    final float zoom;

    public CameraView(final Vector3f position, final Quaternionf rotation, final float zoom) {
        this.position = position;
        this.rotation = rotation;
        this.zoom = zoom;
    }
}

public class Camera {
    final iRenderer renderer;

    final static float DEFAULT_ZOOM = -4f;
    final static Vector3f POSITION_ZERO = new Vector3f();
    private static double c = .5f / sin(.25f * PI);

    final static CameraView initial = new CameraView(POSITION_ZERO, new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), 30)
            .mul(
                    new Quaternionf().fromAxisAngleDeg(new Vector3f(1, 0, 0), 30)
            ), DEFAULT_ZOOM);
    final static CameraView front = new CameraView(POSITION_ZERO, new Quaternionf(0, 0, 0, 1), DEFAULT_ZOOM);
    final static CameraView left = new CameraView(POSITION_ZERO, new Quaternionf(0, -c, 0, c), DEFAULT_ZOOM);
    final static CameraView right = new CameraView(POSITION_ZERO, new Quaternionf(0, -c, 0, -c), DEFAULT_ZOOM);
    final static CameraView back = new CameraView(POSITION_ZERO, new Quaternionf(0, 1, 0, 0), DEFAULT_ZOOM);
    final static CameraView top = new CameraView(POSITION_ZERO, new Quaternionf(c, 0, 0, c), DEFAULT_ZOOM);
    final static CameraView bottom = new CameraView(POSITION_ZERO, new Quaternionf(c, 0, 0, -c), DEFAULT_ZOOM);

    final static float INITIAL_FOV = 45f;

    float zNear = .1f;
    float zFar = 1000f;

    float fov = INITIAL_FOV;

    final Vector3f position = new Vector3f();
    final Quaternionf rotation = new Quaternionf();
    private float zoom = -1;
    private final Vector2f prevMousePos = new Vector2f();

    //cache matrices
    Matrix4f currentViewMatrix = null;
    Matrix4f currentProjectionMatrix = null;
    Matrix4f currentViewProjectionMatrix = null;

    public Camera(iRenderer renderer, CameraView preset) {
        this.renderer = renderer;
        setFromPreset(preset);
        if (renderer != null) {
            updateCameraTransform();
        }
    }

    public Camera() {
        this(null);
    }

    public Camera(iRenderer renderer) {
        this(renderer, initial);
    }


    public Camera(iRenderer renderer, Vector3f eye, Vector3f viewCenter, Vector3f upDir) {
        this(renderer, presetFromLookAt(eye, viewCenter, upDir));
    }

    public static CameraView presetFromLookAt(Vector3f eye, Vector3f center, Vector3f up) {
        if (up.y() == 1 && eye.get(1) == 0) {
            eye.y += 1.0e-10f;
        }
        final Vector3f dir = new Vector3f(center)
                .sub(eye);
        final Vector3f zAxis = new Vector3f(dir)
                .normalize();

        final Vector3f xAxis = new Vector3f(zAxis)
                .cross(new Vector3f(up).normalize())
                .normalize();
        final Vector3f yAxis = new Vector3f(xAxis)
                .cross(zAxis)
                .normalize();
        xAxis.set(
                new Vector3f(zAxis)
                        .cross(yAxis)
        ).normalize();
        return new CameraView(
                new Vector3f(center)
                        .negate(),
                new Quaternionf()
                        .setFromNormalized(
                                new Matrix3f(
                                        xAxis,
                                        yAxis,
                                        zAxis
                                )
                                        .transpose()
                        ).normalize(),
                -dir.length()
        );

    }

    public Matrix4f getViewMatrix() {
        if (currentViewMatrix == null) {
            currentViewMatrix = new Matrix4f()
                    .setTranslation(
                            new Vector3f(0, 0, zoom)
                    )
                    .rotate(rotation)
                    .translate(position);
        }

        return new Matrix4f(currentViewMatrix);
    }

    public Matrix4f getProjectionMatrix() {
        if (currentProjectionMatrix == null) {
            currentProjectionMatrix = new Matrix4f()
                    .setPerspective(getFieldOfView(), ((float) renderer.getCanvasWidth()) / renderer.getCanvasHeight(), zNear, zFar);
        }
        return new Matrix4f(currentProjectionMatrix);
    }

    public Matrix4f getViewProjectionMatrix() {
        if (currentViewProjectionMatrix == null) {
            currentViewProjectionMatrix = new Matrix4f(getProjectionMatrix())
                    .mul(getViewMatrix());
        }
        return new Matrix4f(currentViewProjectionMatrix);
    }

    public Vector3f getRayOrigin() {
        final Vector4f invView = new Vector4f(0, 0, 0, 1)
                .mul(new Matrix4f(
                        getViewMatrix()
                )
                        .invert());
        //invView.div(invView.w);
        return new Vector3f(invView.x(), invView.y(), invView.z());
    }


    public float getFieldOfView() {
        return fov;
    }

    public float getFocalLength() {
        return 1.0f / tan(toRadians(getFieldOfView()) / 2f);
    }

    public void init(MouseEvent e) {
        prevMousePos.x = e.getX();
        prevMousePos.y = e.getY();
        updateCameraTransform();
    }

    public void zoom(float delta) {
        if (zoom + delta >= 0) {
            return;
        }
        zoom += delta;
        updateCameraTransform();
    }

    public void translate(MouseEvent e) {
        final Vector2f translationNDC = new Vector2f(screenCoordToNDC(e))
                .sub(screenCoordToNDC(prevMousePos));
        prevMousePos.x = e.getX();
        prevMousePos.y = e.getY();
        final float hh = abs(zoom) * tan(getFieldOfView() * 0.5f);
        final float hw = hh * renderer.getAspectRatio();

        final Vector4f invTransform = new Matrix4f(getViewMatrix())
                .invert()
                .transform(
                        new Vector4f(translationNDC.x() * hw, translationNDC.y() * hh, 0.0f, 0)
                );

        position.x += invTransform.x;
        position.y += invTransform.y;
        position.z += invTransform.z;
        updateCameraTransform();
    }

    public void rotate(MouseEvent e) {
        final Vector2i dimensions = new Vector2i(renderer.getCanvasWidth(), renderer.getCanvasHeight());
        rotation.set(new Quaternionf(ndcToArcBall(boundlessScreenCoordToNDC(e, dimensions)))

                .mul(ndcToArcBall(boundlessScreenCoordToNDC(prevMousePos, dimensions)))
                .mul(rotation)
        )
                .normalize();

        prevMousePos.x = e.getX();
        prevMousePos.y = e.getY();
        updateCameraTransform();
    }

    public void updateCameraTransform() {
        currentViewMatrix = null;
        currentProjectionMatrix = null;
        currentViewProjectionMatrix = null;
        if(renderer!=null){
            renderer.redraw();

        }
    }

    private Vector2f screenCoordToNDC(MouseEvent e) {
        return screenCoordToNDC(new Vector2f(e.getX(), e.getY()));
    }

    private Vector2f boundlessScreenCoordToNDC(MouseEvent e, Vector2i dimensions) {
        return boundlessScreenCoordToNDC(new Vector2f(e.getX(), e.getY()), dimensions);
    }

    private Vector2f boundlessScreenCoordToNDC(Vector2f mousePos, Vector2i dimensions) {
        final Vector2f mod = new Vector2f(mousePos);
        mod.x = mod.x() % (dimensions.x());
        mod.y = mod.y() % (dimensions.y());
        if (mousePos.x < 0) {
            mod.x = dimensions.x - abs(mod.x);
        }
        if (mousePos.y < 0) {
            mod.y = dimensions.y - abs(mod.y);
        }
        return screenCoordToNDC(mod);
    }

    private Vector2f screenCoordToNDC(Vector2f mousePos) {

        return new Vector2f(mousePos.x() * 2.0f / renderer.getCanvasWidth() - 1.0f,
                1.0f - 2.0f * mousePos.y() / renderer.getCanvasHeight());
    }


    public static Quaternionf ndcToArcBall(Vector2f p) {
        final float dist = p.dot(p);
        /* Point is on sphere */
        if (dist <= 1.0f) {
            return new Quaternionf(p.x(), p.y(), sqrt(1.0f - dist), 0.0f);
            /* Point is outside sphere */
        } else {
            final Vector2f proj = new Vector2f(p)
                    .normalize();
            return new Quaternionf(proj.x(), proj.y(), 0.0f, 0.0f);
        }
    }

    @Override
    public String toString() {
        return String.format("ArcBallCamera (position: %s, rotation: %s, zoom: %.3f)", position, rotation, zoom);
    }

    public void set(final Vector3f position, final Quaternionf rotation, final float zoom) {
        this.rotation.set(rotation);
        this.zoom = zoom;
        this.position.set(position);
        updateCameraTransform();
    }

    private void setFromPreset(CameraView preset) {
        set(preset.position, preset.rotation, preset.zoom);
    }

}
