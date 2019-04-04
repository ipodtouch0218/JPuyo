package me.ipodtouch0218.multiplayerpuyo.objects.particle;

import java.awt.Graphics2D;
import java.util.ArrayList;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoInfo;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ParticleFallingPuyo extends GameObject {
	
	private static final double GRAVITY = 0.15;
	
	private ArrayList<ParticleFallingPuyo> list;
	
	private ObjPuyoBoard board;
	private double yVel = 0.2;
	private PuyoInfo type;
	private int targetX;
	private int targetY;
	private int pixelY;
	
	private boolean landed;
	
	
	public ParticleFallingPuyo(ObjPuyoBoard board, PuyoInfo inf, int targetX, int targetY, ArrayList<ParticleFallingPuyo> list, double yVel) {
		this.board = board;
		this.type = inf.clone();
		this.targetX = targetX;
		this.list = list;
		this.yVel = yVel;
		if (type.type == PuyoType.GARBAGE && inf.partyItem != null) {
			setSprite(inf.partyItem.getSprite());
		} else {
			setSprite(type.type.getSprite(PuyoSprites.BASE));
		}
		this.targetY = targetY;
		if (targetY != -1) {
			this.pixelY = (int) (board.getY()+(targetY*16));
		} else {
			this.pixelY = (int) (board.getY()+(board.getHeight()*16));
		}
		if (list != null)
			list.add(this);
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		
		yVel += GRAVITY*60d*delta;
		y += (yVel*60d)*delta;
		
		if (landed) {
			onLand();
			return;
		}
		
		if (y >= pixelY) {
			y -= (yVel*60d*delta);
			bounce();
		}
	}
	
	@Override
	public void render(Graphics2D g) {
		if (PuyoBoardManager.isPaused()) { return; }
		
		g.setClip((int) board.getX()-3, (int) board.getY()+4, board.getWidth()*16, (board.getHeight()-2)*16);
		g.drawImage(sprite.getImage(), (int) x-4, (int) y-32, null);
		if (type.ice) {
			GameSprite overlay = ObjPuyoBoard.iceoverlay;
			if (type.icetimer < type.icehealth) {
				overlay = ObjPuyoBoard.icebroken;
			}
			g.drawImage(overlay.getImage(), (int) x-4, (int) y-32, null);
		}
		g.setClip(null);
	}
	
	private void bounce() {
		if (PuyoGameMain.quality == RenderQuality.LOW) {
			landed = true;
			return;
		}
		y = pixelY;
		
		boolean squish = true;
		for (ParticleFallingPuyo others : list) {
			if (others.targetX == this.targetX && others.targetY > targetY) {
				squish = false;
				break;
			}
		}
		boolean partysound = type.partyItem != null;
		if (squish) {
			if (partysound) {
				GameSounds.PARTY_BOX_LAND.play();
			} else {
				board.squishcolumn(targetX, 0.03, 5);
				GameSounds.DROP.play();
			}
		}
		landed = true;
	}
	
	private void onLand() {
		if (!(targetX == -1 || targetY == -1)) {
			board.setPuyoAt(targetX, targetY, type);
		}
		if (list != null)
			list.remove(this);
		GameEngine.removeGameObject(this);
	}
}
