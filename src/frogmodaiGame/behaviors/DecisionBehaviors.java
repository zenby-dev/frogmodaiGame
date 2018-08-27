package frogmodaiGame.behaviors;

import java.util.function.Function;

import com.artemis.ComponentMapper;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import frogmodaiGame.*;
import frogmodaiGame.commands.*;
import frogmodaiGame.components.*;

public enum DecisionBehaviors {
	PlayerMove(new Function<Integer, Integer>() {
		ComponentMapper<VirtualController> mVirtualController;

		public Integer apply(Integer e) {
			KeyStroke keystroke = FFMain.keystroke;
			VirtualController virtualController = mVirtualController.create(e);
			int MOVE_COST = -1; //The default is to wait another cycle
			//Until pre-requisites are met
			//TODO: Other actors having limited number of cycles to make a decision???

			// virtualController.moveX = 0.0f;
			// virtualController.moveY = 0.0f;

			if (keystroke != null) {
				KeyType keytype = keystroke.getKeyType();
				boolean moving = true;
				if (keytype == KeyType.ArrowRight)
					virtualController.addAction(new MoveCommand(1, 0));
				else if (keytype == KeyType.ArrowLeft)
					virtualController.addAction(new MoveCommand(-1, 0));
				else if (keytype == KeyType.ArrowUp)
					virtualController.addAction(new MoveCommand(0, -1));
				else if (keytype == KeyType.ArrowDown)
					virtualController.addAction(new MoveCommand(0, 1));
				else if (keytype == KeyType.Character && keystroke.getCharacter() == '1') {
					virtualController.addAction(new MoveCommand(-1, 1));
				} else if (keytype == KeyType.Character && keystroke.getCharacter() == '3') {
					virtualController.addAction(new MoveCommand(1, 1));
				} else if (keytype == KeyType.Character && keystroke.getCharacter() == '7') {
					virtualController.addAction(new MoveCommand(-1, -1));
				} else if (keytype == KeyType.Character && keystroke.getCharacter() == '9') {
					virtualController.addAction(new MoveCommand(1, -1));
				} else {
					moving = false;
				}
				if (moving) {
					MOVE_COST = (int) (TimedActor.TICK_ENERGY * 0.9); // moving should take a majority of your energy
				}
				
				if (keytype == KeyType.Character) {
					char c = keystroke.getCharacter();
					if (c == 'p') {
						virtualController.addAction(new PickupCommand());
						MOVE_COST = (int) (TimedActor.TICK_ENERGY * -0.1);
						//Pause to open menu
						//Except I don't want to add the menu yet, just checking the local tile for objects
					}
					if (c == 'd') {
						virtualController.addAction(new DropCommand());
						MOVE_COST = (int) (TimedActor.TICK_ENERGY * -0.1);
						//Pause to open menu
						//Except I don't want to add the menu yet, just checking the local tile for objects
					}
				}
				
				FFMain.keystroke = null; // KEYSTROKES SHOULD NOT COUNT MORE THAN ONCE
			}

			return MOVE_COST;
		}
	}),

	GoblinMove(new Function<Integer, Integer>() {
		ComponentMapper<VirtualController> mVirtualController;

		public Integer apply(Integer e) {
			// KeyStroke keystroke = FFMain.keystroke;
			VirtualController virtualController = mVirtualController.create(e);
			int MOVE_COST = -1;

			// virtualController.moveX = 0.0f;
			// virtualController.moveY = 0.0f;

			// if (keystroke != null) {
			// KeyType keytype = keystroke.getKeyType();
			boolean moving = true;
			int r = FFMain.random.nextInt(9);
			if (r == 0)
				virtualController.addAction(new MoveCommand(1, 0));
			else if (r == 1)
				virtualController.addAction(new MoveCommand(-1, 0));
			else if (r == 2)
				virtualController.addAction(new MoveCommand(0, -1));
			else if (r == 3)
				virtualController.addAction(new MoveCommand(0, 1));
			else if (r == 4) {
				virtualController.addAction(new MoveCommand(-1, 1));
			} else if (r == 5) {
				virtualController.addAction(new MoveCommand(1, 1));
			} else if (r == 6) {
				virtualController.addAction(new MoveCommand(-1, -1));
			} else if (r == 7) {
				virtualController.addAction(new MoveCommand(1, -1));
			} else {
				moving = false;
			}
			if (moving) {
				MOVE_COST = (int) (TimedActor.TICK_ENERGY * 0.9); // goblin should move ridiculously fast
			}
			// FFMain.keystroke = null; //KEYSTROKES SHOULD NOT COUNT MORE THAN ONCE
			// }

			// System.out.println(String.format("@%d %d", e, MOVE_COST));

			return MOVE_COST;
		}
	});

	public Function<Integer, Integer> act;

	DecisionBehaviors(Function<Integer, Integer> _act) {
		act = _act;
	}
}
