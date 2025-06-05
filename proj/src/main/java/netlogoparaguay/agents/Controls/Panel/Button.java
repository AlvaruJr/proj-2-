package netlogoparaguay.agents.Controls.Panel;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 * Representa um botão clicável na interface do usuário.
 * Consiste em um fundo (Quad) e um texto (BitmapText).
 * Permite definir uma ação a ser executada ao ser clicado.
 */
public class Button extends Node {
    private Geometry backgroundGeom; // Geometria do fundo do botão
    private BitmapText textLabel;    // Rótulo de texto do botão
    private Runnable onClickAction;  // Ação a ser executada ao clicar
    private float buttonWidth, buttonHeight; // Dimensões do botão

    /**
     * Construtor do Botão.
     * @param label O texto a ser exibido no botão.
     * @param width A largura do botão.
     * @param height A altura do botão.
     * @param assetManager Gerenciador de assets para carregar fontes e materiais.
     */
    public Button(String label, float width, float height, AssetManager assetManager) {
        // Gera um nome para o nó do botão, útil para debugging e busca na cena
        // Remove espaços e caracteres especiais para um nome de nó mais limpo
        super("Button_" + label.replaceAll("[^a-zA-Z0-9_-]", ""));
        this.buttonWidth = width;
        this.buttonHeight = height;

        // Cria a forma (Quad) para o fundo do botão
        Quad quadShape = new Quad(width, height);
        backgroundGeom = new Geometry(this.getName() + "_Bg", quadShape); // Nome para a geometria do fundo

        // Cria e aplica o material ao fundo
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.DarkGray); // Cor padrão do botão
        backgroundGeom.setMaterial(mat);
        this.attachChild(backgroundGeom); // Adiciona o fundo como filho deste nó (Button)

        // Carrega a fonte padrão e cria o rótulo de texto
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        textLabel = new BitmapText(font, false); // 'false' para não duplicar blocos de texto (mais eficiente)
        textLabel.setText(label);
        // Define o tamanho do texto (ex: 50% da altura do botão)
        textLabel.setSize(height * 0.5f);
        textLabel.setColor(ColorRGBA.White); // Cor padrão do texto
        centerText(); // Centraliza o texto dentro do botão
        this.attachChild(textLabel); // Adiciona o texto como filho deste nó (Button)
    }

    /**
     * Centraliza o texto dentro do botão.
     * Chamado no construtor e sempre que o texto do botão é alterado.
     */
    protected void centerText() { // Mudado para protected para que ToggleButton possa chamar
        if (textLabel != null) {
            // Calcula a posição X para centralizar o texto horizontalmente
            float textX = (this.buttonWidth - textLabel.getLineWidth()) / 2f;

            // Calcula a posição Y para tentar centralizar o texto verticalmente.
            // A origem Y de BitmapText é na sua linha de base. O alinhamento vertical exato
            // pode depender da fonte e do tamanho. Esta é uma aproximação comum.
            float textY = (this.buttonHeight + textLabel.getLineHeight()) / 2f;

            // Garante que o texto esteja ligeiramente à frente do fundo para evitar z-fighting
            textLabel.setLocalTranslation(textX, textY, 0.1f);
        }
    }

    /**
     * Define um novo texto para o rótulo do botão.
     * @param newLabel O novo texto a ser exibido.
     */
    public void setText(String newLabel) {
        if (this.textLabel != null) {
            this.textLabel.setText(newLabel);
            centerText(); // Re-centraliza o texto, pois sua largura pode ter mudado
        }
    }

    /**
     * Define a ação (Runnable) a ser executada quando o botão é clicado.
     * @param action A ação a ser executada.
     */
    public void setOnClick(Runnable action) {
        this.onClickAction = action;
    }

    /**
     * Executa a ação `onClickAction` definida para este botão.
     * Este método deve ser chamado por um sistema externo (ex: GuiInputAppState)
     * quando um clique no botão é detectado.
     */
    public void triggerClick() {
        if (onClickAction != null) {
            onClickAction.run(); // Executa a ação
        } else {
            System.out.println("DEBUG: Botão '" + getName() + "' clicado, mas nenhuma ação (onClickAction) foi definida.");
        }
    }

    /**
     * Define a cor de fundo do botão.
     * @param color A nova cor para o fundo.
     */
    public void setColor(ColorRGBA color) {
        if (backgroundGeom != null && backgroundGeom.getMaterial() != null) {
            backgroundGeom.getMaterial().setColor("Color", color);
        }
    }

    /**
     * Obtém a largura do botão.
     * Usado pelo sistema de detecção de clique para verificar os limites.
     * @return A largura do botão.
     */
    public float getButtonWidth() {
        return buttonWidth;
    }

    /**
     * Obtém a altura do botão.
     * Usado pelo sistema de detecção de clique para verificar os limites.
     * @return A altura do botão.
     */
    public float getButtonHeight() {
        return buttonHeight;
    }

    /**
     * Obtém o objeto BitmapText do rótulo do botão.
     * Pode ser útil se a classe filha (ToggleButton) precisar manipular o texto diretamente.
     * @return O BitmapText do rótulo.
     */
    public BitmapText getTextLabel() {
        return textLabel;
    }
}