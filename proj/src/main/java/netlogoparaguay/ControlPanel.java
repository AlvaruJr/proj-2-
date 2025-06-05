package netlogoparaguay;

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
// Removido import não utilizado: import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import netlogoparaguay.agents.Controls.Panel.Button; // Certifique-se que este pacote está correto
import netlogoparaguay.agents.Controls.Panel.LabeledSlider; // Certifique-se que este pacote está correto
import netlogoparaguay.agents.Controls.Panel.ToggleButton; // Certifique-se que este pacote está correto
import netlogoparaguay.simulation.SimulationAppState;

public class ControlPanel extends Node {

    private final SimulationAppState simulation;
    private boolean isRunning = false; // Este estado 'isRunning' é local do ControlPanel e pode
    // dessincronizar do 'paused' em SimulationAppState.
    // É melhor confiar no estado de 'simulation.isPaused()'.

    public ControlPanel(Application app, SimulationAppState simulation) {
        super("controlPanel");
        this.simulation = simulation;

        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        // Botão Start/Stop
        ToggleButton startStopBtn = new ToggleButton("Start", "Stop", 150, 50, app.getAssetManager());
        startStopBtn.setLocalTranslation(0, 0, 0);
        // Inicializa o estado do botão com base no estado de pausa da simulação
        // (Adicionarei um método para isso no ToggleButton ou você pode setar o texto/cor manualmente)
        // Por agora, a lógica de toggle permanece:
        startStopBtn.setOnToggle(isOn -> {
            // this.isRunning = isOn; // Removido para evitar dessincronização
            simulation.setPaused(!isOn);
        });
        // Para o estado visual inicial do ToggleButton, seria bom que ele refletisse
        // o 'simulation.isPaused()'. Se 'simulation' começa pausado (true),
        // o botão deveria mostrar "Start" (isOn = false).
        // Você pode precisar adicionar um método `setState(boolean isOn)` ao ToggleButton
        // ou o construtor do ToggleButton poderia aceitar um estado inicial.
        // Assumindo que o ToggleButton começa em "Start" (isOn=false) visualmente:
        if (simulation.isPaused()) {
            // O ToggleButton já deve estar no estado "Start" visualmente.
            // Se não, você precisaria de um método para forçar o estado visual sem disparar o callback.
            // Ex: startStopBtn.forceState(false);
        } else {
            // Se a simulação começa rodando, o botão deveria mostrar "Stop".
            // Ex: startStopBtn.forceState(true);
        }

        attachChild(startStopBtn);

        // Controle de agentes Guarani
        // Usando os métodos corretos de SimulationAppState para obter as CONFIGURAÇÕES
        LabeledSlider guaraniSlider = new LabeledSlider("Guaranis:", simulation.getGuaraniCountSetting(), 1, 20, 200, 50, font);
        guaraniSlider.setLocalTranslation(0, -60, 0);
        // O setValue aqui está correto, pois getGuaraniCountSetting() retorna o valor para o slider
        guaraniSlider.setValue(simulation.getGuaraniCountSetting());
        guaraniSlider.onChange(value -> simulation.setGuaraniCount(value)); // setGuaraniCount é o setter correto
        attachChild(guaraniSlider);

        // Controle de agentes Jesuitas
        LabeledSlider jesuitSlider = new LabeledSlider("Jesuitas:", simulation.getJesuitCountSetting(), 1, 20, 200, 50, font);
        jesuitSlider.setLocalTranslation(0, -120, 0);
        jesuitSlider.setValue(simulation.getJesuitCountSetting());
        jesuitSlider.onChange(value -> simulation.setJesuitCount(value)); // setJesuitCount é o setter correto
        attachChild(jesuitSlider);

        // Controle de loops
        // Usando getMaxLoopsSetting() para o valor inicial do slider
        LabeledSlider loopSlider = new LabeledSlider("Loops:", simulation.getMaxLoopsSetting(), 1, 1000, 200, 50, font);
        loopSlider.setLocalTranslation(0, -180, 0);
        loopSlider.setValue(simulation.getMaxLoopsSetting()); // getMaxLoopsSetting para inicializar
        loopSlider.onChange(value -> simulation.setMaxLoops(value)); // setMaxLoops é o setter correto
        attachChild(loopSlider);

        // Botão de reset
        Button resetBtn = new Button("Reset", 150, 50, app.getAssetManager());
        resetBtn.setLocalTranslation(0, -240, 0);
        resetBtn.setOnClick(() -> simulation.resetSimulation());
        attachChild(resetBtn);
    }
}