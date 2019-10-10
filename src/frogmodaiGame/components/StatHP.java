package frogmodaiGame.components;

import frogmodaiGame.events.ChangeStat;
import net.mostlyoriginal.api.event.common.Subscribe;

public class StatHP extends Stat {
	public String name = "HP";
	
	public StatHP() {
		setMaxValue(1, true);
	}
}