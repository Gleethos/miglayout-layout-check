package layoutcheck;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lays out every scenario of the {@link Catalogue} with whichever MigLayout is on the class path, in one look and feel,
 * and writes the bounds of every component (results.json) and a screenshot per step.
 * <p>
 * Usage: java -cp classes:core.jar:swing.jar:flatlaf.jar layoutcheck.Runner OUT_DIR LAF [SCENARIO_REGEX]
 */
public final class Runner
{
	public static void main(String[] args) throws Exception
	{
		File out = new File(args[0]);
		String laf = args[1];
		Pattern only = args.length > 2 ? Pattern.compile(args[2]) : null;

		SwingUtilities.invokeAndWait(() -> {
			try {
				Lafs.set(laf);
			} catch (Exception e) {
				throw new RuntimeException("Cannot set look and feel " + laf, e);
			}
		});

		StringBuilder json = new StringBuilder();
		json.append("{\"laf\":").append(q(laf))
			.append(",\"java\":").append(q(System.getProperty("java.version")))
			.append(",\"uiScale\":").append(q(String.valueOf(System.getProperty("sun.java2d.uiScale"))))
			.append(",\"scenarios\":[\n");
		boolean first = true;
		for (Scenario s : Catalogue.all()) {
			if (only != null && !only.matcher(s.id).find())
				continue;
			if (!first)
				json.append(",\n");
			first = false;
			json.append(runScenario(s, new File(out, s.id)));
		}
		json.append("\n]}\n");
		out.mkdirs();
		Files.writeString(new File(out, "results.json").toPath(), json, StandardCharsets.UTF_8);
		System.exit(0);
	}

	private static String runScenario(Scenario s, File dir)
	{
		dir.mkdirs();
		StringBuilder sb = new StringBuilder();
		sb.append("{\"id\":").append(q(s.id)).append(",\"group\":").append(q(s.group)).append(",\"title\":").append(q(s.title))
			.append(",\"description\":").append(q(s.description)).append(",\"steps\":[");
		StepContext[] ctx = new StepContext[1];
		JFrame[] frame = new JFrame[1];
		try {
			SwingUtilities.invokeAndWait(() -> {
				frame[0] = new JFrame(s.id);
				frame[0].setUndecorated(true);
				JComponent root = s.builder.get();
				frame[0].getContentPane().add(root, BorderLayout.CENTER);
				frame[0].pack(); // makes it displayable, but it is never shown
				ctx[0] = new StepContext(frame[0], root);
			});
			int ix = 0;
			for (Scenario.Step step : s.effectiveSteps()) {
				if (ix > 0)
					sb.append(",");
				String[] result = new String[1];
				final int stepIx = ix;
				SwingUtilities.invokeAndWait(() -> {
					try {
						step.action().apply(ctx[0]);
						result[0] = null;
					} catch (Throwable t) {
						result[0] = stackTrace(t);
					}
				});
				settle(frame[0]);
				String[] capture = new String[1];
				SwingUtilities.invokeAndWait(() -> capture[0] = capture(ctx[0], step, stepIx, dir));
				sb.append("{\"label\":").append(q(step.label()));
				if (result[0] != null)
					sb.append(",\"error\":").append(q(result[0]));
				sb.append(",").append(capture[0]).append("}");
				ix++;
			}
		} catch (Throwable t) {
			sb.append("],\"error\":").append(q(stackTrace(t))).append("}");
			disposeQuietly(frame[0]);
			return sb.toString();
		}
		disposeQuietly(frame[0]);
		sb.append("]}");
		return sb.toString();
	}

	/** Validates as the RepaintManager would and lets queued events (relayouts posted with invokeLater) run, until the
	 * whole tree has been valid with no events pending twice in a row, or for at most 20 rounds. */
	private static void settle(JFrame frame) throws Exception
	{
		int quiet = 0;
		for (int i = 0; i < 20 && quiet < 2; i++) {
			SwingUtilities.invokeAndWait(frame::validate);
			SwingUtilities.invokeAndWait(() -> {});
			boolean[] calm = new boolean[1];
			SwingUtilities.invokeAndWait(() -> calm[0] = allValid(frame) && Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() == null);
			quiet = calm[0] ? quiet + 1 : 0;
		}
	}

