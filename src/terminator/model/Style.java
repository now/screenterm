package terminator.model;

import java.awt.*;

/**
 * Phil talks about this class as being immutable, yet it's not final.
 * I'm also not sure what would break if we could mutate Style
 * instances.
 *
 * For now, I've subclassed this and overridden getBackground.
 * 
 * Here's Phil's commentary:
 * 
 * Oh, and to answer the original question, the Style associated with a
 * piece of text which has been produced by the escape-sequence-filled
 * stream of characters from the process we're running, should never be
 * modified by anyone else.  IOW, no part of the program is allowed to
 * corrupt our only copy of the data.
 * 
 * Of course, anyone's free to create Style subclasses which filter the
 * colors, or act as an adapter to an 'original' Style object.  However,
 * since the data model generator will only produce vanilla Style instances,
 * such activity can't produce model corruption, which is what we wish to
 * avoid.
 * 
 * So it's not quite as strictly immutable as String, but the particular
 * aspect of immutability which is enforced is that which states that the
 * creator knows that the Style's nature cannot be changed, but it does not
 * enforce that the recipient of a Style may know that it cannot change its
 * nature.  Of course, the latter is unlikely to happen, but it's not
 * absolutely disallowed because there's no particular reason it should be.
 */
public class Style {
	// If any of these is null, it should be ignored.  We use Boolean references because they
	// can be used to represent 3 states - null, TRUE and FALSE.
	private Color foreground;
	private Color background;
	private Boolean isUnderlined;
	private boolean isReverseVideo;
	
	public Style(Color foreground, Color background, Boolean isUnderlined, boolean isReverseVideo) {
		this.foreground = foreground;
		this.background = background;
		this.isUnderlined = isUnderlined;
		this.isReverseVideo = isReverseVideo;
	}
	
	/**
	 * Returns a new Style that represents this style's elements applied
	 * to the given style. Any attributes which this style doesn't have
	 * will be copied from the given style.
	 * 
	 * There's a small, fixed set of possible styles returned from this
	 * method, but we don't do any optimization to take advantage of this.
	 * We might want to reconsider that; it looks to me that something like
	 * sweeping a selection will needlessly create lots of new Style
	 * instances.
	 */
	public Style appliedTo(Style originalStyle) {
		Color mutatedForeground = hasForeground() ? getForeground() : originalStyle.getForeground();
		Color mutatedBackground = hasBackground() ? getBackground() : originalStyle.getBackground();
		Boolean mutatedUnderlined = Boolean.valueOf(hasUnderlined() ? isUnderlined() : originalStyle.isUnderlined());
		boolean mutatedReverseVideo = isReverseVideo() || originalStyle.isReverseVideo();
		return new Style(mutatedForeground, mutatedBackground, mutatedUnderlined, mutatedReverseVideo);
	}

	public boolean hasForeground() {
		return foreground != null;
	}
	
	public Color getForeground() {
		return foreground;
	}
	
	public boolean hasBackground() {
		return background != null;
	}
	
	public Color getBackground() {
		return background;
	}
	
	public boolean hasUnderlined() {
		return isUnderlined != null;
	}
	
	public boolean isUnderlined() {
		return hasUnderlined() && isUnderlined.booleanValue();
	}
	
	public boolean isReverseVideo() {
		return isReverseVideo;
	}
}
