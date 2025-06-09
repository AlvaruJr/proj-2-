package netlogoparaguay.agents.Controls.Agent;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.material.Material;
import netlogoparaguay.agents.Controls.controller.AgentControl;
import netlogoparaguay.agents.Controls.controller.JesuitControl;
import netlogoparaguay.simulation.SimulationAppStates;

public class Jesuit extends Agent {

    public Jesuit(String name, AssetManager assetManager, SimulationAppStates simulationManager) {
        super(name, assetManager, simulationManager); // Chama o construtor da classe Agent
        // Qualquer inicialização específica do Jesuit aqui
    }
@Override
public float calculateMaxHealth() {
    return 100 + vitality * 7;
}
    @Override
    protected Spatial loadModel(AssetManager assetManager) {
        try {
            // Modelo 3D simples (cubo vermelho)
            Box box = new Box(0.5f, 0.5f, 0.5f);
            Geometry geom = new Geometry("Jesuit_Model", box);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", new ColorRGBA(0.8f, 0.2f, 0.2f, 1));

            geom.setMaterial(mat);
            return geom;

        } catch (Exception e) {
            System.err.println("Erro crítico ao criar modelo Jesuit: " + e.getMessage());
            throw new RuntimeException("Falha ao inicializar modelo Jesuit", e);
        }
    }

    @Override
    protected AgentControl createControl() {
        return new JesuitControl();
    }
}