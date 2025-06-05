package netlogoparaguay.agents.Controls.Agent;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import netlogoparaguay.agents.Controls.controller.AgentControl;
import netlogoparaguay.resources.ResourceType;
import netlogoparaguay.simulation.SimulationAppStates;

/**
 * Classe base abstrata para todos os agentes na simulação.
 * Um Agente é um Node na cena do jMonkeyEngine, contendo seu modelo visual
 * e seus atributos básicos como vida, força, velocidade e vitalidade.
 * Ele também possui um AgentControl para gerenciar seu comportamento.
 */
public abstract class Agent extends Node {
    // Atributos básicos do agente
    protected float health = 100;
    protected boolean isDead = false;
    protected int strength = 0; // Afeta o dano de ataque
    protected int speedPoints = 0;    // Afeta a velocidade de movimento (pontos de velocidade)
    protected int vitality = 0;   // Afeta a vida máxima e regeneração/dano absorvido

    // Atributos para lógica de multiplicação
    protected static final int VITALITY_NEEDED_TO_MULTIPLY = 3;
    protected static final int RESOURCES_COLLECTED_TO_MULTIPLY = 5;
    protected int resourcesCollectedTotal = 0;
    protected boolean hasMultipliedThisCycle = false;

    // Referência ao gerenciador da simulação para interações globais
    protected SimulationAppStates simulationManager;

    /**
     * Construtor para um Agente.
     * @param name Nome do agente.
     * @param assetManager O gerenciador de assets para carregar modelos.
     * @param simulationManager Referência ao SimulationAppStates para interações com o mundo.
     */
    public Agent(String name, AssetManager assetManager, SimulationAppStates simulationManager) {
        super(name);
        this.simulationManager = simulationManager;

        Spatial model = loadModel(assetManager);
        if (model != null) {
            this.attachChild(model);
        } else {
            System.err.println("Modelo para o agente " + name + " não pôde ser carregado!");
        }

        AgentControl control = createControl();
        if (control != null) {
            this.addControl(control);
        } else {
            System.err.println("Controle para o agente " + name + " não pôde ser criado!");
        }

        this.health = calculateMaxHealth();
    }

    /**
     * Método abstrato para carregar o modelo 3D (Spatial) específico do agente.
     * @param assetManager O gerenciador de assets.
     * @return O Spatial que representa o agente.
     */
    protected abstract Spatial loadModel(AssetManager assetManager);

    /**
     * Método abstrato para criar o AgentControl específico para este tipo de agente.
     * @return O AgentControl para este agente.
     */
    protected abstract AgentControl createControl();

    /**
     * Aplica dano ao agente.
     * @param amount A quantidade base de dano.
     */
    public void takeDamage(float amount) {
        if (isDead) return;

        float damageReduction = Math.min(vitality * 0.02f, 0.5f);
        float actualDamage = amount * (1 - damageReduction);

        health -= actualDamage;

        if (health <= 0) {
            health = 0;
            isDead = true;
            if (simulationManager != null) {
                simulationManager.notifyAgentDeath(this);
            }
        }
    }

    /**
     * "Ressuscita" o agente.
     * @param position A nova posição para o agente após o respawn.
     */
    public void respawn(Vector3f position) {
        health = calculateMaxHealth();
        isDead = false;
        hasMultipliedThisCycle = false;
        resourcesCollectedTotal = 0;
        setLocalTranslation(position);
    }

    /**
     * Processa a coleta de um recurso.
     * @param type O {@link ResourceType} do recurso coletado.
     */
    public void collectResource(ResourceType type) {
        if (isDead) return;

        resourcesCollectedTotal++;

        switch (type) {
            case WOOD:
                strength += 1;
                break;
            case SOY:
                vitality += 1;
                health = Math.min(health + 20, calculateMaxHealth());
                break;
            case MATE:
                speedPoints += 1;
                AgentControl control = getControl(AgentControl.class);
                if (control != null) {
                    control.updateEffectiveSpeed();
                }
                break;
        }
    }

    /**
     * Verifica se o agente atende às condições para se multiplicar.
     * @return true se pode multiplicar, false caso contrário.
     */
    public boolean canMultiply() {
        return !isDead && !hasMultipliedThisCycle &&
                vitality >= VITALITY_NEEDED_TO_MULTIPLY &&
                resourcesCollectedTotal >= RESOURCES_COLLECTED_TO_MULTIPLY;
    }

    /**
     * Chamado quando o agente se multiplica com sucesso.
     */
    public void didMultiply() {
        this.hasMultipliedThisCycle = true;
    }

    /**
     * Reseta o flag que impede a multiplicação, permitindo que o agente tente multiplicar novamente.
     * (O nome deste método foi corrigido do typo anterior).
     */
    public void resetMultiplicationPossibility() {
        this.hasMultipliedThisCycle = false;
    }

    /**
     * Calcula a vida máxima do agente com base na sua vitalidade.
     * @return A vida máxima calculada.
     */
    public float calculateMaxHealth() {
        return 100 + vitality * 10;
    }

    // --- Getters ---
    public boolean isDead() { return isDead; }
    public float getHealth() { return health; }
    public int getStrength() { return strength; }
    public int getSpeedPoints() { return speedPoints; }
    public int getVitality() { return vitality; }
    public SimulationAppStates getSimulationManager() { return simulationManager; }

    public AgentControl getAgentControl() {
        return getControl(AgentControl.class);
    }
}