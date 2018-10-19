package me.ipodtouch0218.multiplayerpuyo.objects.particle.senders;

import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator.GarbageSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoFeverBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjSPGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ParticleGarbageSender extends ParticleSender {

	public static final GameSounds[] soundOrder = {GameSounds.GARBAGE_SEND_0, GameSounds.GARBAGE_SEND_1, GameSounds.GARBAGE_SEND_2, GameSounds.GARBAGE_SEND_3, GameSounds.GARBAGE_SEND_4};
	
	private Object recipient;
	private int amount;
	private int consecutive;
	
	public ParticleGarbageSender(ObjPuyoBoard recipient, int amount, int consecutive, PuyoType color) {
		super((int) (recipient.getX()+8), (int) (recipient.getY()-16), GarbageSprites.LARGE.getSprite(), color.getColor(), 0.44, 7);
		this.recipient = recipient;
		this.amount = amount;
		this.consecutive = consecutive;
	}
	public ParticleGarbageSender(ObjGarbageIndicator recipient, int amount, int consecutive, PuyoType color) {
		super((int) (recipient.getX()+16), (int) (recipient.getY()+6), GarbageSprites.LARGE.getSprite(), color.getColor(), 0.44, 7);
		this.recipient = recipient;
		this.amount = amount;
		this.consecutive = consecutive;
	}
	
	public void arrived() {
		if (recipient instanceof ObjPuyoBoard) {
			ObjPuyoBoard board = (ObjPuyoBoard) recipient;
			if (board instanceof ObjPuyoFeverBoard) {
				ObjPuyoFeverBoard fBoard = (ObjPuyoFeverBoard) recipient;
				if (fBoard.getFeverManager().isInFever()) {
					fBoard.getFeverIndicator().addGarbage(amount);
					return;
				} else {
					board.getGarbageIndicator().addGarbage(amount);
				}
			} else {
				board.getGarbageIndicator().addGarbage(amount);
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
		
		soundOrder[Math.min(soundOrder.length-1, consecutive-1)].play();
	}
}
