package layoutcheck;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;

/**
 * Focused scenarios, one MigLayout feature (or a small family of them) each: layout, column, row and component
 * constraints, content bias, nesting, hidden components and the mutations apps make at run time.
 * <p>
 * They are meant to catch any change in the bounds MigLayout computes, so they favour coverage of the constraint
 * language over looks, while still showing the kind of controls a real application would.
 */
final class ScenariosLayout
{
	private static final String G_LC = "Layout: layout constraints";
	private static final String G_DC = "Layout: columns and rows";
	private static final String G_CC = "Layout: component constraints";
	private static final String G_DOCK = "Layout: docking and positions";
	private static final String G_HIDE = "Layout: hidden components";
	private static final String G_BIAS = "Layout: content bias";
	private static final String G_NEST = "Layout: nesting";
	private static final String G_MUT = "Layout: mutations";
	private static final String G_COMBO = "Layout: combined";

	private static final String LOREM = "MigLayout lays out components in a grid of cells. A text area that wraps its lines "
			+ "needs more height the narrower it gets, so its preferred height depends on the width the layout gives it.";

	static void add(List<Scenario> l)
	{
		layoutConstraints(l);
		hidden(l);
		columnsAndRows(l);
		componentConstraints(l);
		docking(l);
		contentBias(l);
		nesting(l);
		mutations(l);
		combined(l);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Layout constraints

	private static void layoutConstraints(List<Scenario> l)
	{
		l.add(new Scenario("layout-lc-wrap", G_LC, "Wrap after N cells",
				"An address form laid out with 'wrap 4', then rewrapped to 2 and 6 columns through setLayoutConstraints.",
				() -> {
					JPanel p = mig("wrap 4", "[right][grow,fill][right][grow,fill]", "");
					addressFields(p);
					return p;
				}).sizes()
				.step("wrap 2", lcTo(null, "wrap 2"))
				.step("wrap 2, preferred", StepContext::sizeToPreferred)
				.step("wrap 6", lcTo(null, "wrap 6"))
				.step("wrap 6, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-lc-flowy", G_LC, "Vertical flow",
				"'flowy, wrap 3' fills columns top to bottom: checkboxes and radio buttons of an options panel.",
				() -> {
					JPanel p = mig("flowy, wrap 3", "[][][]", "[][][]");
					for (String s : new String[] {"Show line numbers", "Highlight current line", "Show whitespace",
							"Soft wraps", "Show indent guides", "Show method separators"})
						p.add(new JCheckBox(s, s.length() % 2 == 0));
					ButtonGroup g = new ButtonGroup();
					for (String s : new String[] {"Spaces", "Tabs", "Smart tabs"}) {
						JRadioButton r = new JRadioButton(s, s.equals("Tabs"));
						g.add(r);
						p.add(r);
					}
					return p;
				}).sizes()
				.step("flowy, wrap 2, fill", lcTo(null, "flowy, wrap 2, fill"))
				.step("flowx, wrap 3", lcTo(null, "flowx, wrap 3")));

		l.add(new Scenario("layout-lc-fill", G_LC, "fill, fillx and filly",
				"A list with a label and a field, with 'fill', 'fillx', 'filly' and no fill at a larger size.",
				() -> {
					JPanel p = mig("fill", "[][grow]", "[][grow]");
					p.add(new JLabel("Search:"));
					p.add(new JTextField("layout", 15), "growx, wrap");
					p.add(new JScrollPane(list("GridLayout", "BorderLayout", "FlowLayout", "BoxLayout", "MigLayout",
							"GroupLayout", "SpringLayout", "CardLayout")), "span 2, grow");
					return p;
				}).step("preferred", StepContext::sizeToPreferred)
				.step("fill, large", ctx -> ctx.sizeRelative(1.6, 1.6))
				.step("fillx", lcTo(null, "fillx"))
				.step("filly", lcTo(null, "filly"))
				.step("no fill", lcTo(null, ""))
				.step("fill, small", ctx -> { lcTo(null, "fill").apply(ctx); ctx.sizeRelative(0.6, 0.6); }));

		String[] insets = {"insets 0", "insets dialog", "insets panel", "insets 10 20 30 40", "insets 1cm",
				"insets 5mm 10pt 0.5in 2sp", "insets n 20 n 20", "insets 3%", "insets rel unrel para 4lp",
				"insets 10px 10px", "ins 2"};
		Scenario sIns = new Scenario("layout-lc-insets", G_LC, "Insets in every form",
				"A small login form whose layout insets go through 0, dialog, panel, four values, units (cm, mm, pt, in, sp, %), "
						+ "null values and gap keywords.",
				() -> {
					JPanel p = mig("insets 0, wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("User name:"));
					p.add(new JTextField("ada", 14));
					p.add(new JLabel("Password:"));
					p.add(new JPasswordField("secret", 14));
					p.add(new JCheckBox("Remember me"), "skip");
					return p;
				});
		for (String in : insets)
			sIns.step(in, ctx -> { lcTo(null, in + ", wrap 2").apply(ctx); ctx.sizeToPreferred(); });
		sIns.step("insets dialog, wide", ctx -> { lcTo(null, "insets dialog, wrap 2").apply(ctx); ctx.sizeRelative(1.5, 1.3); });
		l.add(sIns);

		String[] gaps = {"gap 0", "gap 20 5", "gapx 2mm", "gapy 1cm", "gap rel unrel", "gap para", "gapx 10:20:30",
				"gap 0 0", "gap 5px 3sp", "gap 12lp"};
		Scenario sGap = new Scenario("layout-lc-gaps", G_LC, "Grid gaps",
				"A 3x3 grid of fields whose default gaps change: zero, pixel, mm, cm, related/unrelated/paragraph and bounded gaps.",
				() -> {
					JPanel p = mig("wrap 3", "[grow,fill][grow,fill][grow,fill]", "");
					String[] t = {"North", "Centre", "South", "Alpha", "Beta", "Gamma", "Red", "Green", "Blue"};
					for (String s : t)
						p.add(new JTextField(s, 6));
					return p;
				});
		for (String g : gaps)
			sGap.step(g, ctx -> { lcTo(null, g + ", wrap 3").apply(ctx); ctx.sizeToPreferred(); });
		sGap.step("gapx 10:20:30, wide", ctx -> { lcTo(null, "gapx 10:20:30, wrap 3").apply(ctx); ctx.sizeRelative(1.8, 1.4); });
		l.add(sGap);

		String[] aligns = {"align left top", "align center center", "align right bottom", "align 30% 70%",
				"align leading", "align trailing", "alignx center, aligny bottom", "ax right, ay center",
				"align 0.25al 0.75al", "aligny bottom, alignx 0.5al"};
		Scenario sAl = new Scenario("layout-lc-align", G_LC, "Aligning the whole grid",
				"A small form in a large panel, the grid aligned left/center/right/top/bottom, by percent, al units, leading and trailing.",
				() -> {
					JPanel p = mig("wrap 2", "[right][]", "");
					p.add(new JLabel("Host:"));
					p.add(new JTextField("example.org", 12));
					p.add(new JLabel("Port:"));
					p.add(new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1)));
					p.add(new JButton("Connect"), "skip, right");
					return p;
				}).step("preferred", StepContext::sizeToPreferred);
		for (String a : aligns)
			sAl.step(a, ctx -> { lcTo(null, a + ", wrap 2").apply(ctx); ctx.sizeRelative(2.0, 1.8); });
		sAl.step("align leading bottom, rtl", ctx -> { lcTo(null, "align leading bottom, wrap 2, rtl").apply(ctx); ctx.sizeRelative(2.0, 1.8); });
		l.add(sAl);

		l.add(new Scenario("layout-lc-nogrid", G_LC, "No grid: flowing rows",
				"'nogrid' makes every row a flow of its own: tags in rows of different lengths, then 'nogrid, flowy'.",
				() -> {
					JPanel p = mig("nogrid", "", "");
					p.add(new JLabel("Tags:"));
					for (String s : new String[] {"swing", "layout", "java"})
						p.add(new JToggleButton(s));
					p.add(new JButton("Add tag..."), "wrap");
					p.add(new JLabel("Assignee:"));
					p.add(new JComboBox<>(new String[] {"Nobody", "Grace Hopper", "Linus Torvalds"}), "wrap");
					p.add(new JTextField("A longer field that sets the width of the last row", 30), "growx");
					return p;
				}).sizes()
				.step("nogrid, flowy", lcTo(null, "nogrid, flowy"))
				.step("nogrid, flowy, preferred", StepContext::sizeToPreferred)
				.step("nogrid, fillx", lcTo(null, "nogrid, fillx")));

		l.add(new Scenario("layout-lc-novisualpadding", G_LC, "Visual padding on and off",
				"Two identical button bars, one with 'novisualpadding', plus a third with per-component 'vp' padding.",
				() -> {
					JPanel p = mig("wrap 1, fillx", "[grow,fill]", "");
					for (String lc : new String[] {"", "novisualpadding", "insets 0"}) {
						JPanel bar = mig(lc, "[][][grow][]", "");
						bar.setBorder(new TitledBorder(lc.isEmpty() ? "default" : lc));
						bar.add(new JComboBox<>(new String[] {"Debug", "Release"}));
						bar.add(new JButton("Build"));
						bar.add(new JTextField("filter", 8), lc.equals("insets 0") ? "growx, vp 2 2 2 2" : "growx");
						bar.add(new JSpinner(new SpinnerNumberModel(4, 1, 64, 1)), lc.equals("insets 0") ? "vp 0 3" : "");
						p.add(bar);
					}
					return p;
				}));

		l.add(new Scenario("layout-lc-nocache", G_LC, "nocache with changing texts",
				"A status form with 'nocache' whose texts change between steps, so nothing may be taken from a cache.",
				() -> {
					JPanel p = mig("nocache, wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("State:"));
					p.add(n("state", new JLabel("Idle")));
					p.add(new JLabel("Progress:"));
					JProgressBar pb = new JProgressBar(0, 100);
					pb.setValue(30);
					pb.setStringPainted(true);
					p.add(pb);
					p.add(new JLabel("Message:"));
					p.add(n("msg", new JTextField("Waiting", 10)));
					p.add(n("go", new JButton("Start")), "span, right");
					return p;
				}).sizes()
				.step("longer texts", ctx -> {
					text(ctx, "state", "Downloading package index from the mirror");
					text(ctx, "go", "Cancel the download");
				})
				.step("longer texts, preferred", StepContext::sizeToPreferred)
				.step("short texts", ctx -> { text(ctx, "state", "Done"); text(ctx, "go", "OK"); text(ctx, "msg", ""); })
				.step("short texts, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-lc-rtl", G_LC, "Right to left",
				"A form with 'rtl', then 'ltr', then no direction but the component orientation set to right-to-left.",
				() -> {
					JPanel p = mig("rtl, wrap 3", "[right][grow,fill][]", "");
					p.add(new JLabel("File:"));
					p.add(new JTextField("/home/ada/notes.txt", 14));
					p.add(new JButton("Browse..."));
					p.add(new JLabel("Encoding:"));
					p.add(new JComboBox<>(new String[] {"UTF-8", "ISO-8859-1", "UTF-16"}), "span 2, growx 0");
					p.add(new JCheckBox("Open read only"), "skip, span 2");
					return p;
				}).sizes()
				.step("ltr", lcTo(null, "ltr, wrap 3"))
				.step("righttoleft keyword", lcTo(null, "righttoleft, wrap 3"))
				.step("orientation right-to-left", ctx -> {
					lcTo(null, "wrap 3").apply(ctx);
					orient(ctx, null, ComponentOrientation.RIGHT_TO_LEFT);
				})
				.step("orientation, wide", ctx -> ctx.sizeRelative(1.5, 1.3))
				.step("orientation left-to-right", ctx -> orient(ctx, null, ComponentOrientation.LEFT_TO_RIGHT)));

		l.add(new Scenario("layout-lc-btt", G_LC, "Bottom to top",
				"Chat messages stacked with 'btt' so the last row sits at the bottom, then 'flowy, btt' and 'ttb'.",
				() -> {
					JPanel p = mig("btt, wrap 2, fill", "[][grow,fill]", "[]rel[][]");
					String[][] m = {{"Ada:", "Has the layout settled?"}, {"Grace:", "Not yet, the second pass changes it."},
							{"Ada:", "Then the cache keeps the first result."}};
					for (String[] r : m) {
						p.add(new JLabel(r[0]));
						p.add(new JLabel(r[1]));
					}
					p.add(new JTextField("Type a message", 20), "span, growx");
					return p;
				}).sizes()
				.step("flowy, btt", lcTo(null, "flowy, btt, wrap 4"))
				.step("bottomtotop, tall", ctx -> { lcTo(null, "bottomtotop, wrap 2, fill").apply(ctx); ctx.sizeRelative(1.2, 2.0); })
				.step("ttb", lcTo(null, "ttb, wrap 2, fill")));

		l.add(new Scenario("layout-lc-size", G_LC, "Layout width and height",
				"Layout constraints 'w' and 'h' with min:pref:max, and wmin/hmax, bounding the size of the whole grid.",
				() -> {
					JPanel p = mig("w 200:300:400, h 120:160:250, wrap 2", "[right][grow,fill]", "[][][grow,fill]");
					p.add(new JLabel("Title:"));
					p.add(new JTextField("Quarterly report"));
					p.add(new JLabel("Author:"));
					p.add(new JTextField("Grace Hopper"));
					p.add(new JLabel("Notes:"), "top");
					p.add(new JScrollPane(new JTextArea("Draft\nTo be reviewed", 3, 10)));
					return p;
				}).sizes()
				.step("width 500!, height 300", ctx -> { lcTo(null, "width 500!, height 300, wrap 2").apply(ctx); ctx.sizeToPreferred(); })
				.step("wmin 250, hmax 140", ctx -> { lcTo(null, "wmin 250, hmax 140, wrap 2").apply(ctx); ctx.sizeToPreferred(); })
				.step("wmin 250, small", ctx -> ctx.size(120, 80))
				.step("wmax 260, large", ctx -> { lcTo(null, "wmax 260, wrap 2").apply(ctx); ctx.size(700, 400); }));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Hidden components

	private static void hidden(List<Scenario> l)
	{
		for (int mode = 0; mode <= 3; mode++) {
			final int m = mode;
			l.add(new Scenario("layout-hidemode-" + m, G_HIDE, "Hide mode " + m,
					"A proxy form with 'hidemode " + m + "': the host field, then its label and a whole row, are hidden and shown again.",
					() -> {
						JPanel p = mig("hidemode " + m + ", wrap 3", "[right][grow,fill][]", "");
						p.add(new JCheckBox("Use a proxy", true), "span, wrap");
						p.add(n("hostLabel", new JLabel("Host:")));
						p.add(n("host", new JTextField("proxy.example.org", 14)));
						p.add(n("test", new JButton("Test")));
						p.add(n("portLabel", new JLabel("Port:")));
						p.add(n("port", new JSpinner(new SpinnerNumberModel(3128, 1, 65535, 1))), "growx 0");
						p.add(n("auth", new JCheckBox("Authenticate")));
						p.add(new JLabel("Bypass:"));
						p.add(new JTextField("localhost, 127.0.0.1", 14), "span 2");
						return p;
					}).step("preferred", StepContext::sizeToPreferred)
					.step("host field hidden", visible("host", false))
					.step("host field hidden, preferred", StepContext::sizeToPreferred)
					.step("port row hidden", ctx -> {
						visible("portLabel", false).apply(ctx);
						visible("port", false).apply(ctx);
						visible("auth", false).apply(ctx);
					})
					.step("port row hidden, preferred", StepContext::sizeToPreferred)
					.step("port row hidden, wide", ctx -> ctx.sizeRelative(1.5, 1.3))
					.step("everything shown", ctx -> {
						for (String s : new String[] {"host", "portLabel", "port", "auth"})
							visible(s, true).apply(ctx);
					})
					.step("everything shown, preferred", StepContext::sizeToPreferred));
		}

		l.add(new Scenario("layout-hidemode-per-component", G_HIDE, "Hide mode per component",
				"Hidden buttons in a toolbar row and fields in a vertical flow, each with its own 'hidemode' overriding the layout's.",
				() -> {
					JPanel p = mig("hidemode 0, wrap 1", "[grow,fill]", "");
					JPanel bar = n("bar", mig("insets 0", "[][][][][][grow]", ""));
					for (int i = 0; i < 4; i++)
						bar.add(n("b" + i, new JButton(new String[] {"Cut", "Copy", "Paste", "Delete"}[i])), "hidemode " + i);
					bar.add(n("b4", new JButton("Select all")), "hidemode 3, gapleft push");
					p.add(bar);
					JPanel col = n("col", mig("insets 0, flowy", "[grow,fill]", ""));
					for (int i = 0; i < 4; i++)
						col.add(n("c" + i, new JTextField("Hide mode " + i, 12)), "hidemode " + i);
					col.add(new JTextField("Always visible", 12));
					p.add(col);
					return p;
				}).step("preferred", StepContext::sizeToPreferred)
				.step("all hidden", ctx -> {
					for (int i = 0; i < 4; i++) {
						visible("b" + i, false).apply(ctx);
						visible("c" + i, false).apply(ctx);
					}
				})
				.step("all hidden, preferred", StepContext::sizeToPreferred)
				.step("modes 0 and 2 shown", ctx -> {
					for (int i : new int[] {0, 2}) {
						visible("b" + i, true).apply(ctx);
						visible("c" + i, true).apply(ctx);
					}
				})
				.step("narrow", ctx -> ctx.sizeRelative(0.6, 1.0)));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Columns and rows

	private static void columnsAndRows(List<Scenario> l)
	{
		l.add(new Scenario("layout-dc-units", G_DC, "Column sizes in every unit",
				"Seven columns sized in px, cm, mm, in, pt, sp and %, and rows in lp, n and pref, each with a field and its caption.",
				() -> {
					JPanel p = mig("wrap 7", "[100px,fill][2cm,fill][30mm,fill][1in,fill][72pt,fill][10sp,fill][10%,fill]",
							"[20lp][n:pref:n][pref]");
					String[] u = {"px", "cm", "mm", "in", "pt", "sp", "%"};
					for (String s : u)
						p.add(new JLabel(s, SwingConstants.CENTER));
					for (String s : u)
						p.add(new JTextField("1 " + s));
					for (String s : u)
						p.add(new JButton("Use " + s));
					return p;
				}));

		l.add(new Scenario("layout-dc-min-pref-max", G_DC, "Bounded column sizes",
				"Columns '[50:100:150]', '[pref!]', '[min]', '[n:200:n]', '[::80]' and '[60!]' at small, preferred and large sizes.",
				() -> {
					JPanel p = mig("wrap 6", "[50:100:150,fill][pref!,fill][min,fill][n:200:n,fill][::80,fill][60!,fill]", "");
					String[] t = {"Bounded", "Fixed to preferred", "Minimum", "Preferred 200", "Max 80", "Exactly 60"};
					for (String s : t)
						p.add(new JLabel(s));
					for (String s : t)
						p.add(new JTextField(s.toLowerCase(), 8));
					for (int i = 0; i < 6; i++)
						p.add(new JComboBox<>(new String[] {"Option " + (i + 1), "Something else"}));
					return p;
				}).sizes()
				.step("very small", ctx -> ctx.size(40, 30))
				.step("very large", ctx -> ctx.size(1600, 1000))
				.step("preferred again", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-dc-grow-weights", G_DC, "Grow weights",
				"Columns '[grow 1][grow 2][grow 3]' and rows growing 1:3 share extra space by weight; '[grow 0]' does not grow.",
				() -> {
					JPanel p = mig("fill, wrap 4", "[grow 0][grow 1,fill][grow 2,fill][grow 3,fill]", "[][grow 1,fill][grow 3,fill]");
					p.add(new JLabel("Weight"));
					p.add(new JLabel("1"));
					p.add(new JLabel("2"));
					p.add(new JLabel("3"));
					p.add(new JLabel("Row 1"));
					p.add(new JScrollPane(list("a", "b")));
					p.add(new JScrollPane(list("c", "d")));
					p.add(new JScrollPane(list("e", "f")));
					p.add(new JLabel("Row 3"));
					p.add(new JScrollPane(new JTextArea("weight 3", 2, 6)));
					p.add(new JScrollPane(new JTextArea("weight 3", 2, 6)));
					p.add(new JScrollPane(new JTextArea("weight 3", 2, 6)));
					return p;
				}).sizes()
				.step("very large", ctx -> ctx.size(1600, 1000))
				.step("very small", ctx -> ctx.size(40, 30)));

		l.add(new Scenario("layout-dc-grow-prio", G_DC, "Grow and shrink priorities",
				"'growprio 200' columns take all extra space first; 'shrinkprio' decides which columns give up space first.",
				() -> {
					JPanel p = mig("fill, wrap 3", "[growprio 200,grow,fill][grow,fill][growprio 50,grow 300,fill]", "");
					p.add(new JTextField("prio 200", 10));
					p.add(new JTextField("prio 100", 10));
					p.add(new JTextField("prio 50, weight 300", 10));
					JPanel q = mig("fill, wrap 3", "[100:200,shrinkprio 200,fill][100:200,fill][100:200,shp 50,fill]", "");
					q.add(new JButton("Shrinks first"));
					q.add(new JButton("Shrinks second"));
					q.add(new JButton("Shrinks last"));
					p.add(q, "span, growx");
					return p;
				}).sizes()
				.step("small", ctx -> ctx.sizeRelative(0.55, 1.0))
				.step("very large", ctx -> ctx.size(1600, 400)));

		l.add(new Scenario("layout-dc-shrink", G_DC, "Shrink weights",
				"Columns '[100:300, shrink 0]', '[shrink 50]' and '[shrink 100]' squeezed below their preferred width.",
				() -> {
					JPanel p = mig("wrap 3", "[100:300,shrink 0,fill][100:300,shrink 50,fill][100:300,shrink 100,fill]",
							"[][100:150,shrink 100]");
					p.add(new JButton("Never shrinks"));
					p.add(new JButton("Shrinks a little"));
					p.add(new JButton("Shrinks a lot"));
					p.add(new JScrollPane(list("Mercury", "Venus", "Earth", "Mars")), "grow");
					p.add(new JScrollPane(list("Jupiter", "Saturn")), "grow");
					p.add(new JScrollPane(list("Uranus", "Neptune")), "grow");
					return p;
				}).step("preferred", StepContext::sizeToPreferred)
				.step("80%", ctx -> ctx.sizeRelative(0.8, 0.8))
				.step("50%", ctx -> ctx.sizeRelative(0.5, 0.5))
				.step("very small", ctx -> ctx.size(40, 30))
				.step("preferred again", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-dc-sizegroup", G_DC, "Column and row size groups",
				"Columns 0 and 2 in size group 'a', 3 and 4 in 'b' ('sg'), and two rows in one row size group.",
				() -> {
					JPanel p = mig("wrap 5", "[sizegroup a,fill][][sg a,fill][sg b,fill][sg b,fill]", "[sg r][][sg r]");
					p.add(new JLabel("Short"));
					p.add(new JLabel("|"));
					p.add(new JLabel("A much longer caption"));
					p.add(new JButton("OK"));
					p.add(new JButton("Apply changes"));
					p.add(new JTextField("x", 3));
					p.add(new JLabel("|"));
					p.add(new JTextField("y", 3));
					p.add(new JCheckBox("Yes"));
					p.add(new JCheckBox("No"));
					p.add(new JScrollPane(new JTextArea("a tall\ncell\nhere", 3, 6)), "span 5, growx");
					return p;
				}));

		l.add(new Scenario("layout-dc-gaps", G_DC, "Gaps between columns and rows",
				"Push gaps '[]push[]', zero gaps '[]0[]', bounded gaps '10:20:50' and unit gaps, rows with 'para' and 'push'.",
				() -> {
					JPanel p = mig("fill, wrap 6", "[]push[]0[]10:20:50[]2mm[]unrel[]", "[]para[]push[]0[]");
					String[] row = {"Back", "Undo", "Redo", "Zoom", "Fit", "Help"};
					for (int r = 0; r < 4; r++)
						for (String s : row)
							p.add(r % 2 == 0 ? new JButton(s) : new JLabel(s));
					return p;
				}).sizes()
				.step("large", ctx -> ctx.size(1200, 600))
				.step("columns '[]0:push[]'", colsTo(null, "[]0:push[]0[]10:20:50[]2mm[]unrel:push[]"))
				.step("small", ctx -> ctx.sizeRelative(0.7, 0.7)));

		l.add(new Scenario("layout-dc-align", G_DC, "Column and row alignment",
				"Columns aligned left, center, right, leading, trailing and fill, crossed with rows aligned top, center, bottom, baseline and fill.",
				() -> {
					JPanel p = mig("fill, wrap 6", "[left,grow][center,grow][right,grow][leading,grow][trailing,grow][fill,grow]",
							"[top,grow][center,grow][bottom,grow][baseline,grow][fill,grow]");
					for (int r = 0; r < 5; r++)
						for (int c = 0; c < 6; c++) {
							JComponent comp = switch ((r + c) % 4) {
								case 0 -> new JButton("Button");
								case 1 -> new JLabel("Label");
								case 2 -> new JTextField("Field", 5);
								default -> new JCheckBox("Check");
							};
							p.add(comp);
						}
					return p;
				}).sizes()
				.step("right-to-left", ctx -> orient(ctx, null, ComponentOrientation.RIGHT_TO_LEFT))
				.step("large", ctx -> ctx.size(1000, 600)));

		l.add(new Scenario("layout-dc-row-baseline", G_DC, "Row baseline against top and bottom",
				"Rows aligned baseline, top, bottom and center, each holding a large label, a field, a combo and a text area.",
				() -> {
					JPanel p = mig("wrap 4", "[][grow,fill][][]", "[baseline][top][bottom][center][]");
					String[] a = {"baseline", "top", "bottom", "center", "default"};
					for (String s : a) {
						JLabel big = new JLabel(s);
						big.setFont(big.getFont().deriveFont(22f));
						p.add(big);
						p.add(new JTextField("aligned " + s, 10));
						p.add(new JComboBox<>(new String[] {"Small", "Medium", "Large"}));
						p.add(new JScrollPane(new JTextArea("two\nlines", 2, 6)));
					}
					return p;
				}).sizes()
				.step("rows with sizes", rowsTo(null, "[baseline,30:60][top,60][bottom,60][center,60][fill,60]"))
				.step("rows with sizes, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-dc-fill-and-nogrid", G_DC, "Column fill and nogrid rows",
				"A '[fill]' column next to an unfilled one, and a row marked 'nogrid' that flows instead of aligning to columns.",
				() -> {
					JPanel p = mig("wrap 3", "[fill][][grow,fill]", "[][nogrid][]");
					p.add(new JButton("Filled"));
					p.add(new JButton("Not filled"));
					p.add(new JTextField("grows", 8));
					p.add(new JButton("Flow"));
					p.add(new JButton("in"));
					p.add(new JButton("a nogrid row"));
					p.add(new JLabel("A long caption in the first column"));
					p.add(new JLabel("Short"));
					p.add(new JTextField("end", 6));
					return p;
				}));

		l.add(new Scenario("layout-dc-mutate", G_DC, "Changing column and row constraints",
				"A settings grid whose column and row constraints are replaced at run time with setColumnConstraints and setRowConstraints.",
				() -> {
					JPanel p = mig("wrap 3", "[right][grow,fill][]", "");
					p.add(new JLabel("Volume:"));
					p.add(new JSlider(0, 100, 70));
					p.add(new JLabel("70%"));
					p.add(new JLabel("Balance:"));
					p.add(new JSlider(-10, 10, 0));
					p.add(new JLabel("Centre"));
					p.add(new JLabel("Device:"));
					p.add(new JComboBox<>(new String[] {"Speakers", "Headphones"}));
					p.add(new JButton("Test"));
					return p;
				}).sizes()
				.step("columns '[left][200!][grow,right]'", colsTo(null, "[left][200!,fill][grow,right]"))
				.step("columns '[]0[]0[]'", colsTo(null, "[]0[fill]0[]"))
				.step("rows '[40!][]push[]'", rowsTo(null, "[40!][]push[]"))
				.step("rows changed, large", ctx -> ctx.sizeRelative(1.3, 1.8))
				.step("constraints reset", ctx -> { colsTo(null, "").apply(ctx); rowsTo(null, "").apply(ctx); ctx.sizeToPreferred(); }));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Component constraints

	private static void componentConstraints(List<Scenario> l)
	{
		l.add(new Scenario("layout-cc-span", G_CC, "Spanning cells",
				"'span 2', 'spanx', 'spany 2', 'span 2 2' and a bare 'span' that takes the rest of the row, in a calendar-like event form.",
				() -> {
					JPanel p = mig("wrap 4", "[right][grow,fill][right][grow,fill]", "");
					p.add(new JLabel("Subject:"));
					p.add(new JTextField("Design review", 20), "span");
					p.add(new JLabel("Start:"));
					p.add(new JSpinner(new SpinnerNumberModel(9, 0, 23, 1)));
					p.add(new JLabel("End:"));
					p.add(new JSpinner(new SpinnerNumberModel(10, 0, 23, 1)));
					p.add(new JLabel("Attendees:"), "top");
					p.add(new JScrollPane(list("Ada", "Grace", "Linus", "Margaret", "Barbara")), "span 2 2, grow");
					p.add(new JButton("Add..."), "spany 2, growy");
					p.add(new JLabel("Room:"));
					p.add(new JLabel("Location:"), "newline");
					p.add(new JComboBox<>(new String[] {"Room 101", "Atrium", "Online"}), "spanx 3");
					p.add(new JCheckBox("All day"), "skip, span 3");
					return p;
				}).sizes());

		l.add(new Scenario("layout-cc-split", G_CC, "Splitting cells",
				"'split 3' for a value with a unit and a button, 'split 2, flowy' for stacked buttons and 'split, right'.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Width:"));
					p.add(new JSpinner(new SpinnerNumberModel(800, 1, 9999, 1)), "split 3, growx");
					p.add(new JLabel("px"));
					p.add(new JButton("Reset"));
					p.add(new JLabel("Items:"), "top");
					p.add(new JScrollPane(list("One", "Two", "Three", "Four")), "split 2, grow");
					p.add(new JButton("Up"), "split 3, flowy, top, sg move");
					p.add(new JButton("Down"), "sg move");
					p.add(new JButton("Remove"), "sg move");
					p.add(new JButton("Cancel"), "span, split, right, tag cancel");
					p.add(new JButton("OK"), "tag ok");
					return p;
				}).sizes());

		l.add(new Scenario("layout-cc-skip-cell", G_CC, "Skip and absolute cells",
				"'skip', 'skip 2', 'cell x y' and 'cell x y w h' placing components out of flow order on a grid.",
				() -> {
					JPanel p = mig("", "[][][][]", "[][][][]");
					p.add(new JButton("A1"), "cell 0 0");
					p.add(new JButton("D4"), "cell 3 3");
					p.add(new JButton("B2 spans two"), "cell 1 1 2 1, growx");
					p.add(new JButton("C3"), "cell 2 2");
					p.add(new JLabel("After C3"), "skip");
					p.add(new JLabel("Skipped two"), "skip 2");
					p.add(new JTextField("cell 0 2 1 2", 8), "cell 0 2 1 2, grow");
					return p;
				}).sizes());

		l.add(new Scenario("layout-cc-wrap-newline", G_CC, "Wrap and newline with gaps",
				"'wrap', 'wrap 20', 'wrap para', 'newline', 'newline 30px' and 'newline push' starting rows with sized gaps.",
				() -> {
					JPanel p = mig("fillx", "[right][grow,fill]", "");
					p.add(new JLabel("First name:"));
					p.add(new JTextField("Ada", 12), "wrap");
					p.add(new JLabel("Last name:"));
					p.add(new JTextField("Lovelace", 12), "wrap 20");
					p.add(new JLabel("Email:"));
					p.add(new JTextField("ada@example.org", 12), "wrap para");
					p.add(new JLabel("Phone:"));
					p.add(new JTextField("+44 20 0000", 12));
					p.add(new JLabel("Notes:"), "newline 30px");
					p.add(new JTextField("Prefers mornings", 12));
					p.add(new JButton("Save"), "newline push, skip, right, growx 0");
					return p;
				}).sizes()
				.step("tall", ctx -> ctx.sizeRelative(1.0, 2.0)));

		l.add(new Scenario("layout-cc-push-grow", G_CC, "Push and grow with weights",
				"'push', 'pushx', 'pushy 2' make cells grow; 'grow', 'growx 50', 'growy 200' make components fill them by weight.",
				() -> {
					JPanel p = mig("", "", "");
					p.add(new JLabel("Find:"));
					p.add(new JTextField("needle", 10), "pushx, growx");
					p.add(new JButton("Next"), "wrap");
					p.add(new JScrollPane(new JTextArea("haystack\nhaystack\nhaystack", 3, 10)), "span 2, push, grow");
					p.add(new JButton("Tall"), "pushy 2, growy 200, wrap");
					p.add(new JLabel("Ratio:"));
					p.add(new JSlider(0, 10, 4), "growx 50, split 2");
					p.add(new JSlider(0, 10, 6), "growx 100");
					p.add(new JProgressBar(0, 10), "pushy, growy");
					return p;
				}).sizes()
				.step("very large", ctx -> ctx.size(1600, 1000))
				.step("very small", ctx -> ctx.size(40, 30))
				.step("preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-cc-sizes", G_CC, "Component widths and heights",
				"'w 120!', 'w 50:100:200', 'h 2cm', 'wmin', 'wmax', 'hmax', 'width 30%', 'w 2cm+10px' and 'w max(100px,20%)'.",
				() -> {
					JPanel p = mig("wrap 2, fillx", "[right][grow]", "");
					String[][] r = {{"w 120!", "Fixed"}, {"w 50:100:200", "Bounded"}, {"h 2cm", "Two centimetres tall"},
							{"wmin 180", "At least 180"}, {"wmax 90, growx", "At most 90"}, {"hmax 18", "Low"},
							{"width 30%", "A third"}, {"w 2cm+10px", "Expression"}, {"w max(100px,20%)", "Max of two"},
							{"w min(300px,40%), h 30:40:50", "Min of two"}, {"wmax 50%, growx", "Half at most"}};
					for (String[] s : r) {
						p.add(new JLabel(s[0]));
						p.add(new JButton(s[1]), s[0]);
					}
					return p;
				}).sizes()
				.step("very small", ctx -> ctx.size(40, 30))
				.step("very large", ctx -> ctx.size(1600, 1000)));

		l.add(new Scenario("layout-cc-sizegroup", G_CC, "Component size groups and end groups",
				"Buttons in 'sg' and 'sgx' groups share a width, fields in 'sgy' share a height, and 'egx' end groups align right edges.",
				() -> {
					JPanel p = mig("wrap 3", "[][grow][]", "");
					p.add(new JButton("New"), "sg btn");
					p.add(new JButton("Open recent project..."), "sg btn");
					p.add(new JButton("Close"), "sgx btn");
					p.add(new JScrollPane(new JTextArea("three\nlines\nhigh", 3, 8)), "sgy tall");
					p.add(new JTextField("as high", 8), "sgy tall");
					p.add(new JButton("Same"), "sgy tall");
					p.add(new JTextField("end group", 10), "egx e, span 2");
					p.add(new JLabel("x"));
					p.add(new JButton("Shorter"), "egx e");
					p.add(new JLabel("Default size group"), "sg");
					p.add(new JLabel("x"), "sg");
					return p;
				}).sizes());

		l.add(new Scenario("layout-cc-gaps", G_CC, "Gaps on components",
				"'gapleft', 'gapright', 'gaptop', 'gapbottom', 'gapbefore', 'gapafter', 'gap 10 20 5 5', 'gapx 2mm 1mm' and push gaps.",
				() -> {
					JPanel p = mig("wrap 3", "[][grow,fill][]", "");
					String[][] r = {{"gapleft 20", "Indented"}, {"gapright 30", "Right gap"}, {"gaptop 15", "Top gap"},
							{"gapbottom 15", "Bottom gap"}, {"gapbefore 2cm", "Before"}, {"gapafter 1cm", "After"},
							{"gap 10 20 5 5", "All four"}, {"gapx 2mm 1mm", "Horizontal"}, {"gapy unrel para", "Vertical"},
							{"gapleft push", "Pushed right"}, {"gap 0!", "Hard zero"}};
					for (String[] s : r) {
						p.add(new JLabel(s[0]));
						p.add(new JTextField(s[1], 8));
						p.add(new JButton("..."), s[0]);
					}
					return p;
				}).sizes()
				.step("right-to-left", ctx -> orient(ctx, null, ComponentOrientation.RIGHT_TO_LEFT)));

		l.add(new Scenario("layout-cc-align", G_CC, "Aligning components in cells",
				"In tall wide cells: alignx left/center/right/leading/trailing/label, aligny top/center/bottom/baseline, and 'align 30% 70%'.",
				() -> {
					JPanel p = mig("wrap 3, fill", "[grow,120::][grow,120::][grow,120::]", "[grow,50::][grow,50::][grow,50::][grow,50::]");
					String[] a = {"alignx left", "alignx center", "alignx right", "ax leading", "ax trailing", "al label",
							"aligny top", "aligny center", "aligny bottom", "aligny baseline", "align 30% 70%", "align right bottom"};
					for (int i = 0; i < a.length; i++)
						p.add(i % 3 == 1 ? new JLabel(a[i]) : new JButton(a[i]), a[i]);
					return p;
				}).sizes()
				.step("right-to-left", ctx -> orient(ctx, null, ComponentOrientation.RIGHT_TO_LEFT)));

		l.add(new Scenario("layout-cc-baseline-vs-top", G_CC, "Baseline against top alignment",
				"A row with a 24pt label, a field, a two-line text area, a combo and a check box, switched between baseline, top, bottom and center.",
				() -> {
					JPanel p = mig("", "[][grow,fill][][][]", "");
					JLabel title = n("title", new JLabel("Total"));
					title.setFont(title.getFont().deriveFont(24f));
					p.add(title);
					p.add(n("field", new JTextField("1,234.50", 8)));
					p.add(n("area", new JTextArea("Paid by card\nRef. 4711", 2, 10)));
					p.add(n("combo", new JComboBox<>(new String[] {"EUR", "USD", "GBP"})));
					p.add(n("check", new JCheckBox("Final")));
					return p;
				}).sizes()
				.step("aligny top", ctx -> { for (String s : new String[] {"title", "field", "area", "combo", "check"}) ccTo(s, "aligny top").apply(ctx); })
				.step("aligny bottom", ctx -> { for (String s : new String[] {"title", "field", "area", "combo", "check"}) ccTo(s, "aligny bottom").apply(ctx); })
				.step("aligny center", ctx -> { for (String s : new String[] {"title", "field", "area", "combo", "check"}) ccTo(s, "aligny center").apply(ctx); })
				.step("aligny baseline", ctx -> { for (String s : new String[] {"title", "field", "area", "combo", "check"}) ccTo(s, "aligny baseline").apply(ctx); })
				.step("mixed", ctx -> { ccTo("title", "aligny baseline").apply(ctx); ccTo("area", "top").apply(ctx); ccTo("combo", "aligny bottom").apply(ctx); })
				.step("tall", ctx -> ctx.sizeRelative(1.0, 2.0)));

		l.add(new Scenario("layout-cc-tag-buttons", G_CC, "Button order by tags",
				"Buttons tagged ok, cancel, help, help2, apply, yes, no, back, next, finish, left, right and other, reordered by the platform's button order.",
				() -> {
					JPanel p = mig("wrap 1, fillx", "[grow,fill]", "");
					p.add(new JLabel("Do you want to save the changes to \"notes.txt\" before closing?"));
					JPanel b1 = mig("insets 0, fillx", "[grow]", "");
					b1.add(new JButton("Help"), "tag help, split");
					b1.add(new JButton("Cancel"), "tag cancel");
					b1.add(new JButton("Apply"), "tag apply");
					b1.add(new JButton("OK"), "tag ok");
					p.add(b1);
					JPanel b2 = mig("insets 0, fillx", "[grow]", "");
					b2.add(new JButton("Yes"), "tag yes, split 4");
					b2.add(new JButton("Don't know"), "tag other");
					b2.add(new JButton("No"), "tag no");
					b2.add(new JButton("More..."), "tag help2");
					p.add(b2);
					JPanel b3 = mig("insets 0, fillx", "[grow]", "");
					b3.add(new JButton("Finish"), "tag finish, split");
					b3.add(new JButton("< Back"), "tag back");
					b3.add(new JButton("Next >"), "tag next");
					b3.add(new JButton("Left"), "tag left");
					b3.add(new JButton("Right"), "tag right");
					b3.add(new JButton("Cancel"), "tag cancel");
					p.add(b3);
					return p;
				}).sizes());

		l.add(new Scenario("layout-cc-pad", G_CC, "Padding after layout",
				"'pad' shifts and grows components after the grid is computed, with positive, negative and unit values.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					String[] r = {"pad 5 5 -5 -5", "pad 0 -10 0 10", "pad 2mm", "pad -3 0 3 0", "pad 0 0 0 20%"};
					for (String s : r) {
						p.add(new JLabel(s));
						p.add(new JTextField("padded", 10), s);
					}
					p.add(new JButton("Not padded"), "skip");
					return p;
				}));

		l.add(new Scenario("layout-cc-shrink", G_CC, "Shrinking components",
				"'shrink 0', 'shrinkx 50', 'shrinkprio 200', 'shp 50' and 'wmin 0' on fields in one row squeezed far below preferred.",
				() -> {
					JPanel p = mig("fillx", "", "");
					p.add(new JTextField("never shrinks", 12), "shrink 0");
					p.add(new JTextField("shrinkx 50", 12), "shrinkx 50");
					p.add(new JTextField("shrinkprio 200", 12), "shrinkprio 200");
					p.add(new JTextField("shp 50", 12), "shp 50");
					p.add(new JTextField("wmin 0", 12), "wmin 0, wrap");
					p.add(new JScrollPane(list("shrinky 0", "never", "loses", "height")), "span, split 2, shrinky 0, growx");
					p.add(new JScrollPane(list("shrinky 100", "gives", "up", "height")), "shrinky 100, growx");
					return p;
				}).sizes()
				.step("half", ctx -> ctx.sizeRelative(0.5, 0.6))
				.step("very small", ctx -> ctx.size(40, 30))
				.step("preferred again", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-cc-growprio", G_CC, "Grow priorities on components",
				"Fields in one column with 'growprio 200', 'gpx 50' and default priority, and 'gpy' on rows growing in a filled layout.",
				() -> {
					JPanel p = mig("fill", "", "");
					p.add(new JTextField("gp 200", 8), "growx, pushx, growprio 200");
					p.add(new JTextField("gp 100", 8), "growx, pushx");
					p.add(new JTextField("gpx 50", 8), "growx, pushx, gpx 50, wrap");
					p.add(new JScrollPane(list("gpy 150", "rows")), "span, split 2, grow, push, gpy 150");
					p.add(new JScrollPane(list("gpy 50", "rows")), "grow, push, gpy 50");
					return p;
				}).sizes()
				.step("very large", ctx -> ctx.size(1600, 1000)));

		l.add(new Scenario("layout-cc-flowy-cell", G_CC, "Vertical flow inside one cell",
				"A cell with 'flowy, split 3' in a horizontal layout: a label over a field over a hint, next to plain cells.",
				() -> {
					JPanel p = mig("", "[grow,fill][grow,fill][]", "");
					p.add(new JLabel("Name"), "flowy, split 3");
					p.add(new JTextField("Ada Lovelace", 12));
					JLabel hint = new JLabel("As on your passport");
					hint.setFont(hint.getFont().deriveFont(10f));
					p.add(hint);
					p.add(new JLabel("Born"), "flowy, split 2");
					p.add(new JFormattedTextField("1815-12-10"));
					p.add(new JButton("Check"), "aligny bottom");
					return p;
				}).sizes());
	}

