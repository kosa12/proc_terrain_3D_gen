package terrain;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureLoader {
    private int textureID;

    public int loadTexture(String path) {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        ByteBuffer data = STBImage.stbi_load(path, width, height, channels, 0);
        if (data == null) {
            throw new RuntimeException("Failed to load texture: " + STBImage.stbi_failure_reason());
        }

        textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        STBImage.stbi_image_free(data);

        return textureID;
    }

    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
    }

    public void cleanup() {
        GL11.glDeleteTextures(textureID);
    }
}