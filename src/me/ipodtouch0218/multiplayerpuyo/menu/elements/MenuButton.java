package me.ipodtouch0218.multiplayerpuyo.menu.elements;

import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.multiplayerpuyo.manager.PlayerManager;

public abstract class MenuButton extends MenuElement {

	private GameSprite selectedSpr, deselectedSpr;
	private long delay;
	
	public MenuButton(int x, int y, GameSprite selected, GameSprite deselected) {
		super(x, y);
		this.selectedSpr = selected;
		this.deselectedSpr = deselected;
	}

	@Override
	public boolean listenForKeys() {
		if (PlayerManager.getInstance().isConfiguring()) {
			delay = -1;
			return false;
		}
		if (delay == -1) {
			delay = System.currentTimeMillis() + 500;
		}
		if (PlayerManager.getInstance().getMainControl().menuEnter && System.currentTimeMillis() > delay) {
			onClick();
		}
		return false;
	}
	
	public abstract void onClick();

	@Override
	public void render(Graphics2D g) {
		if (selected) {
			g.drawImage(selectedSpr.getImage(), x, y, null);
		} else {
			g.drawImage(deselectedSpr.getImage(), x, y, null);
		}
	}

}
