package netlogoparaguay.agents.Controls.controller; // GARANTA QUE ESTA LINHA ESTEJA CORRETA

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import netlogoparaguay.agents.Controls.Panel.Button;
import netlogoparaguay.agents.Controls.Panel.LabeledSlider;
import netlogoparaguay.agents.Controls.Panel.ToggleButton;
import netlogoparaguay.simulation.SimulationAppState;

public class ControlPanel extends Node {

    private final SimulationAppState simulation;
    private ToggleButton startStopBtn;
    private BitmapText speedLabel;
    private Button decreaseSpeedButtonInstance;
    private Button increaseSpeedButtonInstance;
    private BitmapText loopsValueText;

    public ControlPanel(Application app, SimulationAppState simulation) {
        super("MainControlPanel_UI");
        this.simulation = simulation;
        initializeUI(app);
    }

    private void updateLoopsDisplay() {
        if (loopsValueText != null) {
            loopsValueText.setText("Loops: " + simulation.getMaxLoopsSetting());
        }
    }

    private void initializeUI(Application app) {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        // Sliders para contagem de agentes
        LabeledSlider guaraniSlider = new LabeledSlider("Guaranis:", simulation.getGuaraniCountSetting(), 1, 20, 200, 50, font);
        guaraniSlider.setLocalTranslation(0, 0, 0); // Posição inicial no topo
        guaraniSlider.setValue(simulation.getGuaraniCountSetting());
        guaraniSlider.onChange(simulation::setGuaraniCount);
        attachChild(guaraniSlider);

        LabeledSlider jesuitSlider = new LabeledSlider("Jesuitas:", simulation.getJesuitCountSetting(), 1, 20, 200, 50, font);
        jesuitSlider.setLocalTranslation(0, -60, 0);
        jesuitSlider.setValue(simulation.getJesuitCountSetting());
        jesuitSlider.onChange(simulation::setJesuitCount);
        attachChild(jesuitSlider);

        // Controles de Loop
        float loopsControlY = -120f;
        float smallBtnWidth = 50f;
        float smallBtnHeight = 40f;

        Button decreaseLoopsBtn = new Button("-100", smallBtnWidth, smallBtnHeight, app.getAssetManager());
        decreaseLoopsBtn.setLocalTranslation(0, loopsControlY, 0);
        decreaseLoopsBtn.setOnClick(() -> {
            simulation.decreaseMaxLoops(100);
            updateLoopsDisplay();
        });
        attachChild(decreaseLoopsBtn);

        loopsValueText = new BitmapText(font);
        loopsValueText.setSize(font.getCharSet().getRenderedSize() * 0.7f);
        loopsValueText.setLocalTranslation(smallBtnWidth + 10, loopsControlY + smallBtnHeight - 15, 0);
        attachChild(loopsValueText);

        Button increaseLoopsBtn = new Button("+100", smallBtnWidth, smallBtnHeight, app.getAssetManager());
        increaseLoopsBtn.setLocalTranslation(smallBtnWidth + 115, loopsControlY, 0);
        increaseLoopsBtn.setOnClick(() -> {
            simulation.increaseMaxLoops(100);
            updateLoopsDisplay();
        });
        attachChild(increaseLoopsBtn);

        updateLoopsDisplay();

        // Botão de Reset
        Button resetBtn = new Button("Reset", 150, 50, app.getAssetManager());
        resetBtn.setName("ResetButton_UI");
        resetBtn.setLocalTranslation(0, -180, 0);
        resetBtn.setOnClick(() -> {
            simulation.resetSimulation();
            if (startStopBtn != null) {
                startStopBtn.setState(!simulation.isPaused());
            }
            updateSpeedLabelText();
            updateLoopsDisplay();
        });
        attachChild(resetBtn);

        // Controles de Velocidade
        float buttonWidthSmall = 70;
        float buttonHeightSmall = 50;
        float currentY = -240;

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

        // Botões para Adicionar/Remover Agentes
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

        currentY -= (buttonHeightSmall + 10);

        // Botão Start/Stop
        startStopBtn = new ToggleButton("Start", "Stop", 150, 50, app.getAssetManager());
        startStopBtn.setName("StartStopButton_UI");
        startStopBtn.setLocalTranslation(0, currentY, 0);
        startStopBtn.setOnToggle(isOn -> simulation.setPaused(!isOn));
        startStopBtn.setState(!simulation.isPaused());
        attachChild(startStopBtn);
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