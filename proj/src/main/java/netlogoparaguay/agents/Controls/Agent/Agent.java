package netlogoparaguay.agents.Controls.Agent;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import netlogoparaguay.agents.Controls.controller.AgentControl;
import netlogoparaguay.resources.ResourceType;
import netlogoparaguay.simulation.SimulationAppStates; // Import necessário

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
    protected static final int VITALITY_NEEDED_TO_MULTIPLY = 3; // Exemplo: vitalidade necessária
    protected static final int RESOURCES_COLLECTED_TO_MULTIPLY = 5; // Exemplo: total de recursos de qualquer tipo
    protected int resourcesCollectedTotal = 0; // Contador para multiplicação
    protected boolean hasMultipliedThisCycle = false; // Para controlar a frequência de multiplicação

    // Referência ao gerenciador da simulação para interações globais (ex: pedir para multiplicar)
    protected SimulationAppStates simulationManager;

    /**
     * Construtor para um Agente.
     * @param name Nome do agente (geralmente "Agent" ou tipo específico).
     * @param assetManager O gerenciador de assets para carregar modelos.
     * @param simulationManager Referência ao SimulationAppStates para interações com o mundo.
     */
    public Agent(String name, AssetManager assetManager, SimulationAppStates simulationManager) {
        super(name); // Nome do Node (ex: "Guarani_1", "Jesuit_5")
        this.simulationManager = simulationManager;

        // Carrega o modelo 3D específico do agente (definido nas subclasses)
        Spatial model = loadModel(assetManager);
        if (model != null) {
            this.attachChild(model);
        } else {
            System.err.println("Modelo para o agente " + name + " não pôde ser carregado!");
        }

        // Cria e adiciona o controle de comportamento específico do agente
        AgentControl control = createControl(); // O controle agora pegará simulationManager e resourceManager de 'this.simulationManager'
        if (control != null) {
            this.addControl(control);
        } else {
            System.err.println("Controle para o agente " + name + " não pôde ser criado!");
        }

        // Inicializa atributos baseados na vitalidade, se necessário
        this.health = calculateMaxHealth();
    }

    /**
     * Método abstrato para carregar o modelo 3D (Spatial) específico do agente.
     * Deve ser implementado pelas subclasses (Guarani, Jesuit).
     * @param assetManager O gerenciador de assets.
     * @return O Spatial que representa o agente.
     */
    protected abstract Spatial loadModel(AssetManager assetManager);

    /**
     * Método abstrato para criar o AgentControl específico para este tipo de agente.
     * Deve ser implementado pelas subclasses. O control pode acessar o simulationManager
     * e o resourceManager através do `this.simulationManager` do agente.
     * @return O AgentControl para este agente.
     */
    protected abstract AgentControl createControl();

    /**
     * Aplica dano ao agente. A vitalidade pode reduzir o dano sofrido.
     * Se a vida chegar a zero ou menos, o agente é marcado como morto.
     * @param amount A quantidade base de dano.
     */
    public void takeDamage(float amount) {
        if (isDead) return; // Não pode receber dano se já estiver morto

        // Vitalidade reduz o dano percentualmente (ex: 2% por ponto de vitalidade, máximo 50%)
        float damageReduction = Math.min(vitality * 0.02f, 0.5f);
        float actualDamage = amount * (1 - damageReduction);

        health -= actualDamage;
        // System.out.println(this.getName() + " tomou " + actualDamage + " de dano, vida restante: " + health);

        if (health <= 0) {
            health = 0;
            isDead = true;
            // System.out.println(this.getName() + " morreu!");
            // O AgentControl ou SimulationAppStates cuidará da remoção da lista de ativos
            // e possivelmente da remoção do Spatial da cena.
            if (simulationManager != null) {
                simulationManager.notifyAgentDeath(this);
            }
        }
    }

    /**
     * "Ressuscita" o agente, restaurando sua vida e redefinindo seu estado para vivo.
     * Usado se houver uma mecânica de respawn para agentes individuais.
     * @param position A nova posição para o agente após o respawn.
     */
    public void respawn(Vector3f position) {
        health = calculateMaxHealth();
        isDead = false;
        hasMultipliedThisCycle = false; // Permite multiplicar novamente após respawn
        resourcesCollectedTotal = 0;   // Reseta contagem de recursos para multiplicação
        setLocalTranslation(position);
        // System.out.println(this.getName() + " respawnou em " + position);
    }

    /**
     * Processa a coleta de um recurso.
     * Aumenta atributos específicos com base no tipo de recurso e contabiliza para multiplicação.
     * @param type O {@link ResourceType} do recurso coletado.
     */
    public void collectResource(ResourceType type) {
        if (isDead) return;

        resourcesCollectedTotal++; // Contabiliza para multiplicação

        switch (type) {
            case WOOD:
                strength += 1;
                // System.out.println(this.getName() + " coletou WOOD. Força: " + strength);
                break;
            case SOY:
                vitality += 1;
                // Ao ganhar vitalidade, a vida atual pode aumentar e a máxima também
                float oldMaxHealth = calculateMaxHealth() - 10; // Max health antes de adicionar este ponto de vitalidade
                health = Math.min(health + 20, calculateMaxHealth()); // Cura e limita à nova vida máxima
                // System.out.println(this.getName() + " coletou SOY. Vitalidade: " + vitality + ", Vida: " + health + "/" + calculateMaxHealth());
                break;
            case MATE:
                speedPoints += 1;
                // Notifica o AgentControl para atualizar sua velocidade de movimento real
                AgentControl control = getControl(AgentControl.class);
                if (control != null) {
                    control.updateEffectiveSpeed();
                }
                // System.out.println(this.getName() + " coletou MATE. Pontos de Velocidade: " + speedPoints);
                break;
        }
    }

    /**
     * Verifica se o agente atende às condições para se multiplicar.
     * @return true se pode multiplicar, false caso contrário.
     */
    public boolean canMultiply() {
        // Condições: não estar morto, não ter multiplicado neste "ciclo" (para evitar spam),
        // e ter atingido os limiares de recursos/vitalidade.
        return !isDead && !hasMultipliedThisCycle &&
                vitality >= VITALITY_NEEDED_TO_MULTIPLY &&
                resourcesCollectedTotal >= RESOURCES_COLLECTED_TO_MULTIPLY;
    }

    /**
     * Chamado quando o agente se multiplica com sucesso.
     * Reseta os contadores/flags relevantes para evitar multiplicação imediata e contínua.
     */
    public void didMultiply() {
        this.hasMultipliedThisCycle = true; // Impede de multiplicar de novo logo em seguida
        // Opcional: consumir os recursos/stats usados para a multiplicação
        // this.vitality -= VITALITY_NEEDED_TO_MULTIPLY;
        // this.resourcesCollectedTotal = 0; // ou -= RESOURCES_COLLECTED_TO_MULTIPLY
        // Para um comportamento mais simples, apenas o flag hasMultipliedThisCycle pode ser suficiente
        // e os stats permanecem, tornando o agente mais forte para o futuro.
        // Para permitir futuras multiplicações, hasMultipliedThisCycle precisaria ser resetado
        // (ex: após um tempo, ou se a população diminuir). Por agora, é uma vez por "vida" ou ciclo.
    }

    /**
     * Reseta o flag que impede a multiplicação, permitindo que o agente tente multiplicar novamente.
     * Pode ser chamado periodicamente ou sob certas condições do jogo.
     */
    public void resetMultiplicationPossibility() {
        this.hasMultipliedThisCycle = false;
    }


    /**
     * Calcula a vida máxima do agente com base na sua vitalidade.
     * @return A vida máxima calculada.
     */
    public float calculateMaxHealth() {
        return 100 + vitality * 10; // Ex: cada ponto de vitalidade adiciona 10 de vida máxima
    }

    // --- Getters ---
    public boolean isDead() { return isDead; }
    public float getHealth() { return health; }
    public int getStrength() { return strength; }
    public int getSpeedPoints() { return speedPoints; } // Renomeado de getSpeed para clareza
    public int getVitality() { return vitality; }
    public SimulationAppStates getSimulationManager() { return simulationManager; }

    /**
     * Obtém o AgentControl associado a este agente.
     * @return O AgentControl, ou null se não houver.
     */
    public AgentControl getAgentControl() {
        return getControl(AgentControl.class);
    }
}