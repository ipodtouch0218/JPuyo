package me.ipodtouch0218.multiplayerpuyo.objects.particle.senders;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.misc.GarbageSenderStatus;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator.GarbageSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjSPGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPartyBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoFeverBoard;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ParticleGarbageSender extends ParticleSender {

	public static final GameSounds[] soundOrder = {GameSounds.GARBAGE_SEND_0, GameSounds.GARBAGE_SEND_1, GameSounds.GARBAGE_SEND_2, GameSounds.GARBAGE_SEND_3, GameSounds.GARBAGE_SEND_4};
	
	private PuyoType color;
	private GarbageSenderStatus sender;
	private ObjPuyoBoard boardSender;
	private Object recipient;
	private int amount;
	private int consecutive;
	private boolean red;
	
	public ParticleGarbageSender(ObjPuyoBoard recipient, GarbageSenderStatus sender, int amount, int consecutive, PuyoType color, boolean red) {
		super((int) (recipient.getX()+52), (int) (recipient.getY()-16), (red ? GarbageSprites.RED.getSprite() : GarbageSprites.LARGE.getSprite()), color.getColor(), 0.44, 7);
		this.recipient = recipient;
		this.amount = amount;
		this.consecutive = consecutive;
		this.sender = sender;
		this.red = red;
		this.color = color;
		this.boardSender = sender.getBoard();
	}
	public ParticleGarbageSender(ObjGarbageIndicator recipient, GarbageSenderStatus sender, int amount, int consecutive, PuyoType color, boolean red) {
		super((int) (recipient.getX()+48), (int) (recipient.getY()+6), (red ? GarbageSprites.RED.getSprite() : GarbageSprites.LARGE.getSprite()), color.getColor(), 0.44, 7);
		this.recipient = recipient;
		this.amount = amount;
		this.consecutive = consecutive;
		this.sender = sender;
		this.color = color;
		this.red = red;
		this.boardSender = sender.getBoard();
	}
	public ParticleGarbageSender(ObjPuyoBoard recipient, ObjPuyoBoard sender, int amount, int consecutive, PuyoType color, boolean red) {
		super((int) (recipient.getX()+52), (int) (recipient.getY()-16), (red ? GarbageSprites.RED.getSprite() : GarbageSprites.LARGE.getSprite()), color.getColor(), 0.44, 7);
		this.recipient = recipient;
		this.amount = amount;
		this.consecutive = consecutive;
		this.boardSender = sender;
		this.color = color;
		this.red = red;
	}
	
	public void arrived() {
		if (recipient instanceof ObjPuyoBoard) {
			ObjPuyoBoard board = (ObjPuyoBoard) recipient;
			if (board instanceof ObjPuyoFeverBoard) {
				ObjPuyoFeverBoard fBoard = (ObjPuyoFeverBoard) recipient;
				if (fBoard.getFeverManager().isInFever()) {
					fBoard.getFeverIndicator().addGarbage(amount);
				} else {
					board.getGarbageIndicator().addGarbage(amount);
				}
			} else if (board instanceof ObjPartyBoard) {
				if (((ObjPartyBoard) board).getGarbageDeflector()) {
					GameEngine.addGameObject(new ParticleGarbageSender(boardSender, (ObjPuyoBoard) recipient, amount, consecutive, color, red), x, y);
					GameSounds.PARTY_ITEM_SHIELD_HIT.play();
					return;
				}
				board.getGarbageIndicator().addGarbage(amount);
			} else {
				board.getGarbageIndicator().addGarbage(amount);
			}
			if (sender != null) {
				board.addDroppableGarbage(sender.getBoard(), amount);
			} else {
				board.addDroppableGarbage(null, amount);
			}
			board.garbageShake();
		} else if (recipient instanceof ObjGarbageIndicator) {
			if (recipient instanceof ObjSPGarbageIndicator) {
				if (consecutive == 1) {
					((ObjSPGarbageIndicator) recipient).clear();
				}
			}
			((ObjGarbageIndicator) recipient).addGarbage(amount);
		}

		if (sender != null) {
			sender.checkForFinished();
		}
		soundOrder[Math.min(soundOrder.length-1, consecutive-1)].play();
	}
}
