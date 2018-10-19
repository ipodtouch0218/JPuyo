package me.ipodtouch0218.multiplayerpuyo.menu.elements;

import java.awt.Graphics2D;

public abstract class MenuElement {

	protected boolean selected;
	protected int x, y;
	
	public MenuElement(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void onSelect() {}
	public void onDeselect() {}
	
	public abstract boolean listenForKeys();
	public abstract void render(Graphics2D g);

	public void setSelected(boolean value) { selected = value; }
	public boolean isSelected() { return selected; }
}
