package netlogoparaguay.agents.Controls.Panel;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import java.util.function.Consumer;

/**
 * Um botão que alterna entre dois estados (ligado/desligado) e dois textos/cores.
 * Herda da classe Button.
 */
public class ToggleButton extends Button {
    private boolean isOn; // Estado atual do botão (true = ON, false = OFF)
    private final String onText;  // Texto para o estado ON
    private final String offText; // Texto para o estado OFF
    private Consumer<Boolean> onToggleCallback; // Callback chamado quando o estado muda

    // Cores para os estados ON e OFF (padrão)
    private ColorRGBA colorWhenOn = new ColorRGBA(0.2f, 0.7f, 0.2f, 1f); // Verde escuro
    private ColorRGBA colorWhenOff = new ColorRGBA(0.7f, 0.2f, 0.2f, 1f); // Vermelho escuro

    /**
     * Construtor do ToggleButton.
     * @param offText Texto exibido quando o botão está no estado "desligado" (inicial).
     * @param onText Texto exibido quando o botão está no estado "ligado".
     * @param width Largura do botão.
     * @param height Altura do botão.
     * @param assetManager Gerenciador de assets.
     */
    public ToggleButton(String offText, String onText, float width, float height, AssetManager assetManager) {
        super(offText, width, height, assetManager); // Inicializa com o texto "off"
        this.onText = onText;
        this.offText = offText;
        this.isOn = false; // Começa no estado "desligado"
        updateAppearance(); // Define a aparência inicial (cor para OFF)
    }

    /**
     * Define a ação (Consumer) a ser executada quando o estado do botão muda.
     * O Consumer recebe 'true' se o novo estado é ON, e 'false' se é OFF.
     * @param callback A ação a ser executada na mudança de estado.
     */
    public void setOnToggle(Consumer<Boolean> callback) {
        this.onToggleCallback = callback;
        // Configura a ação de clique herdada da classe Button para alternar o estado
        super.setOnClick(() -> {
            this.isOn = !this.isOn; // Inverte o estado atual
            updateAppearance();     // Atualiza a aparência (texto e cor)
            if (onToggleCallback != null) {
                onToggleCallback.accept(this.isOn); // Chama o callback com o novo estado
            }
        });
    }

    /**
     * Atualiza a aparência do botão (texto e cor) com base no estado atual (isOn).
     */
    private void updateAppearance() {
        BitmapText label = super.getTextLabel(); // Obtém o BitmapText da classe pai
        if (label != null) {
            label.setText(this.isOn ? this.onText : this.offText); // Define o texto apropriado
            super.centerText(); // Re-centraliza, pois o texto pode ter mudado de tamanho
        }
        super.setColor(this.isOn ? this.colorWhenOn : this.colorWhenOff); // Define a cor apropriada
    }

    /**
     * Define o estado visual e lógico do botão diretamente, SEM disparar o onToggleCallback.
     * Útil para inicializar o botão ou sincronizá-lo com um estado externo.
     * @param isOn true para definir como ON (exibindo onText), false para definir como OFF (exibindo offText).
     */
    public void setState(boolean isOn) {
        if (this.isOn != isOn) { // Só atualiza se o estado realmente mudou
            this.isOn = isOn;
            updateAppearance();
        }
    }

    /**
     * Verifica se o botão está atualmente no estado "ligado" (ON).
     * @return true se está ligado, false caso contrário.
     */
    public boolean isOn() {
        return isOn;
    }

    /**
     * Permite customizar a cor do botão para o estado "ligado" (ON).
     * @param color Nova cor para o estado ON.
     */
    public void setColorWhenOn(ColorRGBA color) {
        this.colorWhenOn = color;
        if (this.isOn) { // Se já estiver ON, atualiza a cor imediatamente
            updateAppearance();
        }
    }

    /**
     * Permite customizar a cor do botão para o estado "desligado" (OFF).
     * @param color Nova cor para o estado OFF.
     */
    public void setColorWhenOff(ColorRGBA color) {
        this.colorWhenOff = color;
        if (!this.isOn) { // Se já estiver OFF, atualiza a cor imediatamente
            updateAppearance();
        }
    }
}