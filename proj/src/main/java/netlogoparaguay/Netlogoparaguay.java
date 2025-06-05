package netlogoparaguay;

import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

import netlogoparaguay.simulation.SimulationAppState;
import netlogoparaguay.simulation.SimulationAppStates;
import netlogoparaguay.agents.Controls.controller.ControlPanel;
import netlogoparaguay.agents.Controls.Panel.Button;
import netlogoparaguay.agents.Controls.Panel.StatsPanel;
import netlogoparaguay.agents.Controls.Panel.StatsUpdater;

public class Netlogoparaguay extends SimpleApplication implements ActionListener {

    private SimulationAppState uiAppState;
    private SimulationAppStates simulationEngine;
    public static final String MAPPING_UI_CLICK = "UIClick";

    public static void main(String[] args) {
        Netlogoparaguay app = new Netlogoparaguay();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Simulação NetLogo Paraguai");
        settings.setResolution(1280, 720);
        settings.setSamples(4);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        if (flyCam != null) {
            flyCam.setDragToRotate(false);
            flyCam.setEnabled(false);
            System.out.println("FlyCam desabilitada (cursor visível).");
        }
        inputManager.setCursorVisible(true);

        cam.setLocation(new Vector3f(0f, 0f, 45f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        AmbientLight ambient = new AmbientLight(new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        rootNode.addLight(ambient);
        DirectionalLight sun = new DirectionalLight(new Vector3f(-0.5f, -0.8f, -0.4f).normalizeLocal(), new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
        rootNode.addLight(sun);

        uiAppState = new SimulationAppState();
        simulationEngine = new SimulationAppStates();
        uiAppState.setSimulationEngineReference(simulationEngine);
        simulationEngine.setUiAppStateReference(uiAppState);

        ControlPanel controlPanelUI = new ControlPanel(this, uiAppState);
        float cpMargin = 20f;
        controlPanelUI.setLocalTranslation(cpMargin, cam.getHeight() - cpMargin, 0);
        guiNode.attachChild(controlPanelUI);

        StatsPanel statsPanel = new StatsPanel(this);
        float statsPanelWidthEst = 300f;
        float topMargin = 20f;
        float desiredRightMargin = 20f;
        float statsPanelX = cam.getWidth() - statsPanelWidthEst - desiredRightMargin;
        float statsPanelY = cam.getHeight() - topMargin;
        statsPanel.setLocalTranslation(statsPanelX, statsPanelY, 0);
        guiNode.attachChild(statsPanel);

        StatsUpdater statsUpdater = new StatsUpdater(statsPanel, uiAppState);

        initKeys();

        stateManager.attach(uiAppState);
        stateManager.attach(simulationEngine);
        stateManager.attach(statsUpdater);

        setDisplayStatView(false);
        setDisplayFps(false);
    }

    private void initKeys() {
        inputManager.addMapping(MAPPING_UI_CLICK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, MAPPING_UI_CLICK);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(MAPPING_UI_CLICK) && !isPressed) {
            Vector2f click2d = inputManager.getCursorPosition();
            Node controlPanel = (Node) guiNode.getChild("MainControlPanel_UI");

            if (controlPanel != null) {
                for (Spatial uiElement : controlPanel.getChildren()) {
                    if (uiElement instanceof Button) {
                        Button button = (Button) uiElement;

                        // [CORRIGIDO] Usa getWorldTranslation() para obter a posição final na tela.
                        Vector3f buttonPos = button.getWorldTranslation();
                        float buttonWidth = button.getButtonWidth();
                        float buttonHeight = button.getButtonHeight();

                        if (click2d.x >= buttonPos.x && click2d.x <= (buttonPos.x + buttonWidth) &&
                                click2d.y >= buttonPos.y && click2d.y <= (buttonPos.y + buttonHeight)) {

                            System.out.println("CLIQUE DETECTADO E PROCESSADO EM: " + button.getName());
                            button.triggerClick();
                            break;
                        }
                    }
                }
            }
        }
    }
}