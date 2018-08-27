package frogmodaiGame;

import java.util.HashMap;
import java.util.function.*;

public class HookManager {
	
	static HashMap<String, HashMap<String, Consumer<Object>>> hookMap = new HashMap<String, HashMap<String, Consumer<Object>>>();
	
	public static void add(String hookName, String instanceName, Consumer<Object> func) {
		confirmHookExists(hookName);
		hookMap.get(hookName).put(instanceName, func);
	}
	
	private static void confirmHookExists(String hookName) {
		if (!hookMap.containsKey(hookName)) {
			hookMap.put(hookName, new HashMap<String, Consumer<Object>>());
		}
	}
	
	public static void call(String hookName, Object args) { //calls all instances of the given hook
		confirmHookExists(hookName);
		for (String hookInstanceName : hookMap.get(hookName).keySet()) {
			hookMap.get(hookName).get(hookInstanceName).accept(args);
		}
	}
	
	public static void call(String hookName) { //calls all instances of the given hook
		call(hookName, new HookArgs() {});
	}
	
	public static void remove(String hookName, String instanceName) {
		if (!hookMap.containsKey(hookName)) return;
		hookMap.get(hookName).remove(instanceName);
	}
}