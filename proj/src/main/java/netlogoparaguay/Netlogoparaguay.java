package netlogoparaguay;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

// Imports para UI
import netlogoparaguay.simulation.SimulationAppState; // O estado que a UI usa (singular)
import netlogoparaguay.simulation.SimulationAppStates; // O motor da simulação (plural)
import netlogoparaguay.ControlPanel; // Assumindo que a classe é ControlPanel com 'C' maiúsculo
import netlogoparaguay.agents.Controls.Panel.StatsPanel; // Corrigindo o caminho se necessário
import netlogoparaguay.agents.Controls.Panel.StatsUpdater;

public class Netlogoparaguay extends SimpleApplication {

    private SimulationAppState uiAppState; // Estado para a UI
    private SimulationAppStates simulationEngine; // Motor da simulação

    public static void main(String[] args) {
        Netlogoparaguay app = new Netlogoparaguay();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Simulação Guaranis vs Jesuítas - 3D Interativa");
        settings.setResolution(1280, 720);
        settings.setSamples(4);
        app.setSettings(settings);
        app.setShowSettings(false);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Configuração da Câmera 3D e Iluminação (como na resposta anterior)
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(25f);
        flyCam.setDragToRotate(true);
        cam.setLocation(new Vector3f(0f, 0f, 35f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.8f, -0.4f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(0.7f));
        rootNode.addLight(sun);

        // 1. Inicializar os AppStates da Simulação
        uiAppState = new SimulationAppState(); // Estado que a UI manipula
        simulationEngine = new SimulationAppStates(); // Motor principal da simulação

        // Fornecer referências cruzadas para comunicação
        uiAppState.setSimulationEngineReference(simulationEngine); // uiAppState precisa saber sobre o engine
        simulationEngine.setUiAppStateReference(uiAppState); // engine precisa saber sobre o estado de pausa da uiAppState

        stateManager.attach(uiAppState);
        stateManager.attach(simulationEngine);

        // 2. Inicializar e anexar o Painel de Controle
        // Certifique-se que o nome da classe ControlPanel está correto.
        // No seu código original, às vezes era 'controlPanel' e às vezes 'ControlPanel'.
        // Vou usar 'ControlPanel' com 'C' maiúsculo.
        ControlPanel controlPanel = new ControlPanel(this, uiAppState);
        // Posicionar o painel de controle (ex: canto superior esquerdo)
        // Ajuste os valores X e Y conforme necessário para o tamanho do seu painel.
        controlPanel.setLocalTranslation(20, cam.getHeight() - 20, 0);
        guiNode.attachChild(controlPanel); // Adiciona à guiNode para elementos 2D

        // 3. Inicializar e anexar o Painel de Estatísticas e seu Atualizador
        StatsPanel statsPanel = new StatsPanel(this);
        // Posicionar o painel de estatísticas (ex: canto superior direito)
        // Assumindo que StatsPanel tem aproximadamente 300px de largura.
        statsPanel.setLocalTranslation(cam.getWidth() - statsPanel.getLocalScale().x * 300 - 20, cam.getHeight() - 20, 0);
        guiNode.attachChild(statsPanel);

        StatsUpdater statsUpdater = new StatsUpdater(statsPanel, uiAppState);
        stateManager.attach(statsUpdater);

        System.out.println("UI Inicializada. ControlPanel e StatsPanel devem estar visíveis.");
    }
}