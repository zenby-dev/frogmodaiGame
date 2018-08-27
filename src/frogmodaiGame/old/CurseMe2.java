package frogmodaiGame.old;

import java.io.IOException;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.bundle.*;

public class CurseMe2 {

	public static void main(String[] args) throws IOException {
		DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
		Terminal terminal = null;
		try {
			terminal = defaultTerminalFactory.createTerminal();
			terminal.enterPrivateMode();
			terminal.clearScreen();
			terminal.setCursorVisible(false);
			final TextGraphics textGraphics = terminal.newTextGraphics();
			textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
			textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
			textGraphics.putString(2, 1, "Lanterna Tutorial 2 - press ESC to exit", SGR.BOLD);
			textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
			textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
			textGraphics.putString(5, 3, "Terminal Size: ", SGR.BOLD);
			textGraphics.putString(5 + "Terminal Size: ".length(),  3, terminal.getTerminalSize().toString());
			terminal.flush();
			
			terminal.addResizeListener(new TerminalResizeListener() {
				@Override
				public void onResized(Terminal terminal, TerminalSize newSize) {
					// Be careful here though, this is likely running on a separate thread. Lanterna is threadsafe in 
	                // a best-effort way so while it shouldn't blow up if you call terminal methods on multiple threads, 
	                // it might have unexpected behavior if you don't do any external synchronization
					try {
						textGraphics.drawLine(5, 3, newSize.getColumns() - 1, 3, ' ');
						textGraphics.putString(5, 3, "Terminal Size: ", SGR.BOLD);
						textGraphics.putString(5 + "Terminal Size: ".length(),  3, terminal.getTerminalSize().toString());
						terminal.flush();
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
			
			textGraphics.putString(5,  4, "Last Keystroke: ", SGR.BOLD);
			textGraphics.putString(5 + "Last Keystroke: ".length(), 4, "<Pending>");
			terminal.flush();
			KeyStroke keystroke = terminal.readInput();
			while(keystroke.getKeyType() != KeyType.Escape) {
				textGraphics.putString(5,  4, "Last Keystroke: ", SGR.BOLD);
				textGraphics.putString(5 + "Last Keystroke: ".length(), 4, keystroke.toString());
				terminal.flush();
				keystroke = terminal.readInput();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (terminal != null) {
				try {
					terminal.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
