package edu.kosa.terrainproject.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureLoader {
    private int textureID;

    public int loadTexture(String path) {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        // Load texture from classpath
        InputStream inputStream = TextureLoader.class.getResourceAsStream("/" + path);
        if (inputStream == null) {
            throw new RuntimeException("Failed to load texture: Unable to open file " + path);
        }

        // Read image data into ByteBuffer
        ByteBuffer data;
        try {
            byte[] bytes = inputStream.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            data = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load texture: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                // Ignore
            }
        }

        if (data == null) {
            throw new RuntimeException("Failed to load texture: " + STBImage.stbi_failure_reason());
        }

        // Validate texture dimensions (expecting 64x16 for atlas.png)
        if (width.get(0) != 64 || height.get(0) != 16) {
            STBImage.stbi_image_free(data);
            throw new RuntimeException("Texture atlas must be 64x16 pixels, got " + width.get(0) + "x" + height.get(0));
        }

        if (channels.get(0) != 4) {
            STBImage.stbi_image_free(data);
            throw new RuntimeException("Texture atlas must have 4 channels (RGBA), got " + channels.get(0));
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