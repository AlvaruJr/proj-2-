package netlogoparaguay.resources;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/**
 * Representa um recurso no mundo do jogo que pode ser coletado por agentes.
 */
public class Resource {

    private final ResourceType type;
    private final Spatial spatial;
    private boolean available;
    private final AssetManager assetManager;

    public Resource(ResourceType type, AssetManager assetManager, Vector3f initialPosition) {
        this.type = type;
        this.assetManager = assetManager;
        this.available = true;
        this.spatial = createSpatialModel();
        this.spatial.setLocalTranslation(initialPosition);
        this.spatial.setName("Resource_" + type.name() + "_" + System.currentTimeMillis() % 10000);
    }

    private Spatial createSpatialModel() {
        Geometry geom;
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        switch (type) {
            case WOOD:
                Box woodShape = new Box(0.3f, 0.3f, 0.3f);
                geom = new Geometry("WoodResourceModel", woodShape);
                mat.setColor("Color", ColorRGBA.Brown);
                break;
            case SOY:
                Sphere soyShape = new Sphere(16, 16, 0.25f);
                geom = new Geometry("SoyResourceModel", soyShape);
                mat.setColor("Color", ColorRGBA.Yellow);
                break;
            case MATE:
                Sphere mateShape = new Sphere(16, 16, 0.25f);
                geom = new Geometry("MateResourceModel", mateShape);
                mat.setColor("Color", ColorRGBA.Green.mult(0.5f));
                break;
            default:
                Box defaultShape = new Box(0.2f, 0.2f, 0.2f);
                geom = new Geometry("DefaultResourceModel", defaultShape);
                mat.setColor("Color", ColorRGBA.LightGray);
                break;
        }
        geom.setMaterial(mat);
        return geom;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void collect() {
        this.available = false;
    }

    public void spawnOnNode(Node parentNode) {
        if (spatial.getParent() == null) {
            parentNode.attachChild(spatial);
        }
        this.available = true;
    }

    public void removeFromNode() {
        if (spatial.getParent() != null) {
            spatial.getParent().detachChild(spatial);
        }
    }

    public void setPosition(Vector3f position) {
        if (spatial != null) {
            spatial.setLocalTranslation(position);
        }
    }

    public ResourceType getType() {
        return type;
    }

    public Spatial getSpatial() {
        return spatial;
    }

    public boolean isAvailable() {
        return available;
    }

    public Vector3f getPosition() {
        return spatial != null ? spatial.getLocalTranslation() : Vector3f.ZERO;
    }
}