	private static boolean allValid(Component c)
	{
		if (!c.isValid())
			return false;
		if (c instanceof Container)
			for (Component child : ((Container) c).getComponents())
				if (!allValid(child))
					return false;
		return true;
	}

	private static String capture(StepContext ctx, Scenario.Step step, int ix, File dir)
	{
		JComponent root = ctx.root;
		StringBuilder sb = new StringBuilder();
		sb.append("\"w\":").append(root.getWidth()).append(",\"h\":").append(root.getHeight()).append(",\"comps\":[");
		List<String> comps = new ArrayList<>();
		walk(root, root, "", comps);
		sb.append(String.join(",", comps)).append("]");

		String file = String.format("%02d.png", ix);
		try {
			int w = Math.max(1, root.getWidth()), h = Math.max(1, root.getHeight());
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			g.setColor(Color.MAGENTA); // anything left unpainted stands out
			g.fillRect(0, 0, w, h);
			root.printAll(g);
			g.dispose();
			ImageIO.write(img, "png", new File(dir, file));
			sb.append(",\"png\":").append(q(dir.getName() + "/" + file));
		} catch (Throwable t) {
			sb.append(",\"paintError\":").append(q(stackTrace(t)));
		}
		return sb.toString();
	}

	private static void walk(Component root, Component c, String path, List<String> out)
	{
		Rectangle r = SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), root);
		if (c == root)
			r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
		StringBuilder sb = new StringBuilder();
		sb.append("{\"p\":").append(q(path.isEmpty() ? "root" : path))
			.append(",\"c\":").append(q(c.getClass().getSimpleName().isEmpty() ? c.getClass().getName() : c.getClass().getSimpleName()))
			.append(",\"b\":[").append(r.x).append(",").append(r.y).append(",").append(r.width).append(",").append(r.height).append("]");
		if (c.getName() != null)
			sb.append(",\"n\":").append(q(c.getName()));
		String text = textOf(c);
		if (text != null)
			sb.append(",\"t\":").append(q(text.length() > 60 ? text.substring(0, 60) + "..." : text));
		if (!c.isVisible())
			sb.append(",\"hidden\":true");
		if (c instanceof Container && ((Container) c).getLayout() instanceof net.miginfocom.swing.MigLayout) {
			// What the layout reports as the container's sizes, which parents and windows use
			Dimension min = c.getMinimumSize(), pref = c.getPreferredSize();
			sb.append(",\"min\":[").append(min.width).append(",").append(min.height).append("]")
				.append(",\"pref\":[").append(pref.width).append(",").append(pref.height).append("]");
		}
		sb.append("}");
		out.add(sb.toString());
		if (c instanceof Container && !(c instanceof CellRendererPane)) {
			Component[] children = ((Container) c).getComponents();
			for (int i = 0; i < children.length; i++)
				walk(root, children[i], path.isEmpty() ? String.valueOf(i) : path + "/" + i, out);
		}
	}

	private static String textOf(Component c)
	{
		if (c instanceof JLabel)
			return ((JLabel) c).getText();
		if (c instanceof AbstractButton)
			return ((AbstractButton) c).getText();
		if (c instanceof javax.swing.text.JTextComponent && !(c instanceof JEditorPane))
			return ((javax.swing.text.JTextComponent) c).getText();
		return null;
	}

	private static void disposeQuietly(JFrame f)
	{
		if (f == null)
			return;
		try {
			SwingUtilities.invokeAndWait(f::dispose);
		} catch (Exception ignored) {
		}
	}

	static String stackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	static String q(String s)
	{
		if (s == null)
			return "null";
		StringBuilder sb = new StringBuilder("\"");
		for (char ch : s.toCharArray()) {
			switch (ch) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (ch < 0x20)
						sb.append(String.format("\\u%04x", (int) ch));
					else
						sb.append(ch);
				}
			}
		}
		return sb.append('"').toString();
	}

	private Runner() {}
}
