package netlogoparaguay.agents.Controls.controller;

import java.util.List;
import netlogoparaguay.agents.Controls.Agent.Agent;
import netlogoparaguay.agents.Controls.Agent.Guarani; // Importa a classe Guarani para ser o alvo

/**
 * Controle específico para o comportamento dos agentes Jesuitas.
 * Herda a maior parte da lógica de AgentControl e define que os Guaranis são seus inimigos.
 */
public class JesuitControl extends AgentControl {

    /**
     * Construtor para JesuitControl.
     * Configura os parâmetros específicos para os Jesuitas, como velocidade base.
     * As referências a simulationManager e resourceManager são obtidas
     * através do método setSpatial da classe AgentControl quando este controle
     * é anexado a um Agente Jesuit.
     */
    public JesuitControl() {
        super(); // Chama o construtor de AgentControl
        this.baseSpeed = 2.5f;      // Jesuitas podem ser um pouco mais lentos, mas talvez mais resistentes (definido no Agent)
        this.attackRange = 1.7f;    // Alcance de ataque ligeiramente maior
        this.attackCooldownBase = 2.2f; // Cooldown de ataque um pouco maior
        // A effectiveSpeed será calculada em setSpatial -> updateEffectiveSpeed
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
        float minDistanceSq = visionRadius * visionRadius; // Usa o raio de visão definido em AgentControl

        for (Guarani guarani : guaranis) {
            // Verifica se o guarani está vivo e é um alvo válido
            if (guarani != null && !guarani.isDead()) {
                // Calcula a distância quadrada para otimização (evita Math.sqrt)
                float distSq = agent.getLocalTranslation().distanceSquared(guarani.getLocalTranslation());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestEnemy = guarani;
                }
            }
        }

        // if (closestEnemy != null) {
        //    System.out.println(agent.getName() + " (Jesuit) encontrou inimigo Guarani: " + closestEnemy.getName());
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
    //     // Adicionar aqui qualquer lógica de decisão específica para Jesuit, se necessário
    // }
}