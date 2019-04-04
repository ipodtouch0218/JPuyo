package me.ipodtouch0218.multiplayerpuyo.objects.particle;

import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoInfo;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;

public class ParticleIceWave extends GameObject {

	private static final GameSprite spr = new GameSprite("ui/board/icewave.png", true);
	
	private ObjPuyoBoard board;
	private boolean finished;
	private int icedrows;
	
	public ParticleIceWave(ObjPuyoBoard board) {
		this.board = board;
		x = board.getX()-4;
		y = board.getY()+4-32;
	}
	
	@Override
	public void tick(double delta) {
		y += 120*delta;
		int afterBoardY = (int) ((y-board.getY()-4)/16d)+2;
		
		while (afterBoardY >= icedrows) {
			icedrows++;
			if (icedrows >= board.getHeight()) { continue; }
			xloop:
			for (int x = 0; x < board.getWidth(); x++) {
				PuyoInfo inf = board.getPuyoAt(x, icedrows);
				if (inf.type == null) { continue xloop; }
				inf.ice = true;
				inf.icetimer = 3;
				inf.icehealth = 3;
				
				
			}
		}
		
		
		if (y >= board.getY()+((board.getHeight()-2)*16)) {
			finished = true;
			GameEngine.removeGameObject(this);
		}
	}
	
	@Override
	public void render(Graphics2D g) {
		g.setClip((int) board.getX()-4, (int) board.getY(), board.getWidth()*16, (board.getHeight()-2)*16);
		g.drawImage(spr.getImage(), (int) x, (int) y, null);
		g.setClip(null);
	}
	
	public boolean isFinished() { return finished; }
}
