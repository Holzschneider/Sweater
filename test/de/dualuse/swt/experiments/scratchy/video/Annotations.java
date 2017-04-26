package de.dualuse.swt.experiments.scratchy.video;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class Annotations {
	
	List<Annotation> annotations = new ArrayList<>();
	TreeMap<Long,Annotation> keyIdMap = new TreeMap<>();

	// XXX all annotations that have keys < frame and keys > frame
	// 	   (for now: only interpolation/tracking in between and not trailing)
	
	// annotation.firstKey(), annotation.lastKey()
	
	// necessary to iterate over all annotations?
	// we have no cuts, but we could use implicit ranges
	// (add the annotation to all ranges that it spans from minkey to maxkey)
	// (then we could limit the search to all annotations in the current range)
	
	public Set<Annotation> fetchAnnotations(int frame, Set<Annotation> collector) {
		for (Annotation annotation : annotations) {
			
		}
		return null;
	}
	
	public Set<Annotation> fetchAnnotations(int fromFrame, int toFrame, Set<Annotation> collector) {
		long from = ((long)fromFrame)<<32;
		long to = ((long)toFrame)<<32;
		
		for (Long key = keyIdMap.ceilingKey(from); key!=null && key<to; key = keyIdMap.higherKey(key)) {
			Annotation annotation = keyIdMap.get(key);
			collector.add(annotation);
		}
		
		return collector;
	}
	
	protected long keyId(Annotation annotation, int frame) {
		long framePart = ((long)frame)<<32;
		long idPart = ((long)System.identityHashCode(annotation))&0xFFFFFFFFL;
		return framePart | idPart;
	}
	
}
