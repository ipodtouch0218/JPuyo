package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;
import java.util.ArrayList;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPartyBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;

public class ObjNextDisplay extends GameObject {

	private static final SpriteSheet sheet = new SpriteSheet("ui/board/next-box.png", 35, 68);
	private static final int[] xpos = {4, 22};
	private static final int[] xposFlip = {15, 5};
	
	private ObjPuyoBoard board;
	
	public ObjNextDisplay(ObjPuyoBoard board) {
		this.board = board;
	}
	
	public void render(Graphics2D g) {
		GameSprite renderSprite = sheet.getSprite((board.isFlipped() ? 1 : 0) + (board.getBoardManager().isNextDouble() ? 2 : 0), 0);
		int x = (int) (super.x + (board.isFlipped() ? -2 : 2));
		int y = (int) super.y;
		
		g.drawImage(renderSprite.getImage(), x, y, null);
		
		if (PuyoBoardManager.isPaused()) { return; }
		
		ArrayList<PuyoType> order = board.getBoardManager().getPuyoOrder();
		int pos = board.puyoOrderPos;
		
		if (board.puyoOrderPos >= order.size()) {
			board.getBoardManager().getNextPuyo(board);
			board.puyoOrderPos--;
			pos = board.puyoOrderPos;
		}
		int[] xarr = (board.isFlipped() ? xposFlip : xpos);
		
		if (board instanceof ObjPartyBoard) {
			ObjPartyBoard pb = (ObjPartyBoard) board;
			int currentpos = 0;
			
			if (pb.getSingleColorCounter() >= 1) {
				g.drawImage(order.get(pos+currentpos).getSprite(PuyoType.PuyoSprites.LONG).getImage(), x+xarr[0], y+12, null);
				currentpos++;
			} else {
				PuyoType type1 = order.get(pos+currentpos+1);
				PuyoType type2 = order.get(pos+currentpos);
				if (type1 == type2) {
					g.drawImage(type1.getSprite(PuyoType.PuyoSprites.LONG).getImage(), x+xarr[0], y+12, null);
				} else {
					g.drawImage(order.get(pos+currentpos).getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[0], y+28, null);
					g.drawImage(order.get(pos+currentpos+1).getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[0], y+12, null);
				}
				currentpos+=2;
			}
			
			if (pb.getSingleColorCounter() >= 2) {
				g.drawImage(order.get(pos+currentpos).getSprite(PuyoType.PuyoSprites.LONG).getImage(), x+xarr[1], y+46, 8, 16, null);
			} else {
				PuyoType type1 = order.get(pos+currentpos+1);
				PuyoType type2 = order.get(pos+currentpos);
				if (type1 == type2) {
					g.drawImage(type1.getSprite(PuyoType.PuyoSprites.LONG).getImage(), x+xarr[1], y+46, 8, 16, null);
				} else {
					g.drawImage(order.get(pos+currentpos).getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[1], y+55, 8, 8, null);
					g.drawImage(order.get(pos+currentpos+1).getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[1], y+45, 8, 8, null);
				}
			}
		} else {
			PuyoType type1 = order.get(pos+1);
			PuyoType type2 = order.get(pos);
			if (type1 == type2 && board.getBoardManager().getGamemode().getGamemodeSettings().smoothDropper) {
				g.drawImage(type1.getSprite(PuyoType.PuyoSprites.LONG).getImage(), x+xarr[0], y+12, null);
			} else {
				g.drawImage(type1.getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[0], y+12, null);
				g.drawImage(type2.getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[0], y+28, null);
			}
			if (board.getBoardManager().isNextDouble()) {
				PuyoType type3 = order.get(pos+3);
				PuyoType type4 = order.get(pos+2);
				if (type3 == type4 && board.getBoardManager().getGamemode().getGamemodeSettings().smoothDropper) {
					g.drawImage(type3.getSprite(PuyoType.PuyoSprites.LONG).getImage(), x+xarr[1], y+46, 8, 16, null);
				} else {
					g.drawImage(type3.getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[1], y+45, 8, 8, null);
					g.drawImage(type4.getSprite(PuyoType.PuyoSprites.BASE).getImage(), x+xarr[1], y+55, 8, 8, null);
				}
			}
		}
	}
}
