package netlogoparaguay.simulation;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.List; // Import padrão
import java.util.concurrent.CopyOnWriteArrayList;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.agents.Controls.Agent.Guarani;
import netlogoparaguay.agents.Controls.Agent.Jesuit;
import netlogoparaguay.agents.Controls.controller.AgentControl; // Para WORLD_BOUNDS
import netlogoparaguay.resources.ResourceManager;

public class SimulationAppStates extends BaseAppState {

    private SimpleApplication app;
    private AssetManager assetManager;
    private Node simulationRootNode;

    private ResourceManager resourceManager;
    private List<Guarani> guaranis;
    private List<Jesuit> jesuits;

    // Parâmetros de configuração da simulação (podem ser atualizados pela UI)
    private int configInitialGuaraniCount = 5;
    private int configInitialJesuitCount = 5;
    private int configMaxLoops = 200; // Máximo de loops configurado pela UI

    private int maxAgentsPerType = 20;

    private int initialResourcesPerTypeInPool = 10;
    private int maxActiveResourcesOnMap = 15;
    private float resourceRespawnInterval = 8.0f;

    private static final float WORLD_PLACEMENT_WIDTH = 28f;
    private static final float WORLD_PLACEMENT_HEIGHT = 28f;

    private boolean internalPauseSignal = false; // Pausa interna do motor (ex: fim de jogo)
    private SimulationAppState uiAppStateRef;    // Referência ao estado da UI para verificar pausa externa
    private int currentSimulationLoop = 0;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.assetManager = this.app.getAssetManager();
        this.simulationRootNode = new Node("SimulationRootNode_Engine");

        this.guaranis = new CopyOnWriteArrayList<>();
        this.jesuits = new CopyOnWriteArrayList<>();

        // ResourceManager será criado/recriado em resetSimulationWithSettings
        // para garantir que use os parâmetros corretos.

        createEnvironment(); // Cria o fundo/chão

        // A simulação começa efetivamente quando resetSimulationWithSettings é chamado
        // pela primeira vez (pode ser pela UI ou um default).
        // Se uiAppStateRef já estiver disponível, usa os settings dele.
        if (uiAppStateRef != null) {
            resetSimulationWithSettings(
                    uiAppStateRef.getGuaraniCountSetting(),
                    uiAppStateRef.getJesuitCountSetting(),
                    uiAppStateRef.getMaxLoopsSetting()
            );
            // Garante que o estado de pausa inicial seja o da UI
            setSimulationPausedByUi(uiAppStateRef.isPaused());
        } else {
            // Fallback para defaults se uiAppStateRef não estiver pronto
            resetSimulationWithSettings(
                    configInitialGuaraniCount,
                    configInitialJesuitCount,
                    configMaxLoops
            );
            setSimulationPausedByUi(true); // Começa pausado por default
        }

