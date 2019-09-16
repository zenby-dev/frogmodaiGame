package frogmodaiGame.components;

import java.util.function.BiConsumer;

import com.artemis.Component;

public class OnTouched extends Component {
	public BiConsumer<Integer, Integer> act;
}
