package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.java2dengine.util.Vector2D;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter.CharacterPose;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter.PoseInfo;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;

public class ObjCharacterPose extends GameObject {

	private ObjPuyoBoard board;
	private PoseInfo info;
	private Vector2D direction;
	private boolean persist;
	
	private Rectangle boardRect;
	private double sintimer;
	private boolean wasInside;
	
	public ObjCharacterPose(ObjPuyoBoard board, CharacterPose pose) {
		this.info = board.getCharacter().getPose(pose);
		sprite = info.sprite;
		this.board = board;
		this.persist = pose.persist;
		
		int halfwidth = ((board.getHeight()-2)*16)/2;
		int halfheight = ((board.getHeight()-2)*16)/2;
		
		double xdiff = -info.dir.startX;
		double ydiff = (board.getY() + halfheight) - (info.dir.startY + 192/2);
		this.direction = new Vector2D(xdiff, ydiff).normalize();
		
		boardRect = new Rectangle();
		boardRect.setBounds((int) board.getX() + halfwidth-24, (int) board.getY() + halfheight-24, 48, 48);
	}
	
	@Override
	public void onCreate() {
		this.x = info.dir.startX + board.getX() + 48;
		this.y = info.dir.startY;
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		int centeredx = (int) x+sprite.getImage().getWidth()/2;
		int centeredy = (int) y+sprite.getImage().getHeight()/2;
		
		boolean inside = boardRect.contains(centeredx, centeredy);
		
		if (inside || (persist && wasInside)) {
			if (persist) {
				sintimer += delta*2d;
				y += (Math.sin(sintimer)/6d)*60d*delta;
			} else {
				x += direction.getX()*48*delta;
				y += direction.getY()*48*delta;
			}		
			wasInside = true;
		} else {
			x += direction.getX()*600*delta;
			y += direction.getY()*600*delta;
			
			if (wasInside && (y <= -192 || y >= 360)) {
				GameEngine.removeGameObject(this);
			}
		}
	}
	
	@Override
	public void render(Graphics2D g) {
		g.drawImage(info.sprite.getImage(), (int) x-sprite.getImage().getWidth()/2-4, (int) y, null);
	}
}
