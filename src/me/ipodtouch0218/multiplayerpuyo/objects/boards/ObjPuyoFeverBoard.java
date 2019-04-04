package me.ipodtouch0218.multiplayerpuyo.objects.boards;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteAnimation;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.manager.Controls;
import me.ipodtouch0218.multiplayerpuyo.manager.FeverManager;
import me.ipodtouch0218.multiplayerpuyo.manager.LandingFeverManager;
import me.ipodtouch0218.multiplayerpuyo.manager.LandingPuyoManager;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjDropper;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;

public class ObjPuyoFeverBoard extends ObjPuyoBoard {

	private static final SpriteSheet feverMeterAnim = new SpriteSheet("ui/board/fever_meter_full.png", 32, 80);
	private static GameSprite[] FEVER_METER = new GameSprite[8];
	private static SpriteAnimation anim = null;
	{ 
		SpriteSheet feverMeter = new SpriteSheet("ui/board/fever_meter_sheet.png", 32, 80);
		for (int i = 0; i<8; i++) {
			FEVER_METER[i] = feverMeter.getSprite(i, 0);
		}
		GameSprite[] frames = new GameSprite[7];
		for (int i = 0; i<7; i++) {
			frames[i] = feverMeterAnim.getSprite(i, 0);
		}
		anim = new SpriteAnimation(frames, 12);
		anim.start(true);
	}
	
	private FeverManager feverManager = GameEngine.addGameObject(new FeverManager(this));
	private ObjGarbageIndicator feverGarbage;
	
	private double flipTimer;
	
	public ObjPuyoFeverBoard(int width, int height, Controls controls, PuyoBoardManager bm, int playerNumb) {
		super(width, height, controls, bm, playerNumb);
		gameOverSpots = new int[][]{{2,2},{3,2}};
	}

	public ObjGarbageIndicator getFeverIndicator() { return feverGarbage; }
	public FeverManager getFeverManager() { return feverManager; }
	
	@Override
	public void createObjects() {
		super.createObjects();
		feverGarbage = GameEngine.addGameObject(new ObjGarbageIndicator(((double) playerNumb/(double) boardManager.getPlayers()) >= 0.5), x, y);
	}
	
	@Override
	public void onPuyoLand(boolean instaDrop) {
		flashing.clear();
		if (feverManager.isInFever()) { 
			landThread = new LandingFeverManager(this, feverGarbage);
		} else {
			landThread = new LandingPuyoManager(this);
		}
		dropper.disable();
		midchain = true;
		PuyoGameMain.getThreadPool().execute(landThread);
	}
	
	@Override
	public void tick(double delta) {
		super.tick(delta);
		
		manageFeverTimer(delta);
	}
	
	//---tick methods---//
	private void manageFeverTimer(double delta) {
		ObjDropper dropper = getDropper();
		if (flipTimer > 0) {
			flipTimer-=(delta*3d);
			if (dropper != null && !dropper.isDisabled()) {
				dropper.disable();
			}
		}
		if (flipTimer < 0) {
			flipTimer = 0;
			if (feverManager.isInFever()) {
				if (dropper == null) {
					setReadyForPuyo(true);
				} else {
					dropper.enable();
				}
				
			}
		}
		if (flipTimer != 0 && flipTimer < 0.5 && flipTimer+(delta*3d) >= 0.5) {
			if (!feverManager.isInFever()) {
				feverManager.startFever();
			} else {
				feverManager.endFeverFully();
			}
		}
	}
	

	private AffineTransform flip;
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		if (x == 0 || y == 0) { return; }
		if (flip == null) {
			if (isFlipped()) {
				flip = AffineTransform.getScaleInstance(-1, 1);
				flip.translate(-(x-14), y+((getHeight()-2)*16)-100);
			} else {
				flip = new AffineTransform();
				flip.translate((int) x+(getWidth()*16)+8, (int) y+((getHeight()-2)*16)-100);
			}
		}
		
		if (feverManager.getCharge() < 7) {
			g.drawImage(FEVER_METER[feverManager.getCharge()].getImage(), flip, null);
		} else {
			g.drawImage(anim.getCurrentFrame().getImage(), flip, null);
		}
		if (feverManager.getTimeRemaining() != -1) {
			g.setFont(PuyoGameMain.puyofont);
			g.drawString(feverManager.getTimeRemaining() + "", (int) flip.getTranslateX()-(isFlipped() ? 6 : 3), (int) y+((getHeight()-2)*16)-60);
		}
	}
	
	@Override
	public void onRemove() {
		super.onRemove();
		GameEngine.removeGameObject(feverGarbage);
		GameEngine.removeGameObject(feverManager);
	}
	
	@Override
	public void reset() {
		super.reset();
		feverManager.reset();
	}
	
	//---methods---//
	public void feverFlip() {
		ObjDropper dropper = getDropper();
		flipTimer = 1;
		if (dropper != null) {
			dropper.disable();
		}
	}
	
	//---setters---//
	
	public void setFeverBackgroundColor(FeverBackground newCol) { backgroundColor = newCol; }
	public void setFeverInChain(boolean value) { backgroundInChain = value; }

	//---getters---//
	public double getFlipTimer() { return flipTimer; }
}
