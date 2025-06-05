package netlogoparaguay.simulation;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

/**
 * AppState que mantém o estado da simulação que é controlado e exibido pela UI.
 * Serve como uma ponte entre a UI e o motor da simulação (SimulationAppStates).
 */
public class SimulationAppState extends BaseAppState {

    // Parâmetros configuráveis pela UI
    private int guaraniCountSetting = 5; // Contagem inicial/configurada padrão
    private int jesuitCountSetting = 5;  // Contagem inicial/configurada padrão
    private int maxLoopsSetting = 200;    // Loops padrão
    private boolean paused = true;       // Começa pausado por padrão

    private SimulationAppStates simulationEngineRef; // Referência ao motor da simulação

    // Getters para os parâmetros que a UI pode querer ler/definir para configuração inicial
    public int getGuaraniCountSetting() { return guaraniCountSetting; }
    public int getJesuitCountSetting() { return jesuitCountSetting; }
    public int getMaxLoopsSetting() { return maxLoopsSetting; }

    public boolean isPaused() { return paused; }

    // Setters que a UI chama para mudar as configurações
    public void setGuaraniCount(int count) { // Nome original do seu método
        this.guaraniCountSetting = Math.max(0, count);
        System.out.println("UI: Contagem de Guaranis configurada para: " + this.guaraniCountSetting);
        // O reset da simulação que aplicará essa contagem é chamado pelo botão "Reset" da UI.
    }

    public void setJesuitCount(int count) { // Nome original do seu método
        this.jesuitCountSetting = Math.max(0, count);
        System.out.println("UI: Contagem de Jesuitas configurada para: " + this.jesuitCountSetting);
    }

    public void setMaxLoops(int loops) { // Nome original do seu método
        this.maxLoopsSetting = Math.max(1, loops);
        System.out.println("UI: Máximo de loops configurado para: " + this.maxLoopsSetting);
        if (simulationEngineRef != null) {
            simulationEngineRef.updateMaxLoopsSetting(this.maxLoopsSetting);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        System.out.println("UI: Estado de pausa definido para: " + this.paused);
        if (simulationEngineRef != null) {
            simulationEngineRef.setSimulationPausedByUi(this.paused);
        }
    }

    /**
     * Define a referência para o motor da simulação.
     * Chamado durante a inicialização em Netlogoparaguay.java.
     * @param engine A instância de SimulationAppStates (o motor).
     */
    public void setSimulationEngineReference(SimulationAppStates engine) {
        this.simulationEngineRef = engine;
    }

    // Getters para DADOS AO VIVO da simulação (delegados ao motor)
    // Usados pelo StatsPanel via StatsUpdater
    public int getCurrentGuaraniCount() {
        return (simulationEngineRef != null) ? simulationEngineRef.getActiveGuaraniCount() : 0;
    }

    public int getCurrentJesuitCount() {
        return (simulationEngineRef != null) ? simulationEngineRef.getActiveJesuitCount() : 0;
    }

    public int getCurrentLoop() {
        return (simulationEngineRef != null) ? simulationEngineRef.getCurrentSimulationLoop() : 0;
    }

    public int getMaxLoops() { // Este é o setting para a UI, o motor usa maxLoopsSetting ou o seu próprio
        return maxLoopsSetting;
    }

    public String getWinner() {
        return (simulationEngineRef != null) ? simulationEngineRef.determineWinner() : "-";
    }

    /**
     * Chamado pelo botão de Reset da UI.
     * Solicita ao motor da simulação para resetar com os parâmetros atuais da UI.
     */
    public void resetSimulation() {
        System.out.println("UI: Botão Reset pressionado. Solicitando reset da simulação...");
        if (simulationEngineRef != null) {
            simulationEngineRef.resetSimulationWithSettings(
                    this.guaraniCountSetting,
                    this.jesuitCountSetting,
                    this.maxLoopsSetting
            );
            // Garante que a simulação fique pausada após o reset para o usuário iniciar.
            if (!isPaused()) {
                setPaused(true);
            }
        }
    }

    @Override
    protected void initialize(Application app) {
        // System.out.println("SimulationAppState (UI State) inicializado.");
    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {
        if (simulationEngineRef != null) {
            simulationEngineRef.setSimulationPausedByUi(this.paused);
        }
    }

    @Override
    protected void onDisable() {}

    @Override
    public void update(float tpf) {
        // Este AppState não tem muita lógica de update por si só.
    }
}