package me.ipodtouch0218.multiplayerpuyo.objects.particle;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.java2dengine.util.Vector2D;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;

public class ParticleIceChip extends GameObject {

	private static GameSprite[] particles;
	{
		particles = new GameSprite[7];
		SpriteSheet sheet = new SpriteSheet("particles/ice.png", 8, 8);
		for (int i = 0; i < 7; i++) {
			particles[i] = sheet.getSprite(i, 0);
		}
	}
	private Vector2D vel = new Vector2D(0, (Math.random()*4d)-6d);
	private double lifetime;
	
	public ParticleIceChip(double xVel) {
		vel.setX(xVel);
		
		setSprite(particles[(int) (Math.random()*7d)]);
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }

		lifetime+=delta;
		vel.add(0,(0.15*60d*delta)); //Gravity
		
		x+=(vel.getX()*60d*delta);
		y+=(vel.getY()*60d*delta);
		
		
		if (lifetime > 2) {
			GameEngine.removeGameObject(this);
		}
	}
	
}
