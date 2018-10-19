package me.ipodtouch0218.multiplayerpuyo.menu.elements;

import java.awt.Color;
import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.manager.PlayerManager;

public class MenuItemArrows<T> extends MenuElement {
	
	private static final GameSprite selectedLeft = new GameSprite("ui/menu/arrow-left-selected.png");
	private static final GameSprite selectedRight = new GameSprite("ui/menu/arrow-right-selected.png");
	private static final GameSprite left = new GameSprite("ui/menu/arrow-left.png");
	private static final GameSprite right = new GameSprite("ui/menu/arrow-right.png");
	
	private T[] display;
	protected int value;
	private int gap;
	
	public MenuItemArrows(int x, int y, int defaultValue, int gap, T[] elements) {
		super(x, y);
		
		value = defaultValue;
		this.display = elements;
		this.gap = gap;
	}

	private long delay = System.currentTimeMillis();
	public boolean listenForKeys() {
		if (delay > System.currentTimeMillis()) { return true; }
		
		if (PlayerManager.getInstance().getMainControl().menuLeft) {
			changeValue(-1);
			return true;
		}
		if (PlayerManager.getInstance().getMainControl().menuRight) {
			changeValue(1);
			return true;
		}
		
		return false;
	}
	private void changeValue(int inc) {
		delay = System.currentTimeMillis() + 125;
		value += inc;
		value = Math.max(0, Math.min(value, display.length-1));
		onValueChange();
	}
	
	public void onValueChange() {
		
	}

	@Override
	public void render(Graphics2D g) {
		int max = display.length-1;
		value = Math.max(0, Math.min(value, max));
		int leftX = PlayerManager.getInstance().getMainControl().menuLeft ? 0 : 16;
		if (value <= 0 || !selected) {
			g.drawImage(left.getImage(), x+16, y, null);
		} else {
			g.drawImage(selectedLeft.getImage(), x+leftX, y, null);
		}
		int rightX = PlayerManager.getInstance().getMainControl().menuRight ? gap+48 : gap+32;
		if (value >= max || !selected) {
			g.drawImage(right.getImage(), x+32+gap, y, null);
		} else {
			g.drawImage(selectedRight.getImage(), x+rightX, y, null);
		}
		
		if (selected) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.GRAY);
		}
		g.setFont(PuyoGameMain.puyofont);
		
		Object output = display[value];
		g.drawString(output.toString(), x+64, y+20);
	}
	
	public T getValue() { return display[value]; }

}
