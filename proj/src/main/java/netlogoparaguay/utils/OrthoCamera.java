package netlogoparaguay.utils;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class OrthoCamera {
    private final SimpleApplication app;
    private static final float WORLD_WIDTH = 30f;
    private static final float WORLD_HEIGHT = 30f;

    public OrthoCamera(SimpleApplication app) {
        this.app = app;
    }

    public void setupPure2DView() {
        Camera cam = app.getCamera();
        cam.setParallelProjection(true);
        cam.setFrustum(0, WORLD_WIDTH, 0, WORLD_HEIGHT, -10, 10);
        cam.setLocation(new Vector3f(WORLD_WIDTH/2, WORLD_HEIGHT/2, 5));
        cam.lookAt(new Vector3f(WORLD_WIDTH/2, WORLD_HEIGHT/2, 0), Vector3f.UNIT_Y);
        app.getFlyByCamera().setEnabled(false);
    }
}