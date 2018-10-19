package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter.CharacterPose;

public class ObjCharacterPose extends GameObject {

	private GameSprite sprite;
	private int spriteheight;
	private double lifetime = 1.25;
	private boolean leavescreen;
	
	private double timer;
	private double speed = 15;
	
	public ObjCharacterPose(PuyoCharacter chara, CharacterPose pose) {
		sprite = chara.getPose(pose);
		setSprite(sprite);
		if (sprite == null) { 
			GameEngine.getInstance().removeGameObject(this);
			return; 
		}
		spriteheight = sprite.getImage().getHeight()*2;
		leavescreen = pose == CharacterPose.CHAIN;
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		if (leavescreen && lifetime < 0) {
			speed += (delta*15d);
			y -= speed*60d*delta;
		} else {
			lifetime -= delta;
			
			if (lifetime > .25 && speed > 0) {
				speed -= (delta*24d);
				y -= speed*60d*delta;
			} else if (lifetime+delta > .25) {
				speed = 0;
				timer = 1.25;
			}
		}

		
		if (!leavescreen && lifetime <= .25) {
			timer += delta*2d;
			y += (Math.sin(timer)/6d)*60d*delta;
		} else if (y < -spriteheight) {
			GameEngine.getInstance().removeGameObject(this);
			return;
		}
	}
	
	@Override
	public void render(Graphics2D g) {
		g.drawImage(sprite.getImage(), (int) x-20, (int) y, 64*2, 96*2, null);
	}
}
