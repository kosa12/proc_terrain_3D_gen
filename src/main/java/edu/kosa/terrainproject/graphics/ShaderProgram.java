package edu.kosa.terrainproject.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public class ShaderProgram {
    private final int programID;
    private boolean useFallback = false;

    public ShaderProgram() {
        String vertexShaderSource = "#version 330 core\n" +
                "layout(location = 0) in vec3 aPos;\n" +
                "layout(location = 1) in vec2 aTexCoord;\n" +
                "layout(location = 2) in vec3 aNormal;\n" +
                "layout(location = 3) in float aAlpha;\n" + // Added alpha attribute
                "uniform mat4 model;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 projection;\n" +
                "out vec2 TexCoord;\n" +
                "out vec3 WorldPos;\n" +
                "out vec3 Normal;\n" +
                "out float Alpha;\n" + // Pass alpha to fragment shader
                "void main() {\n" +
                "    vec4 worldPos = model * vec4(aPos, 1.0);\n" +
                "    WorldPos = worldPos.xyz;\n" +
                "    gl_Position = projection * view * worldPos;\n" +
                "    TexCoord = aTexCoord;\n" +
                "    Normal = mat3(model) * aNormal;\n" +
                "    Alpha = aAlpha;\n" + // Assign alpha
                "}\n";

        String fragmentShaderSource = "#version 330 core\n" +
                "in vec2 TexCoord;\n" +
                "in vec3 WorldPos;\n" +
                "in vec3 Normal;\n" +
                "in float Alpha;\n" + // Receive alpha from vertex shader
                "out vec4 FragColor;\n" +
                "uniform sampler2D textureAtlas;\n" +
                "uniform vec3 cameraPos;\n" +
                "uniform float radius;\n" +
                "uniform vec3 lightDir;\n" +
                "uniform vec3 lightColor;\n" +
                "void main() {\n" +
                "    float dist = length(WorldPos.xz - cameraPos.xz);\n" +
                "    float fade = smoothstep(radius - 5.0, radius, dist);\n" +
                "    vec3 norm = normalize(Normal);\n" +
                "    float diff = max(dot(norm, -lightDir), 0.2);\n" +
                "    vec3 diffuse = diff * lightColor;\n" +
                "    vec4 texColor = texture(textureAtlas, TexCoord);\n" +
                "    vec4 terrainColor = vec4(texColor.rgb * diffuse, texColor.a * Alpha);\n" + // Multiply texture alpha with vertex alpha
                "    vec4 clearColor = vec4(0.1, 0.1, 0.3, 1.0);\n" +
                "    FragColor = mix(terrainColor, clearColor, clamp(fade, 0.0, 1.0));\n" +
                "}\n";

        String fallbackFragmentSource = "#version 330 core\n" +
                "in vec2 TexCoord;\n" +
                "in float Alpha;\n" + // Receive alpha for fallback
                "out vec4 FragColor;\n" +
                "uniform sampler2D textureAtlas;\n" +
                "void main() {\n" +
                "    vec4 texColor = texture(textureAtlas, TexCoord);\n" +
                "    FragColor = vec4(texColor.rgb, texColor.a * Alpha);\n" + // Apply alpha
                "}\n";

        programID = GL20.glCreateProgram();
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glCompileShader(vertexShader);
        if (!checkShaderCompile(vertexShader, "Vertex")) {
            useFallback = true;
        }

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, useFallback ? fallbackFragmentSource : fragmentShaderSource);
        GL20.glCompileShader(fragmentShader);
        if (!checkShaderCompile(fragmentShader, "Fragment")) {
            useFallback = true;
            GL20.glDeleteShader(fragmentShader);
            fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(fragmentShader, fallbackFragmentSource);
            GL20.glCompileShader(fragmentShader);
            checkShaderCompile(fragmentShader, "Fallback Fragment");
        }

        GL20.glAttachShader(programID, vertexShader);
        GL20.glAttachShader(programID, fragmentShader);
        GL20.glLinkProgram(programID);
        if (!checkProgramLink(programID)) {
            throw new RuntimeException("Shader program linking failed");
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    public void use() {
        GL20.glUseProgram(programID);
    }

    public void setUniforms(Camera camera, float radius) {
        if (useFallback) {
            System.out.println("Using fallback shader (no uniforms set)");
            return;
        }

        Matrix4f model = new Matrix4f().identity();
        Matrix4f view = camera.getViewMatrix();
        Matrix4f projection = camera.getProjectionMatrix();
        Vector3f cameraPos = camera.getPosition();
        Vector3f lightDir = new Vector3f(0.5f, -1f, 0.5f).normalize();
        Vector3f lightColor = new Vector3f(1f, 1f, 1f);

        if (Float.isNaN(radius) || radius <= 0) {
            System.err.println("Invalid radius: " + radius + ", defaulting to 100");
            radius = 100f;
        }

        int modelLoc = GL20.glGetUniformLocation(programID, "model");
        GL20.glUniformMatrix4fv(modelLoc, false, model.get(new float[16]));

        int viewLoc = GL20.glGetUniformLocation(programID, "view");
        GL20.glUniformMatrix4fv(viewLoc, false, view.get(new float[16]));

        int projLoc = GL20.glGetUniformLocation(programID, "projection");
        GL20.glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));

        int cameraPosLoc = GL20.glGetUniformLocation(programID, "cameraPos");
        GL20.glUniform3f(cameraPosLoc, cameraPos.x, cameraPos.y, cameraPos.z);

        int radiusLoc = GL20.glGetUniformLocation(programID, "radius");
        GL20.glUniform1f(radiusLoc, radius);

        int lightDirLoc = GL20.glGetUniformLocation(programID, "lightDir");
        GL20.glUniform3f(lightDirLoc, lightDir.x, lightDir.y, lightDir.z);

        int lightColorLoc = GL20.glGetUniformLocation(programID, "lightColor");
        GL20.glUniform3f(lightColorLoc, lightColor.x, lightColor.y, lightColor.z);

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            System.err.println("OpenGL Error in setUniforms: " + error);
        }
    }

    public void cleanup() {
        GL20.glDeleteProgram(programID);
    }

    private boolean checkShaderCompile(int shader, String type) {
        IntBuffer success = BufferUtils.createIntBuffer(1);
        GL20.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, success);
        if (success.get(0) == GL11.GL_FALSE) {
            String infoLog = GL20.glGetShaderInfoLog(shader);
            System.err.println(type + " Shader compilation failed: " + infoLog);
            return false;
        }
        return true;
    }

    private boolean checkProgramLink(int program) {
        IntBuffer success = BufferUtils.createIntBuffer(1);
        GL20.glGetProgramiv(program, GL20.GL_LINK_STATUS, success);
        if (success.get(0) == GL11.GL_FALSE) {
            String infoLog = GL20.glGetProgramInfoLog(program);
            System.err.println("Shader program linking failed: " + infoLog);
            return false;
        }
        return true;
    }
}