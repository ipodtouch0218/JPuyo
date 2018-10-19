package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.manager.Controls;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoInfo;

public class ObjPuyoIceBoard extends ObjPuyoBoard {

	public static final GameSprite iceoverlay = new GameSprite("puyo/ice.png");
	public static final GameSprite icebroken = new GameSprite("puyo/icebroken.png");
	
	public ObjPuyoIceBoard(int width, int height, Controls controls, PuyoBoardManager bm, int playerNumb) {
		super(width, height, controls, bm, playerNumb);
		gameOverSpots = new int[][]{{2,2}};
	}
	
	
	@Override
	protected void renderPuyo(Graphics2D img, PuyoInfo type, PuyoSprites shape, int ycoordinate, boolean flash) {
		super.renderPuyo(img,type,shape,ycoordinate,flash);
		if (type.ice) {
			img.setComposite(AlphaComposite.SrcOver);
			if (type.icetimer == 2) {
				img.drawImage(iceoverlay.getImage(), 0, ycoordinate, null);
			} else {
				img.drawImage(icebroken.getImage(), 0, ycoordinate, null);
			}
		}
	}
}
