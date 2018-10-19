package me.ipodtouch0218.multiplayerpuyo.objects.particle;

import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.java2dengine.util.Vector2D;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard;

public class ParticlePuyoCleared extends GameObject {

	private double lifetime = 3;
	private Vector2D vel = new Vector2D(Math.random()*4d-2d, -0.5-(Math.random()*0.5d));
	
	public ParticlePuyoCleared(int x, int y, ObjPuyoBoard board, PuyoType type) {
		this.x = (board.getX()+x*16);
		this.y = (board.getY()+y*16);
		this.sprite = type.getSprite(PuyoSprites.POPPING);
	}
	
	@Override
	public void tick(double delta) {
		lifetime-=delta;
		if (lifetime <= 0) { PuyoGameMain.getGameEngine().removeGameObject(this); return; }
		
		vel.add(0, (0.3*60d)*delta);
		x += vel.getX();
		y += vel.getY(); 
	}
	
}
