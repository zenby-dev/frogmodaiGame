package frogmodaiGame;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
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
import com.googlecode.lanterna.terminal.ExtendedTerminal;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

public class FFMain {

	public static WorldManager worldManager;
	public static TerminalSize terminalSize;
	public static Screen screen;
	public static Terminal terminal;
	static WindowBasedTextGUI textGUI;
	public static KeyStroke keystroke;
	public static int playerID = -1;
	public static int cameraID = -1;
	static int loopSpeed = 10;
	public static Random random;
	public static int screenWidth = 64*2;
	public static int screenHeight = 32;
	//static final FFCommandFactory commandFactory = FFCommandFactory.init();

	public static void main(String[] _args) throws IOException {

		// FFHookManager.add("Update", "juke", (FFHookArgs args) ->
		// {System.out.println("hi");});

		DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
		try {
			// Initiate Terminal
			terminal = defaultTerminalFactory.createTerminalEmulator();
			screen = new TerminalScreen(terminal);
			screen.startScreen();
			screen.setCursorPosition(null);
			
			if (terminal instanceof AWTTerminalFrame) {
				AWTTerminalFrame f = (AWTTerminalFrame)terminal;
				FontMetrics m = f.getFontMetrics(f.getFont());
				int windowWidth = screenWidth*m.charWidth(' ')*2;	 //1040;
				int windowHeight = (screenHeight+2)*m.getHeight(); //540
				f.setSize(windowWidth, windowHeight);
				f.setTitle("FROGMODAI");
			}
			if (terminal instanceof SwingTerminalFrame) {
				SwingTerminalFrame f = (SwingTerminalFrame)terminal;
				FontMetrics m = f.getFontMetrics(f.getFont());
				int windowWidth = screenWidth*m.charWidth(' ')*2;	 //1040;
				int windowHeight = (screenHeight+2)*m.getHeight(); //540
				f.setSize(windowWidth, windowHeight);
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
	
	/*Point getMousePos() {
		return terminal
	}*/

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

	public static int red(int c) {
		return (c & 0xFF000000) >> 24;
	}
	
	public static int green(int c) {
		return (c & 0x00FF0000) >> 16;
	}
	
	public static int blue(int c) {
		return (c & 0x0000FF00) >> 8;
	}
	
	//0 = Black
	//1 = Red
	//2 = Green
	//3 = Yellow
	//4 = Blue
	//5 = Magenta
	//6 = Cyan
	//7 = White
	//8 = Default
	
	public static int RGBToLanterna(int c) {
		int r = red(c);
		int g = green(c);
		int b = blue(c);
		
		System.out.println(r + "," + g + "," + b);
		
		if (r == 0 && g == 0 && b == 0) return 0;
		if (r == 255 && g == 0 && b == 0) return 1;
		if (r == 0 && g == 255 && b == 0) return 2;
		if (r == 255 && g == 255 && b == 0) return 3;
		if (r == 0 && g == 0 && b == 255) return 4;
		if (r == 255 && g == 0 && b == 255) return 5;
		if (r == 0 && g == 255 && b == 255) return 6;
		if (r == 255 && g == 255 && b == 255) return 7;
		
		return 0;
	}
}
