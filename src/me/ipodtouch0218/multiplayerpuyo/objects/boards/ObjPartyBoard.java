package me.ipodtouch0218.multiplayerpuyo.objects.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.VolatileImage;

import me.ipodtouch0218.java2dengine.display.GameRenderer;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.manager.Controls;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.misc.PartyItem;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoInfo;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ObjPartyBoard extends ObjPuyoBoard {

	private static final GameSprite deflector = new GameSprite("ui/board/deflector.png", false);
	
	public ObjPartyBoard(int width, int height, Controls controls, PuyoBoardManager bm, int playerNumb) {
		super(width, height, controls, bm, playerNumb);
		gameOverSpots = new int[][]{{2,2},{3,2}};
	}

	//party items
	private boolean norotation;
	private boolean searchlight;
	private double attackpower = 1;
	private boolean garbagedeflector;
	private double customdropspeed = -1;
	private int singlecolorcounter;
	private boolean freeze;
	//
	private double searchlightTimer;
	private double rotTimer;
	//
	
	
	@Override
	public void tick(double delta) {
		if (searchlight) {
			searchlightTimer += delta;
		}
		super.tick(delta);
		
		if (PuyoBoardManager.isPaused()) { return; }
		
		if (rotTimer > 0) {
			rotTimer -= delta;
			double time = 3-rotTimer;
			rot = (-0.7 * Math.exp((-1.84 * time) + 7.343))+1084.4;
			
			if (rotTimer <= 0) {
				rot = 0;
				
				CharacterSounds.PARTY_KO.getSound(getCharacter(), isCharacterAlt()).play();
				setDroppableGarbage(0);
				getGarbageIndicator().setOverallGarbage(0);
				dropFirstItem();
			}
		}
	}
	
	private VolatileImage overlay;
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		if (searchlight) {
			if (overlay == null || overlay.validate(GameRenderer.gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
				overlay = GameRenderer.createVolatile(getWidth()*16, (getHeight()-2)*16, true);
				GameRenderer.clearImage(overlay);
			}
			Graphics2D gr = overlay.createGraphics();
			gr.setComposite(AlphaComposite.SrcOver);
			gr.setColor(Color.BLACK);
			gr.fillRect(0, 0, overlay.getWidth(), overlay.getHeight());
			gr.setComposite(AlphaComposite.Clear);
			
			double modtimer = searchlightTimer%3.2;
			int timerx = (int) ((modtimer>1.6 ? (3.2-modtimer)/1.6 : modtimer/1.6)*((getWidth()*16)+(80)));
			
			gr.fillPolygon(new int[]{getWidth()*16/2,timerx-15, timerx-65}, new int[]{-70,(getHeight()-2)*16,(getHeight()-2)*16}, 3);

			if (getDropper() != null) {
				getDropper().cullSpotlight(gr);
			}
				
			g.drawImage(overlay, (int) x-3, (int) y+3, null);
		}
		if (garbagedeflector) {
			for (int i = 0; i < 5; i++) {
				g.drawImage(deflector.getImage(), (int) (getX()+i*20)-5, (int) (getY()-28), null);
			}
		}
	}
	
	@Override
	public void gameOver() {
		setReadyForPuyo(false);
		clearBoard(true);
		rotbottom = false;
//		rotv = 160;
		rotTimer = 3;
		GameSounds.PARTY_BOARDKO.play();
		addScore(-1000);
		for (ObjPuyoBoard boards : boardManager.getBoards()) {
			if (boards == this) { continue; }
			boards.addScore(1000);
		}
		
		freeze = false;
		searchlight = false;
		customdropspeed = -1;
		attackpower = 1;
		norotation = false;
	}
	
	public void dropFirstItem() {
		PuyoInfo inf = getPuyoAt(0, 0);
		inf.type = PuyoType.GARBAGE;
		inf.partyItem = PartyItem.getRandomItem(getScorePosition() == 1, getCharacter().getPartyItemSet());
//		inf.partyItem = PartyItem.GARBAGE_DEFLECTOR;
		onPuyoLand(false);
	}
	
	@Override
	public double getPanic() {
		return 0;
	}
	
	//---setters---//
	public void setNoRotation(boolean value) { norotation = value; }
	public void setSearchlight(boolean value) { searchlight = value; searchlightTimer = 0; }
	public void setAttackPower(double value) { attackpower = value; }
	public void setGarbageDeflector(boolean value) { garbagedeflector = value; }
	public void setCustomDropSpeed(double value) { customdropspeed = value; }
	public void addSingleColor(int value) { singlecolorcounter += value; }
	public void setFreeze(boolean value) { freeze = value; }
	
	//---getters---//
	public boolean getNoRotation() { return norotation; }
	public boolean getSearchlight() { return searchlight; }
	public double getAttackPower() { return attackpower; }
	public boolean getGarbageDeflector() { return garbagedeflector; }
	public double getCustomDropSpeed() { return customdropspeed; }
	public boolean getSingleColorPuyo() { return singlecolorcounter > 0; }
	public int getSingleColorCounter() { return singlecolorcounter; }
	public boolean willFreeze() { return freeze; }
	
	public int getScorePosition() {
		int pos = 1;
		for (ObjPuyoBoard other : boardManager.getBoards()) {
			if (other.getScore() > getScore()) { pos++; }
		}
		return pos;
	}
}
