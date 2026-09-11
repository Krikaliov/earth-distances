package com.krikaliov.earthdistances;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class RotatingTriangle3D {

    private long window;
    private int vaoId;
    private int vboPosId;
    private int vboColId;
    private int shaderProgramId;
    private int uTransformLoc;

    // Code des shaders
    private static final String VERTEX_SHADER_SRC =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "layout (location = 1) in vec3 aColor;\n" +
                    "out vec3 vertexColor;\n" +
                    "uniform mat4 uTransform;\n" +
                    "void main() {\n" +
                    "    gl_Position = uTransform * vec4(aPos, 1.0);\n" +
                    "    vertexColor = aColor;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER_SRC =
            "#version 330 core\n" +
                    "in vec3 vertexColor;\n" +
                    "out vec4 FragColor;\n" +
                    "void main() {\n" +
                    "    FragColor = vec4(vertexColor, 1.0);\n" +
                    "}\n";

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // 1. Initialisation de GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossible d'initialiser GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        window = GLFW.glfwCreateWindow(800, 600, "LWJGL 3.1.0 - Triangle 3D", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Échec de création de la fenêtre GLFW");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // Activation du V-Sync
        GLFW.glfwShowWindow(window);

        // Crucial pour LWJGL 3 : rend les fonctions OpenGL disponibles
        GL.createCapabilities();

        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // 2. Compilation et liaison des Shaders
        initShaders();

        // 3. Préparation des données géométriques (Triangle)
        initGeometry();
    }

    private void initShaders() {
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, VERTEX_SHADER_SRC);
        GL20.glCompileShader(vertexShader);
        checkShaderCompile(vertexShader);

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, FRAGMENT_SHADER_SRC);
        GL20.glCompileShader(fragmentShader);
        checkShaderCompile(fragmentShader);

        shaderProgramId = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgramId, vertexShader);
        GL20.glAttachShader(shaderProgramId, fragmentShader);
        GL20.glLinkProgram(shaderProgramId);

        if (GL20.glGetProgrami(shaderProgramId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Erreur de linking : " + GL20.glGetProgramInfoLog(shaderProgramId));
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        uTransformLoc = GL20.glGetUniformLocation(shaderProgramId, "uTransform");
    }

    private void initGeometry() {
        // Sommets du triangle (X, Y, Z)
        float[] positions = {
                0.0f,  0.5f, 0.0f, // Sommet haut
                -0.5f, -0.5f, 0.0f, // Sommet bas-gauche
                0.5f, -0.5f, 0.0f  // Sommet bas-droite
        };

        // Couleurs des sommets (R, G, B)
        float[] colors = {
                1.0f, 0.0f, 0.0f, // Rouge
                0.0f, 1.0f, 0.0f, // Vert
                0.0f, 0.0f, 1.0f  // Bleu
        };

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // VBO Positions
        vboPosId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPosId);
        FloatBuffer posBuffer = BufferUtils.createFloatBuffer(positions.length).put(positions);
        posBuffer.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        // VBO Couleurs
        vboColId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColId);
        FloatBuffer colBuffer = BufferUtils.createFloatBuffer(colors.length).put(colors);
        colBuffer.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void loop() {
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0), 800.0f / 600.0f, 0.1f, 100.0f);
        Matrix4f view = new Matrix4f().lookAt(0.0f, 0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix4f model = new Matrix4f();
        Matrix4f transform = new Matrix4f();

        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

        while (!GLFW.glfwWindowShouldClose(window)) {
            float time = (float) GLFW.glfwGetTime();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

            GL20.glUseProgram(shaderProgramId);

            // Calculation des transformations : Translation (Y) et Rotation (Y)
            float yOffset = (float) Math.sin(time * 2.0f) * 0.3f; // Oscillation entre -0.3 et 0.3
            float rotationAngle = time * 2.0f;                   // Vitesse de rotation

            model.identity()
                    .translate(0.0f, yOffset, 0.0f)
                    .rotate(rotationAngle, 0.0f, 1.0f, 0.0f);

            // Combinaison : P * V * M
            transform.set(projection).mul(view).mul(model);

            // Envoi de la matrice de transformation au shader
            transform.get(matrixBuffer);
            GL20.glUniformMatrix4fv(uTransformLoc, false, matrixBuffer);

            // Rendu
            GL30.glBindVertexArray(vaoId);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            GL30.glBindVertexArray(0);

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private void checkShaderCompile(int shaderId) {
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Erreur de compilation du shader : " + GL20.glGetShaderInfoLog(shaderId));
        }
    }

    private void cleanup() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboPosId);
        GL15.glDeleteBuffers(vboColId);

        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);

        GL20.glDeleteProgram(shaderProgramId);

        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new RotatingTriangle3D().run();
    }
}
