package me.ipodtouch0218.multiplayerpuyo.objects.particle.senders;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.misc.GarbageSenderStatus;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator.GarbageSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoFeverBoard;

public class ParticleGarbageCancelSender extends ParticleSender {

	private PuyoType color;
	private GarbageSenderStatus sender;
	private ObjGarbageIndicator indicator;
	private int amount;
	private int consecutive;
	private boolean red;
	
	public ParticleGarbageCancelSender(GarbageSenderStatus sender, ObjGarbageIndicator indi, int cancelAmount, int consecutive, PuyoType color, boolean red) {
		super((int) (sender.getBoard().getX()+16), (int) (sender.getBoard().getY()-8), (red ? GarbageSprites.RED.getSprite() : GarbageSprites.LARGE.getSprite()), color.getColor(), 0.44, 7);
		this.color = color;
		this.sender = sender;
		this.amount = cancelAmount;
		this.indicator = indi;
		this.red = red;
		this.consecutive = consecutive;
	}
	
	public void arrived() {
		
		int garbage = indicator.getOverallGarbage()-amount;
		indicator.setOverallGarbage(Math.max(0, garbage));
		sender.getBoard().addDroppableGarbage(null, -amount);
		
		if (consecutive != -1) {
			ParticleGarbageSender.soundOrder[Math.min(ParticleGarbageSender.soundOrder.length-1, consecutive-1)].play();
		}
		if (garbage >= 0) { return; }
		if (sender.getBoard() instanceof ObjPuyoFeverBoard) {
			ObjPuyoFeverBoard fs = (ObjPuyoFeverBoard) sender.getBoard();
			if (fs.getFeverManager().isInFever()) {
				if (indicator == fs.getFeverIndicator()) {
					if (fs.getGarbageIndicator().getOverallGarbage() > 0) {
						GameEngine.addGameObject(new ParticleGarbageCancelSender(sender, sender.getBoard().getGarbageIndicator(), -garbage, -1, color, red), x, y);
						return;
					}
				}
			}
		}
		ParticleGarbageSender[] send = sender.getBoard().getBoardManager().sendGarbage(sender, -garbage, true, color, red, consecutive);
		for (ParticleGarbageSender senders : send) {
			senders.setLocation(this.x, this.y);
		}
	}
}
