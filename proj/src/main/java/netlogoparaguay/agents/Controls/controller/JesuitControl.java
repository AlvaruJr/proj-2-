package netlogoparaguay.agents.Controls.controller;

import java.util.List;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.agents.Controls.Agent.Guarani; // Importa a classe Guarani para ser o alvo
// import com.jme3.math.Vector3f; // Necessário se for sobrescrever handleAttacking

/**
 * Controle específico para o comportamento dos agentes Jesuitas.
 * Herda a maior parte da lógica de AgentControl e define que os Guaranis são seus inimigos.
 * [MODIFICADO] Atributos ajustados para balanceamento.
 */
public class JesuitControl extends AgentControl {

    /**
     * Construtor para JesuitControl.
     * Configura os parâmetros específicos para os Jesuitas.
     * As referências a simulationManager e resourceManager são obtidas
     * através do método setSpatial da classe AgentControl.
     */
    public JesuitControl() {
        super(); // Chama o construtor de AgentControl

        // --- VALORES ORIGINAIS COMENTADOS PARA REFERÊNCIA ---
        // this.baseSpeed = 2.5f;
        // this.attackRange = 1.7f;
        // this.attackCooldownBase = 2.2f;

        // --- NOVOS VALORES PARA BALANCEAMENTO ---
        // Objetivo: Tornar os Jesuítas mais competitivos contra os Guaranis.
        // Guaranis (para referência): baseSpeed = 2.8f, attackCooldownBase = 1.8f, attackRange = 1.6f

        // Aumenta a velocidade base dos Jesuitas.
        // Ainda um pouco mais lentos que os Guaranis, mas a diferença é menor.
        this.baseSpeed = 2.7f; // Anteriormente 2.5f

        // Mantém o alcance de ataque ligeiramente maior.
        // Pode ser uma pequena vantagem tática se conseguirem atacar antes.
        this.attackRange = 1.7f; // Sem alteração

        // Reduz significativamente o cooldown de ataque dos Jesuitas.
        // Eles atacarão com mais frequência, aproximando-se da cadência dos Guaranis.
        this.attackCooldownBase = 1.95f; // Anteriormente 2.2f

        // A effectiveSpeed será calculada em setSpatial -> updateEffectiveSpeed com base no baseSpeed.

        System.out.println("JesuitControl: Velocidade base=" + this.baseSpeed +
                ", Alcance Ataque=" + this.attackRange +
                ", Cooldown Ataque=" + this.attackCooldownBase);
    }

    /**
     * Encontra o agente Guarani mais próximo para ser definido como inimigo.
     * Percorre a lista de Guaranis ativos fornecida pelo simulationManager.
     * @return O Agent (Guarani) inimigo mais próximo dentro do raio de visão, ou null se nenhum for encontrado.
     */
    @Override
    protected Agent findClosestEnemy() {
        if (simulationManager == null) {
            // System.err.println("JesuitControl: simulationManager é nulo, não pode encontrar inimigos.");
            return null;
        }

        List<Guarani> guaranis = simulationManager.getGuaranis(); // Obtém a lista de Guaranis ativos
        if (guaranis == null || guaranis.isEmpty()) {
            return null; // Nenhum Guarani para atacar
        }

        Agent closestEnemy = null;
        // Usa o visionRadius definido na classe base AgentControl
        float minDistanceSq = visionRadius * visionRadius;

        for (Guarani guaraniTarget : guaranis) {
            // Verifica se o guarani está vivo e é um alvo válido
            if (guaraniTarget != null && !guaraniTarget.isDead()) {
                // Calcula a distância quadrada para otimização (evita Math.sqrt)
                float distSq = agent.getLocalTranslation().distanceSquared(guaraniTarget.getLocalTranslation());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestEnemy = guaraniTarget;
                }
            }
        }
        return closestEnemy;
    }

    /**
     * (Opcional) Sobrescrever 'handleAttacking' para dar aos Jesuítas uma fórmula de dano diferente.
     * Se descomentar este método, os Jesuítas usarão esta lógica de ataque em vez da de AgentControl.
     * Isso pode ser usado para, por exemplo, dar-lhes um bônus de dano base ou um multiplicador de força diferente.
     */
    /*
    @Override
    protected void handleAttacking(float tpfForLogic) {
        if (currentEnemyTarget == null || currentEnemyTarget.isDead()) {
            currentState = AgentState.IDLE;
            currentEnemyTarget = null;
            return;
        }

        // Se o inimigo saiu do alcance, volta a persegui-lo
        if (agent.getLocalTranslation().distance(currentEnemyTarget.getLocalTranslation()) > attackRange) {
            currentState = AgentState.SEEKING_ENEMY;
            return;
        }

        // Se o cooldown do ataque terminou
        if (currentAttackCooldown <= 0) {
            agent.lookAt(currentEnemyTarget.getLocalTranslation(), Vector3f.UNIT_Y); // Agente olha para o inimigo

            // --- Fórmula de Dano Modificada para Jesuítas ---
            // Fórmula original em AgentControl: agent.getStrength() * 2.0f + 5.0f;
            // Exemplo: Jesuítas têm um bônus de +1 no dano base
            float damage = agent.getStrength() * 2.0f + 6.5f;
            // Ou um multiplicador de força ligeiramente maior:
            // float damage = agent.getStrength() * 2.1f + 5.0f;

            System.out.println(agent.getName() + " (Jesuit) ataca " + currentEnemyTarget.getName() + " causando " + damage + " de dano.");
            currentEnemyTarget.takeDamage(damage); // Aplica o dano ao inimigo

            currentAttackCooldown = attackCooldownBase; // Reinicia o cooldown de ataque (usa o attackCooldownBase do JesuitControl)

            // Se o inimigo morreu após o ataque
            if (currentEnemyTarget.isDead()) {
                System.out.println(currentEnemyTarget.getName() + " foi derrotado por " + agent.getName());
                currentEnemyTarget = null; // Limpa o alvo
                currentState = AgentState.IDLE; // Volta ao estado IDLE
            }
        }
    }
    */
}