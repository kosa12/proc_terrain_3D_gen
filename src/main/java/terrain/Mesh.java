package terrain;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

public class Mesh {
    private int vaoID;
    private int vboID;
    private int tboID;
    private int nboID;
    private int eboID;
    private int vertexCount;

    public Mesh(float[] vertices, float[] texCoords, float[] normals, int[] indices) {
        vertexCount = indices.length;

        // Create VAO
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Vertices VBO
        vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        // Texture Coordinates VBO
        tboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tboID);
        FloatBuffer texBuffer = BufferUtils.createFloatBuffer(texCoords.length);
        texBuffer.put(texCoords).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(1);

        // Normals VBO
        nboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, nboID);
        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(normals.length);
        normalBuffer.put(normals).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(2);

        // Indices EBO
        eboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        // Unbind
        GL30.glBindVertexArray(0);
    }

    public void render() {
        GL30.glBindVertexArray(vaoID);
        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void cleanup() {
        GL30.glDeleteVertexArrays(vaoID);
        GL15.glDeleteBuffers(vboID);
        GL15.glDeleteBuffers(tboID);
        GL15.glDeleteBuffers(nboID);
        GL15.glDeleteBuffers(eboID);
    }
}
