package netlogoparaguay.simulation;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.Arrays;
import java.util.List;

public class SimulationAppState extends BaseAppState {

    private int guaraniCountSetting = 5;
    private int jesuitCountSetting = 5;
    private int maxLoopsSetting = 10000;
    private boolean paused = true;

    // [CORRIGIDO] O tipo da variável foi corrigido para SimulationAppStates (com 's').
    private SimulationAppStates simulationEngineRef;

    private float simulationSpeed = 1.0f;
    private final List<Float> speedLevels = Arrays.asList(0.25f, 0.5f, 1.0f, 2.0f, 4.0f, 8.0f);
    private int currentSpeedLevelIndex = 2;

    public void increaseMaxLoops(int amount) {
        this.maxLoopsSetting += amount;
        if (simulationEngineRef != null) {
            simulationEngineRef.updateMaxLoopsSetting(this.maxLoopsSetting);
        }
    }

    public void decreaseMaxLoops(int amount) {
        this.maxLoopsSetting = Math.max(1, this.maxLoopsSetting - amount);
        if (simulationEngineRef != null) {
            simulationEngineRef.updateMaxLoopsSetting(this.maxLoopsSetting);
        }
    }

    // O restante do seu código está correto e permanece o mesmo...

    public int getGuaraniCountSetting() { return guaraniCountSetting; }
    public int getJesuitCountSetting() { return jesuitCountSetting; }
    public int getMaxLoopsSetting() { return maxLoopsSetting; }
    public boolean isPaused() { return paused; }
    public float getSimulationSpeed() { return simulationSpeed; }

    public void setGuaraniCount(int count) { this.guaraniCountSetting = Math.max(0, count); }
    public void setJesuitCount(int count) { this.jesuitCountSetting = Math.max(0, count); }

    public void setMaxLoops(int loops) {
        this.maxLoopsSetting = Math.max(1, loops);
        if (simulationEngineRef != null) {
            simulationEngineRef.updateMaxLoopsSetting(this.maxLoopsSetting);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (simulationEngineRef != null) {
            simulationEngineRef.setSimulationPausedByUi(this.paused);
        }
    }

    public void setSimulationSpeed(float speed) { this.simulationSpeed = Math.max(0.1f, speed); }

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
        if (defaultIndex != -1) { this.currentSpeedLevelIndex = defaultIndex; }
        else if (!speedLevels.isEmpty()) { this.currentSpeedLevelIndex = Math.min(2, speedLevels.size() - 1); }

        if (!speedLevels.isEmpty()) { this.simulationSpeed = speedLevels.get(this.currentSpeedLevelIndex); }
        else { this.simulationSpeed = 1.0f; }
    }

    public void setSimulationEngineReference(SimulationAppStates engine) { this.simulationEngineRef = engine; }
    public int getCurrentGuaraniCount() { return (simulationEngineRef != null) ? simulationEngineRef.getActiveGuaraniCount() : 0; }
    public int getCurrentJesuitCount() { return (simulationEngineRef != null) ? simulationEngineRef.getActiveJesuitCount() : 0; }
    public int getCurrentLoop() { return (simulationEngineRef != null) ? simulationEngineRef.getCurrentSimulationLoop() : 0; }
    public String getWinner() { return (simulationEngineRef != null) ? simulationEngineRef.determineWinner() : "-"; }

    public void resetSimulation() {
        if (simulationEngineRef != null) {
            initializeDefaultSpeed();
            simulationEngineRef.resetSimulationWithSettings(
                    this.guaraniCountSetting,
                    this.jesuitCountSetting,
                    this.maxLoopsSetting
            );
            setPaused(true);
        }
    }

    public void requestAddGuarani() { if (simulationEngineRef != null) simulationEngineRef.dynamicallyAddAgent("Guarani"); }
    public void requestRemoveGuarani() { if (simulationEngineRef != null) simulationEngineRef.dynamicallyRemoveAgent("Guarani"); }
    public void requestAddJesuit() { if (simulationEngineRef != null) simulationEngineRef.dynamicallyAddAgent("Jesuit"); }
    public void requestRemoveJesuit() { if (simulationEngineRef != null) simulationEngineRef.dynamicallyRemoveAgent("Jesuit"); }

    @Override protected void initialize(Application app) { initializeDefaultSpeed(); }
    @Override protected void cleanup(Application app) { }
    @Override protected void onEnable() { if (simulationEngineRef != null) { simulationEngineRef.setSimulationPausedByUi(this.paused); } }
    @Override protected void onDisable() { }
    @Override public void update(float tpf) { }
}