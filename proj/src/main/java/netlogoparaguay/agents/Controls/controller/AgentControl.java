package netlogoparaguay.agents.Controls.controller;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.List;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.resources.Resource; // Importar a classe Resource
import netlogoparaguay.resources.ResourceManager;
import netlogoparaguay.simulation.SimulationAppStates;

/**
 * Classe abstrata base para o controle de comportamento dos Agentes.
 * Define a lógica de atualização, movimentação e interações básicas com o ambiente,
 * como ataque, coleta de recursos e multiplicação.
 * As subclasses devem implementar a tomada de decisão específica (ex: quem atacar, quando procurar recursos).
 */
public abstract class AgentControl extends AbstractControl {

    // Enum para definir os possíveis estados do agente, ajudando a organizar a lógica.
    protected enum AgentState {
        IDLE,               // Ocioso ou vagando sem um objetivo específico
        SEEKING_RESOURCE,   // Movendo-se em direção a um recurso
        COLLECTING_RESOURCE,// No ato de coletar um recurso (pode ser instantâneo ou levar tempo)
        SEEKING_ENEMY,      // Movendo-se em direção a um inimigo
        ATTACKING,          // No ato de atacar um inimigo
        FLEEING             // Fugindo de uma ameaça (não implementado neste exemplo)
    }

    protected Agent agent;                      // Referência ao Agente (o Spatial que este controle gerencia)
    protected SimulationAppStates simulationManager;  // Gerenciador da simulação para acesso a listas de agentes, etc.
    protected ResourceManager resourceManager;    // Gerenciador de recursos para encontrar e interagir com recursos

    // Atributos de movimento
    protected float baseSpeed = 2.0f;           // Velocidade base do agente, pode ser modificada por atributos
    protected float effectiveSpeed;             // Velocidade atual de movimento, calculada a partir da baseSpeed e bônus
    protected Vector3f currentMoveTarget;       // Posição para onde o agente está se movendo atualmente
    protected float directionChangeTimer;       // Timer para mudar a direção do movimento aleatório
    protected static final float MIN_WANDER_TIME = 2.0f; // Tempo mínimo antes de mudar direção ao vagar
    protected static final float MAX_WANDER_TIME = 5.0f; // Tempo máximo

    // Atributos de combate
    protected Agent currentEnemyTarget;         // Inimigo atual que o agente está focando
    protected float attackRange = 1.5f;         // Alcance para iniciar um ataque
    protected float attackCooldownBase = 2.0f;  // Tempo base entre ataques
    protected float currentAttackCooldown;      // Timer para o próximo ataque

    // Atributos de coleta
    protected Resource currentResourceTarget;   // Recurso atual que o agente está focando
    protected float collectionRange = 1.0f;     // Alcance para coletar um recurso

    // Constantes de percepção e interação
    protected float visionRadius = 10.0f;       // Raio de "visão" para detectar inimigos ou recursos
    protected static final float TARGET_REACHED_THRESHOLD = 0.5f; // Quão perto precisa estar para considerar o alvo alcançado
    public static final float WORLD_BOUNDS = 30f; // Limites do mundo (para movimento aleatório)

    protected AgentState currentState = AgentState.IDLE; // Estado inicial do agente

    /**
     * Construtor para AgentControl.
     * Obtém referências essenciais do Agente ao qual está anexado.
     * É esperado que o Agente já tenha as referências para simulationManager e resourceManager.
     */
    public AgentControl() {
        // As referências serão inicializadas no método setSpatial,
        // quando o controle é efetivamente adicionado ao Agente.
    }

