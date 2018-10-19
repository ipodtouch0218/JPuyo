package me.ipodtouch0218.multiplayerpuyo.manager;

public class DualJoyconControls extends Controls {
	
	protected JoyconControls leftJoycon;
	protected JoyconControls rightJoycon;
	
	public DualJoyconControls(JoyconControls left, JoyconControls right) {
		if (left == null || right == null) { return; }
		this.leftJoycon = left;
		this.rightJoycon = right;
		
		left.setRotated(false);
		right.setRotated(false);
		
	}
	
	@Override
	public void tick(double delta) {
		
		turningLeft = rightJoycon.btnRight;
		menuEnter = turningLeft;
		
		if (player == 0) { return; }
		
		turningRight = rightJoycon.btnDown;
		
		pause = rightJoycon.pause;
		
		movingLeft = leftJoycon.btnLeft || leftJoycon.stickLeft;
		movingRight = leftJoycon.btnRight || leftJoycon.stickRight;
		instaDrop = leftJoycon.btnUp || leftJoycon.stickUp;
		fastDrop = leftJoycon.btnDown || leftJoycon.stickDown;
		
		menuLeft = movingLeft;
		menuRight = movingRight;
		menuUp = instaDrop;
		menuDown = fastDrop;
	}	
	
	@Override
	public void setPlayer(int player) {
		super.setPlayer(player);
		
		leftJoycon.setPlayer(player);
		rightJoycon.setPlayer(player);
	}
}
