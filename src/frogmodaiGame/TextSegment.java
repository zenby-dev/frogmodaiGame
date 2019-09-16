package frogmodaiGame;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;

public class TextSegment {
	public String text;
	public TextColor.ANSI foreground;
	public TextColor.ANSI background;
	public SGR sgr;
	
	public TextSegment(String s) {
		text = s;
		foreground = TextColor.ANSI.WHITE;
		background = TextColor.ANSI.BLACK;
	}
	
	public TextSegment(String s, TextColor.ANSI fg, TextColor.ANSI bg) {
		text = s;
		foreground = fg;
		background = bg;
	}
	
	public TextSegment(String s, TextColor.ANSI fg, TextColor.ANSI bg, SGR _sgr) {
		text = s;
		foreground = fg;
		background = bg;
		sgr = _sgr;
	}
}
