package com.devicemanagement.modedetection;

/**
 * 高通滤波器，把变化部分取出，把静态部分去除，并将变化的部分放大alpha倍。
 * sqrt(x*x + y*y + z*z) * 100
 * @author discotek
 *
 */
public class HighPassFilter {

	public HighPassFilter(float factor) {
		alpha = factor;
	}
	
	public synchronized void offer(float[] src) {
		if(last[0] == 0) {
			last[0] = src[0];
			last[1] = src[1];
			last[2] = src[2];
		}
		lastFiltered[0] = alpha * (src[0] - last[0]);
		lastFiltered[1] = alpha * (src[1] - last[1]);
		lastFiltered[2] = alpha * (src[2] - last[2]);		
		last[0] = src[0];
		last[1] = src[1];
		last[2] = src[2];
	}
	
	public synchronized double getVectorValue() {
		return Math.sqrt(lastFiltered[0] * lastFiltered[0] + lastFiltered[1] * lastFiltered[1] + lastFiltered[2] * lastFiltered[2]);
	}

	public synchronized float[] get() {
		float[] res = new float[3];
		System.arraycopy(lastFiltered, 0, res, 0, 3);
		return res;
//		return lastFiltered;
	}

	private float[] lastFiltered = new float[3];
	private float[] last = new float[3];
	private float alpha;
}