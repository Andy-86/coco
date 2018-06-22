package com.devicemanagement.modedetection;

import java.util.LinkedList;
import java.util.TreeSet;

/**
 * 低通滤波器，把静态部分保留，抹平强烈变化部分。 
 * @author discotek
 *
 */
public class LowPassFilter {
	
	public LowPassFilter(int sampleCounts) {
//		if(sampleCounts < 2) {
//			this.sampleCount = 2;
//		} else {
			this.sampleCount = sampleCounts;
//		}
		samples = new LinkedList<Float>();
		if(sampleCounts >= 50) {
			l2 = new Level2Filter(sampleCounts * 15 / 100);
		}
	}
	
	public synchronized void offer(float[] src) {
		float v = calc(src);
		samples.add(v);
		if(samples.size() > sampleCount) {
			samples.removeFirst();
		}
		if(l2 != null) {
			l2.clear();
		}
		float sum = 0;
		for(float val : samples) {
			if(l2 != null)
				l2.offer(val);
			sum += val;
		}
		if(l2 != null) {
			sum -= l2.getVariance();
			lastValue = sum / (samples.size() - l2.count);
		} else {
			lastValue = sum / samples.size();
		}
	}

	public synchronized float get() {
		return lastValue;
	}
	
	private float calc(float[] src) {
		return (float) Math.sqrt(src[0] * src[0] + src[1] * src[1] + src[2] * src[2]);
	}

	private LinkedList<Float> samples;
	private float lastValue = 0;
	private int sampleCount;
	
	private Level2Filter l2;
	
	static class Level2Filter {
		Level2Filter(int count) {
			this.count = count;
		}
		
		void offer(float value) {
			chaos.add(value);
		}
		
		float getVariance() {
//			Log.e("sensor", "chaos " + chaos);
			if(chaos == null || chaos.size() < 2 * count) {
//				Log.e("sensor", "filter value null");
				return 0;
			}
			float sum = 0;
			for(int i=0; i<count; i++) {
				sum += chaos.pollFirst();
				sum += chaos.pollLast();
			}
			return sum;
		}
		
		void clear() {
			chaos.clear();
		}
		
		TreeSet<Float> chaos = new TreeSet<Float>();
		
		int count;
	}
}
