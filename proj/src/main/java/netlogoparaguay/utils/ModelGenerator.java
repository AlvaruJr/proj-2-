package netlogoparaguay.utils;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.math.ColorRGBA;
import com.jme3.material.Material;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import java.io.File;
import java.io.IOException;

public class ModelGenerator {

    public static void createGuaraniModel() {
        // Configuração básica
        AssetManager assetManager = new DesktopAssetManager();

        // Criar geometria
        Sphere sphere = new Sphere(16, 16, 0.4f);
        Geometry geom = new Geometry("Guarani", sphere);

        // Criar material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);

        geom.setMaterial(mat);

        // Exportar para arquivo
        BinaryExporter exporter = BinaryExporter.getInstance();
        File file = new File("assets/Models/guarani.j3o");

        try {
            file.getParentFile().mkdirs(); // Criar diretório se não existir
            exporter.save(geom, file);
            System.out.println("Modelo guarani.j3o criado com sucesso!");
        } catch (IOException ex) {
            System.err.println("Erro ao salvar modelo: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        createGuaraniModel();
    }
}