    /**
     * Chamado quando este controle é anexado a um Spatial (o Agente).
     * Aqui inicializamos as referências importantes.
     * @param spatial O Spatial (Agente) ao qual este controle foi anexado.
     */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial instanceof Agent) {
            this.agent = (Agent) spatial;
            // Obtém os gerenciadores através do Agente, que deve tê-los
            this.simulationManager = this.agent.getSimulationManager();
            if (this.simulationManager != null) {
                this.resourceManager = this.simulationManager.getResourceManager(); // Assumindo que SimulationAppStates tem getResourceManager()
            } else {
                System.err.println("ERRO: SimulationManager não foi definido no Agente " + this.agent.getName() + " para o AgentControl.");
            }
            if (this.resourceManager == null && this.simulationManager != null) {
                System.err.println("ERRO: ResourceManager não pôde ser obtido do SimulationManager para o Agente " + this.agent.getName());
            }
            updateEffectiveSpeed(); // Calcula a velocidade inicial
        } else {
            throw new IllegalStateException("AgentControl só pode ser adicionado a um Spatial do tipo Agent.");
        }
    }

    /**
     * Atualiza a velocidade efetiva do agente com base na sua velocidade base e
     * nos pontos de velocidade acumulados pelo agente.
     */
    public void updateEffectiveSpeed() {
        if (agent != null) {
            // Exemplo: Cada ponto de velocidade aumenta a velocidade base em 5%
            this.effectiveSpeed = this.baseSpeed * (1 + agent.getSpeedPoints() * 0.05f);
        } else {
            this.effectiveSpeed = this.baseSpeed;
        }
    }

    /**
     * Método principal de atualização do controle, chamado a cada frame pelo motor jME.
     * Contém a máquina de estados do agente.
     * @param tpf Tempo por frame.
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (agent == null || agent.isDead() || !isEnabled() || simulationManager == null || resourceManager == null) {
            // Se o agente está morto, desabilitado, ou faltam dependências, não faz nada.
            return;
        }

        // Atualiza cooldowns
        if (currentAttackCooldown > 0) {
            currentAttackCooldown -= tpf;
        }
        if (directionChangeTimer > 0) {
            directionChangeTimer -= tpf;
        }

        // Máquina de Estados principal
        // A ordem de verificação define as prioridades (ex: atacar é mais importante que coletar)
        decideNextState(); // Avalia o ambiente e define o próximo estado se necessário

        switch (currentState) {
            case SEEKING_ENEMY:
                handleSeekingEnemy(tpf);
                break;
            case ATTACKING:
                handleAttacking(tpf);
                break;
            case SEEKING_RESOURCE:
                handleSeekingResource(tpf);
                break;
            case COLLECTING_RESOURCE: // Pode ser instantâneo ou uma sub-estado de SEEKING_RESOURCE
                handleCollectingResource(); // A coleta em si pode ser instantânea
                break;
            case IDLE:
            default:
                handleIdle(tpf); // Comportamento de vagar
                break;
        }

        // Lógica de Multiplicação (pode ocorrer independentemente do estado principal, se as condições forem atendidas)
        tryMultiply();
    }

    /**
     * Avalia o ambiente e decide qual deve ser o próximo estado do agente.
     * Este método é crucial para a IA do agente.
     * As subclasses podem sobrescrever para refinar a tomada de decisão.
     */
    protected void decideNextState() {
        // 1. Prioridade Máxima: Encontrar e atacar inimigos
        currentEnemyTarget = findClosestEnemy(); // Implementado nas subclasses
        if (currentEnemyTarget != null) {
            float distanceToEnemy = agent.getLocalTranslation().distance(currentEnemyTarget.getLocalTranslation());
            if (distanceToEnemy <= attackRange) {
                currentState = AgentState.ATTACKING;
            } else {
                currentState = AgentState.SEEKING_ENEMY;
            }
            return; // Decisão tomada
        }

        // 2. Prioridade Média: Coletar recursos se não houver inimigos próximos
        // Só procura recursos se a vida não estiver cheia ou se precisar de recursos para multiplicar/stats
        if (agent.getHealth() < agent.calculateMaxHealth() || agent.getVitality() < 5 || agent.getStrength() < 5 || agent.getSpeedPoints() < 5 ) { // Condição de exemplo
            currentResourceTarget = findClosestAvailableResource();
            if (currentResourceTarget != null) {
                float distanceToResource = agent.getLocalTranslation().distance(currentResourceTarget.getPosition());
                if (distanceToResource <= collectionRange) {
                    currentState = AgentState.COLLECTING_RESOURCE;
                } else {
                    currentState = AgentState.SEEKING_RESOURCE;
                }
                return; // Decisão tomada
            }
        }

        // 3. Se nenhuma das anteriores, fica ocioso/vagando
        currentState = AgentState.IDLE;
    }

    // --- Métodos de Manipulação de Estado ---

    /**
     * Lida com o comportamento do agente quando está no estado IDLE (vagando).
     * @param tpf Tempo por frame.
     */
    protected void handleIdle(float tpf) {
        if (directionChangeTimer <= 0 || currentMoveTarget == null ||
                agent.getLocalTranslation().distance(currentMoveTarget) < TARGET_REACHED_THRESHOLD) {

            currentMoveTarget = getRandomWanderTarget();
            directionChangeTimer = MIN_WANDER_TIME + FastMath.nextRandomFloat() * (MAX_WANDER_TIME - MIN_WANDER_TIME);
        }
        if (currentMoveTarget != null) {
            moveTo(currentMoveTarget, tpf);
        }
    }

    /**
     * Lida com o comportamento do agente ao se mover em direção a um inimigo.
     * @param tpf Tempo por frame.
     */
    protected void handleSeekingEnemy(float tpf) {
        if (currentEnemyTarget == null || currentEnemyTarget.isDead()) {
            currentState = AgentState.IDLE; // Inimigo morreu ou desapareceu
            currentEnemyTarget = null;
            return;
        }
        moveTo(currentEnemyTarget.getLocalTranslation(), tpf);
        // Se chegou perto o suficiente, o decideNextState() mudará para ATTACKING
    }

    /**
     * Lida com o comportamento do agente ao atacar um inimigo.
     * @param tpf Tempo por frame.
     */
    protected void handleAttacking(float tpf) {
        if (currentEnemyTarget == null || currentEnemyTarget.isDead()) {
            currentState = AgentState.IDLE; // Inimigo morreu ou desapareceu
            currentEnemyTarget = null;
            return;
        }

        // Verifica se ainda está ao alcance
        if (agent.getLocalTranslation().distance(currentEnemyTarget.getLocalTranslation()) > attackRange) {
            currentState = AgentState.SEEKING_ENEMY; // Inimigo se afastou, precisa persegui-lo
            return;
        }

        // Tenta atacar se o cooldown permitir
        if (currentAttackCooldown <= 0) {
            // Olhar para o inimigo (opcional, mas bom para visual)
            agent.lookAt(currentEnemyTarget.getLocalTranslation(), Vector3f.UNIT_Y);

            float damage = agent.getStrength() * 2.0f + 5.0f; // Dano baseado na força (exemplo)
            currentEnemyTarget.takeDamage(damage);
            // System.out.println(agent.getName() + " atacou " + currentEnemyTarget.getName() + " causando " + damage + " de dano.");

            currentAttackCooldown = attackCooldownBase; // Reseta o cooldown

            if (currentEnemyTarget.isDead()) {
                // System.out.println(currentEnemyTarget.getName() + " foi derrotado por " + agent.getName());
                currentEnemyTarget = null; // Limpa o alvo
                currentState = AgentState.IDLE; // Volta a procurar o que fazer
            }
        }
    }

    /**
     * Lida com o comportamento do agente ao se mover em direção a um recurso.
     * @param tpf Tempo por frame.
     */
    protected void handleSeekingResource(float tpf) {
        if (currentResourceTarget == null || !currentResourceTarget.isAvailable()) {
            currentState = AgentState.IDLE; // Recurso desapareceu ou foi coletado por outro
            currentResourceTarget = null;
            return;
        }
        moveTo(currentResourceTarget.getPosition(), tpf);
        // Se chegou perto o suficiente, o decideNextState() mudará para COLLECTING_RESOURCE
    }

    /**
     * Lida com o comportamento do agente ao coletar um recurso.
     * Neste exemplo, a coleta é instantânea.
     */
    protected void handleCollectingResource() {
        if (currentResourceTarget == null || !currentResourceTarget.isAvailable()) {
            currentState = AgentState.IDLE;
            currentResourceTarget = null;
            return;
        }

        // System.out.println(agent.getName() + " coletando " + currentResourceTarget.getType());
        agent.collectResource(currentResourceTarget.getType());
        resourceManager.notifyResourceCollected(currentResourceTarget); // Notifica o manager que o recurso foi coletado

        currentResourceTarget = null; // Limpa o alvo de recurso
        currentState = AgentState.IDLE; // Volta a procurar o que fazer
    }

    // --- Métodos Auxiliares de Comportamento ---

    /**
     * Move o agente em direção a uma posição alvo.
     * @param targetPosition A posição para onde se mover.
     * @param tpf Tempo por frame.
     */
    protected void moveTo(Vector3f targetPosition, float tpf) {
        if (targetPosition == null) return;

        Vector3f direction = targetPosition.subtract(agent.getLocalTranslation());
        direction.setZ(0); // Mantém o movimento no plano XY (ou XZ dependendo da sua câmera)

        if (direction.lengthSquared() > FastMath.FLT_EPSILON) { // Só normaliza e move se não estiver muito perto
            direction.normalizeLocal();
            agent.move(direction.mult(effectiveSpeed * tpf));

            // Faz o agente olhar na direção do movimento (opcional)
            // Isso pode causar giros bruscos se o alvo mudar rapidamente.
            // agent.lookAt(agent.getLocalTranslation().add(direction), Vector3f.UNIT_Y);
        }
    }

    /**
     * Gera uma posição aleatória dentro dos limites do mundo para o agente vagar.
     * @return Um Vector3f com a posição alvo aleatória.
     */
    protected Vector3f getRandomWanderTarget() {
        // Ajuste para que os agentes não fiquem presos nas bordas exatas.
        float padding = 1.0f;
        return new Vector3f(
                FastMath.nextRandomFloat() * (WORLD_BOUNDS - padding * 2) + padding - (WORLD_BOUNDS / 2f), // Centralizado
                FastMath.nextRandomFloat() * (WORLD_BOUNDS - padding * 2) + padding - (WORLD_BOUNDS / 2f), // Centralizado
                0 // Assume que Z=0 é o plano do chão
        );
    }

    /**
     * Encontra o recurso disponível mais próximo dentro do raio de visão.
     * @return O {@link Resource} mais próximo, ou null se nenhum for encontrado.
     */
    protected Resource findClosestAvailableResource() {
        if (resourceManager == null) return null;

        List<Resource> availableResources = resourceManager.getAvailableResources(); // Precisa deste método no ResourceManager
        Resource closestResource = null;
        float minDistanceSq = visionRadius * visionRadius; // Compara distâncias quadradas para evitar sqrt

        for (Resource resource : availableResources) {
            if (resource.isAvailable()) { // Dupla verificação, pois a lista pode ter sido cacheada
                float distSq = agent.getLocalTranslation().distanceSquared(resource.getPosition());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestResource = resource;
                }
            }
        }
        return closestResource;
    }

    /**
     * Tenta realizar a multiplicação se as condições do agente forem atendidas.
     */
    protected void tryMultiply() {
        if (agent.canMultiply()) {
            boolean success = simulationManager.requestAgentMultiplication(agent);
            if (success) {
                agent.didMultiply(); // Notifica o agente que ele se multiplicou
                // System.out.println(agent.getName() + " se multiplicou com sucesso!");
            }
        }
    }

    // --- Métodos Abstratos a serem implementados pelas subclasses ---

    /**
     * Encontra o inimigo mais próximo. A definição de "inimigo" depende da subclasse.
     * (ex: Guarani procura Jesuit, Jesuit procura Guarani).
     * @return O {@link Agent} inimigo mais próximo, ou null se nenhum for encontrado.
     */
    protected abstract Agent findClosestEnemy();


    // --- Implementação de Métodos de Controle Abstratos ---
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Normalmente não é necessário para controles de IA, a menos que queira desenhar debug.
    }
}