package netlogoparaguay.resources;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Gerencia todos os recursos no jogo.
 * Responsável por criar recursos iniciais, rastrear seu estado (disponível/coletado),
 * e reintroduzi-los (respawn) no ambiente periodicamente.
 * Utiliza um sistema de "pooling" simples: os recursos coletados são marcados como indisponíveis
 * e podem ser reposicionados e reativados, em vez de serem destruídos e recriados.
 */
public class ResourceManager {

    private final AssetManager assetManager;    // Para carregar modelos/materiais dos recursos
    private final Node resourceNode;            // Nó pai na cena onde os Spatials dos recursos são anexados/removidos

    // Lista de todos os recursos gerenciados (pool de recursos)
    private final List<Resource> resourcePool;

    // Configurações de respawn
    private final int maxActiveResourcesOnMap; // Número máximo de recursos ativos simultaneamente no mapa
    private final float respawnInterval;       // Intervalo de tempo em segundos para tentar o respawn
    private float currentRespawnTimer;         // Timer para controlar o próximo ciclo de respawn
    private Random random = new Random();      // Para gerar posições aleatórias

    // Limites do mundo para o spawn de recursos (deve corresponder aos limites usados pelos agentes)
    private static final float WORLD_SPAWN_AREA_WIDTH = 30f; // Ex: -15 a +15
    private static final float WORLD_SPAWN_AREA_HEIGHT = 30f; // Ex: -15 a +15
    private static final float RESOURCE_MIN_SPAWN_Y = 0.3f; // Altura Y para spawnar os recursos

    /**
     * Construtor do ResourceManager.
     * @param assetManager O AssetManager para carregar assets.
     * @param rootNode O nó da cena onde os recursos serão gerenciados (pode ser um sub-nó do rootNode principal).
     * @param initialResourceCount Quantidade inicial de cada tipo de recurso a ser criado no pool.
     * @param maxActiveResourcesOnMap Número máximo de recursos que podem estar ativos (visíveis/coletáveis) no mapa.
     * @param respawnInterval Intervalo em segundos para tentar reintroduzir recursos.
     */
    public ResourceManager(AssetManager assetManager, Node rootNode, int initialResourceCountPerType, int maxActiveResourcesOnMap, float respawnInterval) {
        this.assetManager = assetManager;
        this.resourceNode = new Node("ManagedResourcesNode"); // Cria um nó dedicado para organizar os recursos
        rootNode.attachChild(this.resourceNode); // Anexa ao nó pai fornecido

        this.resourcePool = new ArrayList<>();
        this.maxActiveResourcesOnMap = maxActiveResourcesOnMap;
        this.respawnInterval = respawnInterval;
        this.currentRespawnTimer = 0f; // Inicia o timer

        initializeResourcePool(initialResourceCountPerType);
        spawnInitialResources();
    }

    /**
     * Cria o pool inicial de objetos de Recurso.
     * Estes recursos começam como indisponíveis e fora da cena, prontos para serem "respawnados".
     * @param countPerType Quantidade de cada tipo de recurso a ser criada.
     */
    private void initializeResourcePool(int countPerType) {
        for (ResourceType type : ResourceType.values()) {
            for (int i = 0; i < countPerType; i++) {
                // Cria o recurso, mas não o adiciona à cena ainda.
                // A posição inicial aqui é temporária, será definida no respawn.
                Resource res = new Resource(type, assetManager, Vector3f.ZERO);
                // res.removeFromNode(); // Garante que não está na cena
                // O construtor de Resource já não adiciona ao node, então está ok
                res.setAvailable(false); // Começa indisponível no pool
                resourcePool.add(res);
            }
        }
        // System.out.println("ResourceManager: Pool de recursos inicializado com " + resourcePool.size() + " recursos.");
    }

    /**
     * Coloca um número inicial de recursos no mapa, tirando-os do pool.
     */
    private void spawnInitialResources() {
        int spawnedCount = 0;
        for (Resource resource : resourcePool) {
            if (spawnedCount >= maxActiveResourcesOnMap) break; // Não exceder o máximo

            if (!resource.isAvailable()) { // Pega um do pool que está inativo
                respawnSpecificResource(resource);
                spawnedCount++;
            }
        }
        // System.out.println("ResourceManager: " + spawnedCount + " recursos iniciais foram spawnados no mapa.");
    }

