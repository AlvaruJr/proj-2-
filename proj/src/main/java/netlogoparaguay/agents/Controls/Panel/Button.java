package netlogoparaguay.agents.Controls.Panel;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.material.Material;

public class Button extends Node {
    private final Geometry background;
    private final BitmapText text;
    private Runnable onClick;

    public Button(String label, float width, float height, AssetManager assetManager) {
        // Create button background
        Quad quad = new Quad(width, height);
        background = new Geometry("ButtonBackground", quad);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Gray);
        background.setMaterial(mat);
        this.attachChild(background);

        // Create button text
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        text = new BitmapText(font, false);
        text.setText(label);
        text.setSize(height * 0.5f);
        text.setColor(ColorRGBA.White);
        text.setLocalTranslation(
                width * 0.5f - text.getLineWidth() * 0.5f,
                height * 0.5f + text.getLineHeight() * 0.25f,
                0
        );
        this.attachChild(text);
    }

    public void setOnClick(Runnable action) {
        this.onClick = action;
    }

    public void triggerClick() {
        if (onClick != null) {
            onClick.run();
        }
    }

    public void setColor(ColorRGBA color) {
        background.getMaterial().setColor("Color", color);
    }
}