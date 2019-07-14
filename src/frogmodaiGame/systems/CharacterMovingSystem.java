package frogmodaiGame.systems;

import com.artemis.Aspect;
import com.artemis.Aspect.Builder;
import com.artemis.ComponentMapper;
import com.artemis.EntitySystemTest.C;
import com.artemis.systems.IteratingSystem;

import frogmodaiGame.*;
import frogmodaiGame.commands.*;
import frogmodaiGame.components.*;

public class CharacterMovingSystem extends IteratingSystem {
	ComponentMapper<Position> mPosition;
	ComponentMapper<Mobile> mMobile;
	ComponentMapper<VirtualController> mVirtualController;
	ComponentMapper<ChunkAddress> mChunkAddress;
	ComponentMapper<OnTile> mOnTile;
	ComponentMapper<Tile> mTile;
	ComponentMapper<IsPlayer> mIsPlayer;
	ComponentMapper<OnTouch> mOnTouch;
	ComponentMapper<CameraWindow> mCameraWindow;
	//TODO: set player OnTile

	public CharacterMovingSystem() {
		super(Aspect.all(Position.class, Mobile.class, VirtualController.class, ChunkAddress.class));
	}

	private boolean tryMove(int e) {
		// Access Components
		Position pos = mPosition.create(e);
		Mobile mobile = mMobile.create(e);
		VirtualController virtualController = mVirtualController.create(e);
		ChunkAddress chunkAddress = mChunkAddress.create(e);
		Chunk chunk = FFMain.worldManager.getChunk(chunkAddress.worldID);
		OnTile onTile = mOnTile.create(e);
		if (onTile.tile == -1) {
			onTile.tile = chunk.getTile(pos.x, pos.y);
		}
		//TODO: Exception?
		Tile tile = mTile.create(onTile.tile);

		int targetX = pos.x;
		int targetY = pos.y;

		if (!(virtualController.peek() instanceof MoveCommand)) // only skim off and process move commands!
			return false;

		for (Command command : virtualController.actionList.toArray(new Command[1])) {
			if (command instanceof MoveCommand) {
				MoveCommand move = (MoveCommand) command;
				virtualController.actionList.remove(command);
				// And it will only go when earlier commands have been processed by their system
				// (ie a MoveCommand is at the front of the queue)

				// Read controller input
				targetX = pos.x + move.dx;
				targetY = pos.y + move.dy;

				//System.out.println(String.format("%d %d", targetX, targetY));
				
				int dir = DirectionConverter.toInt(new Position(move.dx, move.dy));
				
				// Failure points
				// If it does fail, all the moves that it didn't make it to in this list will
				// try again and again and again...
				if (!chunk.posInChunk(targetX, targetY) || tile.neighbors[dir] != -1) { //Any outside or special
					//POTENTIALLY MOVING INTO A DIFFERENT CHUNK
					//If tile.atplayerpos.neighborInDirectionPlayerIsMoving then
					//	move player there
					//DONE: Player should be able to grab what tile they're on
					//	So they can grab their neighboring tiles
					
					if (tile.neighbors[dir] == -1) return false; //But if neither, fail to move
					int neighbor = tile.neighbors[dir];
					
					Position tilePos = mPosition.create(neighbor); //Change the ent's position to be chunk-relative
					//We know what tile we're going to because neighbors
					ChunkAddress tileChunkAddress = mChunkAddress.create(neighbor);
					
					Chunk newChunk = FFMain.worldManager.getChunk(tileChunkAddress.worldID);
					CameraWindow camWindow = mCameraWindow.create(FFMain.cameraID);
					Position camPos = mPosition.create(FFMain.cameraID);
					Position camOffset = new Position();
					
					if (newChunk.isSolid(tilePos.x, tilePos.y)) {
						collisionEvent(e, neighbor);
						return false;
					}
					if (newChunk.isOccupied(tilePos.x, tilePos.y)) {
						collisionEvent(e, neighbor);
						return false;
					}
					
					//These were before failure points!!!! Don't mutate data b4 failure points please.
					chunkAddress.worldID = tileChunkAddress.worldID;
					
					camOffset.x = camPos.x - pos.x;
					camOffset.y = camPos.y - pos.y;
					
					chunk.setOccupied(pos.x, pos.y, false);
					pos.x = tilePos.x;
					pos.y = tilePos.y;
					onTile.tile = neighbor;
					//onTile.tile = newChunk.getTile(pos.x, pos.y);
					newChunk.setOccupied(pos.x, pos.y, true);
					if (mIsPlayer.has(e)) {
						FFMain.worldManager.shiftChunks(newChunk);
						int nx = pos.x + camOffset.x - move.dx;
						int ny = pos.y + camOffset.y - move.dy;
						nx %= camWindow.width;
						ny %= camWindow.height;
//						if (nx < 0)
//							nx += camWindow.width;
//						if (ny < 0)
//							ny += camWindow.height;
//						if (nx >= camWindow.width)
//							nx %= camWindow.width;
//						if (ny >= camWindow.height)
//							ny %= camWindow.height;
						camPos.x = nx;
						camPos.y = ny;
					}
				} else { //New position is within same chunk
					int neighbor = chunk.getTile(targetX, targetY);
					if (chunk.isSolid(targetX, targetY)) {
						collisionEvent(e, neighbor);
						return false;
					}
					if (chunk.isOccupied(targetX, targetY)) {
						collisionEvent(e, neighbor);
						return false;
					}

					// Locking in new values (if no failure!)
					chunk.setOccupied(pos.x, pos.y, false); // update occupation per-move
					pos.x = targetX;
					pos.y = targetY;
					onTile.tile = neighbor; //THIS DOES NOT WORK FOR CHANGING CHUNKS THO
					chunk.setOccupied(pos.x, pos.y, true);
				}
			}
		}

		return true;
	}

	private void collisionEvent(int e, int neighbor) {
		OnTouch onTouch = mOnTouch.create(e);
		onTouch.act.accept(e, neighbor);
	}

	@Override
	protected void process(int e) {
		Position pos = mPosition.create(e);
		int x = pos.x;
		int y = pos.y;

		// ***TODO*** Only move if it's your turn!
		boolean success = tryMove(e);

		// Access Components
//		pos = mPosition.create(e);
//		Mobile mobile = mMobile.create(e);
//		VirtualController virtualController = mVirtualController.create(e);
//		ChunkAddress chunkAddress = mChunkAddress.create(e);
//		Chunk chunk = FFMain.worldManager.getChunk(chunkAddress.worldID);

		/*
		 * if (success) { // HOPE THIS COVERS IT //if (x != pos.x || y != pos.y)
		 * //chunk.setOccupied(x, y, false); //just always set where you were to
		 * unoccupied, because only you will have the chance to move there in this cycle
		 * 
		 * chunk.setOccupied(pos.x, pos.y, true); }
		 */
	}

}
