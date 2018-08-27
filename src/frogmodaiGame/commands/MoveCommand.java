package frogmodaiGame.commands;

import frogmodaiGame.*;

public class MoveCommand implements Command {
	public int dx = 0;
	public int dy = 0;
	
	public MoveCommand(int _dx, int _dy) {
		dx = _dx;
		dy = _dy;
	}
	
	@Override
	public void execute() {
		
	}
	
}
