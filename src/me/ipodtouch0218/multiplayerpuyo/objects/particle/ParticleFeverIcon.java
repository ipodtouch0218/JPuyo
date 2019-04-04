package me.ipodtouch0218.multiplayerpuyo.objects.particle;

import java.awt.Graphics;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;

public class ParticleFeverIcon extends GameObject {

	private static final GameSprite fever = new GameSprite("particles/fever.png", false);
	private double lifetime = 1.5;
	private double stretch = 0;
	
	public ParticleFeverIcon(ObjPuyoBoard board) {
		this.x = board.getX()+((board.getWidth()*16)/2)-fever.getImage().getWidth()/2;
		this.y = board.getY()+20;
	}
	
	@Override
	public void tick(double delta) {
		lifetime-=delta;
		if (lifetime >= 2.75) {
			stretch = Math.min(1, (((1.5)-lifetime)/.416));
		}
		
		if (lifetime <= 0) {
			GameEngine.removeGameObject(this);
		}
	}
	
	public void render(Graphics g) {
		if (stretch == 0) { return; }
		int width = (int) (fever.getImage().getWidth()*stretch);
		g.drawImage(fever.getImage(), (int) (x+(fever.getImage().getWidth()/2-width/2)), (int) y, width, fever.getImage().getHeight(), null);
	}
}
