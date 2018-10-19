package me.ipodtouch0218.multiplayerpuyo.menu;

import java.awt.Graphics2D;
import java.util.ArrayList;

import me.ipodtouch0218.multiplayerpuyo.manager.Controls;
import me.ipodtouch0218.multiplayerpuyo.manager.PlayerManager;
import me.ipodtouch0218.multiplayerpuyo.menu.elements.MenuElement;

public abstract class MenuPanel {

	private String name;
	private boolean wrapAround;
	protected MenuElement[][] elements;
	private int selectedX, selectedY;
	private long inputDelay;
	
	public MenuPanel(String name, boolean wrapAround) {
		this.name = name;
		this.wrapAround = wrapAround;
	}
	
	public abstract void createElements();
	
	private static boolean enterDown;
	public void tick() {
		if (PlayerManager.getInstance() == null || PlayerManager.getInstance().getMainControl() == null) { return; }
		Controls controls = PlayerManager.getInstance().getMainControl();
		if (elements == null) return;
		if (elements[selectedX][selectedY] != null) {
			elements[selectedX][selectedY].setSelected(true);
			
			if (!enterDown) {
				if (elements[selectedX][selectedY].listenForKeys()) return;
			}
			enterDown = controls.menuEnter;
		}
		
		int oldX = selectedX;
		int oldY = selectedY;
		
		if (inputDelay <= System.currentTimeMillis()) {
			if (controls.menuDown) {
				selectedY += 1;
			}
			if (controls.menuUp) {
				selectedY -= 1;
			}
			if (controls.menuLeft) {
				selectedX -= 1;
			}
			if (controls.menuRight) {
				selectedX += 1;
			}
		}
		
		if (selectedX < 0 || selectedX > elements.length-1) {
			if (wrapAround) {
				if (selectedX < 0) {
					selectedX = selectedX + elements.length;
				} else {
					selectedX = selectedX - elements.length ;
				}
			} else {
				if (selectedX < 0) {
					selectedX = 0;
				} else {
					selectedX = elements.length - 1;
				}
			}
		}
		
		if (selectedY < 0 || selectedY > elements[0].length-1) {
			if (wrapAround) {
				if (selectedY < 0) {
					selectedY = selectedY + elements[0].length;
				} else {
					selectedY = selectedY - elements[0].length;
				}
			} else {
				if (selectedY < 0) {
					selectedY = 0;
				} else {
					selectedY = elements[0].length - 1;
				}
			}
		}
		
		if (oldX != selectedX || oldY != selectedY) {
			inputDelay = System.currentTimeMillis()+150;
			if (elements[oldX][oldY] != null) {
				elements[oldX][oldY].onDeselect();
				elements[oldX][oldY].setSelected(false);
			}
			
			if (elements[selectedX][selectedY] != null) {
				elements[selectedX][selectedY].setSelected(true);
			
				elements[selectedX][selectedY].onSelect();
			}
		}
	}
	public void render(Graphics2D g) {
		ArrayList<MenuElement> rendered = new ArrayList<>();
		if (elements == null) return;
		for (MenuElement[] elementY : elements) {
			for (MenuElement elementX : elementY) {
				if (rendered.contains(elementX)) continue;
				if (elementX == null) continue;
				
				elementX.render(g);
				rendered.add(elementX);
			}
		}
	}
	
	public String getName() { return name; }
}
