package me.ipodtouch0218.multiplayerpuyo.misc;

import java.awt.image.VolatileImage;

public class PuyoColumnInfo {

	public double columnSquish = 1;	
	public double columnSquishTimer = 0;
	public double columnSquishIncrementAmount = 0;
	public VolatileImage renderImage;
	public VolatileImage flashImage;
	
	public boolean rerender = true;
	
//	private PuyoInfo[] cachedColumn; //cached, used to ask if you should re-render the image
//	
//	public boolean reRenderColumn(PuyoInfo[] column) { //if the cached column has changed
//		boolean equal = Arrays.equals(cachedColumn, column);
//		if (!equal) {
//			cachedColumn = new PuyoInfo[column.length]; //TODO not rely on cloning all the puyo infos
//			for (int i = 0; i < column.length; i++) {
//				cachedColumn[i] = column[i].clone();
//			} 
//			return true;
//		}
//		return false;
//	}
//	
}
