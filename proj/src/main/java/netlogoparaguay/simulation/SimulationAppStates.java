package netlogoparaguay.simulation;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.agents.Controls.Agent.Guarani;
import netlogoparaguay.agents.Controls.Agent.Jesuit;
import netlogoparaguay.resources.ResourceManager;

public class SimulationAppStates extends BaseAppState {

    private SimpleApplication app;
    private AssetManager assetManager;
    private Node simulationRootNode;
    private ResourceManager resourceManager;

    private List<Guarani> guaranis = new ArrayList<>();
    private List<Jesuit> jesuits = new ArrayList<>();

    // [ALTERADO] Valores iniciais removidos daqui para evitar confusão.
    // A única fonte de verdade para as configurações iniciais agora é a classe SimulationAppState.
    private int initialGuaraniCount;
    private int initialJesuitCount;
    private int maxLoops;

    private int currentLoop = 0;
    private boolean internalPauseSignal = false;
    private SimulationAppState uiAppStateRef;

    private final int res_capacityPerType = 20;
    private final int res_maxOnMapPerType = 8;
    private final float res_respawnInterval = 10.0f;

    public static final float SIMULATION_AREA_WIDTH = 30f;
    public static final float SIMULATION_AREA_HEIGHT = 30f;

    public final Random random = new Random();

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.assetManager = this.app.getAssetManager();

        this.simulationRootNode = new Node("SimulationEngine_RootNode");
        this.app.getRootNode().attachChild(this.simulationRootNode);

        this.resourceManager = new ResourceManager(
                this.assetManager,
                this.simulationRootNode,
                res_capacityPerType,
                res_maxOnMapPerType,
                res_respawnInterval,
                SIMULATION_AREA_WIDTH,
                SIMULATION_AREA_HEIGHT
        );

