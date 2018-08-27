package frogmodaiGame.components;

import java.util.function.BiConsumer;

import com.artemis.Component;

public class OnTouch extends Component {
	public BiConsumer<Integer, Integer> act;
}
