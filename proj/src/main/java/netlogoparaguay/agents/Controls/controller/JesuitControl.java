package netlogoparaguay.agents.Controls.controller;

import java.util.List;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.agents.Controls.Agent.Guarani;

public class JesuitControl extends AgentControl {

    public JesuitControl() {
        super();

        // MODIFICAÇÃO: Velocidade base do Jesuíta foi reduzida.
        // O valor original era 2.7f.
        this.baseSpeed = 1.5f;

        // Outros atributos permanecem como estavam.
        this.attackRange = 1.7f;
        this.attackCooldownBase = 1.95f;
    }

    @Override
    protected Agent findClosestEnemy() {
        if (simulationManager == null) {
            return null;
        }

        List<Guarani> guaranis = simulationManager.getGuaranis();
        if (guaranis == null || guaranis.isEmpty()) {
            return null;
        }

        Agent closestEnemy = null;
        float minDistanceSq = visionRadius * visionRadius;

        for (Guarani guaraniTarget : guaranis) {
            if (guaraniTarget != null && !guaraniTarget.isDead()) {
                float distSq = agent.getLocalTranslation().distanceSquared(guaraniTarget.getLocalTranslation());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestEnemy = guaraniTarget;
                }
            }
        }
        return closestEnemy;
    }
}