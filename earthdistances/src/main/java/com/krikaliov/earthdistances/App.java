package com.krikaliov.earthdistances;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class App {

  public final String title = Main.APPNAME + " " + Main.VERSION;

  protected final String unions() {
    String r = "";
    final int n = title.length() + 8;
    for (int i = 0 ; i < n ; i++) r = r.concat("-");
    return r;
  }

  private final String label = this.unions() + "\n--| " + this.title + " |--\n" + this.unions();

  private long window;

  private final EarthDataView earthDataView = new EarthDataView(new EarthData());
  private final PinDataViewer pinDataViewer = new PinDataViewer();
  private final RenderEngine renderEngine;
  private final Logger logger = Logger.getInstance();

  public App(int width, int height) {
    // Setup Console Manager
    this.logger.set(this.quitCmdFn, System.in, System.out);

    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW");

    // Configure GLFW
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

    // Create the window
    this.window = glfwCreateWindow(width, height, this.title, NULL, NULL);
    if (this.window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(this.window, (win, key, code, action, mods) -> {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
        this.quitCmdFn.exec(new Argument[0]); // We will detect this in the rendering loop
    });

    // Setup a window close callback to make the app closes when the user clicks on the red cross.
    glfwSetWindowCloseCallback(this.window, (win) -> this.quitCmdFn.exec(new Argument[0]));

    // Get the thread stack and push a new frame
    try (MemoryStack stack = stackPush()) {
      final IntBuffer pWidth = stack.mallocInt(1); // int*
      final IntBuffer pHeight = stack.mallocInt(1); // int*

      // Get the window size passed to glfwCreateWindow
      glfwGetWindowSize(this.window, pWidth, pHeight);

      // Get the resolution of the primary monitor
      final GLFWVidMode screen = glfwGetVideoMode(glfwGetPrimaryMonitor());

      // Center the window
      glfwSetWindowPos(this.window, (screen.width() - pWidth.get(0)) / 2, (screen.height() - pHeight.get(0)) / 2);
    } // the stack frame is popped automatically

    // Make the OpenGL context current
    glfwMakeContextCurrent(this.window);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(this.window);

    // Give window ref to render engine after setup
    this.renderEngine = new RenderEngine(this.window);
  }

  public void loop() {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities();

    // Once the GLCapabilities are created, set the clear color once for all
    glClearColor(
        EarthDataView.SPACEBOX_COLOR_RED,
        EarthDataView.SPACEBOX_COLOR_BLUE,
        EarthDataView.SPACEBOX_COLOR_GREEN,
        EarthDataView.SPACEBOX_COLOR_ALPHA
    );

    this.logger.log(this.label);
    this.logger.log("");

    this.logger.log(this.earthDataView.toString());
    this.logger.log("");

    this.logger.log(this.pinDataViewer.toString());
    this.logger.openConsole();

    // Run the rendering loop until the user has either :
    // - closed the window
    // - pressed ESCAPE
    // - requested 'quit' command from the logger opened console
    while (!glfwWindowShouldClose(this.window)) {
      glfwPollEvents();
      renderEngine.render();
    }

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
  }

  @SuppressWarnings("rawtypes")
  public final CommandFunction quitCmdFn = (Argument[] x) -> {
    glfwSetWindowShouldClose(this.window, true); // We will detect this in the rendering loop
    this.logger.log("Thank you for using my software! © krikaliov");
    this.logger.log("Closing ...");
    this.logger.closeConsole();
  };
}
