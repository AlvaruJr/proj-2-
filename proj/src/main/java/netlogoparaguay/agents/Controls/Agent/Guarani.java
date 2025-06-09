package netlogoparaguay.agents.Controls.Agent;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.material.Material;
import netlogoparaguay.agents.Controls.controller.AgentControl;
import netlogoparaguay.agents.Controls.controller.GuaraniControl;
import netlogoparaguay.simulation.SimulationAppStates;

public class Guarani extends Agent {

    public Guarani(String name, AssetManager assetManager, SimulationAppStates simulationManager) {
        super(name, assetManager, simulationManager); // Chama o construtor da classe Agent
        // Qualquer inicialização específica do Guarani aqui
    }

    @Override
    protected Spatial loadModel(AssetManager assetManager) {
        try {
            // Modelo 3D simples (esfera verde)
            Sphere sphere = new Sphere(32, 32, 0.5f);
            Geometry geom = new Geometry("Guarani_Model", sphere);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", new ColorRGBA(0.2f, 0.8f, 0.3f, 1));

            geom.setMaterial(mat);
            return geom;

        } catch (Exception e) {
            System.err.println("Erro crítico ao criar modelo Guarani: " + e.getMessage());
            throw new RuntimeException("Falha ao inicializar modelo Guarani", e);
        }
    }
    @Override
    public float calculateMaxHealth() {
        return 80 + vitality * 10;
    }
    @Override
    protected AgentControl createControl() {
        return new GuaraniControl();
    }
}