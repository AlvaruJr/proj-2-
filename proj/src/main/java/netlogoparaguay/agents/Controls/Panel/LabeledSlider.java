package netlogoparaguay.agents.Controls.Panel;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

public class LabeledSlider extends Node {
    private final BitmapText labelText;
    private final BitmapText valueText;
    private int value;
    private final int min;
    private final int max;
    private SliderChangeListener listener;

    public interface SliderChangeListener {
        void onChange(int newValue);
    }

    public LabeledSlider(String label, int initialValue, int min, int max, float width, float height, BitmapFont font) {
        this.min = min;
        this.max = max;
        this.value = initialValue;

        // Create label
        labelText = new BitmapText(font, false);
        labelText.setText(label);
        labelText.setSize(height * 0.4f);
        labelText.setLocalTranslation(0, height * 0.7f, 0);
        attachChild(labelText);

        // Create value display
        valueText = new BitmapText(font, false);
        valueText.setSize(height * 0.4f);
        valueText.setLocalTranslation(width - 30, height * 0.7f, 0);
        updateValueText();
        attachChild(valueText);
    }

    public void setValue(int value) {
        this.value = Math.min(max, Math.max(min, value));
        updateValueText();
    }

    public int getValue() {
        return value;
    }

    public void onChange(SliderChangeListener listener) {
        this.listener = listener;
    }

    private void updateValueText() {
        valueText.setText(String.valueOf(value));
        if (listener != null) {
            listener.onChange(value);
        }
    }
}