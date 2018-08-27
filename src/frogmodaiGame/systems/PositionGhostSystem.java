package frogmodaiGame.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Aspect.Builder;
import com.artemis.systems.IteratingSystem;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import frogmodaiGame.Chunk;
import frogmodaiGame.FFMain;
import frogmodaiGame.components.*;

public class PositionGhostSystem extends IteratingSystem {
	ComponentMapper<Position> mPosition;
	ComponentMapper<CameraWindow> mCameraWindow;
	
	Screen screen;
	
	public PositionGhostSystem(Screen _screen) {
		super(Aspect.all(IsPositionGhost.class, Position.class));
		screen = _screen;
	}

	@Override
	protected void process(int e) { //This could be adapted into targeting systems etc
		if (FFMain.cameraID == -1) return;
		CameraWindow camWindow = mCameraWindow.create(FFMain.cameraID);
		if (e != camWindow.focus) return;
		Position pos = mPosition.create(e);
		Position camPos = mPosition.create(FFMain.cameraID);
		if (FFMain.keystroke != null) {
			KeyStroke k = FFMain.keystroke;
			Chunk chunk = FFMain.worldManager.getActiveChunk();
			if (k.getKeyType() == KeyType.ArrowLeft && pos.x > 0) pos.x--;
			if (k.getKeyType() == KeyType.ArrowRight && pos.x < chunk.width - 1) pos.x++;
			if (k.getKeyType() == KeyType.ArrowUp && pos.y > 0) pos.y--;
			if (k.getKeyType() == KeyType.ArrowDown && pos.y < chunk.height - 1) pos.y++;
			
			//System.out.println(String.format("%d %d", screenPos.x, screenPos.y));
		}
		Position screenPos = new Position();
		screenPos.x = pos.x - camPos.x;
		screenPos.y = pos.y - camPos.y;
		screen.setCharacter(screenPos.x, screenPos.y, new TextCharacter('X', TextColor.ANSI.WHITE, TextColor.ANSI.BLACK, SGR.BOLD));
	}

}
