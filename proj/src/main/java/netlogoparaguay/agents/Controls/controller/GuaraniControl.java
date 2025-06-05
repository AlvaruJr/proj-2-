package netlogoparaguay.agents.Controls.controller;

import java.util.List;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.agents.Controls.Agent.Jesuit; // Importa a classe Jesuit para ser o alvo

/**
 * Controle específico para o comportamento dos agentes Guarani.
 * Herda a maior parte da lógica de AgentControl e define que os Jesuítas são seus inimigos.
 */
public class GuaraniControl extends AgentControl {

    /**
     * Construtor para GuaraniControl.
     * Configura os parâmetros específicos para os Guaranis, como velocidade base.
     * As referências a simulationManager e resourceManager são obtidas
     * através do método setSpatial da classe AgentControl quando este controle
     * é anexado a um Agente Guarani.
     */
    public GuaraniControl() {
        super(); // Chama o construtor de AgentControl
        this.baseSpeed = 2.8f;      // Guaranis podem ser um pouco mais rápidos por padrão
        this.attackRange = 1.6f;    // Alcance de ataque ligeiramente diferente
        this.attackCooldownBase = 1.8f; // Cooldown de ataque um pouco menor
        // A effectiveSpeed será calculada em setSpatial -> updateEffectiveSpeed
    }

    /**
     * Encontra o agente Jesuit mais próximo para ser definido como inimigo.
     * Percorre a lista de Jesuítas ativos fornecida pelo simulationManager.
     * @return O Agent (Jesuit) inimigo mais próximo dentro do raio de visão, ou null se nenhum for encontrado.
     */
    @Override
    protected Agent findClosestEnemy() {
        if (simulationManager == null) {
            // System.err.println("GuaraniControl: simulationManager é nulo, não pode encontrar inimigos.");
            return null;
        }

        List<Jesuit> jesuits = simulationManager.getJesuits(); // Obtém a lista de Jesuítas ativos
        if (jesuits == null || jesuits.isEmpty()) {
            return null; // Nenhum Jesuit para atacar
        }

        Agent closestEnemy = null;
        float minDistanceSq = visionRadius * visionRadius; // Usa o raio de visão definido em AgentControl

        for (Jesuit jesuit : jesuits) {
            // Verifica se o jesuit está vivo e é um alvo válido
            if (jesuit != null && !jesuit.isDead()) {
                // Calcula a distância quadrada para otimização (evita Math.sqrt)
                float distSq = agent.getLocalTranslation().distanceSquared(jesuit.getLocalTranslation());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestEnemy = jesuit;
                }
            }
        }

        // if (closestEnemy != null) {
        //     System.out.println(agent.getName() + " (Guarani) encontrou inimigo Jesuit: " + closestEnemy.getName());
        // }
        return closestEnemy;
    }

    /**
     * (Opcional) As subclasses podem sobrescrever 'decideNextState' se tiverem uma lógica
     * de priorização de estados diferente da padrão em AgentControl.
     * Para este exemplo, a lógica padrão de AgentControl é suficiente.
     */
    // @Override
    // protected void decideNextState() {
    //     super.decideNextState(); // Chama a lógica base
    //     // Adicionar aqui qualquer lógica de decisão específica para Guarani, se necessário
    // }
}