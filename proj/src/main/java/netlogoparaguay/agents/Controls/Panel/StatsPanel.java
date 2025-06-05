package netlogoparaguay.agents.Controls.Panel;
import com.jme3.scene.Geometry;
import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.material.Material;

public class StatsPanel extends Node {

    private BitmapText guaraniStats;
    private BitmapText jesuitStats;
    private BitmapText loopStats;
    private BitmapText winnerText;

    public StatsPanel(Application app) {
        super("StatsPanel");

        // Fundo do painel
        Quad bgQuad = new Quad(300, 300);
        Geometry bgGeom = new Geometry("StatsBackground", bgQuad);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.5f));
        bgGeom.setMaterial(mat);
        attachChild(bgGeom);

        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        // Título
        BitmapText title = new BitmapText(font);
        title.setText("Estatísticas");
        title.setSize(24);
        title.setLocalTranslation(10, -30, 0);
        attachChild(title);

        // Estatísticas
        guaraniStats = createStatText(font, "Guaranis: 0", 10, -70);
        jesuitStats = createStatText(font, "Jesuitas: 0", 10, -110);
        loopStats = createStatText(font, "Loops: 0/0", 10, -150);
        winnerText = createStatText(font, "Vencedor: -", 10, -190);
        winnerText.setColor(ColorRGBA.Yellow);
    }

    private BitmapText createStatText(BitmapFont font, String text, float x, float y) {
        BitmapText statText = new BitmapText(font);
        statText.setText(text);
        statText.setSize(20);
        statText.setLocalTranslation(x, y, 0);
        attachChild(statText);
        return statText;
    }

    public void updateStats(int guaraniCount, int jesuitCount, int currentLoop, int maxLoops, String winner) {
        guaraniStats.setText("Guaranis: " + guaraniCount);
        jesuitStats.setText("Jesuitas: " + jesuitCount);
        loopStats.setText("Loops: " + currentLoop + "/" + maxLoops);
        winnerText.setText("Vencedor: " + winner);
    }
}