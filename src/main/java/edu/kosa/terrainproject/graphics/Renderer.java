package edu.kosa.terrainproject.graphics;

import edu.kosa.terrainproject.terrain.Chunk;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class Renderer {
    private final ShaderProgram shaderProgram;
    private final int textureID;

    public Renderer(ShaderProgram shaderProgram, int textureID) {
        this.shaderProgram = shaderProgram;
        this.textureID = textureID;
    }

    public void render(Camera camera, List<Chunk> chunks, float radius) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        shaderProgram.use();
        shaderProgram.setUniforms(camera, radius);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        int renderedChunks = 0;
        for (Chunk chunk : chunks) {
            if (chunk.getMesh() != null) {
                chunk.getMesh().render();
                renderedChunks++;
            }
        }

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            System.err.println("OpenGL Error in render: " + error);
        }
    }
}