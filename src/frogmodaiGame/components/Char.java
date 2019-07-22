package frogmodaiGame.components;

import java.util.Random;

import com.artemis.Component;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

//0 = Black
//1 = Red
//2 = Green
//3 = Yellow
//4 = Blue
//5 = Magenta
//6 = Cyan
//7 = White
//8 = Default

public class Char extends Component {
	// byte type;
	public int fgc;
	public int bgc;
	public char character;
	public boolean bold;
	// short properties;
	// FFObject tileObject;

	public Char(char _character, byte _fgc, byte _bgc, boolean _bold) {
		character = _character;
		fgc = _fgc;
		bgc = _bgc;
		bold = _bold;
		// type = _type;
		// properties = _properties;
		// tileObject = _tileObject;
	}

	public Char() { // default is green period on black background (grass)
		// type = 0;
		// properties = 0;
		fgc = 2;
		bgc = 0;
		Random r = new Random();
		character = r.nextInt(10) > 3 ? '.' : ',';
		bold = false;
		// tileObject = new FFObject();
	}

	public TextCharacter getTextCharacter() {
		if (bold) {
			return new TextCharacter(character, TextColor.ANSI.values()[fgc], TextColor.ANSI.values()[bgc], SGR.BOLD);
		} else {
			return new TextCharacter(character, TextColor.ANSI.values()[fgc], TextColor.ANSI.values()[bgc]);
		}
	}
	
	public TextCharacter getTextCharacter(int f, int b, boolean bo) {
		if (bo) {
			return new TextCharacter(character, TextColor.ANSI.values()[f], TextColor.ANSI.values()[b], SGR.BOLD);
		} else {
			return new TextCharacter(character, TextColor.ANSI.values()[f], TextColor.ANSI.values()[b]);
		}
	}
	
	public TextCharacter getTextCharacter(boolean bo) {
		if (bo) {
			return new TextCharacter(character, TextColor.ANSI.values()[fgc], TextColor.ANSI.values()[bgc], SGR.BOLD);
		} else {
			return new TextCharacter(character, TextColor.ANSI.values()[fgc], TextColor.ANSI.values()[bgc]);
		}
	}
}
