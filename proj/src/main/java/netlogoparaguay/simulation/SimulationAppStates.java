
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
import java.util.Random; // Import já deve existir
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

    private int initialGuaraniCount = 5;
    private int initialJesuitCount = 5;
    private int maxLoops = 200;
    private int currentLoop = 0;

    private boolean internalPauseSignal = false;
    private SimulationAppState uiAppStateRef;

    private final int res_capacityPerType = 20;
    private final int res_maxOnMapPerType = 8;
    private final float res_respawnInterval = 10.0f;

    public static final float SIMULATION_AREA_WIDTH = 30f;
    public static final float SIMULATION_AREA_HEIGHT = 30f;

    // [ADICIONADO] Instância de Random para ser compartilhada com outros componentes, como os AgentControls.
    public final Random random = new Random();
    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.assetManager = this.app.getAssetManager();

        // Cria o nó raiz da simulação e o anexa ao rootNode principal da aplicação
        this.simulationRootNode = new Node("SimulationEngine_RootNode_ParaAgentesERecursos");
        this.app.getRootNode().attachChild(this.simulationRootNode);
        System.out.println("SimulationAppStates: simulationRootNode anexado ao rootNode da aplicação.");

        // Inicializa o ResourceManager, passando o nó onde os recursos devem ser adicionados
        this.resourceManager = new ResourceManager(
                this.assetManager,
                this.simulationRootNode, // Recursos serão filhos deste nó
                res_capacityPerType,
                res_maxOnMapPerType,
                res_respawnInterval,
                SIMULATION_AREA_WIDTH,
                SIMULATION_AREA_HEIGHT
        );
        System.out.println("SimulationAppStates: ResourceManager inicializado.");

        if (uiAppStateRef != null) {
            resetSimulationWithSettings(
                    uiAppStateRef.getGuaraniCountSetting(),
                    uiAppStateRef.getJesuitCountSetting(),
                    uiAppStateRef.getMaxLoopsSetting()
            );
            setSimulationPausedByUi(uiAppStateRef.isPaused());
        } else {
            System.out.println("SimulationAppStates: uiAppStateRef é nulo na inicialização. Usando defaults.");
            resetSimulationWithSettings(initialGuaraniCount, initialJesuitCount, maxLoops);
            setSimulationPausedByUi(true); // Começa pausado por padrão
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
            System.out.println("Motor: Chamando resourceManager.resetAndRepopulate()...");
            resourceManager.resetAndRepopulate(); // ESSENCIAL PARA OS RECURSOS APARECEREM
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
        System.out.println("Motor: Criando agentes iniciais...");
        for (int i = 0; i < initialGuaraniCount; i++) {
            addAgentToList(new Guarani("Guarani_" + (i + 1), assetManager, this), guaranis);
        }
        for (int i = 0; i < initialJesuitCount; i++) {
            addAgentToList(new Jesuit("Jesuit_" + (i + 1), assetManager, this), jesuits);
        }
        System.out.println("Motor: " + guaranis.size() + " Guaranis e " + jesuits.size() + " Jesuitas criados.");
    }

    private <T extends Agent> void addAgentToList(T agent, List<T> list) {
        float x = (random.nextFloat() - 0.5f) * (SIMULATION_AREA_WIDTH - 2f);
        float y = (random.nextFloat() - 0.5f) * (SIMULATION_AREA_HEIGHT - 2f);
        agent.setLocalTranslation(x, y, 0);
        list.add(agent);
        // Adiciona o agente ao nó da simulação, NÃO ao guiNode
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
            System.out.println("Motor: Adicionado dinamicamente Guarani. Total: " + guaranis.size());
        } else if ("Jesuit".equalsIgnoreCase(type) && jesuits.size() < 50) {
            newAgent = new Jesuit("Jesuit_d" + (jesuits.size() + 1), assetManager, this);
            addAgentToList((Jesuit)newAgent, jesuits);
            System.out.println("Motor: Adicionado dinamicamente Jesuit. Total: " + jesuits.size());
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
            System.out.println("Motor: Removido dinamicamente " + agentToRemove.getName());
        }
    }

    @Override
    protected void cleanup(Application app) {
        System.out.println("Motor: Iniciando cleanup...");
        cleanupAgentsAndResources();
        if (simulationRootNode != null && simulationRootNode.getParent() != null) {
            // Destaca o nó da simulação do nó raiz da aplicação
            this.app.getRootNode().detachChild(simulationRootNode);
            System.out.println("Motor: simulationRootNode destacado do rootNode da aplicação.");
        } else if (simulationRootNode != null) {
            System.out.println("Motor: simulationRootNode não tinha pai ou já foi destacado.");
        }
        System.out.println("Motor: Limpo (cleanup).");
    }

    private void cleanupAgentsAndResources() {
        System.out.println("Motor: Limpando agentes e recursos...");
        if (resourceManager != null) {
            resourceManager.cleanupAllResources();
        } else {
            System.err.println("Motor ERRO: ResourceManager é nulo durante cleanupAgentsAndResources!");
        }

        // Remove agentes da lista e da cena
        for (Guarani g : new ArrayList<>(guaranis)) { // Itera sobre cópia para evitar ConcurrentModification
            if (g.getParent() != null) g.getParent().detachChild(g);
        }
        guaranis.clear();
        for (Jesuit j : new ArrayList<>(jesuits)) {
            if (j.getParent() != null) j.getParent().detachChild(j);
        }
        jesuits.clear();

        // Garante que o nó raiz da simulação esteja limpo
        if (simulationRootNode != null) {
            simulationRootNode.detachAllChildren();
            System.out.println("Motor: Todos os filhos de simulationRootNode foram destacados.");
        }
    }

    @Override
    protected void onEnable() {
        if (simulationRootNode != null) simulationRootNode.setCullHint(Spatial.CullHint.Inherit);
        System.out.println("Motor: Habilitado.");
        if (uiAppStateRef != null) {
            setSimulationPausedByUi(uiAppStateRef.isPaused());
        }
    }

    @Override
    protected void onDisable() {
        if (simulationRootNode != null) simulationRootNode.setCullHint(Spatial.CullHint.Always);
        System.out.println("Motor: Desabilitado.");
    }

    @Override
    public void update(float tpf) {
        boolean isPausedByUI = false;
        float currentSimSpeed = 1.0f;

        if (uiAppStateRef != null) {
            isPausedByUI = uiAppStateRef.isPaused();
            currentSimSpeed = uiAppStateRef.getSimulationSpeed();
        }

        if (internalPauseSignal || isPausedByUI || !isEnabled()) {
            return;
        }

        float effectiveTpf = tpf * currentSimSpeed;
        currentLoop++;

        if (maxLoops > 0 && currentLoop >= maxLoops) {
            if(!internalPauseSignal) System.out.println("Motor: Máximo de loops (" + maxLoops + ") atingido.");
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
        // System.out.println("Motor: Notificado da morte de " + deadAgent.getName());
        if (deadAgent.getParent() != null) {
            deadAgent.getParent().detachChild(deadAgent); // Remove da cena
        }
        boolean removed = false;
        if (deadAgent instanceof Guarani) {
            removed = guaranis.remove(deadAgent);
        } else if (deadAgent instanceof Jesuit) {
            removed = jesuits.remove(deadAgent);
        }
        // if(removed) System.out.println("Motor: " + deadAgent.getName() + " removido das listas ativas.");
        // else System.out.println("Motor: " + deadAgent.getName() + " não encontrado nas listas ativas para remoção pós-morte.");


        boolean guaranisRemaining = !guaranis.isEmpty();
        boolean jesuitsRemaining = !jesuits.isEmpty();

        if (currentLoop > 0 && (!guaranisRemaining || !jesuitsRemaining)) {
            if (!internalPauseSignal) System.out.println("Motor: Condição de fim de jogo por extermínio atingida.");
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

        if (newAgent != null) {
            // System.out.println("Motor: " + parent.getName() + " multiplicou. Novo: " + newAgent.getName());
            return true;
        }
        return false;
    }

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

        if (maxLoops > 0 && currentLoop >= maxLoops && internalPauseSignal) { // Verifica se pausou por loops
            if (guaranis.size() > jesuits.size()) return "Guarani (Tempo)";
            if (jesuits.size() > guaranis.size()) return "Jesuita (Tempo)";
            return "Empate (Tempo)";
        }

        // Verifica vitória por extermínio apenas se a simulação não terminou por loops e está pausada internamente
        if (internalPauseSignal) {
            if (guaranisExist && !jesuitsExist) return "Guarani";
            if (!guaranisExist && jesuitsExist) return "Jesuita";
            if (!guaranisExist && !jesuitsExist && currentLoop > 0) return "Empate (Extermínio)";
        }
        return "-"; // Jogo em andamento
    }

    public void setUiAppStateReference(SimulationAppState uiState) { this.uiAppStateRef = uiState; }

    public void setSimulationPausedByUi(boolean pausedFromUi) {
        if (internalPauseSignal && !pausedFromUi) { // Se motor pausou (fim de jogo), UI não pode despausar
            if (uiAppStateRef != null) {
                uiAppStateRef.setPaused(true);
            }
            // System.out.println("Motor: UI tentou despausar, mas motor está internamente pausado.");
            return;
        }
        // System.out.println("Motor: Estado de pausa da UI ("+pausedFromUi+") recebido e será aplicado no próximo update do motor.");
        // O estado de pausa da UI é verificado diretamente no método update() do motor.
    }
    public void updateMaxLoopsSetting(int newMaxLoops) { this.maxLoops = newMaxLoops; }
}
