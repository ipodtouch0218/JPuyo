package me.ipodtouch0218.multiplayerpuyo;

import java.awt.Color;
import java.util.HashMap;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.multiplayerpuyo.misc.LoadingListener;

public enum PuyoType {

	RED('R', new Color(234,51,51)),
	YELLOW('Y', new Color(255,216,0)), 
	GREEN('G', new Color(20,150,9)), 
	BLUE('B', new Color(27,0,206)), 
	PURPLE('P', new Color(130,23,165)), 
	GARBAGE('X', new Color(158,158,158));
	
	private char chainSymbol;
	private HashMap<PuyoSprites, GameSprite> sprites = new HashMap<>();
	private SpriteSheet sheet = null;
	private Color color;
	
	PuyoType(char symbol, Color color) {
		chainSymbol = symbol;
		this.color = color;
	}
	
	public void loadSprites(LoadingListener listener) {
		if (sheet == null) {
			sheet = new SpriteSheet("puyo/" + name().toLowerCase() + ".png", 16, 16);
		}
		for (PuyoSprites spr : PuyoSprites.values()) {
			GameSprite sprite = null;
			if (spr.getEndX() != -1) {
				sprite = sheet.getSpriteRange(spr.getStartX(), spr.getStartY(), spr.getEndX(), spr.getEndY());
			} else {
				sprite = sheet.getSprite(spr.getStartX(), spr.getStartY());
			}
			sprites.put(spr, sprite);
			listener.loaded(sprite.getFileName());
		}
		sheet.close();
	}
	public GameSprite getSprite(PuyoSprites type) { return sprites.get(type); }
	public char getChainSymbol() { return chainSymbol; }
	public Color getColor() { return color; }
	
	public enum PuyoSprites {
		BASE(0,0),RIGHT(1,0),LEFT(2,0),HORIZONTAL_LINE(3,0),
		DOWN(0,1),RIGHT_DOWN_CORNER(1,1),LEFT_DOWN_CORNER(2,1),HORIZONTAL_LINE_DOWN(3,1),
		UP(0,2),RIGHT_UP_CORNER(1,2),LEFT_UP_CORNER(2,2),HORIZONTAL_LINE_UP(3,2),
		VERTICAL_LINE(0,3),VERTICAL_LINE_RIGHT(1,3),VERTICAL_LINE_LEFT(2,3),CROSS(3,3),
		DROPPER(0,4),/*SQUISHED("squished"),*/POP_PARTICLE(2,4),/*SMALL("small"),*/
		POPPING(0,5),HIGHLIGHTED(1,5),BIG(2,5,3,6),
		LONG(0,6,0,7),
		ELLE(1,7,2,8), SWIRL(3,7,3,8);
		
		private int startX,startY,endX=-1,endY=-1;
		
		PuyoSprites(int x, int y) {
			startX = x;
			startY = y;
		}
		PuyoSprites(int stX, int stY, int enX, int enY) {
			startX = stX;
			startY = stY;
			endX = enX;
			endY = enY;
		}
		public int getStartX() { return startX; }
		public int getStartY() { return startY; }
		public int getEndX() { return endX; }
		public int getEndY() { return endY; }
	}
	
	public static PuyoType getPuyoTypeFromSymbol(char symbol) {
		if (symbol == '-') { return null; }
		for (PuyoType types : PuyoType.values()) {
			if (symbol == types.getChainSymbol()) { return types; }
		}
		return null;
	}
}
