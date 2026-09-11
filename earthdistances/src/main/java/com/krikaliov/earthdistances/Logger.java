package com.krikaliov.earthdistances;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Logger {

  private static final Logger INSTANCE = new Logger();

  private Thread inputConsoleThread;
  private volatile boolean running = false;

  private volatile CommandManager cmdManager;
  private volatile BufferedReader reader;
  private volatile PrintStream output;

  private final PinDataViewer pinViewer = new PinDataViewer();

  private Logger() {}

  public static Logger getInstance() { return INSTANCE; }

  public void set(CommandFunction quitAppFn, InputStream input, PrintStream output) {
    this.output = output;
    this.cmdManager = (output != null) ? new CommandManager(output, quitAppFn) : null;
    this.reader = (input != null) ? new BufferedReader(new InputStreamReader(input)) : null;
  }

  public synchronized void openConsole() {
    if (!running) {
      running = true;
      inputConsoleThread = new Thread(this::runInputConsole, "Logger-Console-Thread");
      inputConsoleThread.start();
    }
  }

  public synchronized void closeConsole() {
    if (running) {
      running = false;
      if (inputConsoleThread != null) {
        inputConsoleThread.interrupt();
        inputConsoleThread = null;
      }
      set(null, null, null);
    }
  }

  private void runInputConsole() {
    while (running && !Thread.currentThread().isInterrupted()) {
      try {
        if (output != null && reader != null) {
          if (reader.ready()) {
            String raw = reader.readLine();
            if (raw != null && !raw.trim().isEmpty() && cmdManager != null) {
              cmdManager.scan(raw.trim());
              output.println(pinViewer);
            }
          } else {
            Thread.sleep(50);
          }
        } else {
          Thread.sleep(100);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (IOException ignored) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
      }
    }
  }

  public void log(String msg) {
    if (output != null) output.println(msg);
  }
}
