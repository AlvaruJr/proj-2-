package netlogoparaguay.agents.Controls.Panel;

import com.jme3.app.state.BaseAppState;
import netlogoparaguay.simulation.SimulationAppState;

public class StatsUpdater extends BaseAppState {
    private final StatsPanel statsPanel;
    private final SimulationAppState simulation;
    private float timeSinceLastUpdate = 0;

    public StatsUpdater(StatsPanel statsPanel, SimulationAppState simulation) {
        this.statsPanel = statsPanel;
        this.simulation = simulation;
    }

    @Override
    public void update(float tpf) {
        timeSinceLastUpdate += tpf;
        if (timeSinceLastUpdate > 0.5f) {
            statsPanel.updateStats(
                    simulation.getCurrentGuaraniCount(),
                    simulation.getCurrentJesuitCount(),
                    simulation.getCurrentLoop(),
                    simulation.getMaxLoops(),
                    simulation.getWinner()
            );
            timeSinceLastUpdate = 0;
        }
    }

    // Other required BaseAppState methods
    @Override protected void initialize(com.jme3.app.Application app) {}
    @Override protected void cleanup(com.jme3.app.Application app) {}
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}