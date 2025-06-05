package netlogoparaguay.agents.Controls.Panel;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import java.util.function.Consumer;

public class ToggleButton extends Button {
    private boolean isOn;
    private final String onText;
    private final String offText;
    private Consumer<Boolean> toggleCallback;

    public ToggleButton(String offText, String onText, float width, float height, AssetManager assetManager) {
        super(offText, width, height, assetManager);
        this.onText = onText;
        this.offText = offText;
        this.isOn = false;
    }

    public void setOnToggle(Consumer<Boolean> callback) {
        this.toggleCallback = callback;
        super.setOnClick(() -> {
            this.isOn = !this.isOn;
            updateAppearance();
            if (toggleCallback != null) {
                toggleCallback.accept(isOn);
            }
        });
    }

    private void updateAppearance() {
        ((BitmapText)getChild(1)).setText(isOn ? onText : offText);
        setColor(isOn ? ColorRGBA.Green : ColorRGBA.Red);
    }
}