	// ------------------------------------------------------------------------------------------------------------
	// Docking and absolute positions

	private static void docking(List<Scenario> l)
	{
		l.add(new Scenario("layout-dock-sides", G_DOCK, "Docking on all sides",
				"A window body with a toolbar docked north, status bar south, tree west, inspector east and a grid in the middle.",
				() -> {
					JPanel p = mig("fill", "[grow,fill]", "[grow,fill]");
					JPanel tools = mig("insets 2", "[][][]push[]", "");
					tools.add(new JButton("New"));
					tools.add(new JButton("Open"));
					tools.add(new JButton("Save"));
					tools.add(new JTextField("Search", 10));
					p.add(tools, "dock north");
					p.add(new JLabel("Ready"), "dock south");
					p.add(new JScrollPane(tree()), "dock west");
					JPanel insp = titled("Inspector", mig("wrap 2", "[][grow,fill]", ""));
					insp.add(new JLabel("Width:"));
					insp.add(new JSpinner(new SpinnerNumberModel(100, 0, 999, 1)));
					insp.add(new JLabel("Visible:"));
					insp.add(new JCheckBox());
					p.add(insp, "dock east");
					p.add(new JScrollPane(table()), "grow");
					return p;
				}).sizes()
				.step("very small", ctx -> ctx.size(40, 30))
				.step("very large", ctx -> ctx.size(1600, 1000)));

		l.add(new Scenario("layout-dock-stacked", G_DOCK, "Several docks on one side",
				"Three components docked north and two west, with the short forms 'north'/'west', gaps and 'dock center'.",
				() -> {
					JPanel p = mig("fill", "", "");
					p.add(new JLabel("Main menu would be here"), "north");
					p.add(new JSeparator(), "dock north, growx");
					p.add(new JTextField("breadcrumb / path / to / file", 20), "dock north, gapbottom 5");
					p.add(new JButton("Files"), "west, growy");
					p.add(new JButton("Outline"), "dock west, growy, gapright 10");
					p.add(new JProgressBar(0, 100), "dock south, h 8!");
					p.add(new JScrollPane(wrapping(LOREM, 20)), "dock center");
					return p;
				}).sizes());

		l.add(new Scenario("layout-pos-absolute", G_DOCK, "Absolute positions",
				"'pos 10 10', 'pos 50% 50%', 'pos x y x2 y2', 'pos 0.5al 1al', 'pos visual.x2-pref visual.y2-pref' and 'x'/'y2' bounds in grid.",
				() -> {
					JPanel p = mig("", "[grow]", "[grow]");
					p.add(new JLabel("Canvas"), "w 300, h 200");
					p.add(new JButton("Top left"), "pos 10 10");
					p.add(new JButton("Half way"), "pos 50% 50%");
					p.add(new JTextField("x y x2 y2"), "pos 20 60 160 90");
					p.add(new JLabel("Centred at the bottom"), "pos 0.5al 1al");
					p.add(new JButton("Corner"), "pos visual.x2-pref visual.y2-pref");
					p.add(new JCheckBox("x and y2"), "x 30, y2 container.h-30");
					p.add(new JLabel("Right half"), "pos container.w/2 n container.x2 20");
					return p;
				}).sizes()
				.step("very large", ctx -> ctx.size(1600, 1000)));

		l.add(new Scenario("layout-pos-links", G_DOCK, "Positions linked to other components",
				"Components positioned relative to others by id: 'pos b1.x2+rel b1.y', 'x b1.x, y b1.y2+10', group ids and 'x2' links.",
				() -> {
					JPanel p = mig("wrap 2", "[][]", "");
					p.add(new JLabel("Anchor:"));
					p.add(new JTextField("anchored field", 16), "id anchor");
					p.add(new JLabel("Second:"));
					p.add(new JTextField("second field", 16), "id grp.second");
					p.add(new JButton("Beside"), "id b1, pos anchor.x2+rel anchor.y");
					p.add(new JLabel("Linked to the anchor's x"), "x anchor.x-40, y second.y2+30");
					p.add(new JLabel("Right edge"), "x2 second.x2, y second.y2+rel");
					p.add(new JButton("Under beside"), "pos b1.x b1.y2+rel b1.x2 n");
					p.add(new JLabel("Group bounds"), "pos grp.x grp.y2+rel");
					return p;
				}).sizes()
				.step("anchor text longer", ctx -> {
					Component c = ((JPanel) ctx.root).getComponent(1);
					((JTextField) c).setColumns(24);
					((JComponent) c).revalidate();
				})
				.step("anchor text longer, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-cc-external", G_DOCK, "External components",
				"A component marked 'external' keeps the bounds the app gave it, and another component is linked to it.",
				() -> {
					JPanel p = mig("wrap 2", "[][grow,fill]", "");
					p.add(new JLabel("Name:"));
					p.add(new JTextField("layout", 12));
					JButton ext = new JButton("Placed by hand");
					ext.setBounds(40, 38, 140, 26);
					p.add(ext, "external, id ext");
					p.add(new JLabel("Follows the external button"), "pos ext.x2+10 ext.y");
					p.add(new JLabel("Notes:"), "newline 50");
					p.add(new JTextField("under it", 12));
					return p;
				}).sizes()
				.step("external moved", ctx -> {
					Component c = ((JPanel) ctx.root).getComponent(2);
					c.setBounds(150, 36, 180, 28);
					((JComponent) ctx.root).revalidate();
				}));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Content bias: heights that depend on widths

	private static void contentBias(List<Scenario> l)
	{
		l.add(new Scenario("layout-bias-wrapping-areas", G_BIAS, "Text areas that wrap",
				"Three line-wrapping text areas in a growing column; their heights follow the width they are given.",
				() -> {
					JPanel p = mig("wrap 2, fillx", "[right][grow,fill]", "[top]");
					p.add(new JLabel("Summary:"));
					p.add(wrapping("A short summary that wraps once it is narrow.", 0));
					p.add(new JLabel("Description:"));
					p.add(wrapping(LOREM, 30));
					p.add(new JLabel("Steps:"));
					p.add(wrapping("1. Open the dialog. 2. Make the window narrow. 3. See how the text wraps and the rows grow.", 20));
					p.add(new JButton("Submit"), "skip, right, growx 0");
					return p;
				}).sizes()
				.step("very narrow", ctx -> ctx.sizeRelative(0.35, 1.0))
				.step("very wide", ctx -> ctx.size(1600, 400))
				.step("preferred again", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-bias-nested-areas", G_BIAS, "Wrapping text in nested panels",
				"Two titled panels side by side, each a nested MigLayout with a wrapping text area and a field, sharing a row.",
				() -> {
					JPanel p = mig("fill", "[grow,fill][grow,fill]", "[grow,fill]");
					for (String t : new String[] {"Before", "After"}) {
						JPanel q = titled(t, mig("wrap 1, fillx", "[grow,fill]", ""));
						q.add(wrapping(t + ": " + LOREM, 15));
						q.add(new JTextField("Reviewed by Ada", 10));
						JPanel inner = mig("insets 0, wrap 2", "[][grow,fill]", "");
						inner.add(new JLabel("Note:"), "top");
						inner.add(wrapping("Wrapping inside a third level of nesting.", 0));
						q.add(inner);
						p.add(q);
					}
					return p;
				}).sizes()
				.step("very narrow", ctx -> ctx.sizeRelative(0.4, 1.0))
				.step("preferred again", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-bias-html-labels", G_BIAS, "Multi-line HTML labels",
				"HTML labels whose width is bounded with 'wmax 200', 'w 250', 'w 30%' and 'w 100:300', so their height follows the width.",
				() -> {
					JPanel p = mig("wrap 2, fillx", "[][grow,fill]", "[top]");
					String html = "<html>When this option is on, <b>every</b> file you save is also copied to the backup folder, "
							+ "which may take a while for large projects.</html>";
					String[] w = {"wmax 200", "w 250", "w 30%", "w 100:300, growx"};
					for (String s : w) {
						p.add(new JCheckBox(s));
						p.add(new JLabel(html), s);
					}
					return p;
				}).sizes()
				.step("very narrow", ctx -> ctx.sizeRelative(0.4, 1.0)));

		l.add(new Scenario("layout-bias-text-changes", G_BIAS, "Changing wrapped text",
				"A wrapping text area and an HTML label whose texts get longer and shorter, so their preferred heights change.",
				() -> {
					JPanel p = mig("wrap 1, fillx", "[grow,fill]", "");
					p.add(n("area", wrapping("Short text.", 25)));
					p.add(n("html", new JLabel("<html>One line.</html>")), "wmax 300");
					p.add(new JButton("Close"), "right, growx 0");
					return p;
				}).sizes()
				.step("longer texts", ctx -> {
					text(ctx, "area", LOREM + " " + LOREM);
					text(ctx, "html", "<html>" + LOREM + "</html>");
				})
				.step("longer texts, preferred", StepContext::sizeToPreferred)
				.step("narrow", ctx -> ctx.sizeRelative(0.6, 1.0))
				.step("short texts again", ctx -> { text(ctx, "area", "Short again."); text(ctx, "html", "<html>Short.</html>"); })
				.step("short texts, preferred", StepContext::sizeToPreferred));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Nesting

	private static void nesting(List<Scenario> l)
	{
		l.add(new Scenario("layout-nest-four-levels", G_NEST, "Four levels of nesting",
				"A MigLayout panel holding a panel holding a panel holding a baseline-aligned form row, each level with its own insets and grow.",
				() -> {
					JPanel l1 = mig("wrap 1, fill", "[grow,fill]", "[][grow,fill]");
					l1.add(new JLabel("Account settings"));
					JPanel l2 = n("l2", titled("Profile", mig("fill", "[][grow,fill]", "[grow,fill]")));
					l2.add(new JLabel(UIManager.getIcon("OptionPane.informationIcon")), "top");
					JPanel l3 = n("l3", mig("insets 0, wrap 1, fillx", "[grow,fill]", ""));
					for (String[] r : new String[][] {{"Display name:", "Ada"}, {"Email:", "ada@example.org"}}) {
						JPanel l4 = mig("insets 0", "[90!][grow,fill][]", "[baseline]");
						l4.add(new JLabel(r[0]));
						l4.add(new JTextField(r[1], 12));
						l4.add(new JButton("Edit"));
						l3.add(l4);
					}
					JPanel l4b = n("l4b", mig("insets 0", "[90!][][grow]", ""));
					l4b.add(n("big", new JLabel("Plan:")));
					l4b.add(new JComboBox<>(new String[] {"Free", "Pro", "Team"}));
					l4b.add(new JCheckBox("Renew automatically"));
					l3.add(l4b);
					l2.add(l3);
					l1.add(l2);
					return l1;
				}).sizes()
				.step("border on level 3", ctx -> {
					JPanel p = ctx.find("l3");
					p.setBorder(new EmptyBorder(10, 20, 10, 20));
					p.revalidate();
				})
				.step("bigger font on level 4", ctx -> font(ctx, "big", 20f))
				.step("changed, preferred", StepContext::sizeToPreferred)
				.step("very small", ctx -> ctx.size(40, 30)));

		l.add(new Scenario("layout-nest-scrollpane", G_NEST, "A form in a scroll pane",
				"A long MigLayout form inside a JScrollPane inside a MigLayout panel, made smaller than the form so it scrolls.",
				() -> {
					JPanel p = mig("fill, wrap 1", "[grow,fill]", "[][grow,fill][]");
					p.add(new JLabel("Preferences"));
					JPanel form = mig("wrap 2", "[right][grow,fill]", "");
					for (int i = 1; i <= 12; i++) {
						form.add(new JLabel("Setting " + i + ":"));
						form.add(i % 3 == 0 ? new JCheckBox("Enabled") : i % 3 == 1 ? new JTextField("value " + i, 10)
								: new JComboBox<>(new String[] {"Low", "Medium", "High"}));
					}
					p.add(new JScrollPane(form));
					p.add(new JButton("Apply"), "right, growx 0");
					return p;
				}).sizes()
				.step("small", ctx -> ctx.size(260, 200))
				.step("very small", ctx -> ctx.size(40, 30))
				.step("very large", ctx -> ctx.size(1600, 1000)));

		l.add(new Scenario("layout-nest-splitpane", G_NEST, "Forms in a split pane",
				"A JSplitPane with a list on the left and a MigLayout detail form on the right, its divider at a fixed position.",
				() -> {
					JPanel p = mig("fill, insets 0", "[grow,fill]", "[grow,fill]");
					JPanel left = mig("fill, wrap 1", "[grow,fill]", "[][grow,fill]");
					left.add(new JTextField("Filter", 8));
					left.add(new JScrollPane(list("Ada Lovelace", "Grace Hopper", "Alan Turing", "Margaret Hamilton")));
					JPanel right = mig("wrap 2", "[right][grow,fill]", "[][][][grow,fill]");
					right.add(new JLabel("Name:"));
					right.add(new JTextField("Grace Hopper", 12));
					right.add(new JLabel("Born:"));
					right.add(new JSpinner(new SpinnerNumberModel(1906, 1800, 2100, 1)));
					right.add(new JLabel("Rank:"));
					right.add(new JComboBox<>(new String[] {"Rear Admiral", "Commodore"}));
					right.add(new JLabel("Bio:"), "top");
					right.add(new JScrollPane(wrapping("Pioneer of computer programming, who invented one of the first linkers.", 15)));
					JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
					sp.setDividerLocation(180);
					sp.setResizeWeight(0.3);
					p.add(sp);
					return p;
				}).sizes()
				.step("very large", ctx -> ctx.size(1600, 1000)));

		l.add(new Scenario("layout-nest-tabs", G_NEST, "Forms in tabs",
				"A JTabbedPane whose tabs are MigLayout panels of different sizes; a step selects the second tab.",
				() -> {
					JPanel p = mig("fill", "[grow,fill]", "[grow,fill][]");
					JTabbedPane tabs = n("tabs", new JTabbedPane());
					JPanel general = mig("wrap 2", "[right][grow,fill]", "");
					general.add(new JLabel("Language:"));
					general.add(new JComboBox<>(new String[] {"English", "Deutsch", "Français"}));
					general.add(new JCheckBox("Check for updates"), "skip");
					tabs.addTab("General", general);
					JPanel editor = mig("wrap 3", "[][grow,fill][]", "[][][grow,fill]");
					editor.add(new JLabel("Font:"));
					editor.add(new JComboBox<>(new String[] {"Monospaced", "Serif"}));
					editor.add(new JSpinner(new SpinnerNumberModel(12, 6, 72, 1)));
					editor.add(new JLabel("Tab size:"));
					editor.add(new JSlider(1, 8, 4), "span 2");
					editor.add(new JScrollPane(table()), "span, grow");
					tabs.addTab("Editor", editor);
					p.add(tabs, "wrap");
					p.add(new JButton("Close"), "right, growx 0");
					return p;
				}).sizes()
				.step("second tab", ctx -> {
					JTabbedPane t = ctx.find("tabs");
					t.setSelectedIndex(1);
					t.revalidate();
				})
				.step("second tab, small", ctx -> ctx.sizeRelative(0.6, 0.7)));

		l.add(new Scenario("layout-nest-titled-groups", G_NEST, "Titled groups sharing sizes",
				"Titled-border panels in a grid whose label columns share a width through size groups across nested layouts.",
				() -> {
					JPanel p = mig("wrap 2, fill", "[grow,fill][grow,fill]", "[top]");
					String[][][] groups = {
							{{"Server:", "mail.example.org"}, {"Port:", "993"}, {"Security:", null}},
							{{"User name:", "ada"}, {"Password:", "****"}},
							{{"Check every:", "10 minutes"}},
							{{"Signature:", "Ada"}, {"Reply above quote:", null}}};
					String[] titles = {"Incoming", "Login", "Schedule", "Composing"};
					for (int i = 0; i < groups.length; i++) {
						JPanel g = titled(titles[i], mig("wrap 2", "[sg lbl,right][grow,fill]", ""));
						for (String[] r : groups[i]) {
							g.add(new JLabel(r[0]));
							g.add(r[1] == null ? new JCheckBox("Yes") : r[0].startsWith("Pass") ? new JPasswordField(r[1], 8)
									: new JTextField(r[1], 8));
						}
						p.add(g, i == 2 ? "span" : "");
					}
					return p;
				}));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Mutations

	private static void mutations(List<Scenario> l)
	{
		l.add(new Scenario("layout-mut-text", G_MUT, "Texts getting longer and shorter",
				"Labels, buttons, a check box and a field whose texts change; the grid must follow every change.",
				() -> {
					JPanel p = mig("wrap 3", "[right][grow,fill][]", "");
					p.add(n("l1", new JLabel("Name:")));
					p.add(n("f1", new JTextField("Ada", 8)));
					p.add(n("b1", new JButton("Edit")));
					p.add(n("l2", new JLabel("Status:")));
					p.add(n("c2", new JCheckBox("Active")), "span 2");
					return p;
				}).sizes()
				.step("longer", ctx -> {
					text(ctx, "l1", "Full legal name:");
					text(ctx, "b1", "Edit in a separate window...");
					text(ctx, "c2", "Active, and visible to everyone in the organisation");
				})
				.step("longer, preferred", StepContext::sizeToPreferred)
				.step("shorter", ctx -> { text(ctx, "l1", "N:"); text(ctx, "b1", "E"); text(ctx, "c2", "A"); text(ctx, "f1", ""); })
				.step("shorter, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-mut-font", G_MUT, "Fonts getting larger and smaller",
				"The fonts of a label, a field, a combo box and a button change size; baselines and row heights must follow.",
				() -> {
					JPanel p = mig("", "[][grow,fill][][]", "");
					p.add(n("lab", new JLabel("Zoom:")));
					p.add(n("fld", new JTextField("100 %", 6)));
					p.add(n("cmb", new JComboBox<>(new String[] {"Fit width", "Fit page"})));
					p.add(n("btn", new JButton("Apply")));
					return p;
				}).sizes()
				.step("20pt", ctx -> { for (String s : new String[] {"lab", "fld", "cmb", "btn"}) font(ctx, s, 20f); })
				.step("20pt, preferred", StepContext::sizeToPreferred)
				.step("mixed 9pt and 28pt", ctx -> { font(ctx, "lab", 28f); font(ctx, "fld", 9f); font(ctx, "cmb", 9f); font(ctx, "btn", 16f); })
				.step("mixed, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-mut-add-remove", G_MUT, "Adding and removing rows",
				"A list of filter rows: rows are added with constraints, one is removed from the middle and the panel is revalidated.",
				() -> {
					JPanel p = n("rows", mig("wrap 4", "[][grow,fill][grow,fill][]", ""));
					p.add(new JLabel("Where"));
					p.add(new JComboBox<>(new String[] {"Name", "Size", "Modified"}));
					p.add(new JTextField("*.java", 8));
					p.add(new JButton("-"));
					return p;
				}).sizes()
				.step("three rows added", ctx -> {
					JPanel p = ctx.find("rows");
					for (int i = 0; i < 3; i++) {
						p.add(n("and" + i, new JLabel("and")));
						p.add(new JComboBox<>(new String[] {"Size", "Name", "Modified"}));
						p.add(new JTextField("> " + (i + 1) + "0 kB", 8));
						p.add(new JButton("-"));
					}
					p.revalidate();
				})
				.step("three rows, preferred", StepContext::sizeToPreferred)
				.step("middle row removed", ctx -> {
					JPanel p = ctx.find("rows");
					int ix = java.util.Arrays.asList(p.getComponents()).indexOf(ctx.find("and1"));
					for (int i = 0; i < 4; i++)
						p.remove(ix);
					p.revalidate();
				})
				.step("insert at index 0 with 'wrap'", ctx -> {
					JPanel p = ctx.find("rows");
					p.add(new JLabel("Find files where all of these match:"), "span, wrap", 0);
					p.revalidate();
				})
				.step("changed, preferred", StepContext::sizeToPreferred)
				.step("all removed", ctx -> {
					JPanel p = ctx.find("rows");
					p.removeAll();
					p.add(new JLabel("No filters"));
					p.revalidate();
				})
				.step("empty, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-mut-constraints", G_MUT, "Changing component constraints",
				"setComponentConstraints moves a button between cells, changes span, alignment and gaps, then docks it.",
				() -> {
					JPanel p = mig("wrap 3", "[][grow,fill][]", "");
					p.add(new JLabel("Query:"));
					p.add(new JTextField("select * from layouts", 14));
					p.add(n("run", new JButton("Run")));
					p.add(new JLabel("Limit:"));
					p.add(new JSpinner(new SpinnerNumberModel(100, 1, 10000, 10)));
					p.add(new JCheckBox("Explain"));
					return p;
				}).sizes()
				.step("cell 0 2, span 3, right", ccTo("run", "cell 0 2, span 3, right"))
				.step("gapleft 40, aligny top", ccTo("run", "cell 1 2, gapleft 40, aligny top"))
				.step("w 200!, h 40!", ccTo("run", "cell 1 2, w 200!, h 40!"))
				.step("dock east", ccTo("run", "dock east"))
				.step("pos 0 0", ccTo("run", "pos 0 0"))
				.step("back to the flow", ccTo("run", ""))
				.step("layout constraints 'flowy'", lcTo(null, "flowy, wrap 3"))
				.step("changed, preferred", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-mut-rtl-toggle", G_MUT, "Switching orientation of a nested panel",
				"Only the nested button bar of a dialog is switched to right-to-left and back, with applyComponentOrientation.",
				() -> {
					JPanel p = mig("wrap 1, fill", "[grow,fill]", "[grow,fill][]");
					JPanel form = n("form", mig("wrap 2", "[right][grow,fill]", ""));
					form.add(new JLabel("Title:"));
					form.add(new JTextField("Invoice 2026-114", 12));
					form.add(new JLabel("Amount:"));
					form.add(new JFormattedTextField("1,250.00"));
					p.add(form);
					JPanel bar = n("bar", mig("insets 0", "[]push[][]", ""));
					bar.add(new JButton("Help"));
					bar.add(new JButton("OK"), "sg");
					bar.add(new JButton("Cancel"), "sg");
					p.add(bar);
					return p;
				}).sizes()
				.step("bar right-to-left", ctx -> orient(ctx, "bar", ComponentOrientation.RIGHT_TO_LEFT))
				.step("form right-to-left too", ctx -> orient(ctx, "form", ComponentOrientation.RIGHT_TO_LEFT))
				.step("all left-to-right", ctx -> orient(ctx, null, ComponentOrientation.LEFT_TO_RIGHT)));

		l.add(new Scenario("layout-mut-border", G_MUT, "Borders added and removed",
				"A nested panel gets an empty border, a titled border and none again; its insets change the layout around it.",
				() -> {
					JPanel p = mig("wrap 2", "[grow,fill][grow,fill]", "");
					JPanel q = n("q", mig("wrap 2", "[][grow,fill]", ""));
					q.add(new JLabel("Colour:"));
					q.add(new JComboBox<>(new String[] {"Red", "Green", "Blue"}));
					q.add(new JLabel("Opacity:"));
					q.add(new JSlider(0, 100, 80));
					p.add(q);
					p.add(new JScrollPane(list("Layer 1", "Layer 2", "Background")), "grow");
					return p;
				}).sizes()
				.step("empty border 20", ctx -> { JPanel q = ctx.find("q"); q.setBorder(new EmptyBorder(20, 20, 20, 20)); q.revalidate(); })
				.step("titled border", ctx -> { JPanel q = ctx.find("q"); q.setBorder(new TitledBorder("Fill")); q.revalidate(); })
				.step("titled border, preferred", StepContext::sizeToPreferred)
				.step("no border", ctx -> { JPanel q = ctx.find("q"); q.setBorder(null); q.revalidate(); }));

		l.add(new Scenario("layout-mut-extreme-sizes", G_MUT, "Extreme sizes",
				"A dense form taken far below its minimum, to a very large size and back, several times in a row.",
				() -> {
					JPanel p = mig("wrap 4, fill", "[right][grow,fill][right][grow,fill]", "[][][][grow,fill]");
					addressFields(p);
					p.add(new JScrollPane(table()), "span, grow");
					return p;
				}).step("preferred", StepContext::sizeToPreferred)
				.step("40x30", ctx -> ctx.size(40, 30))
				.step("1600x1000", ctx -> ctx.size(1600, 1000))
				.step("1x1", ctx -> ctx.size(1, 1))
				.step("preferred", StepContext::sizeToPreferred)
				.step("1600x30", ctx -> ctx.size(1600, 30))
				.step("40x1000", ctx -> ctx.size(40, 1000))
				.step("preferred again", StepContext::sizeToPreferred));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Combined

	private static void combined(List<Scenario> l)
	{
		l.add(new Scenario("layout-combo-settings-dialog", G_COMBO, "A settings dialog",
				"Docked header, tabs with nested forms, size-grouped tagged buttons, and an advanced section that is hidden and shown.",
				() -> {
					JPanel p = mig("fill, hidemode 3", "[grow,fill]", "[grow,fill]");
					JLabel head = new JLabel("Network settings");
					head.setFont(head.getFont().deriveFont(Font.BOLD, 18f));
					p.add(head, "dock north, gapbottom unrel");
					JPanel form = mig("wrap 3, hidemode 3", "[right][grow,fill][]", "");
					form.add(new JLabel("Mode:"));
					form.add(new JComboBox<>(new String[] {"Automatic", "Manual"}), "span 2");
					form.add(new JLabel("Address:"));
					form.add(new JTextField("192.168.1.20", 12));
					form.add(new JButton("Detect"));
					form.add(new JCheckBox("Show advanced options"), "skip, span 2");
					form.add(n("adv", titled("Advanced", mig("wrap 2", "[sg l,right][grow,fill]", ""))), "span, growx");
					JPanel adv = (JPanel) form.getComponent(form.getComponentCount() - 1);
					adv.add(new JLabel("MTU:"));
					adv.add(new JSpinner(new SpinnerNumberModel(1500, 576, 9000, 1)));
					adv.add(new JLabel("DNS:"));
					adv.add(new JTextField("1.1.1.1, 9.9.9.9", 12));
					JTabbedPane tabs = new JTabbedPane();
					tabs.addTab("IPv4", form);
					tabs.addTab("IPv6", mig("", "", ""));
					p.add(tabs);
					JPanel buttons = mig("insets 0, fillx", "[grow]", "");
					buttons.add(new JButton("Help"), "split 4, tag help, sg b");
					buttons.add(new JButton("Cancel"), "tag cancel, sg b");
					buttons.add(new JButton("Apply"), "tag apply, sg b");
					buttons.add(new JButton("OK"), "tag ok, sg b");
					p.add(buttons, "dock south, gaptop unrel");
					return p;
				}).sizes()
				.step("advanced hidden", visible("adv", false))
				.step("advanced hidden, preferred", StepContext::sizeToPreferred)
				.step("advanced shown, large font", ctx -> { visible("adv", true).apply(ctx); ctx.find("adv").setFont(new Font(Font.DIALOG, Font.PLAIN, 18)); })
				.step("very small", ctx -> ctx.size(40, 30))
				.step("preferred again", StepContext::sizeToPreferred));

		l.add(new Scenario("layout-combo-mail-compose", G_COMBO, "Composing a message",
				"Header fields with splits and links, a wrapping body that fills the rest, attachments flowing in a nogrid row.",
				() -> {
					JPanel p = mig("fill, wrap 2", "[right][grow,fill]", "[][][][grow,fill][]");
					p.add(new JLabel("From:"));
					p.add(new JComboBox<>(new String[] {"Ada <ada@example.org>", "Work <ada@work.example>"}));
					p.add(new JLabel("To:"));
					p.add(new JTextField("grace@example.org", 20), "split 2, id to");
					p.add(new JButton("Cc..."), "growx 0");
					p.add(new JLabel("Subject:"));
					p.add(new JTextField("Layout results", 20));
					p.add(new JScrollPane(wrapping(LOREM + "\n\n" + LOREM, 0)), "span, grow, h 100:200");
					JPanel att = mig("insets 0, nogrid", "", "");
					for (String s : new String[] {"report.pdf", "bounds.json", "screenshot-01.png", "screenshot-02.png"})
						att.add(new JToggleButton(s));
					p.add(att, "span, growx");
					p.add(new JLabel("draft saved"), "pos to.x to.y2+2");
					return p;
				}).sizes()
				.step("narrow", ctx -> ctx.sizeRelative(0.5, 1.0))
				.step("very large", ctx -> ctx.size(1600, 1000)));

		l.add(new Scenario("layout-combo-inspector", G_COMBO, "A property inspector",
				"A split pane with a tree and a nested inspector of sliders, spinners, progress bars, a toggle group and a separator.",
				() -> {
					JPanel insp = mig("wrap 3, fillx", "[right][grow,fill][40!]", "");
					insp.add(new JLabel("Transform"), "span, split 2");
					insp.add(new JSeparator(), "growx, gapleft rel");
					for (String s : new String[] {"X", "Y", "Rotation"}) {
						insp.add(new JLabel(s + ":"));
						insp.add(new JSlider(0, 360, s.length() * 30));
						insp.add(new JSpinner(new SpinnerNumberModel(s.length() * 30, 0, 360, 1)));
					}
					insp.add(new JLabel("Appearance"), "span, split 2, gaptop para");
					insp.add(new JSeparator(), "growx, gapleft rel");
					insp.add(new JLabel("Blend:"));
					JPanel tg = mig("insets 0, gap 0", "[sg t][sg t][sg t]", "");
					for (String s : new String[] {"Normal", "Multiply", "Screen"})
						tg.add(new JToggleButton(s, s.equals("Normal")), "growx");
					insp.add(tg, "span 2");
					insp.add(new JLabel("Load:"));
					JProgressBar pb = new JProgressBar(0, 100);
					pb.setValue(64);
					insp.add(pb, "span 2");
					insp.add(new JLabel("Name:"));
					insp.add(new JComboBox<>(new String[] {"Layer 1", "Shape", "Text"}) {{ setEditable(true); }}, "span 2");
					JPanel p = mig("fill, insets 0", "[grow,fill]", "[grow,fill]");
					JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree()), new JScrollPane(insp));
					sp.setDividerLocation(150);
					p.add(sp);
					return p;
				}).sizes()
				.step("very small", ctx -> ctx.size(40, 30))
				.step("very large", ctx -> ctx.size(1600, 1000)));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Helpers

	private static JPanel mig(String lc, String cols, String rows)
	{
		return new JPanel(new MigLayout(lc, cols, rows));
	}

	private static <T extends Component> T n(String name, T c)
	{
		c.setName(name);
		return c;
	}

	private static JList<String> list(String... items)
	{
		JList<String> l = new JList<>(items);
		l.setVisibleRowCount(Math.min(6, Math.max(2, items.length)));
		return l;
	}

	private static JTextArea wrapping(String text, int columns)
	{
		JTextArea a = new JTextArea(text);
		a.setLineWrap(true);
		a.setWrapStyleWord(true);
		if (columns > 0)
			a.setColumns(columns);
		return a;
	}

	private static JTable table()
	{
		JTable t = new JTable(new Object[][] {
				{"Ada Lovelace", "Analyst", 1815},
				{"Charles Babbage", "Engineer", 1791},
				{"Grace Hopper", "Admiral", 1906},
				{"Alan Turing", "Mathematician", 1912},
				{"Edsger Dijkstra", "Professor", 1930}},
				new Object[] {"Name", "Role", "Born"});
		t.setPreferredScrollableViewportSize(new Dimension(300, 90));
		return t;
	}

	private static JTree tree()
	{
		JTree t = new JTree();
		t.setVisibleRowCount(6);
		return t;
	}

	private static JPanel titled(String title, JPanel p)
	{
		p.setBorder(new TitledBorder(title));
		return p;
	}

	private static void addressFields(JPanel p)
	{
		String[][] f = {{"Street:", "221B Baker Street"}, {"City:", "London"}, {"Postcode:", "NW1 6XE"},
				{"Country:", "United Kingdom"}};
		for (String[] r : f) {
			p.add(new JLabel(r[0]));
			p.add(new JTextField(r[1], 10));
		}
		p.add(new JLabel("Phone:"));
		p.add(new JFormattedTextField("+44 20 7946 0000"));
		p.add(new JLabel("Type:"));
		p.add(new JComboBox<>(new String[] {"Home", "Work", "Other"}));
	}

	private static JPanel panelOf(StepContext ctx, String name)
	{
		return name == null ? (JPanel) ctx.root : ctx.find(name);
	}

	private static MigLayout layoutOf(Container c)
	{
		return (MigLayout) c.getLayout();
	}

	private static Scenario.Action lcTo(String panel, String lc)
	{
		return ctx -> {
			JPanel p = panelOf(ctx, panel);
			layoutOf(p).setLayoutConstraints(lc);
			p.revalidate();
		};
	}

	private static Scenario.Action colsTo(String panel, String cols)
	{
		return ctx -> {
			JPanel p = panelOf(ctx, panel);
			layoutOf(p).setColumnConstraints(cols);
			p.revalidate();
		};
	}

	private static Scenario.Action rowsTo(String panel, String rows)
	{
		return ctx -> {
			JPanel p = panelOf(ctx, panel);
			layoutOf(p).setRowConstraints(rows);
			p.revalidate();
		};
	}

	private static Scenario.Action ccTo(String comp, String cc)
	{
		return ctx -> {
			Component c = ctx.find(comp);
			JComponent parent = (JComponent) c.getParent();
			layoutOf(parent).setComponentConstraints(c, cc);
			parent.revalidate();
		};
	}

	private static Scenario.Action visible(String comp, boolean v)
	{
		return ctx -> {
			Component c = ctx.find(comp);
			c.setVisible(v);
			((JComponent) c.getParent()).revalidate();
		};
	}

	private static void text(StepContext ctx, String comp, String text)
	{
		Component c = ctx.find(comp);
		if (c instanceof JLabel lb)
			lb.setText(text);
		else if (c instanceof AbstractButton b)
			b.setText(text);
		else if (c instanceof JTextComponent t)
			t.setText(text);
		else
			throw new IllegalArgumentException(comp + " has no text");
		((JComponent) c).revalidate();
	}

	private static void font(StepContext ctx, String comp, float size)
	{
		Component c = ctx.find(comp);
		c.setFont(c.getFont().deriveFont(size));
		((JComponent) c).revalidate();
	}

	private static void orient(StepContext ctx, String panel, ComponentOrientation o)
	{
		JPanel p = panelOf(ctx, panel);
		p.applyComponentOrientation(o);
		p.revalidate();
	}

	private ScenariosLayout() {}
}
