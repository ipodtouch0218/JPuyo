package me.ipodtouch0218.multiplayerpuyo.objects.particle;

import java.awt.Graphics2D;
import java.util.ArrayList;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoIceBoard;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ParticleFallingPuyo extends GameObject {
	
	private static final double GRAVITY = 0.15;
	
	private ArrayList<ParticleFallingPuyo> list;
	
	private ObjPuyoBoard board;
	private double yVel = 0.2;
	private PuyoType type;
	private int targetX;
	private int targetY;
	private int pixelY;
	private Runnable torun;
	private boolean ice;
	private int icehealth;
	
	private boolean landed;
	
	
	public ParticleFallingPuyo(ObjPuyoBoard board, PuyoType type, int targetX, int targetY, ArrayList<ParticleFallingPuyo> list, double yVel, Runnable torun, boolean ice, int icehealth) {
		this.board = board;
		this.type = type;
		this.targetX = targetX;
		this.torun = torun;
		this.list = list;
		this.yVel = yVel;
		setSprite(type.getSprite(PuyoType.PuyoSprites.BASE));
		this.targetY = targetY;
		if (targetY != -1) {
			this.pixelY = (int) (board.getY()+(targetY*16));
		} else {
			this.pixelY = (int) (board.getY()+(board.getHeight()*16));
		}
		if (list != null)
			list.add(this);
		this.ice = ice;
		this.icehealth = icehealth;
	}
	
	@Override
	public void tick(double delta) {
		while (PuyoBoardManager.isPaused()) { return; }
		if (torun != null) {
			torun.run();
			torun = null;
		}
		
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

		g.setClip((int) board.getX()-3, (int) board.getY()+4, board.getWidth()*16, (board.getHeight()-2)*16);
		g.drawImage(sprite.getImage(), (int) x-4, (int) y-32, null);
		if (ice) {
			GameSprite overlay = ObjPuyoIceBoard.iceoverlay;
			if (icehealth < 2) {
				overlay = ObjPuyoIceBoard.icebroken;
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
		if (squish) {
			board.squishRow(targetX, 0.03, 5);
			GameSounds.DROP.play();
		}
		landed = true;
	}
	
	private void onLand() {
		if (!(targetX == -1 || targetY == -1)) {
			board.setPuyoAt(targetX, targetY, type);
			if (ice) {
				board.setIcy(targetX, targetY, true);
				board.getPuyoAt(targetX, targetY).icetimer = icehealth;
			}
		}
		if (list != null)
			list.remove(this);
		GameEngine.getInstance().removeGameObject(this);
	}
}
