package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;
import java.awt.Image;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;

public class ObjCountdown extends GameObject {

	private GameSprite currentSprite;
	private boolean shrink;
	private double shrinkRate = .025;
	
	private double scaleTimer = .5;
	@Override
	public void tick(double delta) {
		
		if (PuyoBoardManager.isPaused()) { return; }
		
		if (!shrink) {
			if (currentSprite == null) currentSprite = sprite;
			
			if (currentSprite != sprite) {
				scaleTimer = .5*60d*delta;
				currentSprite = sprite;
			}
			scaleTimer+=.025*60d*delta;
		} else {
			shrinkRate-=.01*60d*delta;
			
			scaleTimer+=shrinkRate;
			if (scaleTimer-.1 <= 0) {
				PuyoGameMain.getGameEngine().removeGameObject(this);
				
			}
		}
		currentSprite.setScale(scaleTimer, scaleTimer);
	}
	public void render(Graphics2D g) {
		int width = (int) (sprite.getScaleX()*sprite.getImage().getWidth(null));
		int height = (int) (sprite.getScaleY()*sprite.getImage().getHeight(null));
		if (width == 0 || height == 0) { return; }
		g.drawImage(sprite.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH), (int) (x-(width/2)), (int) (y-(height/2)), null);
	}
	public void shrink() {
		shrink = true;
	}
}
