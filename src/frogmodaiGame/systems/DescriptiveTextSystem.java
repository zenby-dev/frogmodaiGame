package frogmodaiGame.systems;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.annotations.EntityId;
import com.artemis.systems.IteratingSystem;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenBuffer;

import frogmodaiGame.Chunk;
import frogmodaiGame.FFMain;
import frogmodaiGame.Paragraph;
import frogmodaiGame.TextSegment;
import frogmodaiGame.components.*;

public class DescriptiveTextSystem extends IteratingSystem { // This is for terrain only
	Screen screen;
	@EntityId
	public int perspective = -1;
	boolean fullRedraw = false;
	TextCharacter emptyCharacter;
	double PI = 3.14159265;

	public boolean drewThisFrame = false;

	ScreenBuffer buffer;
	TerminalPosition bufferPosition;
	TerminalSize bufferSize;

	ArrayDeque<Paragraph> paragraphs;
	int maxParagraphs = 40;

	public DescriptiveTextSystem(Screen _screen) { // Matches camera, not tiles, for performance
		super(Aspect.all(Position.class, CameraWindow.class));

		bufferPosition = new TerminalPosition(FFMain.screenWidth / 2, 0);
		bufferSize = new TerminalSize(FFMain.screenWidth / 2, FFMain.screenHeight);

		screen = _screen;
		System.out.printf("%d, %d\n", FFMain.screenWidth / 2, FFMain.screenHeight);
		buffer = new ScreenBuffer(bufferSize, new TextCharacter(' ', TextColor.ANSI.BLACK, TextColor.ANSI.BLACK));
		emptyCharacter = new TextCharacter(' ', TextColor.ANSI.BLACK, TextColor.ANSI.BLACK);

		paragraphs = new ArrayDeque<Paragraph>();

		triggerRedraw();
	}

	public void addParagraph(Paragraph p) {
		paragraphs.addLast(p);
		if (paragraphs.size() > maxParagraphs) {
			paragraphs.pollFirst();
		}
		triggerRedraw();
	}

	public void triggerRedraw() {
		fullRedraw = true;
	}

	@Override
	protected void process(int e) { // this happens with high frequency
		drewThisFrame = false;

		// fullRedraw = true;
		if (fullRedraw) {
			clearBuffer();

			// DO DRAWING STUFF HERE
			drawBorder();
			drawParagraphs();

			// Renders this buffer to the screen
			TextGraphics tg = screen.newTextGraphics();
			tg.fillRectangle(bufferPosition, bufferSize, emptyCharacter);
			tg.drawImage(new TerminalPosition(FFMain.screenWidth / 2, 0), buffer);

			fullRedraw = false;
			drewThisFrame = true;
		}

	}

	private void clearBuffer() {
		buffer.newTextGraphics().fillRectangle(new TerminalPosition(0, 0), buffer.getSize(), emptyCharacter);
	}

	// buffer.setCharacterAt(screenPos.x, screenPos.y,
	// character.getTextCharacter());

	private void drawBorder() {
		TextGraphics tg = buffer.newTextGraphics();
		tg.drawRectangle(new TerminalPosition(0, 0), bufferSize,
				new TextCharacter('*', ANSI.YELLOW, ANSI.BLUE, SGR.BOLD));
	}

	private void drawParagraphs() {
		int py = bufferSize.getRows() - 2;
		Paragraph[] ps = new Paragraph[paragraphs.size()];
		paragraphs.toArray(ps);
		for (int i = paragraphs.size() - 1; i >= 0; i--) {
			Paragraph para = ps[i];
			int numLines = wrappedNumLines(para);
			py -= numLines;
			drawWrappedLines(py, para);
			py--;
			if (py < 1) break;
		}
	}
	// buffer.setCharacterAt(1, py, new TextCharacter('@', ANSI.CYAN, ANSI.GREEN));

	private void drawWrappedLines(int py, Paragraph p) {
		int width = buffer.getSize().getColumns() - 2;
		int x = 0;
		int y = 0;
		for (TextSegment ts : p.segments) {
			// x += ts.text.length();
			String text = ts.text.toString();
			if (x + text.length() > width) {
				while (x + text.length() > width) {
					// Starting from the beginning of the string,
					// use indexOf(' ') to chop off words from the original string
					// until the next one would make it too long.
					// Draw that segment and repeat.
					StringBuilder str1 = new StringBuilder();
					String str2 = text.toString();
					int i = 0;
					int nx = x;
					int nextIndex = str2.indexOf(' ');
					while (nx + nextIndex+1 < width) {
						nx += nextIndex+1;
						str1.append(str2.subSequence(i, nextIndex+1));
						str2 = str2.substring(nextIndex+1);
						nextIndex = str2.indexOf(' ');
						if (nextIndex == -1) nextIndex = str2.length()-1;
						// x -= width;
						// y++;
					}
					TextSegment nts = new TextSegment(str1.toString(), ts.foreground, ts.background, ts.sgr);
					if (py+y > 0)
						drawSegment(1 + x, py + y, nts);
					text = str2.toString();
					y++;
					x = 0;
				}
			} 
			if (x + text.length() < width) {
				if (py+y > 0)
					drawSegment(1 + x, py + y, new TextSegment(text, ts.foreground, ts.background, ts.sgr));
				x += text.length();
			}
		}
	}

	private void drawSegment(int sx, int sy, TextSegment ts) {
		for (int i = 0; i < ts.text.length(); i++) {
			char c = ts.text.charAt(i);
			// System.out.printf("%d %d %c\n", sx+i, sy, c);
			if (ts.sgr == null) {
				buffer.setCharacterAt(sx + i, sy, new TextCharacter(c, ts.foreground, ts.background));
			} else {
				buffer.setCharacterAt(sx + i, sy, new TextCharacter(c, ts.foreground, ts.background, ts.sgr));
			}
		}
	}

	private int wrappedNumLines(Paragraph p) {
		int width = buffer.getSize().getColumns() - 2;
		int x = 0;
		int y = 0;
		for (TextSegment ts : p.segments) {
			// x += ts.text.length();
			String text = ts.text.toString();
			if (x + text.length() > width) {
				while (x + text.length() > width) {
					// Starting from the beginning of the string,
					// use indexOf(' ') to chop off words from the original string
					// until the next one would make it too long.
					// Draw that segment and repeat.
					StringBuilder str1 = new StringBuilder();
					String str2 = text.toString();
					int i = 0;
					int nx = x;
					int nextIndex = str2.indexOf(' ');
					while (nx + nextIndex+1 < width) {
						nx += nextIndex+1;
						str1.append(str2.subSequence(i, nextIndex+1));
						str2 = str2.substring(nextIndex+1);
						nextIndex = str2.indexOf(' ');
						if (nextIndex == -1) nextIndex = str2.length()-1;
						// x -= width;
						// y++;
					}
					//TextSegment nts = new TextSegment(str1.toString(), ts.foreground, ts.background, ts.sgr);
					//drawSegment(1 + x, py + y, nts);
					text = str2.toString();
					y++;
					x = 0;
				}
			} 
			if (x + text.length() < width) {
				//drawSegment(1 + x, py + y, new TextSegment(text, ts.foreground, ts.background, ts.sgr));
				x += text.length();
			}
		}
		return y;
	}

}
