package frogmodaiGame.old;

import java.io.IOException;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class CurseMe {

	public static void main(String[] args) throws IOException {
		DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
		
		Terminal terminal = null;
		try {
			terminal = defaultTerminalFactory.createTerminal();
			terminal.putCharacter('H');
			terminal.putCharacter('e');
			terminal.putCharacter('l');
			terminal.putCharacter('l');
			terminal.putCharacter('o');
			terminal.putCharacter('\n');
			terminal.flush();
			Thread.sleep(2000);
			TerminalPosition startPosition = terminal.getCursorPosition();
			terminal.setCursorPosition(startPosition.withRelative(3, 2));
			terminal.flush();
			Thread.sleep(2000);
			terminal.setBackgroundColor(TextColor.ANSI.BLUE);
			terminal.setForegroundColor(TextColor.ANSI.YELLOW);
			for (char c : "Yellow on Blue".toCharArray()) {
				terminal.putCharacter(c);
				terminal.flush();
				Thread.sleep(100);
			}
			terminal.putCharacter('\n');
			Thread.sleep(2000);
			terminal.setCursorPosition(startPosition.withRelative(3, 3));
			terminal.enableSGR(SGR.BOLD);
			for (char c : "Yellow on Blue".toCharArray()) {
				terminal.putCharacter(c);
				terminal.flush();
				Thread.sleep(100);
			}
			Thread.sleep(2000);
			terminal.resetColorAndSGR();
			terminal.setCursorPosition(terminal.getCursorPosition().withColumn(0).withRelativeRow(1));
			for (char c : "Done".toCharArray()) {
				terminal.putCharacter(c);
			}
			terminal.flush();
			Thread.sleep(2000);
			terminal.bell();
			terminal.flush();
			Thread.sleep(200);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (terminal != null) {
				try {
					terminal.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
