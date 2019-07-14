package frogmodaiGame;

import java.util.function.Function;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import frogmodaiGame.behaviors.*;
import frogmodaiGame.components.*;

public class CreatureBuilder {

	ComponentMapper<Char> mChar;
	ComponentMapper<Position> mPosition;
	ComponentMapper<ChunkAddress> mChunkAddress;
	ComponentMapper<TimedActor> mTimedActor;
	ComponentMapper<Description> mDescription;
	ComponentMapper<CameraWindow> mCameraWindow;
	ComponentMapper<IsPositionGhost> mIsPositionGhost;
	ComponentMapper<SphereInfo> mSphereInfo;
	ComponentMapper<OnTouch> mOnTouch;

	CreatureBuilder(World world) {
		world.inject(this);
	}

	public int player(Chunk chunk, int x, int y) {
		int player = FFMain.worldManager.world.create(ArchetypeBuilders.Player.archetype);
		FFMain.playerID = player;
		
		Position pos = mPosition.create(player);
		pos.x = x;
		pos.y = y;
		
		Char character = mChar.create(player);
		character.character = '@';
		character.fgc = 7;
		character.bold = true;
		
		ChunkAddress chunkAddress = mChunkAddress.create(player);
		chunkAddress.worldID = chunk.worldID;
		
		TimedActor actor = mTimedActor.create(player);
		actor.speed = TimedActor.TICK_ENERGY;
		actor.energy = actor.speed;
		actor.act = DecisionBehaviors.PlayerMove.act;
		FFMain.worldManager.world.inject(actor.act); // required!!!
		
		Description desc = mDescription.create(player);
		desc.name = "Player";
		
		OnTouch onTouch = mOnTouch.create(player);
		onTouch.act = TouchBehaviors.PlayerTouch.act;
		FFMain.worldManager.world.inject(onTouch.act);
		
		//SphereInfo sphereInfo = mSphereInfo.create(player);
		//sphereInfo.radius = 8;
		
		return player;
	}
	
	public int camera(int focus, int x, int y, int w, int h, int t) {
		int cam = FFMain.worldManager.world.create(ArchetypeBuilders.Camera.archetype);
		FFMain.cameraID = cam;
		Position pos = mPosition.create(cam);
		pos.x = x;
		pos.y = y;
		CameraWindow camWindow = mCameraWindow.create(cam);
		camWindow.width = w;
		camWindow.height = h;
		camWindow.focus = focus;
		camWindow.tolerance = t;
		return cam;
	}
	
	public int positionGhost(int x, int y) {
		int ghost = FFMain.worldManager.world.create();
		Position pos = mPosition.create(ghost);
		pos.x = x;
		pos.y = y;
		mIsPositionGhost.create(ghost);
		return ghost;
	}
	
	public int sphere(int x, int y, int _radius, float _speed) {
		int sphere = FFMain.worldManager.world.create();
		Position pos = mPosition.create(sphere);
		pos.x = x;
		pos.y = y;
		SphereInfo sphereInfo = mSphereInfo.create(sphere);
		sphereInfo.radius = _radius;
		sphereInfo.speed = _speed;
		return sphere;
	}

	public int goblin(Chunk chunk, int x, int y) {
		int gob = FFMain.worldManager.world.create(ArchetypeBuilders.Actor.archetype);
		
		Position pos = mPosition.create(gob);
		pos.x = x;
		pos.y = y;
		
		Char character = mChar.create(gob);
		character.character = 'g';
		character.fgc = 6;
		character.bold = false;
		
		ChunkAddress chunkAddress = mChunkAddress.create(gob);
		chunkAddress.worldID = chunk.worldID;
		
		TimedActor actor = mTimedActor.create(gob);
		actor.speed = TimedActor.TICK_ENERGY;
		actor.energy = actor.speed;
		actor.act = DecisionBehaviors.GoblinMove.act;
		FFMain.worldManager.world.inject(actor.act); // required!!!
		
		Description desc = mDescription.create(gob);
		desc.name = "Goblin";
		
		OnTouch onTouch = mOnTouch.create(gob);
		onTouch.act = TouchBehaviors.GoblinTouch.act;
		FFMain.worldManager.world.inject(onTouch.act);
		
		return gob;
	}
}
