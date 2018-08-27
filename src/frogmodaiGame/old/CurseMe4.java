package frogmodaiGame.old;

import java.io.IOException;
import java.util.Random;

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

public class CurseMe4 {

	public static void main(String[] args) throws IOException {
		DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
		Screen screen = null;
		try {
			Terminal terminal = defaultTerminalFactory.createTerminal();
			screen = new TerminalScreen(terminal);
			screen.startScreen();
			screen.setCursorPosition(null);
			Random random = new Random();
			TerminalSize terminalSize = screen.getTerminalSize();
			for (int column = 0; column < terminalSize.getColumns(); column++) {
				for (int row = 0; row < terminalSize.getRows(); row++) {
					screen.setCharacter(column, row, new TextCharacter(
							' ',
							TextColor.ANSI.DEFAULT,
							TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)]));
				}
			}
			screen.refresh();
			
			long startTime = System.currentTimeMillis();
			while(System.currentTimeMillis() - startTime < 2000) {
				if (screen.pollInput() != null) {
					break;
				}
				try {
					Thread.sleep(1);
				} catch(InterruptedException ignore) {
					break;
				}
			}
			
			while(true) {
				KeyStroke keystroke = screen.pollInput();
				if (keystroke != null && (keystroke.getKeyType() == KeyType.Escape || keystroke.getKeyType() == KeyType.EOF)) {
					break;
				}
				TerminalSize newSize = screen.doResizeIfNecessary();
				if (newSize != null) {
					terminalSize = newSize;
				}
				
				//Increase this to increase speed
				final int charactersToModifyPerLoop = 1;
				for (int i = 0; i < charactersToModifyPerLoop; i++) {
					TerminalPosition cellToModify = new TerminalPosition(
							random.nextInt(terminalSize.getColumns()),
							random.nextInt(terminalSize.getRows()));
					TextColor.ANSI color = TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)];
					TextCharacter characterInBackBuffer = screen.getBackCharacter(cellToModify);
					characterInBackBuffer = characterInBackBuffer.withBackgroundColor(color);
					characterInBackBuffer = characterInBackBuffer.withCharacter(' ');
					screen.setCharacter(cellToModify, characterInBackBuffer);
				}
				
				String sizeLabel = "Terminal Size: " + terminalSize;
				TerminalPosition labelBoxTopLeft = new TerminalPosition(1, 1);
				TerminalSize labelBoxSize = new TerminalSize(sizeLabel.length() + 2, 3);
				TerminalPosition labelBoxTopRightCorner = labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 1);
				TextGraphics textGraphics = screen.newTextGraphics();
				textGraphics.fillRectangle(labelBoxTopLeft, labelBoxSize, ' ');
				
				textGraphics.drawLine(
						labelBoxTopLeft.withRelativeColumn(1), 
						labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 2), 
						Symbols.DOUBLE_LINE_HORIZONTAL);
				textGraphics.drawLine(
						labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(1),
						labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(labelBoxSize.getColumns() - 2),
						Symbols.DOUBLE_LINE_HORIZONTAL);
				textGraphics.setCharacter(labelBoxTopLeft, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
				textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
				textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(2), Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
				textGraphics.setCharacter(labelBoxTopRightCorner, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
				textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
				textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(2), Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);

				textGraphics.putString(labelBoxTopLeft.withRelative(1, 1), sizeLabel);
				
				screen.refresh();
				Thread.yield();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (screen != null) {
				try {
					screen.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
