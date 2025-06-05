package netlogoparaguay.simulation;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.Arrays;
import java.util.List;

public class SimulationAppState extends BaseAppState {

    private int guaraniCountSetting = 5;
    private int jesuitCountSetting = 5;
    private int maxLoopsSetting = 200;
    private boolean paused = true;

    private SimulationAppStates simulationEngineRef; // Esta é a ponte para o motor

    private float simulationSpeed = 1.0f;
    private final List<Float> speedLevels = Arrays.asList(0.25f, 0.5f, 1.0f, 2.0f, 4.0f, 8.0f);
    private int currentSpeedLevelIndex = 2;

    public SimulationAppState() {
        // System.out.println("SimulationAppState (Gerenciador de Estado da UI): Instância criada.");
    }

    public int getGuaraniCountSetting() { return guaraniCountSetting; }
    public int getJesuitCountSetting() { return jesuitCountSetting; }
    public int getMaxLoopsSetting() { return maxLoopsSetting; } // Usado por StatsUpdater
    public boolean isPaused() { return paused; } // Usado por controlPanel e AgentControl
    public float getSimulationSpeed() { return simulationSpeed; } // Usado por controlPanel e AgentControl

    public void setGuaraniCount(int count) {
        this.guaraniCountSetting = Math.max(0, count);
        // System.out.println("UI State: Guarani count setting: " + this.guaraniCountSetting);
        // A contagem só terá efeito no próximo reset da simulação.
    }

    public void setJesuitCount(int count) {
        this.jesuitCountSetting = Math.max(0, count);
        // System.out.println("UI State: Jesuit count setting: " + this.jesuitCountSetting);
    }

    public void setMaxLoops(int loops) {
        this.maxLoopsSetting = Math.max(0, loops);
        // System.out.println("UI State: Max loops setting: " + this.maxLoopsSetting);
        if (simulationEngineRef != null) {
            simulationEngineRef.updateMaxLoopsSetting(this.maxLoopsSetting); // Notifica o motor
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        // System.out.println("UI State: Paused: " + this.paused);
        if (simulationEngineRef != null) {
            simulationEngineRef.setSimulationPausedByUi(this.paused); // Notifica o motor
        }
    }

    public void setSimulationSpeed(float speed) { // Chamado por increase/decrease
        this.simulationSpeed = Math.max(0.1f, speed);
        // System.out.println("UI State: Simulation speed: " + this.simulationSpeed + "x");
        // AgentControl já busca essa velocidade a cada frame, não precisa notificação explícita para o motor,
        // a menos que o motor precise dela para outros sistemas (ex: ResourceManager respawn timer).
        // Se ResourceManager precisar, adicione:
        // if (simulationEngineRef != null) {
        //     simulationEngineRef.updateSimulationSpeedFactor(this.simulationSpeed);
        // }
    }

    public void increaseSimulationSpeed() {
        if (speedLevels.isEmpty()) return;
        currentSpeedLevelIndex = (currentSpeedLevelIndex + 1) % speedLevels.size();
        setSimulationSpeed(speedLevels.get(currentSpeedLevelIndex));
    }

    public void decreaseSimulationSpeed() {
        if (speedLevels.isEmpty()) return;
        currentSpeedLevelIndex = (currentSpeedLevelIndex - 1 + speedLevels.size()) % speedLevels.size();
        setSimulationSpeed(speedLevels.get(currentSpeedLevelIndex));
    }

    private void initializeDefaultSpeed() {
        int defaultIndex = speedLevels.indexOf(1.0f);
        if (defaultIndex != -1) {
            this.currentSpeedLevelIndex = defaultIndex;
        } else if (!speedLevels.isEmpty()) {
            this.currentSpeedLevelIndex = Math.min(2, speedLevels.size() - 1);
            // A condição 'this.currentSpeedLevelIndex < 0' era marcada como sempre falsa pelo IDE.
            // Se Math.min(2, speedLevels.size() - 1) puder ser < 0 (só se speedLevels.size() for 0),
            // mas speedLevels.isEmpty() já trata isso.
        }

        if (!speedLevels.isEmpty()) {
            this.simulationSpeed = speedLevels.get(this.currentSpeedLevelIndex);
        } else {
            this.simulationSpeed = 1.0f;
        }
        // System.out.println("UI State: Velocidade inicial: " + this.simulationSpeed + "x");
    }

    public void setSimulationEngineReference(SimulationAppStates engine) {
        this.simulationEngineRef = engine;
    }

    public int getCurrentGuaraniCount() {
        return (simulationEngineRef != null) ? simulationEngineRef.getActiveGuaraniCount() : 0;
    }

    public int getCurrentJesuitCount() {
        return (simulationEngineRef != null) ? simulationEngineRef.getActiveJesuitCount() : 0;
    }

    public int getCurrentLoop() {
        return (simulationEngineRef != null) ? simulationEngineRef.getCurrentSimulationLoop() : 0;
    }

    public String getWinner() {
        return (simulationEngineRef != null) ? simulationEngineRef.determineWinner() : "-";
    }

    public void resetSimulation() {
        // System.out.println("UI State: Comando Reset recebido.");
        if (simulationEngineRef != null) {
            initializeDefaultSpeed();
            simulationEngineRef.resetSimulationWithSettings(
                    this.guaraniCountSetting,
                    this.jesuitCountSetting,
                    this.maxLoopsSetting
            );
            if (!isPaused()) { // Se estava rodando, pausa após o reset
                setPaused(true);
            } else { // Se já estava pausado, apenas garante que o motor saiba
                simulationEngineRef.setSimulationPausedByUi(true);
            }
        }
    }

    public void requestAddGuarani() {
        if (simulationEngineRef != null) simulationEngineRef.dynamicallyAddAgent("Guarani");
    }
    public void requestRemoveGuarani() {
        if (simulationEngineRef != null) simulationEngineRef.dynamicallyRemoveAgent("Guarani");
    }
    public void requestAddJesuit() {
        if (simulationEngineRef != null) simulationEngineRef.dynamicallyAddAgent("Jesuit");
    }
    public void requestRemoveJesuit() {
        if (simulationEngineRef != null) simulationEngineRef.dynamicallyRemoveAgent("Jesuit");
    }

    @Override
    protected void initialize(Application app) {
        initializeDefaultSpeed();
        // System.out.println("SimulationAppState (Gerenciador de Estado da UI): Inicializado.");
    }

    @Override
    protected void cleanup(Application app) {
        // System.out.println("SimulationAppState (Gerenciador de Estado da UI): Limpo.");
    }

    @Override
    protected void onEnable() {
        if (simulationEngineRef != null) {
            simulationEngineRef.setSimulationPausedByUi(this.paused);
        }
        // System.out.println("SimulationAppState (Gerenciador de Estado da UI): Habilitado. Pausa: " + this.paused);
    }

    @Override
    protected void onDisable() {
        // System.out.println("SimulationAppState (Gerenciador de Estado da UI): Desabilitado.");
    }

    @Override
    public void update(float tpf) {
        // Não precisa de lógica de update aqui, é mais um "data holder" e "event dispatcher"
    }
}
