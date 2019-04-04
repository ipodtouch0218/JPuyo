package me.ipodtouch0218.multiplayerpuyo.menu.elements;

import java.awt.Color;
import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.manager.PlayerManager;

public class MenuCheckbox extends MenuElement {

	private static final GameSprite checked = new GameSprite("ui/menu/checkbox-1.png", false);
	private static final GameSprite unchecked = new GameSprite("ui/menu/checkbox-0.png", false);
	
	private String text;
	private boolean value;
	private int gap;
	
	public MenuCheckbox(int x, int y, String text, int gap, boolean defaultValue) {
		super(x, y);
		this.text = text;
		this.gap = gap;
		this.value = defaultValue;
	}

	private boolean isDown;
	public boolean listenForKeys() {
		if (PlayerManager.getInstance().getMainControl().menuEnter && !isDown) {
			value = !value;
			isDown = true;
		} 
		isDown = PlayerManager.getInstance().getMainControl().menuEnter;
		return false;
	}
	@Override
	public void render(Graphics2D g) {
		int x = super.x-((32+gap)/2);
		if (selected) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.GRAY);
		}
		g.setFont(PuyoGameMain.puyofont);
		g.drawString(text, x, y+20);
		
		if (value) {
			g.drawImage(checked.getImage(), x + gap, y, null);
		} else {
			g.drawImage(unchecked.getImage(), x + gap, y, null);
		}
	}
	

	public boolean getValue() { return value; }

}
