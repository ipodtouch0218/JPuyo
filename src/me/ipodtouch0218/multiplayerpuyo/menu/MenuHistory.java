package me.ipodtouch0218.multiplayerpuyo.menu;

import java.util.ArrayList;

public class MenuHistory {

	private ArrayList<MenuPanel> previousPanels = new ArrayList<>();
	
	public void addNewPanel(MenuPanel p) {
		previousPanels.add(p);
	}
	
	public MenuPanel getPreviousPanel() {
		if (previousPanels.size() < 2) { return null; }
		
		MenuPanel panel = previousPanels.get(previousPanels.size()-2);
		previousPanels.remove(previousPanels.size()-1);
		
		return panel;
	}
}
