package netlogoparaguay.resources;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ResourceManager {

    private final AssetManager assetManager;
    private final Node resourceRootNode;
    private final Map<ResourceType, List<Resource>> resourcePool;
    private final Map<ResourceType, List<Resource>> activeResources;

    private final int capacityPerType;
    private final int maxOnMapPerType;
    private final float respawnInterval;
    private float currentRespawnTimer = 0f;

    private final Random random = new Random();

    private final float areaWidth;
    private final float areaHeight;

    public ResourceManager(AssetManager assetManager, Node resourceRootNode,
                           int capacityPerType, int maxOnMapPerType, float respawnInterval,
                           float areaWidth, float areaHeight) {
        this.assetManager = assetManager;
        this.resourceRootNode = resourceRootNode;
        this.capacityPerType = capacityPerType;
        this.maxOnMapPerType = maxOnMapPerType;
        this.respawnInterval = respawnInterval;
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;

        this.resourcePool = new EnumMap<>(ResourceType.class);
        this.activeResources = new EnumMap<>(ResourceType.class);

        for (ResourceType type : ResourceType.values()) {
            resourcePool.put(type, new ArrayList<>());
            activeResources.put(type, new ArrayList<>());
            for (int i = 0; i < capacityPerType; i++) {
                Resource res = new Resource(type, assetManager, Vector3f.ZERO);
                res.setAvailable(false);
                resourcePool.get(type).add(res);
            }
        }
    }

    public void resetAndRepopulate() {
        cleanupAllResources();
        for (ResourceType type : ResourceType.values()) {
            for (int i = 0; i < maxOnMapPerType; i++) {
                spawnResource(type);
            }
        }
        currentRespawnTimer = 0f;
    }

    public void cleanupAllResources() {
        for (List<Resource> activeList : activeResources.values()) {
            for (Resource res : new ArrayList<>(activeList)) {
                res.removeFromNode();
                res.setAvailable(false);
            }
            activeList.clear();
        }
    }

    private void spawnResource(ResourceType type) {
        if (activeResources.get(type).size() >= maxOnMapPerType) {
            return;
        }

        Resource resourceToSpawn = findInactiveResourceInPool(type);

        if (resourceToSpawn != null) {
            float x = (random.nextFloat() - 0.5f) * (areaWidth - 1f);
            float y = (random.nextFloat() - 0.5f) * (areaHeight - 1f);
            resourceToSpawn.setPosition(new Vector3f(x, y, 0));
            resourceToSpawn.setAvailable(true);
            resourceToSpawn.spawnOnNode(resourceRootNode);
            activeResources.get(type).add(resourceToSpawn);
        }
    }

    private Resource findInactiveResourceInPool(ResourceType type) {
        List<Resource> poolForType = resourcePool.get(type);
        List<Resource> activeForType = activeResources.get(type);
        for (Resource res : poolForType) {
            if (!activeForType.contains(res)) {
                return res;
            }
        }
        return null;
    }


    public void update(float tpf) {
        currentRespawnTimer += tpf;
        if (currentRespawnTimer >= respawnInterval) {
            currentRespawnTimer = 0f;
            for (ResourceType type : ResourceType.values()) {
                if (activeResources.get(type).size() < maxOnMapPerType) {
                    spawnResource(type);
                }
            }
        }
    }

    public void notifyResourceCollected(Resource resource) {
        if (resource == null || !resource.isAvailable()) return;
        resource.collect();
        resource.removeFromNode();
        activeResources.get(resource.getType()).remove(resource);
    }

    public List<Resource> getAvailableResources() {
        List<Resource> allAvailable = new ArrayList<>();
        for (List<Resource> listPerType : activeResources.values()) {
            for (Resource res : listPerType) {
                if (res.isAvailable()) {
                    allAvailable.add(res);
                }
            }
        }
        return allAvailable;
    }
}