package de.dualuse.swt.experiments.scratchy.video;

import java.util.concurrent.CopyOnWriteArrayList;

public class VideoEditor {
	
	Video video;
	Video videoSD;
		
	int position;
	int total;
	
//==[ Constructor ]=================================================================================
	
	public VideoEditor(Video video) {
		this.video = video;
		this.total = video.numFrames();
	}
	
	public VideoEditor(Video video, Video videoSD) {
		this.video = video;
		this.videoSD = videoSD;
		this.total = video.numFrames();
		
		if (video.numFrames() != videoSD.numFrames())
			throw new IllegalArgumentException("Non-matching SD and HD frames (amount should be equal).");
	}

	public Video getVideo() {
		return video;
	}
	
	public Video getVideoSD() {
		return videoSD;
	}
	
//==[ Get/Set Position ]============================================================================
	
	public void scratchTo(int frame) {
		int from = position;
		position = limit(frame);
		fireScratchedTo(from, position);
	}
	
	public void scratchRelative(int frames) {
		scratchTo(position + frames);
	}
	
	public void moveTo(int frame) {
		int from = position;
		position = limit(frame);
		fireMovedTo(from, position);
	}
	
	public void moveRelative(int frames) {
		moveTo(position + frames);
	}
	
	public int getPosition() {
		return position;
	}

	/////
	
	private int limit(int pos) {
		return Math.max(0, Math.min(total-1, pos));
	}
	
//==[ Listener ]====================================================================================
	
	public interface EditorListener {
		void scratchedTo(int from, int to);
		void movedTo(int from, int to);
	}
	
	CopyOnWriteArrayList<EditorListener> listeners = new CopyOnWriteArrayList<EditorListener>();
	
	public void addEditorListener(EditorListener listener) {
		listeners.add(listener);
	}
	
	public void removeEditorListener(EditorListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireScratchedTo(int from, int to) {
		for (EditorListener listener : listeners)
			listener.scratchedTo(from, to);
	}
	
	protected void fireMovedTo(int from, int to) {
		for (EditorListener listener : listeners)
			listener.movedTo(from, to);
	}
}
