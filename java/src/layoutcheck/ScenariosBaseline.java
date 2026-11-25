package layoutcheck;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.text.ParseException;
import java.util.List;

/**
 * Scenarios about baseline alignment, written to catch every way a component's baseline can depend on more than the
 * width and height it is asked for: HTML views that keep the layout of the previous request (on the component itself,
 * or on a renderer, an editor or a border label that the component asks in turn), state such as the size a scroll
 * pane's view was last given, and mutations that do not revalidate, followed by a layout that reuses MigLayout's grid.
 */
final class ScenariosBaseline
{
	private static final String G_DIRECT = "Baseline: HTML direct";
	private static final String G_INDIRECT = "Baseline: HTML indirect";
	private static final String G_STATE = "Baseline: state and scroll panes";
	private static final String G_FONTS = "Baseline: mixed fonts";
	private static final String G_MUT = "Baseline: mutations";
	private static final String G_NEST = "Baseline: nesting";

	static void add(List<Scenario> l)
	{
		htmlDirect(l);
		htmlIndirect(l);
		stateAndScrollPanes(l);
		mixedFonts(l);
		nesting(l);
		mutations(l);
	}

	// ------------------------------------------------------------------------------------------------------------
	// HTML directly on the component MigLayout asks

	private static void htmlDirect(List<Scenario> l)
	{
		l.add(new Scenario("baseline-html-label-single-line", G_DIRECT, "Single-line HTML captions",
				"Captions with bold and italic HTML next to text fields; each caption shares the field's baseline.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("<html><b>First</b> name:</html>"));
					p.add(new JTextField("Grace", 18));
					p.add(new JLabel("<html>Last <i>name</i>:</html>"));
					p.add(new JTextField("Hopper", 18));
					p.add(new JLabel("<html>E-mail <font color='#888888'>(work)</font>:</html>"));
					p.add(new JTextField("grace.hopper@navy.example", 18));
					p.add(new JLabel("<html><u>P</u>hone:</html>"));
					p.add(new JFormattedTextField("+1 555 0100"));
					return p;
				}));

