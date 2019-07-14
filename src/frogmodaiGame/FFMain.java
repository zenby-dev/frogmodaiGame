package frogmodaiGame;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Random;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

public class FFMain {

	public static WorldManager worldManager;
	static TerminalSize terminalSize;
	static Screen screen;
	static WindowBasedTextGUI textGUI;
	public static KeyStroke keystroke;
	static int playerID = -1;
	public static int cameraID = -1;
	static int loopSpeed = 10;
	public static Random random;
	//static final FFCommandFactory commandFactory = FFCommandFactory.init();

	public static void main(String[] _args) throws IOException {

		// FFHookManager.add("Update", "juke", (FFHookArgs args) ->
		// {System.out.println("hi");});

		DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
		try {
			// Initiate Terminal
			Terminal terminal = defaultTerminalFactory.createTerminalEmulator();
			screen = new TerminalScreen(terminal);
			screen.startScreen();
			screen.setCursorPosition(null);
			
			int screenWidth = 1040;
			int screenHeight = 540;
			
			if (terminal instanceof AWTTerminalFrame) {
				AWTTerminalFrame f = (AWTTerminalFrame)terminal;
				f.setSize(screenWidth, screenHeight);
				f.setTitle("FROGMODAI");
			}
			if (terminal instanceof SwingTerminalFrame) {
				SwingTerminalFrame f = (SwingTerminalFrame)terminal;
				f.setSize(screenWidth, screenHeight);
				f.setTitle("FROGMODAI");
			}
			
			textGUI = new MultiWindowTextGUI(screen);
			random = new Random();
			terminalSize = screen.getTerminalSize();

			// Initiate the world
			worldManager = new WorldManager(screen);

			// Initiate Archetypes
			ArchetypeBuilders.initArchetypes();

			////////////////////////////////////////////
			// ***THIS IS WHERE GAME START CODE GOES***//
			////////////////////////////////////////////

			// Load Chunks
			worldManager.start();

			mainLoop();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (screen != null) {
				try {
					screen.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static void mainLoop() {
		try {
			while (true) { // Main Loop
				// Handle Input
				keystroke = screen.pollInput();
				if (keystroke != null
						&& (keystroke.getKeyType() == KeyType.Escape || keystroke.getKeyType() == KeyType.EOF)) {
					break;
				} else if (keystroke != null) {
					HookManager.call("KeyPressed", (Object) keystroke);
				}

				// Handle Resizing
				TerminalSize newSize = screen.doResizeIfNecessary();
				if (newSize != null) {
					terminalSize = newSize;
					HookManager.call("ScreenResized");
				}

				worldManager.process();

				// Redraw screen
				screen.refresh();
				
				keystroke = null;
				
				try {
					Thread.sleep(loopSpeed);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
