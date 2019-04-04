package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPartyBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ObjDropper extends GameObject {

	private static final GameSprite noRotate = new GameSprite("ui/board/norotate.png", false);
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
		this(board);
		resetDropper(main, other);
	}
	public ObjDropper(ObjPuyoBoard board) {
		this.board = board;
		this.smooth = (board.getBoardManager().getGamemode().getGamemodeSettings().smoothDropper);
	}
	
	private boolean deleted;
	private boolean update;
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		if (deleted) {
			return;
		}
		if (update) {
			cachedlocs = finalDropLocations();
			board.checkForFlashing(cachedlocs);
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
			
			boolean rotd = checkForRotation();
			if (rotd) {
				update = true;
			}
			
			checkForLanding(delta);
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

		int[][] offsets = shapeOffset[rotation];
		if (mainType != otherType || !smooth) {
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
		if (board instanceof ObjPartyBoard) {
			if (((ObjPartyBoard) board).getNoRotation() && doublerotTimer > 0) {
				g.drawImage(noRotate.getImage(), (int) (board.getX()-4 + (offsets[0][0] + puyoX)*16)-8, (int) (board.getY()-4 + ((offsets[0][1] + puyoY-2)*16))-8, null);
			}
		}
	}
	public void cullSpotlight(Graphics2D gr) {
		AffineTransform old = gr.getTransform();
		gr.rotate(Math.toRadians(rotateAmt));
		
		gr.setComposite(AlphaComposite.Clear);
		gr.fillRoundRect(puyoX*16+xfac, (int) (puyoY*((board.getHeight()-2)*16)), 32, 16, 4, 3);
		//draw shape/image (will be rotated)
		gr.setComposite(AlphaComposite.SrcOver);
		gr.setTransform(old);
	}
	
	private AffineTransform tx;
	private double rotateAmt;
	private int xfac;
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
	    xfac = (int) ((prevX-puyoX)*16*(moveDelay/0.045));
		
		tx.translate(8, 24);
	    
		rotateAmt = rotation;
		if (PuyoGameMain.quality == RenderQuality.HIGH) {
			rotateAmt = rotation + ((prevrot+(prevrot+3 == rotation ? 4 : 0)-(rotation+(prevrot == rotation+3 ? 4 : 0)))*(rotTimer/0.1d));
		}
	    tx.translate((board.getX()-4 + (puyoX*16)) + xfac, (board.getY()-2-3 + (((puyoY-1)-2)*16)));
	    
	    tx.rotate(Math.toRadians(rotateAmt*90d));
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
			int orig = (int) puyoY;
			for (int i = 0; i < 4; i++) {
				moveVertical(12d*delta/0.75/4);
			}
			if ((int) puyoY != orig) {
				board.addScore(1);
			}
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
		
		if (board instanceof ObjPartyBoard) {
			if (((ObjPartyBoard) board).getNoRotation() && ((rotright && !rotrightdown) || (rotleft && !rotleftdown))) {
				rotrightdown = rotright;
				rotleftdown = rotleft;
				GameSounds.PARTY_ITEM_ROTATE_DENY.play();
				doublerotTimer = 0.5;
				return false;
			}
		}
		
		if ((rotright || rotleft) && !((rotright && rotleft) && !(rotleftdown && rotrightdown))) {
			if (rotright && !rotrightdown) {
				returnv = turn(-1);
				
				if (!returnv && board.getBoardManager().getGamemode().getGamemodeSettings().verticalFlip) {
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
				
				if (!returnv && board.getBoardManager().getGamemode().getGamemodeSettings().verticalFlip) {
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
			resetLandTimer();
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
				resetDropTimer();
				moveVertical(0.5);
			}
		} else {
			double speed = board.getBoardManager().getDropSpeed();
			if (board instanceof ObjPartyBoard) {
				if (((ObjPartyBoard) board).getCustomDropSpeed() != -1) {
					speed = ((ObjPartyBoard) board).getCustomDropSpeed();
				}
			}
			moveVertical((0.5/(speed/45d))*delta);
		}
	}
	
	private void instaDrop() {
		prevY = puyoY;
		while (moveVertical(0.5)) { board.addScore(0.5); }
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
					board.squishcolumn(adj, 0.03, 4);
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
					board.squishcolumn(adj, 0.03, 4);
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
		if (!intersectsBoard(puyoX, puyoY + amount, rotation)) {
			puyoY += amount;
			return true;
		} else {
			double prevY = puyoY;
			puyoY = (int) getTopPuyo(puyoX)+1;
			while (intersectsBoard(puyoX, puyoY - (smooth ? 0.01 : 0), rotation)) {
				puyoY -= 0.51;
			}
			return !((puyoY-prevY) <= (smooth ? 0.05 : 0.05));
		}
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
		disable();
		int[][] locs = dropLocations();
		board.setPuyoTypeAt(locs[0][0], (int) (locs[0][1]), mainType);
		board.setPuyoTypeAt(locs[1][0], (int) (locs[1][1]), otherType);
		board.onPuyoLand(false);
//		GameEngine.removeGameObject(this);
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
	
	public void resetDropper(PuyoType main, PuyoType other) {
		this.mainType = main;
		this.otherType = other;
		this.puyoX = 2;
		this.puyoY = 2;
		this.rotation = 0;
		this.lowestY = 0;
		this.disabled = false;
		resetDropTimer();
		resetLandTimer();
		update = true;
	}
	
	private void resetDropTimer() {
		double speed = board.getBoardManager().getDropSpeed();
		if (board instanceof ObjPartyBoard) {
			if (((ObjPartyBoard) board).getCustomDropSpeed() != -1) {
				speed = ((ObjPartyBoard) board).getCustomDropSpeed();
			}
		}
		this.dropTimer = speed/45d;
	}
	private void resetLandTimer() {
		this.landTimer = 1;
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
