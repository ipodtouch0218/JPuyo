package me.ipodtouch0218.multiplayerpuyo.objects.particle;

import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.java2dengine.util.Vector2D;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;

public class ParticlePuyoPop extends GameObject {

	private Vector2D vel = new Vector2D(0, (Math.random()*4d)-6d);
	private double lifetime;
	
	public ParticlePuyoPop(double xVel, PuyoType color) {
		vel.setX(xVel);
		if (color == null) {
			return;
		}
		
		setSprite(color.getSprite(PuyoType.PuyoSprites.POP_PARTICLE));
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }

		lifetime+=delta;
		vel.add(0,(0.2*60d*delta)); //Gravity
		
		x+=(vel.getX()*60d*delta);
		y+=(vel.getY()*60d*delta);
		
		
		if (lifetime > 2) {
			PuyoGameMain.getGameEngine().removeGameObject(this);
		}
	}
	
}
