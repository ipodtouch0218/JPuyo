package me.ipodtouch0218.multiplayerpuyo.menu.elements;

import java.awt.Color;
import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.object.GameObject;

public class LoadingBar extends GameObject {

	private double max = 1;
	private double value = 0;
	private int length = 20;
	private int height = 4;
	
	private Color baseColor = Color.DARK_GRAY;
	private Color color = Color.GREEN;
	
	public LoadingBar(double max, double value) {
		this.max = max;
		this.value = value;
	}
	
	public void render(Graphics2D g) {
		g.setColor(baseColor);
		g.fillRect((int) x, (int) y, length, height);
		
		g.setColor(color);
		g.fillRect((int) x, (int) y, (int) (max > 0 ? (value/max)*length : length), height);
	}
	
	public void setBaseColor(Color color) { this.baseColor = color; }
	public void setColor(Color color) { this.color = color; }
	public void setValue(double value) { this.value = value; }
	public void setMaxValue(double value) { this.max = value; }
	public void setLength(int length) { this.length = length; }
	public void setHeight(int height) { this.height = height; }
	
	public Color getBaseColor() { return baseColor; }
	public Color getColor() { return color; }
	public double getValue() { return value; }
	public double getMaxValue() { return max; }
	public int getLength() { return length; }
	public int getHeight() { return height; }
}
