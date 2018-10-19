package me.ipodtouch0218.multiplayerpuyo.menu;

import java.awt.Graphics2D;
import java.util.ArrayList;

import me.ipodtouch0218.java2dengine.object.GameObject;

public class MenuManager extends GameObject {

	private ArrayList<MenuPanel> panels = new ArrayList<>();
	private MenuHistory history = new MenuHistory();
	private MenuPanel currentPanel;
	private boolean show = true;
	
	public MenuPanel addPanel(MenuPanel p) {
		if (!(p == null || panels.contains(p))) {
			p.createElements();
			panels.add(p);
		}
		
		return p;
	}
	
	@Override
	public void tick(double delta) {
		if (currentPanel == null || !show) return;
		
		currentPanel.tick();
	}
	@Override
	public void render(Graphics2D g) {
		if (currentPanel == null || !show) return;
		
		currentPanel.render(g);
	}
	
	public void openNewPanel(MenuPanel panel) {
		currentPanel = panel;
		history.addNewPanel(panel);
	}
	public void openPreviousPanel() {
		currentPanel = history.getPreviousPanel();
	}
	
	public void hide() {
		show = false;
	}
	public void show() {
		show = true;
	}
	
	public MenuHistory getHistory() { return history; }
	public MenuPanel getPanelFromString(String name) {
		for (MenuPanel panel : panels) {
			if (panel.getName().equals(name)) {
				return panel;
			}
			
		}
		return null;
	}
}