        System.out.println("SimulationAppStates (Motor) inicializado.");
    }

    public void setUiAppStateReference(SimulationAppState uiState) {
        this.uiAppStateRef = uiState;
        // Se o motor já foi inicializado, sincroniza o estado de pausa e loops
        if (isInitialized() && this.uiAppStateRef != null) {
            setSimulationPausedByUi(this.uiAppStateRef.isPaused());
            updateMaxLoopsSetting(this.uiAppStateRef.getMaxLoopsSetting());
            // Pode ser necessário um reset se as contagens de agentes mudarem e a simulação já estiver rodando.
            // A UI tipicamente força um reset ao mudar contagens.
        }
    }

    private void createEnvironment() {
        // Chão 3D
        Quad bgQuad = new Quad(AgentControl.WORLD_BOUNDS, AgentControl.WORLD_BOUNDS);
        Geometry bgGeom = new Geometry("BackgroundFloor", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        bgMat.setBoolean("UseMaterialColors", true);
        bgMat.setColor("Diffuse", new ColorRGBA(0.4f, 0.45f, 0.3f, 1.0f));
        bgMat.setColor("Ambient", new ColorRGBA(0.2f, 0.22f, 0.15f, 1.0f));
        bgGeom.setMaterial(bgMat);
        bgGeom.setLocalTranslation(-AgentControl.WORLD_BOUNDS / 2f, -AgentControl.WORLD_BOUNDS / 2f, -0.1f);
        simulationRootNode.attachChild(bgGeom);
    }

    private void createInitialAgents() {
        // Limpa listas antes de adicionar, caso seja um reset
        for (Guarani g : this.guaranis) { g.removeFromParent(); }
        this.guaranis.clear();
        for (Jesuit j : this.jesuits) { j.removeFromParent(); }
        this.jesuits.clear();

        for (int i = 0; i < configInitialGuaraniCount; i++) {
            Guarani guarani = new Guarani("Guarani_" + i, assetManager, this);
            guarani.setLocalTranslation(getRandomInitialPosition());
            guaranis.add(guarani);
            simulationRootNode.attachChild(guarani);
        }

        for (int i = 0; i < configInitialJesuitCount; i++) {
            Jesuit jesuit = new Jesuit("Jesuit_" + i, assetManager, this);
            jesuit.setLocalTranslation(getRandomInitialPosition());
            jesuits.add(jesuit);
            simulationRootNode.attachChild(jesuit);
        }
    }

    private Vector3f getRandomInitialPosition() {
        float x = (FastMath.nextRandomFloat() - 0.5f) * WORLD_PLACEMENT_WIDTH;
        float y = (FastMath.nextRandomFloat() - 0.5f) * WORLD_PLACEMENT_HEIGHT;
        return new Vector3f(x, y, 0f);
    }

    public void updateMaxLoopsSetting(int newMaxLoops) {
        this.configMaxLoops = Math.max(1, newMaxLoops);
    }

    @Override
    public void update(float tpf) {
        boolean effectivelyPaused = internalPauseSignal || (uiAppStateRef != null && uiAppStateRef.isPaused());

        if (effectivelyPaused || !isEnabled()) {
            return;
        }

        currentSimulationLoop++;
        if (currentSimulationLoop >= configMaxLoops) {
            internalPauseSignal = true; // Pausa interna por fim de loops
            if (uiAppStateRef != null) {
                uiAppStateRef.setPaused(true); // Sincroniza com a UI
            }
            System.out.println("Simulação (Motor) atingiu o máximo de loops: " + configMaxLoops);
            // Não retorna aqui, permite que o ResourceManager e outras lógicas finais de update ocorram.
        }

        if (resourceManager != null) {
            resourceManager.update(tpf);
        }

        // Lógica de fim de jogo (simples)
        if (getActiveGuaraniCount() == 0 && getActiveJesuitCount() > 0) {
            internalPauseSignal = true;
            // System.out.println("Motor: Jesuitas Venceram (extermínio)!");
        } else if (getActiveJesuitCount() == 0 && getActiveGuaraniCount() > 0) {
            internalPauseSignal = true;
            // System.out.println("Motor: Guaranis Venceram (extermínio)!");
        } else if (getActiveGuaraniCount() == 0 && getActiveJesuitCount() == 0 && currentSimulationLoop > 0) {
            internalPauseSignal = true;
            // System.out.println("Motor: Empate (extermínio mútuo)!");
        }

        if (internalPauseSignal && uiAppStateRef != null && !uiAppStateRef.isPaused()) {
            uiAppStateRef.setPaused(true); // Garante que a UI reflita a pausa interna
        }
    }

    public void notifyAgentDeath(Agent deadAgent) {
        boolean removed = false;
        if (deadAgent instanceof Guarani) {
            removed = guaranis.remove((Guarani) deadAgent);
        } else if (deadAgent instanceof Jesuit) {
            removed = jesuits.remove((Jesuit) deadAgent);
        }
        if (deadAgent.getParent() != null) {
            deadAgent.removeFromParent(); // Garante remoção da cena
        }
    }

    public boolean requestAgentMultiplication(Agent parentAgent) {
        Vector3f spawnPosition = parentAgent.getLocalTranslation().add(
                new Vector3f(FastMath.nextRandomFloat() * 2f - 1f,
                        FastMath.nextRandomFloat() * 2f - 1f, 0f)
                        .normalizeLocal().multLocal(1.5f)
        );

        spawnPosition.x = FastMath.clamp(spawnPosition.x, -WORLD_PLACEMENT_WIDTH/2f, WORLD_PLACEMENT_WIDTH/2f);
        spawnPosition.y = FastMath.clamp(spawnPosition.y, -WORLD_PLACEMENT_HEIGHT/2f, WORLD_PLACEMENT_HEIGHT/2f);
        spawnPosition.z = 0f;

        if (parentAgent instanceof Guarani) {
            if (guaranis.size() < maxAgentsPerType) {
                Guarani newGuarani = new Guarani(parentAgent.getName() + "_m" + guaranis.size() , assetManager, this);
                newGuarani.setLocalTranslation(spawnPosition);
                guaranis.add(newGuarani);
                simulationRootNode.attachChild(newGuarani);
                return true;
            }
        } else if (parentAgent instanceof Jesuit) {
            if (jesuits.size() < maxAgentsPerType) {
                Jesuit newJesuit = new Jesuit(parentAgent.getName() + "_m" + jesuits.size(), assetManager, this);
                newJesuit.setLocalTranslation(spawnPosition);
                jesuits.add(newJesuit);
                simulationRootNode.attachChild(newJesuit);
                return true;
            }
        }
        return false;
    }

    public void resetSimulationWithSettings(int initialGuaranis, int initialJesuits, int newMaxLoops) {
        System.out.println("Motor da Simulação: Resetando com Guaranis=" + initialGuaranis +
                ", Jesuitas=" + initialJesuits + ", Loops=" + newMaxLoops);

        // Limpa recursos existentes
        if (resourceManager != null) {
            resourceManager.cleanup();
        }
        // Recria o ResourceManager para garantir estado limpo e novas configurações se necessário
        this.resourceManager = new ResourceManager(
                this.assetManager,
                this.simulationRootNode,
                initialResourcesPerTypeInPool,
                maxActiveResourcesOnMap,
                resourceRespawnInterval
        );

        // Atualiza contagens e loops de configuração
        this.configInitialGuaraniCount = initialGuaranis;
        this.configInitialJesuitCount = initialJesuits;
        this.configMaxLoops = newMaxLoops;
        this.currentSimulationLoop = 0;
        this.internalPauseSignal = false; // Reseta a pausa interna

        // Recria agentes iniciais (método createInitialAgents já limpa as listas)
        createInitialAgents();

        System.out.println("Motor da Simulação: Reset concluído.");
        // O estado de pausa da UI será definido pelo SimulationAppState após o reset.
    }

    @Override
    protected void cleanup(Application app) {
        if (resourceManager != null) {
            resourceManager.cleanup();
        }
        for (Guarani g : guaranis) { g.removeFromParent(); }
        guaranis.clear();
        for (Jesuit j : jesuits) { j.removeFromParent(); }
        jesuits.clear();

        if (simulationRootNode != null) {
            simulationRootNode.detachAllChildren(); // Limpa todos os filhos (incluindo fundo)
            createEnvironment(); // Recria o fundo para o próximo enable, se houver
        }
        System.out.println("SimulationAppStates (Motor) limpo.");
    }

    @Override
    protected void onEnable() {
        if (simulationRootNode != null && this.app != null) {
            if (simulationRootNode.getParent() == null) { // Evita adicionar múltiplas vezes
                this.app.getRootNode().attachChild(simulationRootNode);
            }
        }
        // Sincroniza o estado de pausa com a UI ao ser habilitado
        if (uiAppStateRef != null) {
            setSimulationPausedByUi(uiAppStateRef.isPaused());
        } else {
            // Se não houver UI state, pode definir um default, ex: começar pausado
            // this.internalPauseSignal = true;
        }
    }

    @Override
    protected void onDisable() {
        if (simulationRootNode != null && simulationRootNode.getParent() != null) {
            simulationRootNode.getParent().detachChild(simulationRootNode);
        }
        // Não necessariamente define internalPauseSignal = true aqui, pois pode ser
        // desabilitado temporariamente pelo StateManager por outras razões.
        // A pausa da UI é a principal controladora externa.
    }

    public void setSimulationPausedByUi(boolean paused) {
        this.internalPauseSignal = paused;
    }

    // Getters para o SimulationAppState (UI) e StatsUpdater
    public int getActiveGuaraniCount() { return guaranis.size(); }
    public int getActiveJesuitCount() { return jesuits.size(); }
    public int getCurrentSimulationLoop() { return currentSimulationLoop; }
    public List<Guarani> getGuaranis() { return guaranis; } // Usado por AgentControl
    public List<Jesuit> getJesuits() { return jesuits; }   // Usado por AgentControl
    public ResourceManager getResourceManager() { return resourceManager; } // Usado por AgentControl

    public String determineWinner() {
        boolean guaranisExist = !guaranis.isEmpty();
        boolean jesuitsExist = !jesuits.isEmpty();

        // Verifica se o limite de loops foi atingido
        if (currentSimulationLoop >= configMaxLoops && configMaxLoops > 0) {
            if (guaranis.size() > jesuits.size()) return "Guarani (Tempo)";
            if (jesuits.size() > guaranis.size()) return "Jesuita (Tempo)";
            return "Empate (Tempo)";
        }

        // Verifica condições de extermínio
        if (guaranisExist && !jesuitsExist) return "Guarani";
        if (!jesuitsExist && jesuitsExist) return "Jesuita";
        if (!guaranisExist && !jesuitsExist && currentSimulationLoop > 0) return "Empate (Extinção)"; // Evita "Empate" no loop 0

        return "-"; // Jogo em andamento
    }
}