package netlogoparaguay.resources;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box; // Usando Box como placeholder, pode ser Sphere ou outro
import com.jme3.scene.shape.Sphere;

/**
 * Representa um recurso no mundo do jogo que pode ser coletado por agentes.
 * Mantém informações sobre seu tipo, sua representação visual (Spatial)
 * e seu estado (disponível, posição).
 */
public class Resource {

    private ResourceType type;          // Tipo do recurso (ex: WOOD, SOY, MATE)
    private Spatial spatial;            // Representação visual do recurso no jogo
    private boolean available;          // Indica se o recurso está disponível para coleta
    private AssetManager assetManager;  // Para carregar materiais/modelos

    /**
     * Construtor para um novo objeto de Recurso.
     * @param type O tipo do recurso.
     * @param assetManager O gerenciador de assets para carregar modelos/materiais.
     * @param initialPosition A posição inicial onde o recurso será criado.
     */
    public Resource(ResourceType type, AssetManager assetManager, Vector3f initialPosition) {
        this.type = type;
        this.assetManager = assetManager;
        this.available = true; // Por padrão, um novo recurso está disponível
        this.spatial = createSpatialModel();
        this.spatial.setLocalTranslation(initialPosition);
        this.spatial.setName("Resource_" + type.name() + "_" + System.currentTimeMillis()%10000); // Nome único
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }
    /**
     * Cria o modelo visual (Spatial) para este recurso.
     * A aparência pode variar dependendo do tipo de recurso.
     * @return O Spatial criado para o recurso.
     */
    private Spatial createSpatialModel() {
        Geometry geom;
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        // Define a forma e cor baseada no tipo de recurso
        switch (type) {
            case WOOD:
                Box woodShape = new Box(0.3f, 0.3f, 0.3f); // Pequeno cubo marrom para madeira
                geom = new Geometry("WoodResourceModel", woodShape);
                mat.setColor("Color", ColorRGBA.Brown);
                break;
            case SOY:
                Sphere soyShape = new Sphere(16, 16, 0.25f); // Esfera amarela para soja
                geom = new Geometry("SoyResourceModel", soyShape);
                mat.setColor("Color", ColorRGBA.Yellow);
                break;
            case MATE:
                Sphere mateShape = new Sphere(16, 16, 0.25f); // Esfera verde escura para mate
                geom = new Geometry("MateResourceModel", mateShape);
                mat.setColor("Color", ColorRGBA.Green.mult(0.5f)); // Verde mais escuro
                break;
            default:
                // Recurso genérico se o tipo não for reconhecido
                Box defaultShape = new Box(0.2f, 0.2f, 0.2f);
                geom = new Geometry("DefaultResourceModel", defaultShape);
                mat.setColor("Color", ColorRGBA.LightGray);
                break;
        }
        geom.setMaterial(mat);
        return geom;
    }

    /**
     * Obtém o tipo deste recurso.
     * @return O {@link ResourceType} do recurso.
     */
    public ResourceType getType() {
        return type;
    }

    /**
     * Obtém o Spatial (modelo visual) deste recurso.
     * @return O {@link Spatial} associado a este recurso.
     */
    public Spatial getSpatial() {
        return spatial;
    }

    /**
     * Verifica se o recurso está atualmente disponível para coleta.
     * @return true se disponível, false caso contrário.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Marca o recurso como coletado (indisponível).
     * Geralmente chamado quando um agente coleta este recurso.
     */
    public void collect() {
        this.available = false;
        // A remoção do Spatial da cena é gerenciada pelo ResourceManager
        // para permitir o reaproveitamento (pooling/respawn).
    }

    /**
     * Adiciona o Spatial do recurso a um nó pai na cena.
     * Usado quando o recurso é spawnado ou respawnado.
     * @param parentNode O nó ao qual o Spatial do recurso será anexado.
     */
    public void spawnOnNode(Node parentNode) {
        if (spatial.getParent() == null) { // Evita adicionar múltiplas vezes
            parentNode.attachChild(spatial);
        }
        this.available = true; // Torna disponível ao ser spawnado
    }

    /**
     * Remove o Spatial do recurso de seu nó pai.
     * Usado quando o recurso é coletado e precisa ser removido visualmente.
     */
    public void removeFromNode() {
        if (spatial.getParent() != null) {
            spatial.getParent().detachChild(spatial);
        }
        // Não muda 'available' aqui, pois 'collect()' já faz isso.
        // Se fosse um despawn temporário sem coleta, 'available' seria false.
    }

    /**
     * Define uma nova posição para o recurso.
     * Usado durante o respawn em um local aleatório.
     * @param position A nova {@link Vector3f} posição para o recurso.
     */
    public void setPosition(Vector3f position) {
        if (spatial != null) {
            spatial.setLocalTranslation(position);
        }
    }

    /**
     * Obtém a posição atual do recurso.
     * @return A {@link Vector3f} representando a posição atual.
     */
    public Vector3f getPosition() {
        return spatial != null ? spatial.getLocalTranslation() : Vector3f.ZERO;
    }

}