		l.add(new Scenario("baseline-html-label-multi-line", G_DIRECT, "Multi-line HTML captions",
				"Captions broken with <br> and <p>: the first line of each caption sits on the baseline of its field.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline][baseline][baseline]");
					p.add(new JLabel("<html>Shipping<br>address:</html>"));
					p.add(new JTextField("1 Infinite Loop", 20));
					p.add(new JLabel("<html><p>Delivery</p><p><small>preferred time</small></p></html>"));
					p.add(new JComboBox<>(new String[] {"Morning", "Afternoon", "Evening"}));
					p.add(new JLabel("<html>Gift<br>message<br><i>(optional)</i>:</html>"));
					p.add(new JTextField("Happy birthday!", 20));
					return p;
				}));

		l.add(new Scenario("baseline-html-css-width", G_DIRECT, "HTML options that wrap at a CSS width",
				"Checkboxes and radio buttons whose long explanation sits in a body of fixed CSS width and wraps into "
						+ "several lines; their first line aligns with the caption beside them.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Backups:"));
					p.add(new JCheckBox("<html><body style='width:150px'>Copy changed files to the <b>backup drive</b> "
							+ "every hour while the computer is plugged in.</body></html>", true));
					p.add(new JLabel("Sync:"));
					p.add(new JCheckBox("<html><body style='width:210px'>Keep documents, photos and <i>desktop</i> "
							+ "folders the same on every device you sign in to.</body></html>"));
					p.add(new JLabel("Reports:"));
					p.add(new JRadioButton("<html><body style='width:120px'>Send anonymous crash reports.</body></html>"));
					return p;
				}));

		l.add(new Scenario("baseline-html-wmax", G_DIRECT, "HTML options limited by wmax",
				"Options longer than their column: 'wmax 140' makes MigLayout give them less than their preferred "
						+ "width, so the HTML wraps when painted while the baseline is asked at the preferred size. The "
						+ "HTML captions have no baseline in MigLayout and are centred instead.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("<html>Name of the <b>project</b>:</html>"), "wmax 120");
					p.add(new JTextField("Apollo guidance", 16));
					p.add(new JLabel("Visibility:"));
					p.add(new JCheckBox("<html>Show the project to <b>everyone</b> in the organisation</html>"), "wmax 140");
					p.add(new JLabel("Merging:"));
					p.add(new JRadioButton("<html>Allow <i>squash</i> merges of pull requests</html>", true), "wmax 140");
					p.add(new JLabel("Danger zone:"));
					p.add(new JButton("<html>Archive this <b>repository</b> now</html>"), "wmax 140");
					return p;
				}));

		l.add(new Scenario("baseline-html-label-big-fonts", G_DIRECT, "HTML with larger fonts inside",
				"<h2>, <big> and <font size=+2> make the first line of the caption taller than the field's text.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("<html><h2 style='margin:0'>Total</h2></html>"));
					p.add(new JTextField("1,280.00 EUR", 12));
					p.add(new JLabel("<html><big>Tax</big> rate:</html>"));
					p.add(new JTextField("20 %", 12));
					p.add(new JLabel("<html><font size=+2>D</font>iscount:</html>"));
					p.add(new JTextField("5 %", 12));
					p.add(new JLabel("<html><small>Rounding</small><br><font size=+1>mode:</font></html>"));
					p.add(new JComboBox<>(new String[] {"Half up", "Half even", "Down"}));
					return p;
				}));

		l.add(new Scenario("baseline-html-buttons", G_DIRECT, "HTML buttons in a toolbar row",
				"Buttons and toggle buttons with HTML text, single and multi-line, in one baseline row with a caption.",
				() -> {
					JPanel p = mig("", "[][][][][]", "[baseline]");
					p.add(new JLabel("Selection:"));
					p.add(new JButton("<html><b>Copy</b></html>"));
					p.add(new JButton("<html>Paste<br><small>as plain text</small></html>"));
					p.add(new JToggleButton("<html><i>Wrap</i> lines</html>", true));
					p.add(new JToggleButton("<html><font size=+1>A</font>a</html>"));
					return p;
				}));

		l.add(new Scenario("baseline-html-checks-and-radios", G_DIRECT, "HTML checkboxes and radio buttons",
				"Settings whose options carry an explanation on a second line, beside plain captions.",
				() -> {
					JPanel p = mig("wrap 2", "[right][]", "");
					p.add(new JLabel("Privacy:"));
					p.add(new JCheckBox("<html>Block third-party cookies<br><small>Some sites may not work</small></html>", true));
					p.add(new JLabel("Downloads:"));
					p.add(new JCheckBox("<html>Ask <b>where</b> to save each file</html>"));
					p.add(new JLabel("Startup:"));
					ButtonGroup g = new ButtonGroup();
					JRadioButton a = new JRadioButton("<html>Open the <i>New Tab</i> page</html>", true);
					JRadioButton b = new JRadioButton("<html>Continue where you left off<br><small>Restores all windows</small></html>");
					g.add(a);
					g.add(b);
					p.add(a, "split 2, flowy");
					p.add(b);
					return p;
				}));

		l.add(new Scenario("baseline-html-icons-text-positions", G_DIRECT, "HTML captions with icons around them",
				"Labels with tall and short icons, text above, below, before and after the icon, and different gaps.",
				() -> {
					JPanel p = mig("wrap 3", "[][][]", "");
					p.add(new JLabel("Status:"));
					p.add(iconLabel("<html><b>Online</b></html>", swatch(16, 40, new Color(0x4caf50)),
							SwingConstants.CENTER, SwingConstants.TRAILING, 4));
					p.add(new JTextField("since 09:12", 10));
					p.add(new JLabel("Owner:"));
					p.add(iconLabel("<html>Ada<br><small>admin</small></html>", swatch(32, 32, new Color(0x2196f3)),
							SwingConstants.TOP, SwingConstants.TRAILING, 10));
					p.add(new JTextField("ada@example.org", 10));
					p.add(new JLabel("Queue:"));
					p.add(iconLabel("<html><i>3 jobs</i></html>", swatch(24, 24, new Color(0xff9800)),
							SwingConstants.BOTTOM, SwingConstants.CENTER, 2));
					p.add(new JTextField("printing", 10));
					p.add(new JLabel("Disk:"));
					p.add(iconLabel("<html>82 % <b>used</b></html>", swatch(12, 12, new Color(0x9c27b0)),
							SwingConstants.TOP, SwingConstants.LEADING, 0));
					p.add(new JTextField("/home", 10));
					return p;
				}));

		l.add(new Scenario("baseline-html-buttons-with-icons", G_DIRECT, "HTML buttons with tall icons",
				"Buttons whose icons are taller than their text, text placed beside, above and below the icon.",
				() -> {
					JPanel p = mig("", "[][][][]", "[baseline]");
					p.add(new JLabel("Export as:"));
					JButton pdf = new JButton("<html><b>PDF</b><br>document</html>", swatch(20, 36, new Color(0xe53935)));
					JButton png = new JButton("<html>PNG <i>image</i></html>", swatch(28, 28, new Color(0x43a047)));
					png.setVerticalTextPosition(SwingConstants.BOTTOM);
					png.setHorizontalTextPosition(SwingConstants.CENTER);
					JCheckBox open = new JCheckBox("<html>Open<br>afterwards</html>", swatch(14, 30, new Color(0x1e88e5)));
					open.setVerticalTextPosition(SwingConstants.TOP);
					open.setIconTextGap(12);
					p.add(pdf);
					p.add(png);
					p.add(open);
					return p;
				}));

		l.add(new Scenario("baseline-html-grow-cells", G_DIRECT, "HTML captions stretched by growx",
				"HTML labels that fill a growing column: they are painted wider than the preferred size at which "
						+ "MigLayout asks their baseline.",
				() -> {
					JPanel p = mig("wrap 3", "[grow,fill][grow,fill][]", "");
					p.add(new JLabel("<html>Monthly <b>budget</b> for the marketing team</html>"));
					p.add(new JTextField("12,000", 8));
					p.add(new JButton("Edit"));
					p.add(new JLabel("<html>Spent <i>so far</i></html>"), "growx");
					p.add(new JTextField("7,430", 8));
					p.add(new JButton("Details"));
					p.add(new JLabel("<html>Remaining<br><small>until 30 June</small></html>"));
					p.add(new JTextField("4,570", 8));
					p.add(new JButton("Plan"));
					return p;
				}));

		l.add(new Scenario("baseline-html-shrinking-options", G_DIRECT, "HTML options that shrink",
				"A form whose option column may shrink to the longest word of each HTML checkbox; the narrow steps "
						+ "make them wrap while the baseline row with the spinner stays.",
				() -> {
					JPanel p = mig("wrap 2", "[shrink 100][grow,fill,shrink 0]", "[baseline]");
					p.add(new JCheckBox("<html>Number of <b>retries</b> before giving up</html>", true));
					p.add(new JSpinner(new SpinnerNumberModel(3, 0, 10, 1)), "w 80!");
					p.add(new JCheckBox("<html>Time between <i>retries</i> in seconds</html>"));
					p.add(new JSpinner(new SpinnerNumberModel(30, 0, 600, 5)), "w 80!");
					p.add(new JToggleButton("<html>Report <b>failures</b> to the administrator</html>"));
					p.add(new JTextField("root", 6), "w 80!");
					return p;
				}).sizes()
				.step("very narrow (45%)", ctx -> ctx.sizeRelative(0.45, 1.2))
				.step("very wide (250%)", ctx -> ctx.sizeRelative(2.5, 1.0))
				.step("narrow (60%)", ctx -> ctx.sizeRelative(0.6, 1.1))
				.step("preferred size again (2)", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-html-vertical-alignment", G_DIRECT, "HTML in tall fixed-height cells",
				"Captions given 'h 48!' with top, center and bottom vertical alignment: at that height the "
						+ "alignment decides where the baseline is.",
				() -> {
					JPanel p = mig("wrap 4", "[][][][grow,fill]", "[baseline]");
					p.add(valignLabel("<html><b>Top</b> aligned</html>", SwingConstants.TOP), "h 48!");
					p.add(valignLabel("<html>Centered<br>two lines</html>", SwingConstants.CENTER), "h 48!");
					p.add(valignLabel("<html><i>Bottom</i></html>", SwingConstants.BOTTOM), "h 48!");
					p.add(new JTextField("value", 10));
					p.add(valignLabel("Plain top", SwingConstants.TOP), "h 40!");
					p.add(valignLabel("Plain center", SwingConstants.CENTER), "h 40!");
					p.add(valignLabel("Plain bottom", SwingConstants.BOTTOM), "h 40!");
					p.add(new JTextField("value", 10));
					return p;
				}));

		l.add(new Scenario("baseline-html-shared-text", G_DIRECT, "Many controls with the same HTML",
				"Rows of a table-like form whose captions and buttons repeat the same HTML text.",
				() -> {
					JPanel p = mig("wrap 3", "[right][grow,fill][]", "");
					for (int i = 1; i <= 4; i++) {
						p.add(new JLabel("<html>Reviewer <b>#" + i + "</b>:</html>"));
						p.add(new JTextField(i == 2 ? "Linus" : "", 12));
						p.add(new JButton("<html><u>R</u>emove</html>"));
					}
					return p;
				}));
	}

	// ------------------------------------------------------------------------------------------------------------
	// HTML on a renderer, editor or border label of the component MigLayout asks

	private static void htmlIndirect(List<Scenario> l)
	{
		l.add(new Scenario("baseline-combo-html-items", G_INDIRECT, "Combo boxes with HTML items",
				"Non-editable combo boxes whose selected (first) item is HTML, painted by the default renderer label.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Priority:"));
					p.add(new JComboBox<>(new String[] {"<html><b>High</b> <font color='#c62828'>(urgent)</font></html>",
							"<html>Normal</html>", "<html><i>Low</i></html>"}));
					p.add(new JLabel("Assignee:"));
					p.add(new JComboBox<>(new String[] {"<html>Margaret <b>Hamilton</b></html>", "<html>Katherine Johnson</html>"}));
					p.add(new JLabel("Due:"));
					p.add(new JComboBox<>(new String[] {"<html>Friday<br><small>end of sprint</small></html>", "Monday"}));
					return p;
				}));

		l.add(new Scenario("baseline-combo-html-fixed-narrow", G_INDIRECT, "Narrow combo boxes with long HTML items",
				"Combo boxes held narrower than their items ('w 110!'), so the renderer's HTML wraps when the baseline "
						+ "is asked and when the selected item is painted.",
				() -> {
					JPanel p = mig("wrap 2", "[right][]", "");
					p.add(new JLabel("Template:"));
					p.add(new JComboBox<>(new String[] {"<html>Quarterly <b>report</b> with charts and tables</html>",
							"Letter"}), "w 110!");
					p.add(new JLabel("Paper:"));
					p.add(new JComboBox<>(new String[] {"<html><i>A4</i> portrait, 210 by 297 millimetres</html>",
							"Letter"}), "w 130!");
					p.add(new JLabel("Printer:"));
					p.add(new JTextField("Office laser", 10));
					return p;
				}));

		l.add(new Scenario("baseline-combo-html-wmin", G_INDIRECT, "HTML combo boxes allowed to shrink",
				"Combo boxes with 'wmin 60': MigLayout asks their baseline at the minimum and at the preferred width, "
						+ "both through the same renderer label.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Language:"));
					p.add(new JComboBox<>(new String[] {"<html><b>English</b> (United Kingdom)</html>", "Deutsch"}), "wmin 60");
					p.add(new JLabel("Time zone:"));
					p.add(new JComboBox<>(new String[] {"<html>Central European <i>Summer</i> Time</html>", "UTC"}), "wmin 60");
					p.add(new JLabel("Format:"));
					p.add(new JComboBox<>(new String[] {"<html>24 hours</html>"}), "wmin 60");
					return p;
				}).sizes()
				.step("very narrow (40%)", ctx -> ctx.sizeRelative(0.4, 1.0))
				.step("preferred size again (2)", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-combo-html-prototype", G_INDIRECT, "HTML prototype display value",
				"Combo boxes sized and baseline-aligned by an HTML prototype display value instead of their items.",
				() -> {
					JPanel p = mig("wrap 2", "[right][]", "");
					p.add(new JLabel("Country:"));
					JComboBox<String> c1 = new JComboBox<>(new String[] {"Austria", "Belgium"});
					c1.setPrototypeDisplayValue("<html><b>United Kingdom of Great Britain</b></html>");
					p.add(c1);
					p.add(new JLabel("Region:"));
					JComboBox<String> c2 = new JComboBox<>(new String[] {"<html><i>Tyrol</i></html>", "Vienna"});
					c2.setPrototypeDisplayValue("<html>Some region<br>with two lines</html>");
					p.add(c2);
					p.add(new JLabel("City:"));
					JComboBox<String> c3 = new JComboBox<>(new String[] {"Innsbruck", "Graz"});
					c3.setPrototypeDisplayValue("<html>Wiener Neustadt</html>");
					p.add(c3, "wmin 50");
					return p;
				}).sizes()
				.step("very narrow (40%)", ctx -> ctx.sizeRelative(0.4, 1.0))
				.step("preferred size again (2)", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-combo-shared-html-renderer", G_INDIRECT, "Custom renderer that formats HTML",
				"A renderer that turns plain items into HTML on one shared label, as many applications do for a "
						+ "name and a grey detail.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Font:"));
					JComboBox<String> fonts = new JComboBox<>(new String[] {"Serif|Times-like", "SansSerif|Helvetica-like",
							"Monospaced|Courier-like"});
					fonts.setRenderer(new HtmlDetailRenderer());
					p.add(fonts, "wmin 70");
					p.add(new JLabel("Size:"));
					JComboBox<String> sizes = new JComboBox<>(new String[] {"12|points", "14|points", "18|points"});
					sizes.setRenderer(new HtmlDetailRenderer());
					p.add(sizes);
					p.add(new JLabel("Sample:"));
					p.add(new JTextField("The quick brown fox", 14));
					return p;
				}).sizes()
				.step("very narrow (45%)", ctx -> ctx.sizeRelative(0.45, 1.0))
				.step("preferred size again (2)", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-combo-fresh-label-renderer", G_INDIRECT, "Renderer with a new label per cell",
				"A control case: the renderer makes a new HTML label every time, so no view is reused between requests.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Account:"));
					JComboBox<String> c = new JComboBox<>(new String[] {"Checking", "Savings"});
					c.setRenderer((list, value, index, sel, focus) -> new JLabel("<html><b>" + value + "</b> account</html>"));
					p.add(c, "wmin 60");
					p.add(new JLabel("Amount:"));
					p.add(new JTextField("250.00", 10));
					return p;
				}).sizes()
				.step("very narrow (40%)", ctx -> ctx.sizeRelative(0.4, 1.0)));

		l.add(new Scenario("baseline-combo-editable", G_INDIRECT, "Editable combo boxes",
				"Editable combo boxes take their baseline from the editor's text field; one has an HTML item list.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Search:"));
					JComboBox<String> s = new JComboBox<>(new String[] {"layout manager", "baseline"});
					s.setEditable(true);
					p.add(s);
					p.add(new JLabel("<html><b>Replace</b>:</html>"));
					JComboBox<String> r = new JComboBox<>(new String[] {"<html><i>grid</i></html>", "row"});
					r.setEditable(true);
					p.add(r, "wmin 50");
					return p;
				}));

		l.add(new Scenario("baseline-spinner-html-editor", G_INDIRECT, "Spinners with HTML label editors",
				"Spinners whose editor is an HTML label, so the spinner's baseline comes from that label's view.",
				() -> {
					JPanel p = mig("wrap 2", "[right][]", "");
					p.add(new JLabel("Copies:"));
					p.add(htmlSpinner("<html><b>%d</b> copies</html>", 2));
					p.add(new JLabel("Pages per sheet:"));
					p.add(htmlSpinner("<html>%d pages<br><small>per sheet</small></html>", 4));
					p.add(new JLabel("Margin:"));
					p.add(new JSpinner(new SpinnerNumberModel(10, 0, 50, 1)));
					return p;
				}));

		l.add(new Scenario("baseline-spinner-html-editor-narrow", G_INDIRECT, "Narrow spinners with HTML editors",
				"The same spinners held narrower than their text ('w 90!') or allowed to shrink ('wmin 50'), so the "
						+ "label's HTML wraps differently for the baseline request and for painting.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Delay:"));
					p.add(htmlSpinner("<html><b>%d</b> seconds before the slideshow starts</html>", 5), "w 90!");
					p.add(new JLabel("Repeat:"));
					p.add(htmlSpinner("<html>%d times, then <i>stop</i></html>", 3), "wmin 50");
					p.add(new JLabel("Title:"));
					p.add(new JTextField("Holiday", 10));
					return p;
				}).sizes()
				.step("very narrow (40%)", ctx -> ctx.sizeRelative(0.4, 1.0))
				.step("preferred size again (2)", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-titled-border-html", G_INDIRECT, "Group boxes with HTML titles",
				"Panels with an HTML TitledBorder in a baseline row with a caption: the panel's baseline is that of "
						+ "the title label inside the border.",
				() -> {
					JPanel p = mig("", "[][grow,fill][grow,fill]", "[baseline]");
					p.add(new JLabel("Sections:"));
					p.add(titled("<html><b>General</b> settings</html>", new JCheckBox("Start at login")));
					p.add(titled("<html>Advanced<br><small>for experts</small></html>", new JCheckBox("Verbose log")));
					return p;
				}));

		l.add(new Scenario("baseline-titled-border-html-narrow", G_INDIRECT, "Group boxes narrower than their title",
				"HTML titles longer than their panel ('w 120!'), so the border paints the title wrapped while its "
						+ "baseline is asked at the title's preferred size.",
				() -> {
					JPanel p = mig("", "[][][]", "[baseline]");
					p.add(new JLabel("Options:"));
					p.add(titled("<html>Notifications <i>for messages from people you follow</i></html>",
							new JCheckBox("Sound")), "w 120!");
					p.add(titled("<html><b>Appearance</b> and theme of the whole application</html>",
							new JCheckBox("Dark")), "wmin 80");
					return p;
				}).sizes()
				.step("very narrow (40%)", ctx -> ctx.sizeRelative(0.4, 1.0))
				.step("preferred size again (2)", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-titled-border-positions", G_INDIRECT, "Title positions of group boxes",
				"Plain and HTML titles above, on and below the top and bottom edges; bottom titles have a baseline "
						+ "that depends on the panel's height.",
				() -> {
					JPanel p = mig("wrap 4", "[][][][]", "[baseline][baseline]");
					int[] pos = {TitledBorder.ABOVE_TOP, TitledBorder.TOP, TitledBorder.BELOW_TOP,
							TitledBorder.ABOVE_BOTTOM, TitledBorder.BOTTOM, TitledBorder.BELOW_BOTTOM};
					String[] names = {"Above top", "Top", "Below top", "Above bottom", "Bottom", "Below bottom"};
					p.add(new JLabel("Plain:"));
					for (int i = 0; i < 3; i++)
						p.add(titled(names[i], pos[i], new JLabel("content")));
					p.add(new JLabel("HTML:"));
					for (int i = 3; i < 6; i++)
						p.add(titled("<html><b>" + names[i] + "</b></html>", pos[i], new JLabel("content")), "growy");
					return p;
				}));

		l.add(new Scenario("baseline-tabs-html-top", G_INDIRECT, "Tabbed pane with HTML titles on top",
				"A tabbed pane whose tab titles are HTML, aligned on the baseline of its tab titles with a caption.",
				() -> {
					JPanel p = mig("", "[][grow,fill]", "[baseline]");
					p.add(new JLabel("Editor:"));
					JTabbedPane t = new JTabbedPane();
					t.addTab("<html><b>Main</b>.java</html>", new JTextArea("class Main {}", 4, 20));
					t.addTab("<html><i>README</i></html>", new JTextArea("# Readme", 4, 20));
					t.addTab("<html>Build<br><small>gradle</small></html>", new JTextArea("plugins {}", 4, 20));
					p.add(t);
					return p;
				}));

		l.add(new Scenario("baseline-tabs-html-bottom", G_INDIRECT, "Tabbed pane with HTML titles at the bottom",
				"Tabs at the bottom: the tabbed pane's baseline depends on its height, next to a caption and a field.",
				() -> {
					JPanel p = mig("", "[][grow,fill][]", "[baseline,grow]");
					p.add(new JLabel("Sheets:"));
					JTabbedPane t = new JTabbedPane(JTabbedPane.BOTTOM);
					t.addTab("<html><b>Q1</b> sales</html>", new JTextArea(5, 18));
					t.addTab("<html>Q2 <i>forecast</i></html>", new JTextArea(5, 18));
					p.add(t, "growy");
					p.add(new JTextField("Total", 8));
					return p;
				}));

		l.add(new Scenario("baseline-tabs-tab-components", G_INDIRECT, "Tabs with HTML tab components",
				"Tab components (labels with HTML) instead of tab titles; their baseline is asked at their preferred size.",
				() -> {
					JPanel p = mig("", "[][grow,fill]", "[baseline]");
					p.add(new JLabel("Open files:"));
					JTabbedPane t = new JTabbedPane();
					t.addTab("a", new JLabel("first"));
					t.addTab("b", new JLabel("second"));
					t.setTabComponentAt(0, new JLabel("<html><b>Grid</b>.java</html>"));
					t.setTabComponentAt(1, new JLabel("<html>Layout<br><small>modified</small></html>"));
					p.add(t);
					return p;
				}));

		l.add(new Scenario("baseline-scroll-table-html-header", G_INDIRECT, "Table with an HTML column header",
				"A one-column table in a scroll pane: its baseline is that of the HTML header renderer, which is "
						+ "painted at the column's width.",
				() -> {
					JPanel p = mig("", "[][grow,fill]", "[baseline]");
					p.add(new JLabel("Customers:"));
					JTable t = new JTable(new DefaultTableModel(new Object[][] {{"Ada"}, {"Grace"}, {"Hedy"}},
							new Object[] {"<html><b>Name</b> and postal address of the customer</html>"}));
					t.setPreferredScrollableViewportSize(new Dimension(160, 60));
					p.add(new JScrollPane(t), "w 160:200");
					return p;
				}));

		l.add(new Scenario("baseline-scroll-table-plain-header", G_INDIRECT, "Table with plain column headers",
				"A control case: several plain column headers that share one baseline.",
				() -> {
					JPanel p = mig("", "[][grow,fill]", "[baseline]");
					p.add(new JLabel("Orders:"));
					JTable t = new JTable(new DefaultTableModel(new Object[][] {{"1001", "Lamp", "2"}, {"1002", "Desk", "1"}},
							new Object[] {"Number", "Item", "Quantity"}));
					t.setPreferredScrollableViewportSize(new Dimension(240, 50));
					p.add(new JScrollPane(t));
					return p;
				}));

		l.add(new Scenario("baseline-list-tree-html-renderers", G_INDIRECT, "Lists and trees with HTML cells",
				"A list and a tree that render HTML cells, in scroll panes next to captions.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline][baseline]");
					p.add(new JLabel("Recipients:"));
					JList<String> list = new JList<>(new String[] {"<html><b>Ada</b> &lt;ada@example.org&gt;</html>",
							"<html><i>Grace</i></html>"});
					list.setVisibleRowCount(3);
					p.add(new JScrollPane(list));
					p.add(new JLabel("Folder:"));
					DefaultMutableTreeNode root = new DefaultMutableTreeNode("<html><b>Inbox</b> (3)</html>");
					root.add(new DefaultMutableTreeNode("<html>Archive</html>"));
					JTree tree = new JTree(root);
					tree.setVisibleRowCount(3);
					p.add(new JScrollPane(tree));
					return p;
				}));

		l.add(new Scenario("baseline-scroll-titled-panel-view", G_INDIRECT, "Scroll pane showing a titled panel",
				"A scroll pane whose view is a panel with an HTML TitledBorder: the panel's baseline resize "
						+ "behaviour is constant ascent, so the scroll pane forwards it, at the view's current size.",
				() -> {
					JPanel p = mig("", "[][grow,fill]", "[baseline]");
					p.add(new JLabel("Details:"));
					JPanel view = mig("wrap 2", "[][]", "");
					view.setBorder(new TitledBorder("<html><b>Invoice</b> 2024-0042 from the north office</html>"));
					view.add(new JLabel("Net:"));
					view.add(new JLabel("100.00"));
					view.add(new JLabel("Tax:"));
					view.add(new JLabel("20.00"));
					p.add(new JScrollPane(view), "w 140:220, h 60:90");
					return p;
				}).sizes()
				.step("lay out again without a change", ctx -> ctx.root.doLayout())
				.step("very narrow (45%)", ctx -> ctx.sizeRelative(0.45, 1.0))
				.step("lay out again without a change (2)", ctx -> ctx.root.doLayout()));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Baselines that depend on state, scroll panes and text components

	private static void stateAndScrollPanes(List<Scenario> l)
	{
		l.add(new Scenario("baseline-scroll-text-area", G_STATE, "Notes in a scroll pane",
				"A text area in a scroll pane next to its caption; the scroll pane takes its baseline from the area.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline][baseline,grow]");
					p.add(new JLabel("Subject:"));
					p.add(new JTextField("Quarterly review", 20));
					p.add(new JLabel("Notes:"));
					JTextArea a = new JTextArea("Bring the figures for Q3.\nBook the large room.", 4, 20);
					p.add(new JScrollPane(a), "growy");
					return p;
				}));

		l.add(new Scenario("baseline-scroll-text-area-i18n", G_STATE, "Arabic and Hebrew text in a scroll pane",
				"A wrapping text area with mixed Latin, Arabic and Hebrew text (an i18n document): its baseline is "
						+ "computed from its views at the width the scroll pane's view currently has.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline][baseline,grow]");
					p.add(new JLabel("Name:"));
					p.add(new JTextField("Layla", 16));
					p.add(new JLabel("Address:"));
					JTextArea a = new JTextArea("Deliver to شارع الملك فهد 12، الرياض — or to the office in רחוב הרצל 5, "
							+ "תל אביב on weekdays", 3, 18);
					a.setLineWrap(true);
					a.setWrapStyleWord(true);
					p.add(new JScrollPane(a), "growy");
					return p;
				}).sizes()
				.step("lay out again without a change", ctx -> ctx.root.doLayout())
				.step("very narrow (45%)", ctx -> ctx.sizeRelative(0.45, 1.0))
				.step("lay out again without a change (2)", ctx -> ctx.root.doLayout()));

		l.add(new Scenario("baseline-text-area-i18n-direct", G_STATE, "Arabic and Hebrew text areas",
				"Wrapping i18n text areas placed directly in the form, with mixed scripts in their first line.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					p.add(new JLabel("Greeting:"));
					JTextArea a = new JTextArea("مرحبا Welcome to the conference שלום and enjoy your stay", 2, 16);
					a.setLineWrap(true);
					a.setWrapStyleWord(true);
					p.add(a);
					p.add(new JLabel("Plain:"));
					JTextArea b = new JTextArea("Welcome to the conference and enjoy your stay", 2, 16);
					b.setLineWrap(true);
					p.add(b);
					return p;
				}));

		l.add(new Scenario("baseline-scroll-label-view", G_STATE, "Scroll pane showing a top-aligned label",
				"A plain label aligned to the top as the view of a scroll pane (constant ascent), and an HTML one "
						+ "(no baseline), next to captions.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					p.add(new JLabel("Licence:"));
					JLabel lic = new JLabel("Apache License 2.0", swatch(16, 30, new Color(0x607d8b)), SwingConstants.LEADING);
					lic.setVerticalAlignment(SwingConstants.TOP);
					lic.setVerticalTextPosition(SwingConstants.BOTTOM);
					p.add(new JScrollPane(lic), "h 50:70");
					p.add(new JLabel("Summary:"));
					JLabel sum = new JLabel("<html>You may <b>use</b>, modify and distribute.</html>");
					sum.setVerticalAlignment(SwingConstants.TOP);
					p.add(new JScrollPane(sum), "h 40:60");
					return p;
				}).sizes()
				.step("lay out again without a change", ctx -> ctx.root.doLayout()));

		l.add(new Scenario("baseline-editor-panes", G_STATE, "HTML editor panes next to captions",
				"Editor and text panes showing HTML have no baseline; they sit in baseline rows with captions.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					p.add(new JLabel("Preview:"));
					JEditorPane e = new JEditorPane("text/html", "<html><body><h3>Release notes</h3><p>Fixed the "
							+ "<b>baseline</b> of HTML captions.</p></body></html>");
					p.add(e, "w 200:240");
					p.add(new JLabel("Styled:"));
					JTextPane t = new JTextPane();
					t.setText("A text pane with plain text.");
					p.add(t, "w 200:240");
					p.add(new JLabel("Field:"));
					p.add(new JTextField("after the panes", 12));
					return p;
				}));

		l.add(new Scenario("baseline-text-fields-kinds", G_STATE, "Formatted, password and i18n fields",
				"Formatted, masked, password and right-to-left text fields, each with its caption.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "");
					p.add(new JLabel("Date:"));
					JFormattedTextField date = new JFormattedTextField(new java.text.SimpleDateFormat("yyyy-MM-dd"));
					date.setText("2024-03-01");
					p.add(date);
					p.add(new JLabel("IBAN:"));
					p.add(new JFormattedTextField(mask("UU## #### #### ####")));
					p.add(new JLabel("Password:"));
					p.add(new JPasswordField("secret", 12));
					p.add(new JLabel("Name (Arabic):"));
					JTextField ar = new JTextField("ليلى عبد الله", 12);
					ar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					p.add(ar);
					p.add(new JLabel("Name (Hebrew):"));
					p.add(new JTextField("דנה כהן", 12), "h 36!");
					return p;
				}));

		l.add(new Scenario("baseline-slider-progress", G_STATE, "Sliders and progress bars with text",
				"A slider with painted labels and progress bars with painted strings, which have baselines in some "
						+ "look and feels, next to captions.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					p.add(new JLabel("Volume:"));
					JSlider s = new JSlider(0, 100, 40);
					s.setMajorTickSpacing(25);
					s.setPaintTicks(true);
					s.setPaintLabels(true);
					p.add(s);
					p.add(new JLabel("Download:"));
					JProgressBar b = new JProgressBar(0, 100);
					b.setValue(62);
					b.setStringPainted(true);
					p.add(b);
					p.add(new JLabel("Upload:"));
					JProgressBar u = new JProgressBar(0, 100);
					u.setValue(5);
					u.setString("waiting for the server");
					u.setStringPainted(true);
					p.add(u, "h 30!");
					p.add(new JLabel("Balance:"));
					JSlider v = new JSlider(JSlider.VERTICAL, -10, 10, 0);
					v.setMajorTickSpacing(10);
					v.setPaintLabels(true);
					p.add(v, "h 90!");
					return p;
				}));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Rows whose baseline comes from different fonts and sizes

	private static void mixedFonts(List<Scenario> l)
	{
		l.add(new Scenario("baseline-mixed-font-sizes", G_FONTS, "Price tag with three font sizes",
				"Small, normal and very large text in one row: the largest font decides how far the baseline sits down.",
				() -> {
					JPanel p = mig("", "[][][][][]", "[baseline]");
					p.add(sized(new JLabel("Price:"), 11f));
					p.add(sized(new JLabel("1,299"), 32f));
					p.add(sized(new JLabel(".99"), 18f));
					p.add(sized(new JLabel("EUR"), 10f));
					p.add(new JButton("Buy now"));
					return p;
				}));

		l.add(new Scenario("baseline-mixed-html-and-fonts", G_FONTS, "HTML and large fonts competing",
				"Rows in which a large plain label, an HTML label with a heading and a field compete for the baseline.",
				() -> {
					JPanel p = mig("wrap 3", "[][][grow,fill]", "");
					p.add(sized(new JLabel("Title"), 22f));
					p.add(new JLabel("<html><h2 style='margin:0'>Chapter 1</h2><small>draft</small></html>"));
					p.add(new JTextField("The beginning", 12));
					p.add(new JLabel("<html><font size=-1>Author</font></html>"));
					p.add(sized(new JTextField("M. Shelley", 12), 18f));
					p.add(sized(new JComboBox<>(new String[] {"<html><b>Gothic</b></html>", "Romance"}), 16f));
					return p;
				}));

		l.add(new Scenario("baseline-aligny-constraints", G_FONTS, "Baseline asked for in three ways",
				"The same caption and field pairs aligned through a '[baseline]' row, through 'aligny baseline' on "
						+ "the components in a top row, and through the default row alignment.",
				() -> {
					JPanel p = mig("wrap 4", "[][][][grow,fill]", "[baseline][top][]");
					p.add(sized(new JLabel("<html>Row <b>constraint</b></html>"), 16f));
					p.add(new JTextField("baseline row", 10));
					p.add(new JLabel("small"));
					p.add(new JComboBox<>(new String[] {"<html><i>one</i></html>"}));
					p.add(sized(new JLabel("<html>Component <b>constraint</b></html>"), 16f), "aligny baseline");
					p.add(new JTextField("top row", 10), "aligny baseline");
					p.add(new JLabel("small"), "aligny baseline");
					p.add(new JButton("<html>Go<br>now</html>"), "aligny baseline");
					p.add(sized(new JLabel("<html>Default <b>alignment</b></html>"), 16f));
					p.add(new JTextField("default row", 10));
					p.add(new JLabel("small"));
					p.add(new JCheckBox("<html>Check<br>twice</html>"));
					return p;
				}));

		l.add(new Scenario("baseline-without-baseline", G_FONTS, "Controls without a baseline",
				"Components whose baseline is -1 (panels, separators, plain scroll panes, icon-only labels) next to "
						+ "captions and fields in baseline rows.",
				() -> {
					JPanel p = mig("wrap 4", "[right][grow,fill][][]", "[baseline]");
					p.add(new JLabel("Colour:"));
					JPanel swatch = new JPanel();
					swatch.setBackground(new Color(0x3f51b5));
					swatch.setPreferredSize(new Dimension(60, 30));
					p.add(swatch);
					p.add(new JSeparator(SwingConstants.VERTICAL), "h 30!");
					p.add(new JButton("Pick"));
					p.add(new JLabel("<html>Recent<br>colours:</html>"));
					p.add(new JScrollPane(new JPanel()), "h 40!");
					p.add(new JLabel(swatch(20, 20, Color.ORANGE)));
					p.add(new JTextField("#ff9800", 7));
					return p;
				}));

		l.add(new Scenario("baseline-growy-rows", G_FONTS, "Baseline rows that grow",
				"Rows '[baseline,grow]' with components that grow vertically next to ones that do not, plus a bottom "
						+ "tabbed pane and an HTML caption.",
				() -> {
					JPanel p = mig("wrap 3", "[right][grow,fill][]", "[baseline,grow][baseline,grow]");
					p.add(new JLabel("<html><b>Description</b>:</html>"));
					p.add(new JScrollPane(new JTextArea("Grows with the window.", 3, 18)), "growy");
					p.add(new JButton("<html>Spell<br>check</html>"), "growy");
					p.add(new JLabel("Attachments:"));
					JTabbedPane t = new JTabbedPane(JTabbedPane.BOTTOM);
					t.addTab("<html><i>Files</i></html>", new JLabel("none"));
					p.add(t, "growy");
					p.add(new JComboBox<>(new String[] {"<html>Add <b>file</b></html>"}), "growy");
					return p;
				}));

		l.add(new Scenario("baseline-min-differs-from-pref", G_FONTS, "Minimum smaller than preferred",
				"Components with a minimum size well below their preferred size, so MigLayout asks their baseline at "
						+ "two different sizes; the narrow steps lay them out at their minimum.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					JLabel caption = new JLabel("<html>Short <b>description</b> of the item</html>");
					caption.setMinimumSize(new Dimension(40, 10));
					p.add(caption);
					JTextField f = new JTextField("A rather long default text for the field", 24);
					f.setMinimumSize(new Dimension(30, 10));
					p.add(f);
					JButton b = new JButton("<html>Apply <i>everywhere</i></html>");
					b.setMinimumSize(new Dimension(60, 12)); // narrower than its insets, Swing throws from getBaseline
					p.add(b);
					JComboBox<String> c = new JComboBox<>(new String[] {"<html><b>Every</b> project in the workspace</html>"});
					c.setMinimumSize(new Dimension(50, 12));
					p.add(c);
					return p;
				}).sizes()
				.step("very narrow (35%)", ctx -> ctx.sizeRelative(0.35, 0.8))
				.step("preferred size again (2)", StepContext::sizeToPreferred));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Nested MigLayout panels

	private static void nesting(List<Scenario> l)
	{
		l.add(new Scenario("baseline-nested-forms", G_NEST, "Forms within a form",
				"An outer form whose cells hold inner MigLayout panels with their own baseline rows of HTML "
						+ "captions, combos and spinners.",
				() -> {
					JPanel outer = mig("wrap 2", "[grow,fill][grow,fill]", "[baseline][grow]");
					outer.add(new JLabel("<html><b>Order</b> 4711</html>"));
					outer.add(new JTextField("pending", 10));
					JPanel left = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					left.setBorder(new TitledBorder("<html><i>Customer</i></html>"));
					left.add(new JLabel("<html>Name<br><small>as on the invoice</small></html>"));
					left.add(new JTextField("Hedy Lamarr", 10));
					left.add(new JLabel("Tier:"));
					left.add(new JComboBox<>(new String[] {"<html><b>Gold</b></html>", "Silver"}), "wmin 40");
					JPanel right = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					right.setBorder(new TitledBorder("Delivery"));
					right.add(new JLabel("<html>Parcels:</html>"));
					right.add(htmlSpinner("<html><b>%d</b> boxes</html>", 2));
					right.add(new JLabel("Express:"));
					right.add(new JCheckBox("<html>yes, <i>pay</i> more</html>"));
					outer.add(left);
					outer.add(right);
					return outer;
				}).sizes()
				.step("lay out the inner panels again", ctx -> {
					for (Component c : ctx.root.getComponents())
						if (c instanceof JPanel)
							c.doLayout();
				})
				.step("very narrow (45%)", ctx -> ctx.sizeRelative(0.45, 1.0)));

		l.add(new Scenario("baseline-nested-in-baseline-row", G_NEST, "Inner panels as cells of a baseline row",
				"Inner panels with titled borders (plain and HTML) as cells of the outer baseline row, three levels deep.",
				() -> {
					JPanel outer = mig("", "[][][]", "[baseline]");
					outer.add(new JLabel("Filters:"));
					JPanel a = mig("", "[][]", "[baseline]");
					a.setBorder(new TitledBorder("<html><b>Date</b> range</html>"));
					a.add(new JLabel("From"));
					a.add(new JTextField("2024-01-01", 8));
					JPanel b = mig("", "[][]", "[baseline]");
					b.setBorder(new TitledBorder("Status"));
					JPanel c = mig("", "[][]", "[baseline]");
					c.setBorder(new TitledBorder("<html><i>Flags</i><br>and marks</html>"));
					c.add(new JCheckBox("<html><b>Open</b></html>"));
					c.add(new JCheckBox("Closed"));
					b.add(c);
					b.add(new JComboBox<>(new String[] {"<html>Any <i>owner</i></html>"}));
					outer.add(a);
					outer.add(b);
					return outer;
				}).sizes()
				.step("lay out again without a change", ctx -> ctx.root.doLayout()));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Mutations between layouts

	private static void mutations(List<Scenario> l)
	{
		l.add(new Scenario("baseline-mut-label-text", G_MUT, "Status caption changing its HTML",
				"A status caption changes from short to long wrapping HTML and back, next to a field and a button.",
				() -> {
					JPanel p = mig("", "[][grow,fill][]", "[baseline]");
					p.add(named(new JLabel("<html><b>Ready</b></html>"), "status"), "wmax 200");
					p.add(new JTextField("query", 12));
					p.add(new JButton("Search"));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("long wrapping text", ctx -> ctx.<JLabel>find("status").setText(
						"<html><b>Searching</b> 1,204 documents in <i>all folders</i> for matching words, please wait</html>"))
				.step("narrow (75% wide)", ctx -> ctx.sizeRelative(0.75, 1.0))
				.step("two lines", ctx -> ctx.<JLabel>find("status").setText("<html><font size=+1>Found 3</font><br>results</html>"))
				.step("plain text", ctx -> ctx.<JLabel>find("status").setText("Done"))
				.step("HTML again", ctx -> ctx.<JLabel>find("status").setText("<html><i>Idle</i></html>"))
				.step("preferred size again", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-mut-plain-label-becomes-html", G_MUT, "Plain captions that turn into HTML",
				"Captions that start as plain text, so MigLayout records that they have a baseline, and then get HTML "
						+ "text, wrapping and multi-line, and an HTML button that changes its text.",
				() -> {
					JPanel p = mig("wrap 3", "[right][grow,fill][]", "[baseline]");
					p.add(named(new JLabel("Host:"), "host"), "wmax 150");
					p.add(new JTextField("db.example.org", 12));
					p.add(named(new JButton("Test"), "test"), "wmax 150");
					p.add(named(new JLabel("Port:"), "port"));
					p.add(new JTextField("5432", 12));
					p.add(new JButton("Default"));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("captions become HTML", ctx -> {
					ctx.<JLabel>find("host").setText("<html><b>Host</b>:</html>");
					ctx.<JLabel>find("port").setText("<html>Port<br><small>TCP</small>:</html>");
				})
				.step("long wrapping caption", ctx -> ctx.<JLabel>find("host").setText(
						"<html>Host name or <i>IP address</i> of the database server:</html>"))
				.step("button becomes HTML", ctx -> ctx.<JButton>find("test").setText(
						"<html><b>Test</b> the connection to the server now</html>"))
				.step("narrow (75% wide)", ctx -> ctx.sizeRelative(0.75, 1.0))
				.step("wide (150%)", ctx -> ctx.sizeRelative(1.5, 1.0))
				.step("preferred size again", StepContext::sizeToPreferred)
				.step("doLayout", ctx -> ctx.root.doLayout()));

		l.add(new Scenario("baseline-mut-fonts", G_MUT, "Fonts changing in a baseline row",
				"Captions and fields change their font size one after the other, so a different component decides "
						+ "the row's baseline each time.",
				() -> {
					JPanel p = mig("", "[][grow,fill][]", "[baseline]");
					p.add(named(new JLabel("<html>Amount <b>due</b>:</html>"), "caption"));
					p.add(named(new JTextField("42.00", 8), "field"));
					p.add(named(new JButton("Pay"), "button"));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("large caption", ctx -> grow(ctx.find("caption"), 2f))
				.step("large field", ctx -> grow(ctx.find("field"), 2.5f))
				.step("large button", ctx -> grow(ctx.find("button"), 3f))
				.step("small caption", ctx -> grow(ctx.find("caption"), 0.4f))
				.step("preferred size again", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-mut-valign-then-dolayout", G_MUT, "Alignment changed without a revalidate",
				"setVerticalAlignment only repaints; the form is then laid out again with doLayout() on the still "
						+ "valid container, which keeps MigLayout's grid.",
				() -> {
					JPanel p = mig("", "[][][grow,fill]", "[baseline]");
					p.add(named(valignLabel("Plain caption", SwingConstants.TOP), "plain"), "h 50!");
					JCheckBox html = named(new JCheckBox("<html><b>HTML</b> option</html>"), "html");
					html.setVerticalAlignment(SwingConstants.TOP);
					p.add(html, "h 50!");
					p.add(new JTextField("value", 10));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("plain to bottom, doLayout", ctx -> {
					ctx.<JLabel>find("plain").setVerticalAlignment(SwingConstants.BOTTOM);
					ctx.root.doLayout();
				})
				.step("HTML to bottom, doLayout", ctx -> {
					ctx.<JCheckBox>find("html").setVerticalAlignment(SwingConstants.BOTTOM);
					ctx.root.doLayout();
				})
				.step("both to center, doLayout", ctx -> {
					ctx.<JLabel>find("plain").setVerticalAlignment(SwingConstants.CENTER);
					ctx.<JCheckBox>find("html").setVerticalAlignment(SwingConstants.CENTER);
					ctx.root.doLayout();
				})
				.step("wide (150%)", ctx -> ctx.sizeRelative(1.5, 1.0)));

		l.add(new Scenario("baseline-mut-text-position", G_MUT, "Text moving around its icon",
				"An HTML caption and a button move their text above, below and beside their icon.",
				() -> {
					JPanel p = mig("", "[][][grow,fill]", "[baseline]");
					p.add(named(new JLabel("<html><b>Battery</b></html>", swatch(18, 34, new Color(0x8bc34a)), SwingConstants.LEADING), "label"));
					p.add(named(new JButton("<html>Saver<br>mode</html>", swatch(26, 26, new Color(0xffc107))), "button"));
					p.add(new JTextField("74 %", 6));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("text above", ctx -> {
					ctx.<JLabel>find("label").setVerticalTextPosition(SwingConstants.TOP);
					ctx.<JButton>find("button").setVerticalTextPosition(SwingConstants.TOP);
				})
				.step("text below and centered", ctx -> {
					ctx.<JLabel>find("label").setVerticalTextPosition(SwingConstants.BOTTOM);
					ctx.<JLabel>find("label").setHorizontalTextPosition(SwingConstants.CENTER);
					ctx.<JButton>find("button").setVerticalTextPosition(SwingConstants.BOTTOM);
					ctx.<JButton>find("button").setHorizontalTextPosition(SwingConstants.CENTER);
				})
				.step("larger gap", ctx -> {
					ctx.<JLabel>find("label").setIconTextGap(14);
					ctx.<JButton>find("button").setIconTextGap(14);
				})
				.step("narrow (75% wide)", ctx -> ctx.sizeRelative(0.75, 1.0)));

		l.add(new Scenario("baseline-mut-borders", G_MUT, "Borders added to captions",
				"An HTML caption and a checkbox get thick borders, then compound ones, which move their baselines down.",
				() -> {
					JPanel p = mig("", "[][][grow,fill]", "[baseline]");
					p.add(named(new JLabel("<html>Tags <i>(comma separated)</i>:</html>"), "label"));
					p.add(named(new JCheckBox("<html><b>Public</b></html>"), "check"));
					p.add(new JTextField("layout, swing", 12));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("thick top borders", ctx -> {
					ctx.<JLabel>find("label").setBorder(new EmptyBorder(18, 2, 2, 2));
					ctx.<JCheckBox>find("check").setBorder(new EmptyBorder(10, 4, 0, 4));
				})
				.step("compound borders", ctx -> {
					Border b = new CompoundBorder(new LineBorder(Color.GRAY, 3), new EmptyBorder(6, 6, 6, 6));
					ctx.<JLabel>find("label").setBorder(b);
				})
				.step("no border", ctx -> ctx.<JLabel>find("label").setBorder(null)));

		l.add(new Scenario("baseline-mut-combo-model", G_MUT, "Combo box models swapped",
				"A combo box's model is swapped between plain, HTML and wrapping HTML items, and its selection changes.",
				() -> {
					JPanel p = mig("", "[][grow,fill][]", "[baseline]");
					p.add(new JLabel("Category:"));
					p.add(named(new JComboBox<>(new String[] {"Books", "Music"}), "combo"), "wmin 70");
					p.add(new JButton("Filter"));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("HTML items", ctx -> ctx.<JComboBox<String>>find("combo").setModel(new DefaultComboBoxModel<>(
						new String[] {"<html><b>Books</b> and magazines</html>", "<html><i>Music</i></html>"})))
				.step("select the second", ctx -> ctx.<JComboBox<String>>find("combo").setSelectedIndex(1))
				.step("narrow (60% wide)", ctx -> ctx.sizeRelative(0.6, 1.0))
				.step("two-line items", ctx -> ctx.<JComboBox<String>>find("combo").setModel(new DefaultComboBoxModel<>(
						new String[] {"<html>Garden<br><small>and outdoor</small></html>", "Tools"})))
				.step("preferred size again", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-mut-combo-prototype", G_MUT, "Prototype display value changed",
				"A combo box's prototype display value changes from plain to HTML to null.",
				() -> {
					JPanel p = mig("", "[][][]", "[baseline]");
					p.add(new JLabel("Unit:"));
					p.add(named(new JComboBox<>(new String[] {"mm", "cm", "in"}), "combo"));
					p.add(new JTextField("12.5", 6));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("plain prototype", ctx -> ctx.<JComboBox<String>>find("combo").setPrototypeDisplayValue("millimetres"))
				.step("HTML prototype", ctx -> ctx.<JComboBox<String>>find("combo").setPrototypeDisplayValue(
						"<html><b>Kilo</b>metres</html>"))
				.step("two-line HTML prototype", ctx -> ctx.<JComboBox<String>>find("combo").setPrototypeDisplayValue(
						"<html>Nautical<br>miles</html>"))
				.step("no prototype", ctx -> ctx.<JComboBox<String>>find("combo").setPrototypeDisplayValue(null)));

		l.add(new Scenario("baseline-mut-titled-title", G_MUT, "Group box retitled",
				"A TitledBorder's title is changed in place (the border does not revalidate): once with repaint and "
						+ "a later doLayout(), once with revalidate.",
				() -> {
					JPanel p = mig("", "[][grow,fill]", "[baseline]");
					p.add(new JLabel("Account:"));
					JPanel g = mig("", "[]", "");
					g.setBorder(new TitledBorder("Personal"));
					g.add(new JCheckBox("Remember me"));
					p.add(named(g, "group"));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("HTML title, repaint, doLayout", ctx -> {
					JPanel g = ctx.find("group");
					((TitledBorder) g.getBorder()).setTitle("<html><b>Business</b><br>account</html>");
					g.repaint();
					ctx.root.doLayout();
				})
				.step("revalidate", ctx -> ctx.<JPanel>find("group").revalidate())
				.step("larger title font, doLayout", ctx -> {
					JPanel g = ctx.find("group");
					((TitledBorder) g.getBorder()).setTitleFont(g.getFont().deriveFont(20f));
					ctx.root.doLayout();
				})
				.step("title below top, doLayout", ctx -> {
					JPanel g = ctx.find("group");
					((TitledBorder) g.getBorder()).setTitlePosition(TitledBorder.BELOW_TOP);
					ctx.root.doLayout();
				})
				.step("narrow (75% wide)", ctx -> ctx.sizeRelative(0.75, 1.0)));

		l.add(new Scenario("baseline-mut-tab-placement", G_MUT, "Tabs moved around",
				"A tabbed pane with HTML titles moves its tabs from top to bottom to left, and gets a new title.",
				() -> {
					JPanel p = mig("", "[][grow,fill][]", "[baseline,grow]");
					p.add(new JLabel("View:"));
					JTabbedPane t = named(new JTabbedPane(), "tabs");
					t.addTab("<html><b>Source</b></html>", new JTextArea(4, 16));
					t.addTab("<html><i>Design</i></html>", new JTextArea(4, 16));
					p.add(t, "growy");
					p.add(new JTextField("zoom 100 %", 8));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("tabs at the bottom", ctx -> ctx.<JTabbedPane>find("tabs").setTabPlacement(JTabbedPane.BOTTOM))
				.step("taller (130%)", ctx -> ctx.sizeRelative(1.0, 1.3))
				.step("two-line title", ctx -> ctx.<JTabbedPane>find("tabs").setTitleAt(0, "<html>Source<br><small>edited</small></html>"))
				.step("tabs on the left", ctx -> ctx.<JTabbedPane>find("tabs").setTabPlacement(JTabbedPane.LEFT))
				.step("tabs on top", ctx -> ctx.<JTabbedPane>find("tabs").setTabPlacement(JTabbedPane.TOP)));

		l.add(new Scenario("baseline-mut-sibling-visibility", G_MUT, "Large sibling shown and hidden",
				"A large label that dominates the row's baseline is hidden and shown again; the HTML caption stays.",
				() -> {
					JPanel p = mig("hidemode 3", "[][][grow,fill]", "[baseline]");
					p.add(named(sized(new JLabel("Warning"), 26f), "big"));
					p.add(new JLabel("<html>Disk <b>almost</b> full<br><small>2 GB left</small></html>"));
					p.add(new JButton("Clean up"));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("hide the large label", ctx -> ctx.find("big").setVisible(false))
				.step("preferred size (2)", StepContext::sizeToPreferred)
				.step("show it again", ctx -> ctx.find("big").setVisible(true))
				.step("wide (150%)", ctx -> ctx.sizeRelative(1.5, 1.0)));

		l.add(new Scenario("baseline-mut-preferred-before-change", G_MUT, "Preferred size asked before a change",
				"The form's preferred and minimum sizes are asked while it is invalid, then captions change in ways "
						+ "that do not revalidate, so the next layout reuses the grid built for those questions.",
				() -> {
					JPanel p = mig("", "[][][][grow,fill]", "[baseline]");
					p.add(named(valignLabel("<html><b>Zone</b></html>", SwingConstants.CENTER), "zone"), "h 44!");
					p.add(named(valignLabel("Plain", SwingConstants.CENTER), "plain"), "h 44!");
					p.add(named(new JComboBox<>(new String[] {"<html>Europe/<i>Vienna</i></html>"}), "combo"));
					p.add(new JTextField("UTC+1", 8));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("change text, ask sizes, realign", ctx -> {
					ctx.<JLabel>find("zone").setText("<html>Time <b>zone</b></html>");
					ctx.root.getPreferredSize();
					ctx.root.getMinimumSize();
					ctx.<JLabel>find("zone").setVerticalAlignment(SwingConstants.BOTTOM);
					ctx.<JLabel>find("plain").setVerticalAlignment(SwingConstants.TOP);
				})
				.step("ask sizes on the valid form, realign, doLayout", ctx -> {
					ctx.root.getPreferredSize();
					ctx.root.getMinimumSize();
					ctx.<JLabel>find("zone").setVerticalAlignment(SwingConstants.TOP);
					ctx.<JLabel>find("plain").setVerticalAlignment(SwingConstants.BOTTOM);
					ctx.root.doLayout();
				})
				.step("narrow (75% wide)", ctx -> ctx.sizeRelative(0.75, 1.0))
				.step("doLayout without a change", ctx -> ctx.root.doLayout()));

		l.add(new Scenario("baseline-mut-renderer-state", G_MUT, "Renderer changed without notice",
				"A combo box renderer switches to a bold HTML style through a field of its own (the combo is not told), "
						+ "then the form is laid out again with doLayout().",
				() -> {
					JPanel p = mig("", "[][][grow,fill]", "[baseline]");
					p.add(new JLabel("Owner:"));
					JComboBox<String> c = named(new JComboBox<>(new String[] {"Ada|admin", "Grace|editor"}), "combo");
					c.setRenderer(new HtmlDetailRenderer());
					p.add(c, "wmin 60");
					p.add(new JTextField("since 2019", 8));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("big two-line style, doLayout", ctx -> {
					JComboBox<String> c = ctx.find("combo");
					((HtmlDetailRenderer) c.getRenderer()).twoLines = true;
					ctx.root.doLayout();
				})
				.step("narrow (60% wide)", ctx -> ctx.sizeRelative(0.6, 1.0))
				.step("one line again, doLayout", ctx -> {
					JComboBox<String> c = ctx.find("combo");
					((HtmlDetailRenderer) c.getRenderer()).twoLines = false;
					ctx.root.doLayout();
				})
				.step("preferred size again", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-mut-html-disable", G_MUT, "HTML switched off on a caption",
				"The 'html.disable' client property is set on a caption and a checkbox and the text set again, so the "
						+ "markup shows as plain text; then it is switched back on.",
				() -> {
					JPanel p = mig("", "[][][grow,fill]", "[baseline]");
					p.add(named(new JLabel("<html><b>Markup</b><br>preview</html>"), "label"));
					p.add(named(new JCheckBox("<html><i>Render</i></html>"), "check"));
					p.add(new JTextField("<b>bold</b>", 10));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("disable HTML", ctx -> setHtmlDisabled(ctx, true))
				.step("enable HTML", ctx -> setHtmlDisabled(ctx, false))
				.step("narrow (75% wide)", ctx -> ctx.sizeRelative(0.75, 1.0)));

		l.add(new Scenario("baseline-mut-spinner-editor", G_MUT, "Spinner value and editor changing",
				"A spinner with an HTML label editor steps its value (the label text changes), then gets the default "
						+ "editor back and an HTML editor again.",
				() -> {
					JPanel p = mig("", "[][][grow,fill]", "[baseline]");
					p.add(new JLabel("Quantity:"));
					p.add(named(htmlSpinner("<html><b>%d</b> pieces</html>", 1), "spinner"), "wmin 60");
					p.add(new JTextField("in stock", 8));
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("value 12", ctx -> ctx.<JSpinner>find("spinner").setValue(12))
				.step("value 12, doLayout", ctx -> ctx.root.doLayout())
				.step("narrow (60% wide)", ctx -> ctx.sizeRelative(0.6, 1.0))
				.step("default editor", ctx -> {
					JSpinner s = ctx.find("spinner");
					s.setEditor(new JSpinner.NumberEditor(s));
				})
				.step("HTML editor again", ctx -> {
					JSpinner s = ctx.find("spinner");
					s.setEditor(htmlEditor(s, "<html>%d<br><small>pieces</small></html>"));
				})
				.step("preferred size again", StepContext::sizeToPreferred));

		l.add(new Scenario("baseline-mut-nested-dolayout", G_MUT, "Inner panel laid out directly",
				"An inner form changes the alignment of its captions without revalidating and is laid out with its "
						+ "own doLayout(); then its HTML caption gets new text.",
				() -> {
					JPanel outer = mig("wrap 2", "[][grow,fill]", "[baseline]");
					outer.add(new JLabel("<html><b>Profile</b></html>"));
					outer.add(new JTextField("public", 8));
					JPanel inner = named(mig("", "[][][grow,fill]", "[baseline]"), "inner");
					inner.setBorder(new EtchedBorder());
					inner.add(named(valignLabel("<html>Nick<i>name</i></html>", SwingConstants.TOP), "nick"), "h 40!");
					inner.add(named(valignLabel("plain", SwingConstants.TOP), "plain"), "h 40!");
					inner.add(new JTextField("hedy", 8));
					outer.add(inner, "span 2, growx");
					return outer;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("realign, inner doLayout", ctx -> {
					ctx.<JLabel>find("nick").setVerticalAlignment(SwingConstants.BOTTOM);
					ctx.<JLabel>find("plain").setVerticalAlignment(SwingConstants.BOTTOM);
					ctx.find("inner").doLayout();
				})
				.step("inner doLayout again", ctx -> ctx.find("inner").doLayout())
				.step("new HTML text", ctx -> ctx.<JLabel>find("nick").setText("<html>Screen<br><b>name</b></html>"))
				.step("realign, inner doLayout (2)", ctx -> {
					ctx.<JLabel>find("nick").setVerticalAlignment(SwingConstants.CENTER);
					ctx.find("inner").doLayout();
				})
				.step("wide (150%)", ctx -> ctx.sizeRelative(1.5, 1.0)));

		l.add(new Scenario("baseline-mut-scroll-view", G_MUT, "Scroll pane view changing",
				"A text area in a scroll pane gets i18n text and a larger font; and a doLayout() after each.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					p.add(new JLabel("Comment:"));
					JTextArea a = named(new JTextArea("Looks good to me.", 2, 18), "area");
					a.setLineWrap(true);
					p.add(new JScrollPane(a), "h 40:60");
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("Hebrew text", ctx -> ctx.<JTextArea>find("area").setText("נראה טוב, Looks good, شكرا"))
				.step("doLayout", ctx -> ctx.root.doLayout())
				.step("larger font", ctx -> grow(ctx.find("area"), 1.6f))
				.step("doLayout (2)", ctx -> ctx.root.doLayout())
				.step("narrow (60% wide)", ctx -> ctx.sizeRelative(0.6, 1.0))
				.step("doLayout (3)", ctx -> ctx.root.doLayout()));

		l.add(new Scenario("baseline-mut-dolayout-every-size", G_MUT, "doLayout after every resize",
				"A form of HTML captions, combos, a titled group and a spinner, laid out again with doLayout() on "
						+ "the valid container after each size, so cached baselines meet painted HTML views.",
				() -> {
					JPanel p = mig("wrap 2", "[right][grow,fill]", "[baseline]");
					p.add(new JLabel("<html><b>Title</b> of the talk:</html>"), "wmax 110");
					p.add(new JTextField("Baselines considered harmful", 14));
					p.add(new JLabel("<html>Track<br><small>choose one</small></html>"));
					p.add(new JComboBox<>(new String[] {"<html><b>Desktop</b> and <i>user interfaces</i></html>",
							"Web"}), "wmin 60");
					p.add(new JLabel("Room:"));
					p.add(titled("<html>Hall <b>B</b>, second floor, next to the lifts</html>", new JLabel("120 seats")), "wmin 80");
					p.add(new JLabel("Slot:"));
					p.add(htmlSpinner("<html>slot <b>%d</b> of the afternoon</html>", 3), "wmin 60");
					return p;
				})
				.step("preferred size", StepContext::sizeToPreferred)
				.step("doLayout", ctx -> ctx.root.doLayout())
				.step("narrow (55% wide)", ctx -> ctx.sizeRelative(0.55, 1.0))
				.step("doLayout (2)", ctx -> ctx.root.doLayout())
				.step("wide (160%)", ctx -> ctx.sizeRelative(1.6, 1.2))
				.step("doLayout (3)", ctx -> ctx.root.doLayout())
				.step("preferred size again", StepContext::sizeToPreferred)
				.step("doLayout (4)", ctx -> ctx.root.doLayout()));
	}

	// ------------------------------------------------------------------------------------------------------------
	// Helpers

	private static JPanel mig(String lc, String cols, String rows)
	{
		return new JPanel(new MigLayout(lc, cols, rows));
	}

	private static <T extends Component> T named(T c, String name)
	{
		c.setName(name);
		return c;
	}

	private static <T extends JComponent> T sized(T c, float size)
	{
		c.setFont(c.getFont().deriveFont(size));
		return c;
	}

	private static void grow(Component c, float factor)
	{
		Font f = c.getFont();
		c.setFont(f.deriveFont(f.getSize2D() * factor));
	}

	/** Sets or clears 'html.disable' on the caption and the checkbox, and sets their text again so it takes effect. */
	private static void setHtmlDisabled(StepContext ctx, boolean disabled)
	{
		for (String n : new String[] {"label", "check"}) {
			JComponent c = ctx.find(n);
			c.putClientProperty("html.disable", disabled ? Boolean.TRUE : null);
			if (c instanceof JLabel lb) {
				String t = lb.getText();
				lb.setText("");
				lb.setText(t);
			} else if (c instanceof AbstractButton ab) {
				String t = ab.getText();
				ab.setText("");
				ab.setText(t);
			}
		}
	}

	private static JLabel valignLabel(String text, int valign)
	{
		JLabel l = new JLabel(text);
		l.setVerticalAlignment(valign);
		l.setBorder(new LineBorder(new Color(0xb0bec5)));
		return l;
	}

	private static JLabel iconLabel(String text, Icon icon, int vTextPos, int hTextPos, int gap)
	{
		JLabel l = new JLabel(text, icon, SwingConstants.LEADING);
		l.setVerticalTextPosition(vTextPos);
		l.setHorizontalTextPosition(hTextPos);
		l.setIconTextGap(gap);
		return l;
	}

	private static JPanel titled(String title, JComponent content)
	{
		return titled(title, TitledBorder.DEFAULT_POSITION, content);
	}

	private static JPanel titled(String title, int position, JComponent content)
	{
		JPanel p = mig("", "[grow,fill]", "");
		p.setBorder(new TitledBorder(null, title, TitledBorder.LEADING, position));
		p.add(content);
		return p;
	}

	private static JSpinner htmlSpinner(String format, int value)
	{
		JSpinner s = new JSpinner(new SpinnerNumberModel(value, 0, 99, 1));
		s.setEditor(htmlEditor(s, format));
		return s;
	}

	/** A label showing the spinner's value through an HTML format; it follows the value. */
	private static JLabel htmlEditor(JSpinner s, String format)
	{
		JLabel l = new JLabel(String.format(format, s.getValue()));
		l.setBorder(new EmptyBorder(2, 4, 2, 4));
		s.addChangeListener(e -> l.setText(String.format(format, s.getValue())));
		return l;
	}

	private static MaskFormatter mask(String m)
	{
		try {
			MaskFormatter f = new MaskFormatter(m);
			f.setPlaceholderCharacter('_');
			return f;
		} catch (ParseException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Icon swatch(int w, int h, Color color)
	{
		return new Swatch(w, h, color);
	}

	/** A plain coloured rectangle of a fixed size, so that icons look the same everywhere. */
	private record Swatch(int w, int h, Color color) implements Icon
	{
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(color.darker());
			g.drawRect(x, y, w - 1, h - 1);
		}

		@Override
		public int getIconWidth()
		{
			return w;
		}

		@Override
		public int getIconHeight()
		{
			return h;
		}
	}

	/** Renders "name|detail" items as HTML on one shared label, as a typical application renderer does. */
	private static final class HtmlDetailRenderer extends DefaultListCellRenderer
	{
		boolean twoLines = false;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus)
		{
			String[] parts = String.valueOf(value).split("\\|", 2);
			String detail = parts.length > 1 ? parts[1] : "";
			String html = twoLines
					? "<html><font size=+1><b>" + parts[0] + "</b></font><br><small>" + detail + "</small></html>"
					: "<html><b>" + parts[0] + "</b> <font color='#888888'>" + detail + "</font></html>";
			return super.getListCellRendererComponent(list, html, index, selected, focus);
		}
	}

	private ScenariosBaseline() {}
}
