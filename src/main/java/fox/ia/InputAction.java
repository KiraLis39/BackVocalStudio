package fox.ia;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class InputAction {
	static Map<String, JComponent> compMap = new LinkedHashMap<>();

	public static void add(String name, Window window) {add(name, (JComponent) window.getComponent(0));}
	
	public static void add(String name, JComponent comp) {compMap.put(name, comp);}

	public static void set(String name, String commandName, int key, int mod, AbstractAction action) {
		if (compMap.containsKey(name)) {
			(compMap.get(name)).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, mod), commandName);
			(compMap.get(name)).getActionMap().put(commandName, action);
			return;
		}
		throw new RuntimeException("InputAction: Error!\nMap of InputAction has not contents component '" + name + "'.");
	}

	public static void clearAll() {compMap.clear();}
}
