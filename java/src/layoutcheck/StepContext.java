package layoutcheck;

import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

/**
 * What a scenario step works with: the root component in its (never shown) frame, its size, and a lookup of
 * components by name. All of it runs on the event dispatch thread, and after each step the runner validates the
 * frame, just as the RepaintManager would after a revalidate() on screen.
 */
public final class StepContext
{
	public final JFrame frame;
	public final JComponent root;
	private Dimension firstPreferred;

	StepContext(JFrame frame, JComponent root)
	{
		this.frame = frame;
		this.root = root;
	}

	/** The preferred size the root had when it was first laid out, so that sizes stay stable across mutations. */
	public Dimension basePreferred()
	{
		if (firstPreferred == null)
			firstPreferred = root.getPreferredSize();
		return new Dimension(firstPreferred);
	}

	public void size(int w, int h)
	{
		frame.setSize(Math.max(1, w), Math.max(1, h));
	}

	public void sizeToPreferred()
	{
		basePreferred();
		Dimension d = root.getPreferredSize();
		size(d.width, d.height);
	}

	public void sizeRelative(double fw, double fh)
	{
		Dimension d = basePreferred();
		size((int) Math.round(d.width * fw), (int) Math.round(d.height * fh));
	}

	/** Lays out now, synchronously, as if a pass of the RepaintManager had run. Steps rarely need this. */
	public void validateNow()
	{
		frame.validate();
	}

	@SuppressWarnings("unchecked")
	public <T extends Component> T find(String name)
	{
		Component c = find(root, name);
		if (c == null)
			throw new IllegalArgumentException("No component named " + name);
		return (T) c;
	}

	private static Component find(Component c, String name)
	{
		if (name.equals(c.getName()))
			return c;
		if (c instanceof Container) {
			for (Component child : ((Container) c).getComponents()) {
				Component f = find(child, name);
				if (f != null)
					return f;
			}
		}
		return null;
	}
}
