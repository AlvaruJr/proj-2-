package netlogoparaguay.agents.Controls.controller; // Ou netlogoparaguay. se esta for a localização correta

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.scene.Node;
// Certifique-se de que estes imports para Button, LabeledSlider, ToggleButton
// correspondem à localização real dessas classes no seu projeto.
// Se elas estiverem em netlogoparaguay.agents.Controls.Panel, está correto.
import netlogoparaguay.agents.Controls.Panel.Button;
import netlogoparaguay.agents.Controls.Panel.LabeledSlider;
import netlogoparaguay.agents.Controls.Panel.ToggleButton;
import netlogoparaguay.simulation.SimulationAppState; // Importa a classe correta para o estado da UI

public class controlPanel extends Node { // Considere renomear para ControlPanel (com 'C' maiúsculo) por convenção
    private final SimulationAppState simulation;

    public controlPanel(Application app, SimulationAppState simulation) {
        super("controlPanelNode"); // É uma boa prática dar nomes únicos aos Nodes
        this.simulation = simulation;
        initializeUI(app);
    }

    private void initializeUI(Application app) {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        // Start/Stop Button
        ToggleButton startStopBtn = new ToggleButton("Start", "Stop", 150, 50, app.getAssetManager());
        startStopBtn.setLocalTranslation(0, 0, 0); // Posição relativa ao nó pai (controlPanelNode)
        startStopBtn.setOnToggle(isOn -> simulation.setPaused(!isOn));
        attachChild(startStopBtn);

        // Guarani Slider
        // CORREÇÃO: Usando getGuaraniCountSetting()
        LabeledSlider guaraniSlider = new LabeledSlider("Guaranis:", simulation.getGuaraniCountSetting(), 1, 20, 200, 50, font);
        guaraniSlider.setLocalTranslation(0, -60, 0);
        // A chamada simulation.setGuaraniCount(value) no onChange já estava correta,
        // pois setGuaraniCount é o método setter em SimulationAppState.
        guaraniSlider.onChange(value -> simulation.setGuaraniCount(value));
        attachChild(guaraniSlider);

        // Jesuit Slider
        // CORREÇÃO: Usando getJesuitCountSetting()
        LabeledSlider jesuitSlider = new LabeledSlider("Jesuitas:", simulation.getJesuitCountSetting(), 1, 20, 200, 50, font);
        jesuitSlider.setLocalTranslation(0, -120, 0);
        jesuitSlider.onChange(value -> simulation.setJesuitCount(value));
        attachChild(jesuitSlider);

        // Loop Slider
        // CORREÇÃO: Usando getMaxLoopsSetting() para consistência, já que é um valor de configuração inicial
        LabeledSlider loopSlider = new LabeledSlider("Loops:", simulation.getMaxLoopsSetting(), 1, 1000, 200, 50, font);
        loopSlider.setLocalTranslation(0, -180, 0);
        loopSlider.onChange(value -> simulation.setMaxLoops(value));
        attachChild(loopSlider);

        // Reset Button
        Button resetBtn = new Button("Reset", 150, 50, app.getAssetManager());
        resetBtn.setLocalTranslation(0, -240, 0);
        resetBtn.setOnClick(() -> simulation.resetSimulation());
        attachChild(resetBtn);
    }
}