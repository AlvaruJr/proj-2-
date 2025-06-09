package netlogoparaguay.agents.Controls.controller;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.List;
import java.util.Random;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.resources.Resource;
import netlogoparaguay.resources.ResourceManager;
import netlogoparaguay.simulation.SimulationAppState;
import netlogoparaguay.simulation.SimulationAppStates;

public abstract class AgentControl extends AbstractControl {

    // MODIFICAÇÃO: Adicionado o estado FLEEING
    protected enum AgentState {
        IDLE, SEEKING_RESOURCE, COLLECTING_RESOURCE, SEEKING_ENEMY, ATTACKING, FLEEING
    }

    protected Agent agent;
    protected SimulationAppStates simulationManager;
    protected ResourceManager resourceManager;

    protected float baseSpeed = 2.0f;
    protected float effectiveSpeed;
    protected Vector3f currentMoveTarget;
    protected float directionChangeTimer;
    protected static final float MIN_WANDER_TIME = 2.0f;
    protected static final float MAX_WANDER_TIME = 5.0f;

    protected Agent currentEnemyTarget;
    protected float attackRange = 1.5f;
    protected float attackCooldownBase = 2.0f;
    protected float currentAttackCooldown;

    protected Resource currentResourceTarget;
    protected float collectionRange = 1.0f;

    protected float visionRadius = 10.0f;
    public static final float WORLD_BOUNDS = 30f;

    protected static final float TARGET_REACHED_THRESHOLD = 0.5f;
    protected AgentState currentState = AgentState.IDLE;

    private Random random;

