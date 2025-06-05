package netlogoparaguay.agents.Controls.controller;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.shader.VarType;

public class SpriteAnimationControl extends AbstractControl {
    private final Texture2D spriteSheet;
    private final int cols;
    private final int rows;
    private final float frameTime;

    private float currentTime = 0;
    private int currentFrame = 0;
    private Picture picture;
    private Material spriteMaterial;

    public SpriteAnimationControl(Texture2D spriteSheet, int cols, int rows, float frameTime) {
        if (spriteSheet == null) {
            throw new IllegalArgumentException("SpriteSheet cannot be null");
        }
        if (cols <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Columns and rows must be positive");
        }
        if (frameTime <= 0) {
            throw new IllegalArgumentException("Frame time must be positive");
        }

        this.spriteSheet = spriteSheet;
        this.cols = cols;
        this.rows = rows;
        this.frameTime = frameTime;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null) return;

        currentTime += tpf;
        if (currentTime >= frameTime) {
            currentTime = 0;
            advanceFrame();
            updateFrame();
        }
    }

    private void advanceFrame() {
        currentFrame = (currentFrame + 1) % (cols * rows);
    }

    private void updateFrame() {
        if (picture == null) {
            if (!(spatial instanceof Picture)) {
                throw new IllegalStateException("SpriteAnimationControl can only be attached to Picture objects");
            }
            picture = (Picture) spatial;
            spriteMaterial = picture.getMaterial();

            // Configuração inicial do material para suportar animação
            spriteMaterial.setParam("SpriteCols", VarType.Int, cols);
            spriteMaterial.setParam("SpriteRows", VarType.Int, rows);
        }

        int col = currentFrame % cols;
        int row = currentFrame / cols;

        // Calcula as coordenadas de textura
        Vector2f offset = new Vector2f((float)col/cols, 1f - ((float)(row+1)/rows));
        Vector2f scale = new Vector2f(1f/cols, 1f/rows);

        // Atualiza os parâmetros do shader
        spriteMaterial.setParam("Offset", VarType.Vector2, offset);
        spriteMaterial.setParam("Scale", VarType.Vector2, scale);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Não é necessário implementar para animação de sprites
    }

    public void resetAnimation() {
        currentFrame = 0;
        currentTime = 0;
        if (picture != null) {
            updateFrame();
        }
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int frame) {
        if (frame < 0 || frame >= cols * rows) {
            throw new IllegalArgumentException("Frame index out of bounds");
        }
        currentFrame = frame;
        currentTime = 0;
        if (picture != null) {
            updateFrame();
        }
    }

    public static class GuiControl extends AbstractControl {
        @Override
        protected void controlUpdate(float tpf) {
            // Atualizações da interface
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
            // Renderização adicional se necessário
        }
    }
}