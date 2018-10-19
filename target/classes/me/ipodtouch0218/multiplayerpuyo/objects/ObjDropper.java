package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ObjDropper extends GameObject {

	public static final int[][][] shapeOffset = { {{0,0},{0,-1}}, {{0,0},{1,0}}, {{0,0},{0,1}}, {{0,0},{-1,0}} };
	
	private ObjPuyoBoard board;
	private int puyoX;
	private double puyoY;
	private int rotation;

	
	private double flashTimer = 0.5, rotTimer, moveDelay, dropTimer, landTimer, doublerotTimer;
	private int lowestY;
	private boolean rightrot;

	private PuyoType mainType, otherType;
	private boolean isFalling = true;
	private boolean smooth;
	private boolean disabled;
	
	public ObjDropper(ObjPuyoBoard board, PuyoType main, PuyoType other) {
		this.board = board;
		this.mainType = main;
		this.otherType = other;
		
		this.dropTimer = board.getBoardManager().getDropSpeed()/60d;
		this.landTimer = board.getBoardManager().getDropSpeed()/60d;
		this.smooth = (board instanceof ObjPuyoFeverBoard);
		this.puyoX = 2;
		this.puyoY = 2;
	}
	
	private boolean deleted;
	private boolean update;
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		if (deleted) {
			return;
		}
		update = (cachedlocs == null);
		prevY = puyoY;
		
		doublerotTimer -= delta;
		flashTimer-=delta;
		if (flashTimer <= 0) {
			flashTimer = 0.5;
		}
		if (isFalling && !disabled) {
			if (rotTimer > 0) {
				rotTimer = rotTimer-delta;
			}
			if (moveDelay <= 0) { 
				if (checkForMovement(delta)) {
					update = true;
				}
			} else {
				moveDelay = Math.max(0, moveDelay-delta);
				if (moveDelay == 0) {
					prevX = puyoX;
				}
			}
			if (checkForRotation()) {
				update = true;
			}
			
			checkForLanding(delta);
		}
		
		if (update) {
			cachedlocs = finalDropLocations();
			board.checkForFlashing(cachedlocs);
		}
		if (instaTimer > 0) {
			instaTimer-=delta;
		}
	}
	
	private int prevX;
	private double prevY; //TODO: add insta-drop particle effect
	private double instaTimer;
	
	private int[][] cachedlocs;
	@Override
	public void render(Graphics2D g) {
		if (disabled || PuyoBoardManager.isPaused()) { return; }
		
		if (cachedlocs == null) {
			cachedlocs = finalDropLocations();
		}
		
		g.setClip((int) board.getX()-3, (int) board.getY()+4, board.getWidth()*16, (board.getHeight()-2)*16);
		
		g.drawImage(mainType.getSprite(PuyoSprites.DROPPER).getImage(), (int) (board.getX()-4 + (cachedlocs[0][0]*16)), (int) (board.getY()+3 + ((cachedlocs[0][1]-2)*16)), null);
		g.drawImage(otherType.getSprite(PuyoSprites.DROPPER).getImage(), (int) (board.getX()-4 + (cachedlocs[1][0]*16)), (int) (board.getY()+3 + ((cachedlocs[1][1]-2)*16)), null);
		
		if (mainType != otherType || !smooth) {
			int[][] offsets = shapeOffset[rotation];
			VolatileImage mainImg = mainType.getSprite(PuyoSprites.BASE).getImage();
			if (flashTimer > 0.25) {
				mainImg = mainType.getSprite(PuyoSprites.HIGHLIGHTED).getImage();
			}
			
			int xfac = (int) ((prevX-puyoX)*16*(moveDelay/0.045));
			double rotfac = Math.max(0, Math.min(1, 1-(rotTimer/0.1)));
			
			g.drawImage(mainImg, (int) (board.getX()-4 + (offsets[0][0] + puyoX)*16) + xfac, (int) (board.getY()-4 + ((offsets[0][1] + puyoY-2)*16)), null);
			g.drawImage(otherType.getSprite(PuyoSprites.BASE).getImage(), 
					(int)((board.getX()-4 + puyoX*16 + xfac)
							+(offsets[1][0]*rotfac)*16
							+(getOffset(rotation-(rightrot ? 1 : -1))[1][0]*(1-rotfac))*16),
					
					(int)((board.getY()-4 + (puyoY-2)*16)
							+(offsets[1][1]*rotfac)*16
							+(getOffset(rotation-(rightrot ? 1 : -1))[1][1]*(1-rotfac))*16), 
							
					null);
		} else {
			g.drawImage(mainType.getSprite(PuyoSprites.LONG).getImage(), rotateImg(), null);
		}
		g.setClip(null);
	}
	
	private AffineTransform tx;
	private int prevrot = rotation;
	private AffineTransform rotateImg() {
		if (tx != null && !update && rotTimer == 0 && prevX == puyoX) {
			double amt = (puyoY-prevY)*16d;
			double xamt = 0, yamt = 0;
			
			switch(rotation) {
			case 0: {
				yamt = amt;
				break;
			}
			case 1: {
				xamt = amt;
				break;
			}
			case 2: {
				yamt = -amt;
				break;
			}
			case 3: {
				xamt = -amt;
				break;
			}
			}
			
			tx.translate(xamt, yamt);
			return tx;
		}
		
		tx = new AffineTransform();
	    
	    if (rotTimer < 0) {
	    	prevrot = rotation;
	    	rotTimer = 0;
	    }
	    int xfac = (int) ((prevX-puyoX)*16*(moveDelay/0.045));
		
		tx.translate(8, 24);
	    
		double amt = rotation;
		if (PuyoGameMain.quality == RenderQuality.HIGH) {
			amt = rotation + ((prevrot+(prevrot+3 == rotation ? 4 : 0)-(rotation+(prevrot == rotation+3 ? 4 : 0)))*(rotTimer/0.1d));
		}
	    tx.translate((board.getX()-4 + (puyoX*16)) + xfac, (board.getY()-2-3 + (((puyoY-1)-2)*16)));
	    
	    tx.rotate(Math.toRadians(amt*90d));
	    tx.translate(-8, -24);
	    
	    return tx;
	}
	
	private boolean checkForMovement(double delta) {
		boolean update = false;
		boolean checkForInsta = true;
		boolean checkForQuick = true;
		if (board.getControls() == null) { return false; }
		boolean left = board.getControls().isMovingLeftDown();
		boolean right = board.getControls().isMovingRightDown();
		if (!(left && right)) {
			if (left) {
				if (moveHorizontal(-1)) {
					checkForQuick = false;
					update = true;
				}
				checkForInsta = false;
			}
			if (right) {
				if (moveHorizontal(1)) {
					checkForQuick = false;
					update = true;
				}
				checkForInsta = false;
			}
		}
		if (board.getControls().isFastDropDown() && checkForQuick) {
			for (int i = 0; i < 5; i++) 
				moveVertical(0.1);
			board.addScore(1);
			checkForInsta = false;
		}
		if (board.getControls().isInstaDropDown() && checkForInsta && board.getBoardManager().getInstaDrop()) {
			instaDrop();
		}
		return update;
	}
	
	private boolean rotleftdown, rotrightdown;
	private boolean checkForRotation() {
		if (board.getControls() == null) { return false; }
		boolean returnv = false;
		
		boolean rotright = board.getControls().isTurnRightDown();
		boolean rotleft = board.getControls().isTurnLeftDown();
		
		if ((rotright || rotleft) && !((rotright && rotleft) && !(rotleftdown && rotrightdown))) {
			if (rotright && !rotrightdown) {
				returnv = turn(-1);
				
				if (!returnv && board.getBoardManager().getVerticalFlip()) {
					if (doublerotTimer > 0) {
						returnv = turn(-2);
						doublerotTimer = 0;
					} else {
						doublerotTimer = 0.5;
					}
				}
			}
			if (rotleft && !rotleftdown) {
				returnv = turn(1);
				
				if (!returnv && board.getBoardManager().getVerticalFlip()) {
					if (doublerotTimer > 0) {
						returnv = turn(2);
						doublerotTimer = 0;
					} else {
						doublerotTimer = 0.5;
					}
				}
			}
			
		}
		rotrightdown = rotright;
		rotleftdown = rotleft;
		return returnv;
	}
	
	private void checkForLanding(double delta) {

		if (Math.floor(puyoY) > lowestY) {
			lowestY = (int) Math.floor(puyoY);
			landTimer = (board.getBoardManager().getDropSpeed()*2)/60d;
		}
		if (intersectsBoard(puyoX, puyoY+(smooth ? 0.10 : 0.5), rotation)) {
			landTimer-=delta;
			if (landTimer <= 0 || (board.getControls() != null && board.getControls().isFastDropDown())) {
				dropOnBoard();
			}
		}
		if (!smooth) {
			dropTimer-=delta;
			if (dropTimer <= 0) {
				dropTimer = (board.getBoardManager().getDropSpeed()/45d);
				moveVertical(0.5);
			}
		} else {
			moveVertical((0.5/(board.getBoardManager().getDropSpeed()/45d))*delta);
		}
	}
	
	private void instaDrop() {
		prevY = puyoY;
		while (moveVertical(0.5)) { board.addScore(1); }
		dropOnBoard();
	}
	
	private boolean turn(int amount) {
		int newRot = rotation + amount;
		while (newRot < 0 ) { newRot += 4; }
		newRot %= 4;
		if (!intersectsBoard(puyoX, puyoY, newRot)) {
			if (amount > 0) {
				rightrot = true;
			} else {
				rightrot = false;
			}
			prevrot = rotation;
			rotation = newRot;
			rotTimer = 0.1;
			GameSounds.ROTATE.play();
			return true;
		} else {
			int newX = puyoX - shapeOffset[newRot][1][0];
			double newY = puyoY - shapeOffset[newRot][1][1];
			
			if (!intersectsBoard(newX, newY, newRot)) {
				if (amount > 0) {
					rightrot = true;
				} else {
					rightrot = false;
				}
				int adj = puyoX + shapeOffset[newRot][1][0];
				if (adj >= 0 && adj < board.getWidth()) {
					board.squishRow(adj, 0.03, 4);
				}
				prevrot = rotation;
				puyoX = newX;
				puyoY = newY;
				rotation = newRot;
				rotTimer = 0.1;
				GameSounds.ROTATE.play();
				return true;
			}	
			
			newY = Math.ceil(newY - 1)+0.5;
			
			if (!intersectsBoard(newX, newY, newRot)) {
				if (amount > 0) {
					rightrot = true;
				} else {
					rightrot = false;
				}
				int adj = puyoX + shapeOffset[newRot][1][0];
				if (adj >= 0 && adj < board.getWidth()) {
					board.squishRow(adj, 0.03, 4);
				}
				prevrot = rotation;
				puyoX = newX;
				puyoY = newY;
				rotation = newRot;
				rotTimer = 0.1;
				GameSounds.ROTATE.play();
				return true;
			}	
		}
		return false;
	}
	
	private boolean moveVertical(double amount) {
		if (!intersectsBoard(puyoX, puyoY - (smooth ? 0 : 0.5) + amount, rotation)) {
			puyoY += amount;
			return true;
		}
		return false;
	}
	
	private boolean moveHorizontal(int amount) {
		if (!intersectsBoard(puyoX + amount, puyoY, rotation)) {
			prevX = puyoX;
			puyoX += amount;
			moveDelay = 0.040;
			GameSounds.MOVE.play();
			return true;
		}
		return false;
	}
	
	private void dropOnBoard() {
		int[][] locs = dropLocations();
		board.setPuyoAt(locs[0][0], (int) (locs[0][1]), mainType);
		board.setPuyoAt(locs[1][0], (int) (locs[1][1]), otherType);
		board.onPuyoLand(false);
		PuyoGameMain.getGameEngine().removeGameObject(this);
		GameSounds.DROP.play();
	}
	

	
	private boolean intersectsBoard(int xpos, double ypos, int rotation) {
		ypos+=0.5;
		while (rotation < 0 ) { rotation += 4; }
		rotation %= 4;
		for (int[] offset : shapeOffset[rotation]) {
			int tempx = offset[0] + xpos;
			int tempy = (int) (offset[1] + ypos);
			if (tempy < 0) {
				continue;
			}
			if (!board.isInBounds(tempx, tempy) || board.getPuyoAt(tempx, tempy).type != null) {
				return true;
			}
		}
		return false;
	}
	
	private int getTopPuyo(int x) {
		for (int i = board.getHeight()-1; i >= 0; i--) {
			if (board.getPuyoAt(x, i).type == null) return i;
		}
		return 0;
	}
	
	public int[][] finalDropLocations() {
		int[][] droplocs = new int[2][2];
		int[][] offsets = shapeOffset[rotation];
		
		droplocs[0][0] = offsets[0][0]+puyoX;
		droplocs[1][0] = offsets[1][0]+puyoX;
		
		droplocs[0][1] = getTopPuyo(droplocs[0][0]);
		droplocs[1][1] = getTopPuyo(droplocs[1][0]);
		
		if (droplocs[0][0] == droplocs[1][0]) {
			droplocs[1][1] += offsets[1][1];
			while (intersectsBoard(droplocs[0][0], Math.max(droplocs[0][1], droplocs[1][1])-1, rotation)) {
				droplocs[0][1] -= 1;
				droplocs[1][1] -= 1;
			}
		}
		
		return droplocs;
	}
	
	public int[][] dropLocations() {
		int[][] droplocs = new int[2][2];
		int[][] offsets = shapeOffset[rotation];
		
		droplocs[0][0] = offsets[0][0]+puyoX;
		droplocs[1][0] = offsets[1][0]+puyoX;
		
		droplocs[0][1] = droplocs[0][1]+(int)puyoY;
		droplocs[1][1] = droplocs[1][1]+(int)puyoY;
		
		if (droplocs[0][0] == droplocs[1][0]) {
			droplocs[1][1] += offsets[1][1];
			while (intersectsBoard(droplocs[0][0], Math.max(droplocs[0][1], droplocs[1][1])-1, rotation)) {
				droplocs[0][1] -= 1;
				droplocs[1][1] -= 1;
			}
		}
		
		return droplocs;
	}
	
	//--getters/setters--//
	public void disable() { disabled = true; }
	public void enable() { 
		disabled = false; 
	}

	public boolean isDisabled() {
		return disabled;
	}

	public PuyoType getBasePuyo() {
		return mainType;
	}

	public PuyoType getOtherPuyo() {
		return otherType;
	}
	
	public int getPuyoX() { return puyoX; }
	public double getPuyoY() { return puyoY; }

	public int getRotation() { return rotation; }
	public int[][] getOffset(int rot) {
		while (rot < 0) {
			rot+=4;
		}
		rot%=4;
		return shapeOffset[rot];
	}
	public void delete() {
		deleted = true;
	}
}
