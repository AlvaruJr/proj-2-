package netlogoparaguay.agents.Controls.controller;

import java.util.List;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.agents.Controls.Agent.Guarani;
import netlogoparaguay.agents.Controls.Agent.Jesuit;
import netlogoparaguay.resources.Resource;

public class GuaraniControl extends AgentControl {

    public GuaraniControl() {
        super();
        this.baseSpeed = 2.8f;
        this.attackRange = 1.6f;
        this.attackCooldownBase = 1.8f;
    }

    @Override
    protected Agent findClosestEnemy() {
        if (simulationManager == null) {
            return null;
        }

        List<Jesuit> jesuits = simulationManager.getJesuits();
        if (jesuits == null || jesuits.isEmpty()) {
            return null;
        }

        Agent closestEnemy = null;
        float minDistanceSq = visionRadius * visionRadius;

        for (Jesuit jesuit : jesuits) {
            if (jesuit != null && !jesuit.isDead()) {
                float distSq = agent.getLocalTranslation().distanceSquared(jesuit.getLocalTranslation());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestEnemy = jesuit;
                }
            }
        }
        return closestEnemy;
    }

    /**
     * MODIFICAÇÃO PRINCIPAL: Lógica de decisão para Guaranis.
     * Eles agora verificam por aliados antes de decidir entre lutar ou fugir.
     */
    @Override
    protected void decideNextState() {
        // 1. Primeiro, encontre o inimigo mais próximo.
        currentEnemyTarget = findClosestEnemy();

        // 2. Se um inimigo foi encontrado, decida entre lutar ou fugir.
        if (currentEnemyTarget != null && !currentEnemyTarget.isDead()) {

            // 2a. Conte quantos Guaranis aliados estão por perto.
            int alliesNearby = 0;
            if (simulationManager != null) {
                // Pega a lista de todos os guaranis da simulação
                List<Guarani> allGuaranis = simulationManager.getGuaranis();
                for (Guarani otherGuarani : allGuaranis) {
                    if (otherGuarani == this.agent) {
                        continue; // Não conte a si mesmo
                    }
                    // Se outro guarani estiver dentro do raio de visão, conte como aliado próximo
                    if (this.agent.getLocalTranslation().distanceSquared(otherGuarani.getLocalTranslation()) < visionRadius * visionRadius) {
                        alliesNearby++;
                    }
                }
            }

            // 2b. Tome a decisão com base no número de aliados.
            if (alliesNearby >= 1) {
                // SE TEM AJUDA: Comporte-se normalmente (atacar ou perseguir)
                float distanceToEnemy = agent.getLocalTranslation().distance(currentEnemyTarget.getLocalTranslation());
                if (distanceToEnemy <= attackRange) {
                    currentState = AgentState.ATTACKING;
                } else {
                    currentState = AgentState.SEEKING_ENEMY;
                }
            } else {
                // SE ESTÁ SOZINHO: Fuja!
                currentState = AgentState.FLEEING;
            }
            return; // A decisão foi tomada, não precisa continuar.
        }

        // 3. Se NENHUM inimigo foi encontrado, volte ao comportamento padrão
        // (procurar recursos ou vagar). Esta lógica é a mesma da classe base.
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

        // Se não há inimigos e não precisa de recursos, fique no estado IDLE (vagando).
        currentState = AgentState.IDLE;
    }
}