        if (uiAppStateRef != null) {
            // Caminho Padrão: Usa as configurações da UI
            resetSimulationWithSettings(
                    uiAppStateRef.getGuaraniCountSetting(),
                    uiAppStateRef.getJesuitCountSetting(),
                    uiAppStateRef.getMaxLoopsSetting()
            );
            setSimulationPausedByUi(uiAppStateRef.isPaused());
        } else {
            // [ALTERADO] Caminho de Fallback: Se a UI não for encontrada, usa valores de emergência explícitos.
            System.err.println("SimulationAppStates: uiAppStateRef é nulo! Usando valores de fallback (5, 5, 200).");
            resetSimulationWithSettings(5, 5, 200);
            setSimulationPausedByUi(true);
        }
    }

    public void resetSimulationWithSettings(int initialGuaranis, int initialJesuits, int newMaxLoops) {
        System.out.println("Motor: Resetando simulação -> G:" + initialGuaranis + ", J:" + initialJesuits + ", Loops:" + newMaxLoops);
        this.currentLoop = 0;
        this.internalPauseSignal = false;
        this.initialGuaraniCount = initialGuaranis;
        this.initialJesuitCount = initialJesuits;
        this.maxLoops = newMaxLoops;

        cleanupAgentsAndResources();
        createInitialAgents();

        if (resourceManager != null) {
            resourceManager.resetAndRepopulate();
        } else {
            System.err.println("Motor ERRO: ResourceManager é nulo durante o reset!");
        }

        if (uiAppStateRef != null) {
            setSimulationPausedByUi(uiAppStateRef.isPaused());
        } else {
            this.internalPauseSignal = true;
        }
        System.out.println("Motor: Reset concluído.");
    }

    private void createInitialAgents() {
        for (int i = 0; i < initialGuaraniCount; i++) {
            addAgentToList(new Guarani("Guarani_" + (i + 1), assetManager, this), guaranis);
        }
        for (int i = 0; i < initialJesuitCount; i++) {
            addAgentToList(new Jesuit("Jesuit_" + (i + 1), assetManager, this), jesuits);
        }
    }

    private <T extends Agent> void addAgentToList(T agent, List<T> list) {
        float x = (random.nextFloat() - 0.5f) * (SIMULATION_AREA_WIDTH - 2f);
        float y = (random.nextFloat() - 0.5f) * (SIMULATION_AREA_HEIGHT - 2f);
        agent.setLocalTranslation(x, y, 0);
        list.add(agent);
        if (simulationRootNode != null) {
            simulationRootNode.attachChild(agent);
        } else {
            System.err.println("ERRO: simulationRootNode é nulo ao tentar adicionar agente " + agent.getName());
        }
    }

    public Agent dynamicallyAddAgent(String type) {
        Agent newAgent = null;
        if ("Guarani".equalsIgnoreCase(type) && guaranis.size() < 50) {
            newAgent = new Guarani("Guarani_d" + (guaranis.size() + 1), assetManager, this);
            addAgentToList((Guarani)newAgent, guaranis);
        } else if ("Jesuit".equalsIgnoreCase(type) && jesuits.size() < 50) {
            newAgent = new Jesuit("Jesuit_d" + (jesuits.size() + 1), assetManager, this);
            addAgentToList((Jesuit)newAgent, jesuits);
        }
        return newAgent;
    }

    public void dynamicallyRemoveAgent(String type) {
        Agent agentToRemove = null;
        if ("Guarani".equalsIgnoreCase(type) && !guaranis.isEmpty()) {
            agentToRemove = guaranis.remove(guaranis.size() - 1);
        } else if ("Jesuit".equalsIgnoreCase(type) && !jesuits.isEmpty()) {
            agentToRemove = jesuits.remove(jesuits.size() - 1);
        }
        if (agentToRemove != null) {
            agentToRemove.removeFromParent();
        }
    }

    @Override
    protected void cleanup(Application app) {
        cleanupAgentsAndResources();
        if (simulationRootNode != null && simulationRootNode.getParent() != null) {
            this.app.getRootNode().detachChild(simulationRootNode);
        }
    }

    private void cleanupAgentsAndResources() {
        if (resourceManager != null) {
            resourceManager.cleanupAllResources();
        }

        for (Guarani g : guaranis) {
            g.removeFromParent();
        }
        guaranis.clear();

        for (Jesuit j : jesuits) {
            j.removeFromParent();
        }
        jesuits.clear();

        if (simulationRootNode != null) {
            simulationRootNode.detachAllChildren();
        }
    }

    @Override
    protected void onEnable() {
        if (simulationRootNode != null) simulationRootNode.setCullHint(Spatial.CullHint.Inherit);
        if (uiAppStateRef != null) {
            setSimulationPausedByUi(uiAppStateRef.isPaused());
        }
    }

    @Override
    protected void onDisable() {
        if (simulationRootNode != null) simulationRootNode.setCullHint(Spatial.CullHint.Always);
    }

    @Override
    public void update(float tpf) {
        boolean isPausedByUI = uiAppStateRef != null && uiAppStateRef.isPaused();

        if (internalPauseSignal || isPausedByUI || !isEnabled()) {
            return;
        }

        float effectiveTpf = tpf * (uiAppStateRef != null ? uiAppStateRef.getSimulationSpeed() : 1.0f);
        currentLoop++;

        if (maxLoops > 0 && currentLoop >= maxLoops) {
            internalPauseSignal = true;
            if (uiAppStateRef != null) {
                uiAppStateRef.setPaused(true);
            }
        }

        if (resourceManager != null) {
            resourceManager.update(effectiveTpf);
        }

        if (currentLoop > 0 && currentLoop % 50 == 0) {
            resetMultiplicationFlags();
        }
    }

    public void notifyAgentDeath(Agent deadAgent) {
        if (deadAgent == null) return;

        deadAgent.removeFromParent();

        if (deadAgent instanceof Guarani) {
            guaranis.remove(deadAgent);
        } else if (deadAgent instanceof Jesuit) {
            jesuits.remove(deadAgent);
        }

        boolean guaranisRemaining = !guaranis.isEmpty();
        boolean jesuitsRemaining = !jesuits.isEmpty();

        if (currentLoop > 0 && (!guaranisRemaining || !jesuitsRemaining)) {
            internalPauseSignal = true;
            if(uiAppStateRef != null) uiAppStateRef.setPaused(true);
        }
    }

    private void resetMultiplicationFlags() {
        for (Guarani g : guaranis) g.resetMultiplicationPossibility();
        for (Jesuit j : jesuits) j.resetMultiplicationPossibility();
    }

    public boolean requestAgentMultiplication(Agent parent) {
        Agent newAgent = null;
        float offsetX = (random.nextFloat() - 0.5f) * 2f;
        float offsetY = (random.nextFloat() - 0.5f) * 2f;
        Vector3f childPosition = parent.getLocalTranslation().add(offsetX, offsetY, 0);

        childPosition.x = FastMath.clamp(childPosition.x, -SIMULATION_AREA_WIDTH / 2f + 0.5f, SIMULATION_AREA_WIDTH / 2f - 0.5f);
        childPosition.y = FastMath.clamp(childPosition.y, -SIMULATION_AREA_HEIGHT / 2f + 0.5f, SIMULATION_AREA_HEIGHT / 2f - 0.5f);

        if (parent instanceof Guarani && guaranis.size() < 50) {
            newAgent = new Guarani("Guarani_c" + (guaranis.size() + 1), assetManager, this);
            addAgentToList((Guarani)newAgent, guaranis);
        } else if (parent instanceof Jesuit && jesuits.size() < 50) {
            newAgent = new Jesuit("Jesuit_c" + (jesuits.size() + 1), assetManager, this);
            addAgentToList((Jesuit)newAgent, jesuits);
        }

        return newAgent != null;
    }

    // --- Getters e Setters ---

    public List<Guarani> getGuaranis() { return guaranis; }
    public List<Jesuit> getJesuits() { return jesuits; }
    public ResourceManager getResourceManager() { return resourceManager; }
    public SimulationAppState getUiAppStateReference() { return uiAppStateRef; }
    public int getActiveGuaraniCount() { return guaranis.size(); }
    public int getActiveJesuitCount() { return jesuits.size(); }
    public int getCurrentSimulationLoop() { return currentLoop; }

    public String determineWinner() {
        boolean guaranisExist = !guaranis.isEmpty();
        boolean jesuitsExist = !jesuits.isEmpty();

        if (maxLoops > 0 && currentLoop >= maxLoops && internalPauseSignal) {
            if (guaranis.size() > jesuits.size()) return "Guarani (Tempo)";
            if (jesuits.size() > guaranis.size()) return "Jesuita (Tempo)";
            return "Empate (Tempo)";
        }

        if (internalPauseSignal) {
            if (guaranisExist && !jesuitsExist) return "Guarani";
            if (!guaranisExist && jesuitsExist) return "Jesuita";
            if (!guaranisExist && !jesuitsExist && currentLoop > 0) return "Empate (Extermínio)";
        }
        return "-";
    }

    public void setUiAppStateReference(SimulationAppState uiState) { this.uiAppStateRef = uiState; }

    public void setSimulationPausedByUi(boolean pausedFromUi) {
        if (internalPauseSignal && !pausedFromUi) {
            if (uiAppStateRef != null) {
                uiAppStateRef.setPaused(true);
            }
            return;
        }
    }

    public void updateMaxLoopsSetting(int newMaxLoops) { this.maxLoops = newMaxLoops; }
}