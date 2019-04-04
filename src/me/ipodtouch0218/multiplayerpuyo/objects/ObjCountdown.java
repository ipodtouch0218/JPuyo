package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;

public class ObjCountdown extends GameObject {

	private static final GameSprite[] countdownSprites = {new GameSprite("ui/board/countdown-3.png", false), 
			  new GameSprite("ui/board/countdown-2.png", false),
			  new GameSprite("ui/board/countdown-1.png", false),
			  new GameSprite("ui/board/countdown-go.png", false)};
	
	private PuyoBoardManager manager;
	
	private double timer = 0;
	private double scaleAmount = 1;
	
	public ObjCountdown(PuyoBoardManager manager) {
		sprite = countdownSprites[0];
		this.manager = manager;
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		timer += delta;
		sprite = countdownSprites[Math.min((int) timer, 3)];
		
		if (timer >= 4) { //scale down
			if (!manager.hasStarted()) {
				manager.startGame();
			}
			scaleAmount = -5 * Math.pow((timer-4)-0.2, 2) + 2.2;
		} else {
			scaleAmount = (timer % 1) + 1;
		}
		if (scaleAmount <= 0) {
			GameEngine.removeGameObject(this);
		}
		
	}
	public void render(Graphics2D g) {
		int width = (int) (scaleAmount*sprite.getImage().getWidth(null));
		int height = (int) (scaleAmount*sprite.getImage().getHeight(null));
		
		if (width == 0 || height == 0) { return; }
		g.drawImage(sprite.getImage(), (int) (x-(width/2)), (int) (y-(height/2)), width, height, null);
	}
}
