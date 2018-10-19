package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;
import java.awt.Image;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteAnimation;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.manager.Controls;
import me.ipodtouch0218.multiplayerpuyo.manager.FeverManager;
import me.ipodtouch0218.multiplayerpuyo.manager.LandingFeverManager;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;

public class ObjPuyoFeverBoard extends ObjPuyoBoard {

	private static final SpriteSheet feverMeterAnim = new SpriteSheet("ui/board/fever_meter_full.png", 16, 40);
	private static GameSprite[] FEVER_METER = new GameSprite[8];
	private static SpriteAnimation anim = null;
	{ 
		SpriteSheet feverMeter = new SpriteSheet("ui/board/fever_meter_sheet.png", 16, 40);
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
	
	private FeverManager feverManager = PuyoGameMain.getGameEngine().addGameObject(new FeverManager(this));
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
		feverGarbage = PuyoGameMain.getGameEngine().addGameObject(new ObjGarbageIndicator(((double) playerNumb/(double) boardManager.getPlayers()) >= 0.5), x, y);
	}
	
	@Override
	public void onPuyoLand(boolean instaDrop) {
		midchain = true;
		flashing.clear();
		if (feverManager.isInFever()) { 
			new Thread(new LandingFeverManager(feverManager, this, feverGarbage)).start();
		} else {
			super.onPuyoLand(instaDrop);
		}
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
		if (flipTimer <= 0) {
			flipTimer = 0;
			if (dropper != null && this instanceof ObjPuyoFeverBoard && !((ObjPuyoFeverBoard) this).getFeverManager().isInFever()) {
				dropper.enable();
			}
		}
		if (flipTimer != 0 && flipTimer < 0.5 && flipTimer+(delta*3d) >= 0.5) {
			if (!((ObjPuyoFeverBoard) this).getFeverManager().isInFever()) {
				((ObjPuyoFeverBoard) this).getFeverManager().startFever();
			} else {
				((ObjPuyoFeverBoard) this).getFeverManager().endFeverFully();
			}
		}
	}
	

	@Override
	public void render(Graphics2D g) {
		super.render(g);
		if (feverManager.getCharge() < 7) {
			g.drawImage(FEVER_METER[feverManager.getCharge()].getImage().getScaledInstance(32, 80, Image.SCALE_FAST), (int) x+(getWidth()*16)+8, (int) y+((getHeight()-2)*16)-100, null);
		} else {
			g.drawImage(anim.getCurrentFrame().getImage().getScaledInstance(32, 80, Image.SCALE_FAST), (int) x+(getWidth()*16)+8, (int) y+((getHeight()-2)*16)-100, null);
		}
		g.setFont(PuyoGameMain.puyofont);
		if (feverManager.getTimeRemaining() != -1) {
			g.drawString(feverManager.getTimeRemaining() + "", (int) x+(getWidth()*16)+5, (int) y+((getHeight()-2)*16)-60);
		}
	}
	
	@Override
	public void onRemove() {
		super.onRemove();
		PuyoGameMain.getGameEngine().removeGameObject(feverGarbage);
		PuyoGameMain.getGameEngine().removeGameObject(feverManager);
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
