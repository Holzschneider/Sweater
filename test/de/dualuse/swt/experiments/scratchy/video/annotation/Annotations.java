package de.dualuse.swt.experiments.scratchy.video.annotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Annotations<E> {
	
	List<Annotation<E>> annotations = new ArrayList<>();
	TreeMap<Long,Annotation<E>> keyIdMap = new TreeMap<>();

	// XXX all annotations that have keys < frame and keys > frame
	// 	   (for now: only interpolation/tracking in between and not trailing)
	
	// annotation.firstKey(), annotation.lastKey()
	
	// necessary to iterate over all annotations?
	// we have no cuts, but we could use implicit ranges
	// (add the annotation to all ranges that it spans from minkey to maxkey)
	// (then we could limit the search to all annotations in the current range)

	
	// no cuts ... fetchAnnotations was used to query patches in the current scene (from floor cut to ceil cut)
	// not just patches visible at the current frame
//	public Set<Annotation> fetchAnnotations(int frame) {
//		return fetchAnnotations(frame, new HashSet<Annotation>());
//	}
//	
//	public Set<Annotation> fetchAnnotations(int frame, Set<Annotation> collector) {
//		
//		for (Annotation annotation : annotations) {
//			
//		}
//		return null;
//	}
	
//==[ Add/Remove Annotations ]======================================================================
	
	public void addAnnotation(Annotation<E> annotation) {
		
		annotations.add(annotation);
		
		for (Integer key : annotation.getKeys().keySet())
			keyIdMap.put(keyId(annotation, key), annotation);
		
		// fire listeners
	}
	
	public void removeAnnotation(Annotation<E> annotation) {
		
		annotations.remove(annotation);
		
		for (Integer key : annotation.getKeys().keySet())
			keyIdMap.remove(keyId(annotation, key));
		
		// fire listeners
	}
	
	public void addKey(Annotation<E> annotation, int frame) {
		keyIdMap.put(keyId(annotation, frame), annotation);
	}
	
	public void removeKey(Annotation<E> annotation, int frame) {
		keyIdMap.remove(keyId(annotation, frame));
	}
	
//==[ Fetch Annotations ]===========================================================================
	
	public Set<Annotation<E>> fetchAnnotations(int frame, Set<Annotation<E>> collector) {
		return fetchAnnotations(frame, frame+1, collector);
	}
	
	public Set<Annotation<E>> fetchAnnotations(int fromFrame, int toFrame, Set<Annotation<E>> collector) {
		long from = ((long)fromFrame)<<32;
		long to = ((long)toFrame)<<32;
		
		for (Long key = keyIdMap.ceilingKey(from); key!=null && key<to; key = keyIdMap.higherKey(key)) {
			Annotation<E> annotation = keyIdMap.get(key);
			collector.add(annotation);
		}
		
		return collector;
	}
	
//==[ Navigation ]==================================================================================
	
	public Integer getPrevKeyFrame(int frame) {
		Long key = keyIdMap.lowerKey(key(frame-1));
		if (key==null) return null;
		return (int)(key>>32);
	}
	
	public Integer getNextKeyFrame(int frame) {
		Long key = keyIdMap.higherKey(key(frame+1));
		if (key==null) return null;
		return (int)(key>>32);
	}
	
//==[ Helper Method ]===============================================================================
	
	protected long key(int frame) {
		return ((long)frame)<<32;
	}
	
	protected long keyId(Annotation <E> annotation, int frame) {
		long framePart = ((long)frame)<<32;
		long idPart = ((long)System.identityHashCode(annotation))&0xFFFFFFFFL;
		return framePart | idPart;
	}
	
}
