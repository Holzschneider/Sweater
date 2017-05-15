package de.dualuse.swt.experiments.scratchy.video.annotation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public abstract class Annotation<E> {
	
	TreeMap<Integer,E> keys = new TreeMap<Integer,E>();
	
//==[ Constructor ]=================================================================================
	
	public Annotation(int frame, E data) {
		keys.put(frame, data);
	}
	
//==[ Keys ]========================================================================================

	public int size() {
		return keys.size();
	}
	
	public boolean isKey(int frame) {
		return keys.keySet().contains(frame);
	}
	
	public void setKey(int frame, E data) {
		
//		if (annotations!=null && !isKey(frame))
//			annotations.addKey(this, frame, data);
		
		keys.put(frame, data);
		
//		reportDirty();
		
	}

	
	public Map<Integer,E> getKeys() {
		return keys;
	}
	
	public Integer firstKey() { return keys.firstKey(); }
	public Integer lastKey() { return keys.lastKey(); }
	
	public Integer floorKey(int frame) { return keys.floorKey(frame); }
	public Integer ceilingKey(int frame) { return keys.ceilingKey(frame); }
	
	public Integer higherKey(int frame) { return keys.higherKey(frame); }
	public Integer lowerKey(int frame) { return keys.lowerKey(frame); }
	
	public Integer nearest(int frame) {
		Integer floor = floorKey(frame);
		Integer ceiling = ceilingKey(frame);
		return Math.abs(floor-frame) < Math.abs(ceiling-frame) ? floor : ceiling; 
	}
	
//==[ Values ]======================================================================================

	public E getKey(int frame) {
		return keys.get(frame);
	}
	
	public E getValue(int frame) {
		if (keys.isEmpty())
			return null;
		
		if (frame < keys.firstKey())
			return keys.firstEntry().getValue();
		
		if (frame > keys.lastKey())
			return keys.lastEntry().getValue();
		
		if (isKey(frame))
			return keys.get(frame);
		
		if (keys.size()==1)
			return keys.firstEntry().getValue();
		
		Entry<Integer,E> left = keys.floorEntry(frame);
		Entry<Integer,E> right = keys.ceilingEntry(frame);
		
		double r = (frame-left.getKey())*1./(right.getKey()-left.getKey());
		return interpolate(left.getValue(), right.getValue(), r);
	}
	
	protected abstract E interpolate(E left, E right, double r); 
}