    public AgentControl() {
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial instanceof Agent) {
            this.agent = (Agent) spatial;
            this.simulationManager = this.agent.getSimulationManager();
            if (this.simulationManager != null) {
                this.random = this.simulationManager.random;
                this.resourceManager = this.simulationManager.getResourceManager();
                if (this.resourceManager == null) {
                    System.err.println("ERRO CRÍTICO: ResourceManager é NULO em AgentControl para " + this.agent.getName() + ". Recursos não funcionarão.");
                }
            } else {
                System.err.println("ERRO: SimulationManager não foi definido no Agente " + this.agent.getName() + " para o AgentControl.");
            }
            updateEffectiveSpeed();
        } else {
            throw new IllegalStateException("AgentControl só pode ser adicionado a um Spatial do tipo Agent.");
        }
    }
    public void updateEffectiveSpeed() {
        if (agent != null) {
            this.effectiveSpeed = this.baseSpeed * (1 + agent.getSpeedPoints() * 0.05f);
        } else {
            this.effectiveSpeed = this.baseSpeed;
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (agent == null || agent.isDead() || !isEnabled() || simulationManager == null) {
            return;
        }

        if (resourceManager == null && currentState != AgentState.IDLE && currentState != AgentState.SEEKING_ENEMY && currentState != AgentState.ATTACKING) {
            currentState = AgentState.IDLE;
        }


        float currentSimSpeed = 1.0f;
        SimulationAppState uiState = simulationManager.getUiAppStateReference();
        if (uiState != null) {
            currentSimSpeed = uiState.getSimulationSpeed();
            if (uiState.isPaused()) {
                return;
            }
        }

        float effectiveTpf = tpf * currentSimSpeed;

        if (currentAttackCooldown > 0) {
            currentAttackCooldown -= effectiveTpf;
        }
        if (directionChangeTimer > 0) {
            directionChangeTimer -= effectiveTpf;
        }

        decideNextState();

        // MODIFICAÇÃO: Adicionado o case para o novo estado FLEEING
        switch (currentState) {
            case SEEKING_ENEMY:
                handleSeekingEnemy(effectiveTpf);
                break;
            case ATTACKING:
                handleAttacking(effectiveTpf);
                break;
            case FLEEING:
                handleFleeing(effectiveTpf);
                break;
            case SEEKING_RESOURCE:
                handleSeekingResource(effectiveTpf);
                break;
            case COLLECTING_RESOURCE:
                handleCollectingResource();
                break;
            case IDLE:
            default:
                handleIdle(effectiveTpf);
                break;
        }
        tryMultiply();
    }

    protected void decideNextState() {
        currentEnemyTarget = findClosestEnemy();
        if (currentEnemyTarget != null && !currentEnemyTarget.isDead()) {
            float distanceToEnemy = agent.getLocalTranslation().distance(currentEnemyTarget.getLocalTranslation());
            if (distanceToEnemy <= attackRange) {
                currentState = AgentState.ATTACKING;
            } else {
                currentState = AgentState.SEEKING_ENEMY;
            }
            return;
        }
        currentEnemyTarget = null;

        if (resourceManager != null &&
                (agent.getHealth() < agent.calculateMaxHealth() * 0.8f ||
                        agent.getVitality() < 5 ||
                        agent.getStrength() < 3 ||
                        agent.getSpeedPoints() < 3)) {

            currentResourceTarget = findClosestAvailableResource();
            if (currentResourceTarget != null && currentResourceTarget.isAvailable()) {
                float distanceToResource = agent.getLocalTranslation().distance(currentResourceTarget.getPosition());
                if (distanceToResource <= collectionRange) {
                    currentState = AgentState.COLLECTING_RESOURCE;
                } else {
                    currentState = AgentState.SEEKING_RESOURCE;
                }
                return;
            }
            currentResourceTarget = null;
        }
        currentState = AgentState.IDLE;
    }

    protected void handleIdle(float tpfForMovement) {
        if (directionChangeTimer <= 0 || currentMoveTarget == null ||
                agent.getLocalTranslation().distance(currentMoveTarget) < TARGET_REACHED_THRESHOLD) {
            float bounds = WORLD_BOUNDS;
            currentMoveTarget = new Vector3f(
                    (random.nextFloat() - 0.5f) * (bounds - 2f),
                    (random.nextFloat() - 0.5f) * (bounds - 2f),
                    0
            );
            directionChangeTimer = MIN_WANDER_TIME + random.nextFloat() * (MAX_WANDER_TIME - MIN_WANDER_TIME);
        }
        if (currentMoveTarget != null) {
            moveTo(currentMoveTarget, tpfForMovement);
        }
    }

    protected void handleSeekingEnemy(float tpfForMovement) {
        if (currentEnemyTarget == null || currentEnemyTarget.isDead()) {
            currentState = AgentState.IDLE;
            currentEnemyTarget = null;
            return;
        }
        moveTo(currentEnemyTarget.getLocalTranslation(), tpfForMovement);
    }

    // MÉTODO NOVO: Lógica para fugir de um inimigo
    protected void handleFleeing(float tpfForMovement) {
        if (currentEnemyTarget == null || currentEnemyTarget.isDead()) {
            currentState = AgentState.IDLE;
            currentEnemyTarget = null;
            return;
        }

        // Calcula a direção oposta ao inimigo
        Vector3f agentPos = agent.getLocalTranslation();
        Vector3f enemyPos = currentEnemyTarget.getLocalTranslation();
        Vector3f directionAway = agentPos.subtract(enemyPos);

        // Define um ponto de fuga naquela direção
        Vector3f fleeTarget = agentPos.add(directionAway.normalize().mult(visionRadius));

        // Move o agente para o ponto de fuga
        moveTo(fleeTarget, tpfForMovement);

        // O agente para de fugir se o inimigo se afastar muito
        if (agent.getLocalTranslation().distance(currentEnemyTarget.getLocalTranslation()) > visionRadius * 1.5f) {
            currentEnemyTarget = null;
            currentState = AgentState.IDLE;
        }
    }


    protected void handleAttacking(float tpfForLogic) {
        if (currentEnemyTarget == null || currentEnemyTarget.isDead()) {
            currentState = AgentState.IDLE;
            currentEnemyTarget = null;
            return;
        }
        if (agent.getLocalTranslation().distance(currentEnemyTarget.getLocalTranslation()) > attackRange * 1.1f) {
            currentState = AgentState.SEEKING_ENEMY;
            return;
        }
        if (currentAttackCooldown <= 0) {
            agent.lookAt(currentEnemyTarget.getLocalTranslation(), Vector3f.UNIT_Y);
            float damage = agent.getStrength() * 2.0f + 5.0f;
            currentEnemyTarget.takeDamage(damage);
            currentAttackCooldown = attackCooldownBase;
            if (currentEnemyTarget.isDead()) {
                currentEnemyTarget = null;
                currentState = AgentState.IDLE;
            }
        }
    }

    protected void handleSeekingResource(float tpfForMovement) {
        if (currentResourceTarget == null || !currentResourceTarget.isAvailable() || resourceManager == null) {
            currentState = AgentState.IDLE;
            currentResourceTarget = null;
            return;
        }
        moveTo(currentResourceTarget.getPosition(), tpfForMovement);
    }

    protected void handleCollectingResource() {
        if (currentResourceTarget == null || !currentResourceTarget.isAvailable() || resourceManager == null) {
            currentState = AgentState.IDLE;
            currentResourceTarget = null;
            return;
        }
        agent.collectResource(currentResourceTarget.getType());
        resourceManager.notifyResourceCollected(currentResourceTarget);
        currentResourceTarget = null;
        currentState = AgentState.IDLE;
    }

    protected void moveTo(Vector3f targetPosition, float tpfForMovement) {
        if (targetPosition == null || agent == null) return;
        Vector3f agentPos = agent.getLocalTranslation();
        Vector3f direction = targetPosition.subtract(agentPos);
        direction.setZ(0);
        if (direction.lengthSquared() > FastMath.FLT_EPSILON) {
            direction.normalizeLocal();
            Vector3f movement = direction.mult(effectiveSpeed * tpfForMovement);
            Vector3f nextPos = agentPos.add(movement);
            float halfWidth = WORLD_BOUNDS / 2f - 0.5f;
            nextPos.x = FastMath.clamp(nextPos.x, -halfWidth, halfWidth);
            nextPos.y = FastMath.clamp(nextPos.y, -halfWidth, halfWidth);
            agent.setLocalTranslation(nextPos);
        }
    }

    protected Resource findClosestAvailableResource() {
        if (resourceManager == null) return null;
        List<Resource> availableResources = resourceManager.getAvailableResources();
        if (availableResources == null || availableResources.isEmpty()) return null;
        Resource closestResource = null;
        float minDistanceSq = visionRadius * visionRadius;
        for (Resource resource : availableResources) {
            if (resource.isAvailable()) {
                float distSq = agent.getLocalTranslation().distanceSquared(resource.getPosition());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestResource = resource;
                }
            }
        }
        return closestResource;
    }

    protected void tryMultiply() {
        if (agent.canMultiply()) {
            boolean success = simulationManager.requestAgentMultiplication(agent);
            if (success) {
                agent.didMultiply();
            }
        }
    }

    protected abstract Agent findClosestEnemy();

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}