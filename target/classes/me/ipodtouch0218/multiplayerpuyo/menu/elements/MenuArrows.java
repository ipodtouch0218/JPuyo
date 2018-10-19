package me.ipodtouch0218.multiplayerpuyo.menu.elements;

import java.awt.Color;
import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.manager.PlayerManager;

public class MenuArrows extends MenuElement {

	private static final GameSprite selectedLeft = new GameSprite("ui/menu/arrow-left-selected.png");
	private static final GameSprite selectedRight = new GameSprite("ui/menu/arrow-right-selected.png");
	private static final GameSprite left = new GameSprite("ui/menu/arrow-left.png");
	private static final GameSprite right = new GameSprite("ui/menu/arrow-right.png");
	
	protected String display;
	protected int value;
	private int min, max, interval, gap;
	
	public MenuArrows(int x, int y, String name, int minValue, int maxValue, int defaultValue, int gap) {
		this(x,y,name,minValue,maxValue,defaultValue,gap,1);
	}
	public MenuArrows(int x, int y, String name, int minValue, int maxValue, int defaultValue, int gap, int interval) {
		super(x, y);
		
		display = name;
		min = minValue;
		max = maxValue;
		value = defaultValue;
		this.gap = gap;
		this.interval = interval;
	}

	private long delay = System.currentTimeMillis();
	public boolean listenForKeys() {
		value = Math.max(min, Math.min(value, max));
		if (delay > System.currentTimeMillis()) { return true; }
		
		if (PlayerManager.getInstance().getMainControl().menuLeft) {
			delay = System.currentTimeMillis() + 125;
			value-=interval;
			onValueChange();
			value = Math.max(min, Math.min(value, max));
			return true;
		}
		if (PlayerManager.getInstance().getMainControl().menuRight) {
			delay = System.currentTimeMillis() + 125;
			value+=interval;
			onValueChange();
			value = Math.max(min, Math.min(value, max));
			return true;
		}
		
		return false;
	}
	public void onValueChange() {
		
	}

	@Override
	public void render(Graphics2D g) {
		value = Math.max(min, Math.min(value, max));
		int leftX = PlayerManager.getInstance().getMainControl().menuLeft ? 0 : 16;
		if (value <= min || !selected) {
			g.drawImage(left.getImage(), (int) x + 16, (int) y, null);
		} else {
			g.drawImage(selectedLeft.getImage(), (int) x + leftX, (int) y, null);
		}
		int rightX = PlayerManager.getInstance().getMainControl().menuRight ? gap+48 : gap+32;
		if (value >= max || !selected) {
			g.drawImage(right.getImage(), (int) x + 32 + gap, (int) y, null);
		} else {
			g.drawImage(selectedRight.getImage(), (int) x + rightX, (int) y, null);
		}
		
		if (selected) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.GRAY);
		}
		g.setFont(PuyoGameMain.puyofont);
		
		g.drawString(display + " " + (value == 0 ? "" : value), (int) x + 64, (int) y + 20);
	}
	
	public int getValue() { return value; }
}