    /**
     * "Respawn" de um recurso específico em uma nova posição aleatória.
     * Torna o recurso disponível e o adiciona de volta à cena.
     * @param resource O recurso do pool a ser respawnado.
     */
    private void respawnSpecificResource(Resource resource) {
        Vector3f randomPosition = getRandomSpawnPosition();
        resource.setPosition(randomPosition);
        resource.spawnOnNode(this.resourceNode); // Adiciona ao nó de recursos e marca como disponível
        // System.out.println("Recurso " + resource.getType() + " respawnou em " + randomPosition);
    }


    /**
     * Método de atualização chamado a cada frame pelo SimulationAppStates.
     * Controla o timer de respawn para tentar reintroduzir recursos no mapa.
     * @param tpf Tempo por frame.
     */
    public void update(float tpf) {
        currentRespawnTimer += tpf;
        if (currentRespawnTimer >= respawnInterval) {
            currentRespawnTimer = 0f; // Reseta o timer

            // Conta quantos recursos estão atualmente ativos no mapa
            long activeCount = resourcePool.stream().filter(Resource::isAvailable).count();

            if (activeCount < maxActiveResourcesOnMap) {
                // Tenta encontrar um recurso inativo no pool para respawnar
                for (Resource resource : resourcePool) {
                    if (!resource.isAvailable()) {
                        respawnSpecificResource(resource);
                        break; // Respawnou um, sai do loop e espera o próximo intervalo
                    }
                }
            }
        }
    }

    /**
     * Chamado pelo AgentControl quando um agente coleta um recurso.
     * Marca o recurso como indisponível e remove seu Spatial da cena.
     * O objeto Resource permanece no `resourcePool` para ser reutilizado (respawn).
     * @param collectedResource O recurso que foi coletado.
     */
    public void notifyResourceCollected(Resource collectedResource) {
        if (collectedResource != null && collectedResource.isAvailable()) {
            collectedResource.collect(); // Marca como indisponível internamente
            collectedResource.removeFromNode(); // Remove da cena visualmente
            // System.out.println("ResourceManager: Recurso " + collectedResource.getType() + " coletado. Removido da cena.");
        }
    }

    /**
     * Retorna uma lista de todos os recursos que estão atualmente disponíveis (visíveis e coletáveis) no mapa.
     * Usado pelos AgentControls para encontrar alvos de coleta.
     * @return Uma lista de {@link Resource} disponíveis.
     */
    public List<Resource> getAvailableResources() {
        // Filtra o pool para retornar apenas os que estão marcados como disponíveis
        // e, por segurança, verifica se estão realmente na cena (embora isAvailable() deva bastar).
        return resourcePool.stream()
                .filter(r -> r.isAvailable() && r.getSpatial().getParent() != null)
                .collect(Collectors.toList());
    }

    /**
     * Gera uma posição aleatória dentro da área de spawn definida.
     * @return Um {@link Vector3f} com a posição aleatória.
     */
    /**
     * Gera uma posição aleatória dentro da área de spawn definida.
     * @return Um {@link Vector3f} com a posição aleatória.
     */
    private Vector3f getRandomSpawnPosition() {
        // Gera coordenadas X e Y dentro da área definida, centralizada em (0,0)
        // para consistência com o plano de movimento XY dos agentes (Z=0).
        float x = (random.nextFloat() - 0.5f) * WORLD_SPAWN_AREA_WIDTH;
        float y = (random.nextFloat() - 0.5f) * WORLD_SPAWN_AREA_HEIGHT;

        // Retorna a posição com Z=0, assumindo que a simulação ocorre no plano XY.
        return new Vector3f(x, y, 0f);
    }

    /**
     * Limpa todos os recursos, removendo-os da cena e do pool.
     * Usado ao finalizar ou resetar a simulação.
     */
    public void cleanup() {
        for (Resource resource : resourcePool) {
            resource.removeFromNode();
        }
        resourcePool.clear();
        if (this.resourceNode.getParent() != null) {
            this.resourceNode.getParent().detachChild(this.resourceNode);
        }
        // System.out.println("ResourceManager: Limpeza concluída.");
    }
}