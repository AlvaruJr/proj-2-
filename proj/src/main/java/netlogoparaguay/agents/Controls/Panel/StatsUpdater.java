package netlogoparaguay.agents.Controls.Panel;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import netlogoparaguay.simulation.SimulationAppState;

public class StatsUpdater extends BaseAppState {
    private final StatsPanel statsPanel;
    private final SimulationAppState simulation; // Referência ao estado da UI
    private float timeSinceLastUpdate = 0;

    public StatsUpdater(StatsPanel statsPanel, SimulationAppState simulation) {
        this.statsPanel = statsPanel;
        this.simulation = simulation;
    }

    @Override
    public void update(float tpf) {
        timeSinceLastUpdate += tpf;
        if (timeSinceLastUpdate > 0.5f) {
            if (simulation != null && statsPanel != null && simulation.isEnabled() && isEnabled()) {
                statsPanel.updateStats(
                        simulation.getCurrentGuaraniCount(),
                        simulation.getCurrentJesuitCount(),
                        simulation.getCurrentLoop(),
                        simulation.getMaxLoopsSetting(), // CORRIGIDO: Usar getMaxLoopsSetting()
                        simulation.getWinner()
                );
            }
            timeSinceLastUpdate = 0;
        }
    }

    @Override
    protected void initialize(Application app) {
        // Lógica de inicialização, se necessário
    }

    @Override
    protected void cleanup(Application app) {
        // Lógica de limpeza, se necessário
    }

    @Override
    protected void onEnable() {
        timeSinceLastUpdate = 0;
    }

    @Override
    protected void onDisable() {
        // Lógica ao desabilitar
    }
}
