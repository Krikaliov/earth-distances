package com.krikaliov.earthdistances;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glClearColor;

public class RenderEngine {

  private final long window;

  public RenderEngine(final long window) {
    this.window = window;

  }

  public void render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glfwSwapBuffers(this.window); // swap the color buffers
  }
}
