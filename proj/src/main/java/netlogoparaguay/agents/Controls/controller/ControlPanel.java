// Local do arquivo: src/main/java/netlogoparaguay/agents/Controls/controller/ControlPanel.java

package netlogoparaguay.agents.Controls.controller;

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import netlogoparaguay.agents.Controls.Panel.Button;
import netlogoparaguay.agents.Controls.Panel.LabeledSlider;
import netlogoparaguay.agents.Controls.Panel.ToggleButton;
import netlogoparaguay.simulation.SimulationAppState;

// [ALTERADO] Nome da classe para seguir a convenção Java (iniciar com maiúscula).
public class ControlPanel extends Node {

    private final SimulationAppState simulation;
    private ToggleButton startStopBtn;
    private BitmapText speedLabel;
    private Button decreaseSpeedButtonInstance;
    private Button increaseSpeedButtonInstance;

    public ControlPanel(Application app, SimulationAppState simulation) {
        super("MainControlPanel_UI");
        this.simulation = simulation;
        initializeUI(app);
    }

    private void initializeUI(Application app) {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        startStopBtn = new ToggleButton("Start", "Stop", 150, 50, app.getAssetManager());
        startStopBtn.setName("StartStopButton_UI");
        startStopBtn.setLocalTranslation(0, 0, 0);
        startStopBtn.setOnToggle(isOn -> simulation.setPaused(!isOn));
        startStopBtn.setState(!simulation.isPaused()); // Define o estado visual inicial correto
        attachChild(startStopBtn);

        LabeledSlider guaraniSlider = new LabeledSlider("Guaranis:", simulation.getGuaraniCountSetting(), 1, 20, 200, 50, font);
        guaraniSlider.setLocalTranslation(0, -60, 0);
        guaraniSlider.setValue(simulation.getGuaraniCountSetting());
        guaraniSlider.onChange(simulation::setGuaraniCount);
        attachChild(guaraniSlider);

        LabeledSlider jesuitSlider = new LabeledSlider("Jesuitas:", simulation.getJesuitCountSetting(), 1, 20, 200, 50, font);
        jesuitSlider.setLocalTranslation(0, -120, 0);
        jesuitSlider.setValue(simulation.getJesuitCountSetting());
        jesuitSlider.onChange(simulation::setJesuitCount);
        attachChild(jesuitSlider);

        LabeledSlider loopSlider = new LabeledSlider("Loops:", simulation.getMaxLoopsSetting(), 1, 1000, 200, 50, font);
        loopSlider.setLocalTranslation(0, -180, 0);
        loopSlider.setValue(simulation.getMaxLoopsSetting());
        loopSlider.onChange(simulation::setMaxLoops);
        attachChild(loopSlider);

        Button resetBtn = new Button("Reset", 150, 50, app.getAssetManager());
        resetBtn.setName("ResetButton_UI");
        resetBtn.setLocalTranslation(0, -240, 0);
        resetBtn.setOnClick(() -> {
            simulation.resetSimulation();
            startStopBtn.setState(!simulation.isPaused());
            updateSpeedLabelText();
        });
        attachChild(resetBtn);

        float buttonWidthSmall = 70;
        float buttonHeightSmall = 50;
        float currentY = -300;

        speedLabel = new BitmapText(font);
        speedLabel.setSize(font.getCharSet().getRenderedSize() * 0.6f);

        decreaseSpeedButtonInstance = new Button("- Vel", buttonWidthSmall, buttonHeightSmall, app.getAssetManager());
        decreaseSpeedButtonInstance.setLocalTranslation(0, currentY, 0);
        decreaseSpeedButtonInstance.setOnClick(() -> {
            simulation.decreaseSimulationSpeed();
            updateSpeedLabelText();
        });
        attachChild(decreaseSpeedButtonInstance);

        speedLabel.setLocalTranslation(
                decreaseSpeedButtonInstance.getLocalTranslation().x + buttonWidthSmall + 5,
                currentY + buttonHeightSmall / 2f - speedLabel.getLineHeight() / 2f,
                0
        );
        attachChild(speedLabel);

        increaseSpeedButtonInstance = new Button("+ Vel", buttonWidthSmall, buttonHeightSmall, app.getAssetManager());
        increaseSpeedButtonInstance.setLocalTranslation(
                speedLabel.getLocalTranslation().x + speedLabel.getLineWidth() + 5,
                currentY,
                0
        );
        increaseSpeedButtonInstance.setOnClick(() -> {
            simulation.increaseSimulationSpeed();
            updateSpeedLabelText();
        });
        attachChild(increaseSpeedButtonInstance);
        updateSpeedLabelText();

        currentY -= (buttonHeightSmall + 10);

        Button addGuaraniButton = new Button("+G", buttonWidthSmall, buttonHeightSmall, app.getAssetManager());
        addGuaraniButton.setLocalTranslation(0, currentY, 0);
        addGuaraniButton.setOnClick(simulation::requestAddGuarani);
        attachChild(addGuaraniButton);

        Button removeGuaraniButton = new Button("-G", buttonWidthSmall, buttonHeightSmall, app.getAssetManager());
        removeGuaraniButton.setLocalTranslation(buttonWidthSmall + 5, currentY, 0);
        removeGuaraniButton.setOnClick(simulation::requestRemoveGuarani);
        attachChild(removeGuaraniButton);

        float offsetForJesuitButtons = (buttonWidthSmall + 5) * 2 + 20;
        Button addJesuitButton = new Button("+J", buttonWidthSmall, buttonHeightSmall, app.getAssetManager());
        addJesuitButton.setLocalTranslation(offsetForJesuitButtons, currentY, 0);
        addJesuitButton.setOnClick(simulation::requestAddJesuit);
        attachChild(addJesuitButton);

        Button removeJesuitButton = new Button("-J", buttonWidthSmall, buttonHeightSmall, app.getAssetManager());
        removeJesuitButton.setLocalTranslation(offsetForJesuitButtons + buttonWidthSmall + 5, currentY, 0);
        removeJesuitButton.setOnClick(simulation::requestRemoveJesuit);
        attachChild(removeJesuitButton);
    }

    private void updateSpeedLabelText() {
        if (speedLabel != null && simulation != null) {
            String newText = String.format("%.1fx", simulation.getSimulationSpeed());
            speedLabel.setText(newText);

            if (increaseSpeedButtonInstance != null && decreaseSpeedButtonInstance != null && speedLabel.getParent() != null) {
                speedLabel.setLocalTranslation(
                        decreaseSpeedButtonInstance.getLocalTranslation().x + decreaseSpeedButtonInstance.getButtonWidth() + 5,
                        decreaseSpeedButtonInstance.getLocalTranslation().y + decreaseSpeedButtonInstance.getButtonHeight() / 2f - speedLabel.getLineHeight() / 2f,
                        0
                );
                increaseSpeedButtonInstance.setLocalTranslation(
                        speedLabel.getLocalTranslation().x + speedLabel.getLineWidth() + 5,
                        decreaseSpeedButtonInstance.getLocalTranslation().y,
                        0
                );
            }
        }
    }
}