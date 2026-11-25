package layoutcheck;

import net.miginfocom.swing.SwingComponentWrapper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Finds every component whose baseline, as MigLayout's wrapper reports it, depends on the questions asked before.
 * <p>
 * A layout manager may only memoize a baseline per size when the answer to getBaseline(w, h) depends on w and h alone.
 * For each component of each scenario (laid out at its preferred size first), this asks the wrapper on the class path
 * for its baseline at a handful of sizes, in many orders with repeats, and reports each size that got two different
 * answers. It runs against any MigLayout build, so it shows which components the build's wrapper makes pure.
 * <p>
 * Usage: java -cp classes:core.jar:swing.jar:flatlaf.jar layoutcheck.Probe OUT_FILE LAF [SCENARIO_REGEX]
 */
public final class Probe
{
	private static final int ORDERS = 24;

	public static void main(String[] args) throws Exception
	{
		File out = new File(args[0]);
		String laf = args[1];
		Pattern only = args.length > 2 ? Pattern.compile(args[2]) : null;
		SwingUtilities.invokeAndWait(() -> {
			try {
				Lafs.set(laf);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		StringBuilder json = new StringBuilder("{\"laf\":" + Runner.q(laf) + ",\"impure\":[\n");
		int[] counts = new int[2]; // components probed, impure ones
		boolean[] first = {true};
		for (Scenario s : Catalogue.all()) {
			if (only != null && !only.matcher(s.id).find())
				continue;
			SwingUtilities.invokeAndWait(() -> {
				JFrame f = new JFrame();
				f.setUndecorated(true);
				try {
					JComponent root = s.builder.get();
					f.getContentPane().add(root);
					f.pack();
					f.validate();
					List<Component> all = new ArrayList<>();
					collect(root, all);
					for (Component c : all) {
						counts[0]++;
						String finding = probe(c);
						if (finding != null) {
							counts[1]++;
							if (!first[0])
								json.append(",\n");
							first[0] = false;
							json.append("{\"scenario\":").append(Runner.q(s.id))
								.append(",\"cacheable\":").append(cacheable(c))
								.append(",\"component\":").append(Runner.q(describe(c)))
								.append(",\"finding\":").append(Runner.q(finding)).append("}");
						}
					}
				} catch (Throwable t) {
					System.err.println(s.id + ": " + t);
				} finally {
					f.dispose();
				}
			});
		}
		json.append("\n],\"probed\":").append(counts[0]).append(",\"impureCount\":").append(counts[1]).append("}\n");
		out.getParentFile().mkdirs();
		Files.writeString(out.toPath(), json, StandardCharsets.UTF_8);
		System.out.println(laf + ": probed " + counts[0] + " components, " + counts[1] + " with history-dependent baselines");
		System.exit(0);
	}

	private static void collect(Component c, List<Component> out)
	{
		out.add(c);
		if (c instanceof Container && !(c instanceof CellRendererPane))
			for (Component child : ((Container) c).getComponents())
				collect(child, out);
	}

	/** What the build's wrapper says about remembering the baseline: "true"/"false", or "n/a" when it has no such method. */
	private static String cacheable(Component c)
	{
		try {
			Object r = SwingComponentWrapper.class.getMethod("isBaselineCacheable").invoke(new SwingComponentWrapper(c));
			return "\"" + r + "\"";
		} catch (NoSuchMethodException e) {
			return "\"n/a\"";
		} catch (Exception e) {
			return Runner.q("error: " + e);
		}
	}

	/** Returns null when every size always got the same answer, else what differed. */
	private static String probe(Component c)
	{
		SwingComponentWrapper w = new SwingComponentWrapper(c);
		Dimension min = c.getMinimumSize(), pref = c.getPreferredSize();
		int[][] sizes = {
				{pref.width, pref.height},
				{min.width, min.height},
				{Math.max(1, pref.width / 2), pref.height},
				{pref.width * 2, pref.height},
				{pref.width, pref.height * 2},
				{Math.max(1, c.getWidth()), Math.max(1, c.getHeight())},
		};
		Map<Integer, Set<Integer>> answers = new TreeMap<>();
		Random r = new Random(42);
		for (int order = 0; order < ORDERS; order++) {
			int n = 3 + r.nextInt(6);
			for (int i = 0; i < n; i++) {
				int ix = r.nextInt(sizes.length);
				int b = w.getBaseline(sizes[ix][0], sizes[ix][1]);
				answers.computeIfAbsent(ix, k -> new TreeSet<>()).add(b);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Set<Integer>> e : answers.entrySet()) {
			if (e.getValue().size() > 1) {
				int[] sz = sizes[e.getKey()];
				sb.append(sz[0]).append("x").append(sz[1]).append(" -> ").append(e.getValue()).append("; ");
			}
		}
		return sb.length() == 0 ? null : sb.toString().trim();
	}

	private static String describe(Component c)
	{
		StringBuilder sb = new StringBuilder(c.getClass().getSimpleName().isEmpty() ? c.getClass().getName() : c.getClass().getSimpleName());
		if (c.getName() != null)
			sb.append(" '").append(c.getName()).append("'");
		String text = null;
		if (c instanceof JLabel)
			text = ((JLabel) c).getText();
		else if (c instanceof AbstractButton)
			text = ((AbstractButton) c).getText();
		else if (c instanceof JComboBox && ((JComboBox<?>) c).getItemCount() > 0)
			text = "item0=" + ((JComboBox<?>) c).getItemAt(0);
		if (text != null)
			sb.append(" ").append(text.length() > 70 ? text.substring(0, 70) + "..." : text);
		return sb.toString();
	}

	private Probe() {}
}
