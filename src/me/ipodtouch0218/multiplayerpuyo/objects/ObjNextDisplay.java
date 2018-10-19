package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;

public class ObjNextDisplay extends GameObject {

	private static final GameSprite sprite = new GameSprite("ui/board/next-box.png");
	private static final GameSprite spriteD = new GameSprite("ui/board/next-box-big.png");
	private PuyoType[] firstGroup;
	private PuyoType[] secondGroup;
	private boolean doubled;
	
	public ObjNextDisplay(boolean doubled) {
		this.doubled = doubled;
	}
	
	public void render(Graphics2D g) {
		GameSprite renderSprite = (doubled ? spriteD : sprite);
		int x = (int) (super.x + 2);
		int y = (int) super.y;
		
		g.drawImage(renderSprite.getImage(), x, y, null);
		
		if (PuyoBoardManager.isPaused()) { return; }
		
		if (firstGroup != null) {
			g.drawImage(firstGroup[1].getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+4, y+12, null);
			g.drawImage(firstGroup[0].getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+4, y+28, null);
		}
		
		if (doubled && secondGroup != null) {
			g.drawImage(secondGroup[1].getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+22, y+45, 8, 8, null);
			g.drawImage(secondGroup[0].getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+22, y+55, 8, 8, null);
		}
	}
	
	public PuyoType[] getPuyoGroup() {
		return firstGroup;
	}
	public void addPuyoGroup(PuyoType[] value) {
		if (doubled && firstGroup != null) {
			secondGroup = value;
			return;
		}
		firstGroup = value;
	}

	public void changeGroup() {
		firstGroup = secondGroup;
		secondGroup = null;
	}
	public void clear() {
		firstGroup = null;
		secondGroup = null;
	}
}
