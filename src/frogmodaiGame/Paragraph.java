package frogmodaiGame;

import java.util.ArrayList;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;

public class Paragraph {
	public ArrayList<TextSegment> segments;
	
	public Paragraph() {
		segments = new ArrayList<TextSegment>();
	}
	
	public void add(String s) {
		add(new TextSegment(s));
	}
	
	public void add(String s, TextColor.ANSI fg) {
		add(new TextSegment(s, fg));
	}
	
	public void add(String s, TextColor.ANSI fg, TextColor.ANSI bg) {
		add(new TextSegment(s, fg, bg));
	}
	
	public void add(String s, TextColor.ANSI fg, TextColor.ANSI bg, SGR _sgr) {
		add(new TextSegment(s, fg, bg, _sgr));
	}

	public void add(TextSegment textSegment) {
		segments.add(textSegment);
	}
}
