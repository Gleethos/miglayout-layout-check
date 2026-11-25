package layoutcheck;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * Complete, dense windows of the kind real Swing applications have, each laid out with MigLayout throughout and often
 * nested several levels deep: the "does a real application still look the same" test.
 */
final class ScenariosRealistic
{
	private static final String DIALOGS = "Apps: dialogs";
	private static final String WINDOWS = "Apps: windows";
	private static final String FORMS = "Apps: forms";
	private static final String I18N = "Apps: international";

	private static final Color BLUE = new Color(0x2F6FD6);
	private static final Color GREEN = new Color(0x2E9E5B);
	private static final Color RED = new Color(0xC8372D);
	private static final Color ORANGE = new Color(0xE08A1E);
	private static final Color GREY = new Color(0x7A7F87);
	private static final Color PURPLE = new Color(0x7B4FC9);
	private static final Color TEAL = new Color(0x1E9AA0);

	static void add(List<Scenario> l)
	{
		l.add(preferences());
		l.add(registration());
		l.add(mailComposer());
		l.add(ideWindow());
		l.add(databaseConnection());
		l.add(wizard());
		l.add(printDialog());
		l.add(findReplace());
		l.add(chat());
		l.add(fileProperties());
		l.add(invoice());
		l.add(dashboard());
		l.add(login());
		l.add(calendar());
		l.add(musicPlayer());
		l.add(checkoutRtl());
		l.add(checkoutCjk());
		l.add(propertySheet());
		l.add(notifications());
		l.add(colorPicker());
		l.add(aboutBox());
		l.add(downloads());
		l.add(contactEditor());
	}

	// ------------------------------------------------------------------------------------------------ scenarios

	private static Scenario preferences()
	{
		return new Scenario("app-preferences-dialog", DIALOGS, "Preferences with categories",
				"A category list beside a sectioned settings form with separator headers and a titled preview, and Ok/Cancel/Apply/Help ordered by button tags.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog", "[][grow,fill]", "[grow,fill][]"));
					JList<String> cats = new JList<>(new String[] {"General", "Appearance", "Editor", "Keyboard", "Network", "Updates", "Plugins", "Advanced"});
					cats.setSelectedIndex(1);
					cats.setVisibleRowCount(12);
					root.add(new JScrollPane(cats), "w 150!, growy");

					JPanel form = new JPanel(new MigLayout("wrap 2, insets 0", "[right][grow,fill]", ""));
					JLabel title = new JLabel("Appearance");
					title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 5f));
					form.add(title, "span, left, gapbottom 6");
					section(form, "Theme", false);
					form.add(new JLabel("Colour scheme:"));
					form.add(combo("Daylight", "Dusk", "Paper", "High contrast"));
					form.add(new JLabel("Accent colour:"));
					JPanel accents = new JPanel(new MigLayout("insets 0, gap 4"));
					Color[] cs = {BLUE, GREEN, ORANGE, RED, PURPLE, TEAL};
					ButtonGroup ag = new ButtonGroup();
					for (int i = 0; i < cs.length; i++) {
						JToggleButton b = new JToggleButton(new Glyph("circle", cs[i], 14));
						b.setSelected(i == 0);
						ag.add(b);
						accents.add(b);
					}
					form.add(accents, "growx 0, left");
					form.add(new JLabel("Interface font:"));
					form.add(combo("Sans Serif", "Serif", "Monospaced"), "split 3");
					form.add(new JSpinner(new SpinnerNumberModel(13, 8, 32, 1)), "w 64!");
					form.add(new JLabel("pt"), "growx 0");
					section(form, "Windows", true);
					form.add(new JCheckBox("Restore open windows on startup", true), "skip");
					form.add(new JCheckBox("<html>Show the <b>tab bar</b> even with a single document</html>"), "skip");
					form.add(new JLabel("Title bar:"));
					form.add(combo("Integrated", "System", "Hidden"));
					section(form, "Density", true);
					form.add(new JLabel("Spacing:"));
					ButtonGroup dg = new ButtonGroup();
					JRadioButton compact = new JRadioButton("Compact");
					JRadioButton normal = new JRadioButton("Comfortable", true);
					JRadioButton loose = new JRadioButton("Spacious");
					dg.add(compact);
					dg.add(normal);
					dg.add(loose);
					form.add(compact, "split 3");
					form.add(normal);
					form.add(loose);
					form.add(new JLabel("Zoom:"));
					JSlider zoom = new JSlider(50, 200, 100);
					zoom.setMajorTickSpacing(50);
					zoom.setPaintTicks(true);
					zoom.setPaintLabels(true);
					form.add(zoom);

					JPanel preview = new JPanel(new MigLayout("wrap 2", "[][grow,fill]"));
					preview.setBorder(BorderFactory.createTitledBorder("Preview"));
					preview.add(new JLabel(new Glyph("folder", ORANGE, 32)), "spany 2, aligny top");
					preview.add(new JLabel("<html><b>Quarterly report.odt</b></html>"));
					JLabel hint = new JLabel("<html><font color='#7A7F87'>Changes apply to every open window as soon as you press Apply.</font></html>");
					preview.add(hint, "wmin 0");
					form.add(preview, "span, growx, gaptop para");

					root.add(form, "aligny top, wrap");
					root.add(new JButton("OK"), "span, split 4, tag ok");
					root.add(new JButton("Cancel"), "tag cancel");
					root.add(new JButton("Apply"), "tag apply");
					root.add(new JButton("Help"), "tag help");
					return root;
				})
				.sizes()
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("maximized 1600x1000", c -> c.size(1600, 1000))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario registration()
	{
		return new Scenario("app-registration-form", FORMS, "Account registration",
				"A sign-up form with a large heading, split name fields, a password strength meter and validation hints that appear under their fields.",
				() -> {
					JPanel p = new JPanel(new MigLayout("wrap 2, insets 20 24 20 24, hidemode 3", "[right][grow,fill,300::]", ""));
					JLabel head = new JLabel("Create your account");
					head.setFont(head.getFont().deriveFont(Font.BOLD, 22f));
					p.add(head, "span, left");
					p.add(new JLabel("<html><font color='#7A7F87'>It takes less than a minute. Fields marked * are required.</font></html>"), "span, left, gapbottom para");

					p.add(new JLabel("Name: *"));
					JTextField first = new JTextField("Jana", 10);
					first.setName("first");
					p.add(first, "split 2, sg name");
					p.add(new JTextField("Kowalczyk", 10), "sg name");
					p.add(error("firstError", "Please enter your first name."), "skip");

					p.add(new JLabel("E-mail: *"));
					JTextField mail = new JTextField("jana.k@example", 20);
					mail.setName("mail");
					p.add(mail);
					p.add(error("mailError", "Add a domain ending, such as .org or .net."), "skip");

					p.add(new JLabel("Password: *"));
					p.add(new JPasswordField("secret12", 16));
					p.add(new JLabel("Strength:"));
					JProgressBar strength = new JProgressBar(0, 100);
					strength.setValue(40);
					strength.setName("strength");
					p.add(strength, "split 2, growx");
					JLabel sl = new JLabel("Fair");
					sl.setName("strengthLabel");
					sl.setForeground(ORANGE);
					p.add(sl, "w 60!");
					p.add(new JLabel("Repeat: *"));
					p.add(new JPasswordField("secret21", 16));
					p.add(error("pwError", "The passwords do not match."), "skip");

					p.add(new JLabel("Date of birth:"));
					p.add(combo("1", "2", "3", "14", "28"), "split 3");
					p.add(combo("January", "February", "September", "December"), "growx");
					p.add(combo("1990", "1991", "2001"));
					p.add(new JLabel("Country:"));
					p.add(combo("Poland", "Portugal", "New Zealand", "United Kingdom"));
					p.add(new JLabel("Phone:"));
					p.add(combo("+48", "+351", "+64"), "split 2, growx 0");
					p.add(new JTextField("601 234 567", 12));

					p.add(new JCheckBox("<html>I have read and accept the <a href='#'>terms of use</a></html>"), "skip, gaptop para");
					p.add(new JCheckBox("Send me a short newsletter once a month"), "skip");
					JButton create = new JButton("Create account");
					create.setFont(create.getFont().deriveFont(Font.BOLD));
					p.add(create, "span, growx, gaptop para, h 34!");
					p.add(new JLabel("<html>Already registered? <a href='#'>Sign in</a></html>"), "span, center");
					return p;
				})
				.sizes()
				.step("user typed a longer name", c -> {
					JTextField f = c.find("first");
					f.setText("Maximiliana-Josephine");
					f.revalidate();
				})
				.step("validation errors shown", c -> {
					c.<JComponent>find("mailError").setVisible(true);
					c.<JComponent>find("pwError").setVisible(true);
					c.root.revalidate();
				})
				.step("strong password entered", c -> {
					c.<JProgressBar>find("strength").setValue(90);
					JLabel l = c.find("strengthLabel");
					l.setText("Very strong");
					l.setForeground(GREEN);
					c.<JComponent>find("pwError").setVisible(false);
					c.root.revalidate();
				})
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario mailComposer()
	{
		return new Scenario("app-mail-composer", WINDOWS, "Mail composer",
				"A message window with a toolbar docked north, To/Cc/Bcc/Subject rows, a wrapping body, attachment chips and a status line docked south.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 0, gap 0, fill, hidemode 3", "[grow,fill]", "[grow,fill][]"));
					JPanel tb = new JPanel(new MigLayout("insets 4 8 4 8, gap 2"));
					JButton send = new JButton("Send", new Glyph("play", BLUE, 14));
					send.setFont(send.getFont().deriveFont(Font.BOLD));
					tb.add(send, "gapright 8");
					tb.add(new JButton("Attach", new Glyph("file", GREY, 14)));
					tb.add(new JButton("Save draft"));
					tb.add(new JSeparator(SwingConstants.VERTICAL), "growy, gapx 6 6");
					for (String s : new String[] {"B", "I", "U"}) {
						JToggleButton t = new JToggleButton(s);
						t.setFont(t.getFont().deriveFont(s.equals("B") ? Font.BOLD : s.equals("I") ? Font.ITALIC : Font.PLAIN));
						tb.add(t, "sg fmt");
					}
					tb.add(combo("Sans Serif", "Serif", "Monospaced"), "gapleft 6");
					tb.add(combo("Normal", "Small", "Large"));
					tb.add(new JCheckBox("Request read receipt"), "gapbefore push");
					root.add(tb, "dock north");
					root.add(new JSeparator(), "dock north");

					JPanel head = new JPanel(new MigLayout("wrap 3, insets 8 10 4 10, hidemode 3", "[right][grow,fill][]", ""));
					head.add(new JLabel("From:"));
					head.add(combo("Aurelio Brandt <aurelio@example.org>", "Support <help@example.org>"), "span 2");
					head.add(new JLabel("To:"));
					head.add(new JTextField("Mirela Ionescu <mirela@example.net>; build-team@example.net", 30));
					JButton bccToggle = new JButton("Bcc");
					bccToggle.setName("bccToggle");
					head.add(bccToggle, "sg side");
					head.add(new JLabel("Cc:"));
					head.add(new JTextField("Tomás Ruiz <truiz@example.com>", 30), "span 2");
					JLabel bccL = new JLabel("Bcc:");
					bccL.setName("bccLabel");
					bccL.setVisible(false);
					head.add(bccL);
					JTextField bcc = new JTextField("archive@example.org", 30);
					bcc.setName("bcc");
					bcc.setVisible(false);
					head.add(bcc, "span 2");
					head.add(new JLabel("Subject:"));
					JTextField subj = new JTextField("Release notes for 4.2", 30);
					subj.setName("subject");
					head.add(subj);
					head.add(combo("Normal", "High", "Low"), "sg side");
					root.add(head, "dock north");

					JTextArea body = new JTextArea("Hi Mirela,\n\nattached are the release notes for 4.2 and the list of open questions from Thursday's review. "
							+ "The installer changes are the biggest item; everything else is small.\n\nCould you have a look before Monday? "
							+ "If something is unclear, the build team is around all week.\n\nThanks,\nAurelio", 12, 50);
					body.setLineWrap(true);
					body.setWrapStyleWord(true);
					JScrollPane bs = new JScrollPane(body);
					root.add(bs, "gap 10 10 0 4, wrap");

					JPanel att = new JPanel(new MigLayout("insets 2 10 6 10, gap 6"));
					att.add(new JLabel("Attachments:"));
					att.add(chip("release-notes-4.2.pdf", "312 KB"));
					att.add(chip("open-questions.txt", "4 KB"));
					att.add(chip("installer-diff.png", "1.1 MB"));
					root.add(att, "growx");

					JPanel status = new JPanel(new MigLayout("insets 3 10 3 10", "[grow][][]"));
					status.add(new JLabel("Draft saved at 10:42"));
					status.add(new JLabel("Spell check: English (UK)"));
					status.add(new JLabel("1.4 MB"));
					root.add(status, "dock south");
					return root;
				})
				.sizes()
				.step("Bcc row shown", c -> {
					c.<JComponent>find("bccLabel").setVisible(true);
					c.<JComponent>find("bcc").setVisible(true);
					c.root.revalidate();
				})
				.step("long subject typed", c -> c.<JTextField>find("subject").setText(
						"Release notes for 4.2, the installer rework, and the open questions we collected during Thursday's long review"))
				.step("maximized 1600x1000", c -> c.size(1600, 1000))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario ideWindow()
	{
		return new Scenario("app-ide-window", WINDOWS, "Code editor main window",
				"A toolbar with toggle buttons and a pushed run selector, a project tree, tabbed editors, a problems panel and a status bar of split cells.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[grow,fill]"));

					JPanel tb = new JPanel(new MigLayout("insets 3 6 3 6, gap 2, aligny center"));
					tb.add(new JButton(new Glyph("folder", ORANGE, 16)));
					tb.add(new JButton(new Glyph("file", GREY, 16)));
					tb.add(new JSeparator(SwingConstants.VERTICAL), "growy, gapx 4 4");
					JToggleButton tree = new JToggleButton(new Glyph("square", BLUE, 16), true);
					tb.add(tree);
					tb.add(new JToggleButton(new Glyph("ring", GREY, 16)));
					tb.add(new JToggleButton(new Glyph("warn", ORANGE, 16)));
					tb.add(combo("app [main]", "tests [all]", "bench"), "gapbefore push, w 160!");
					tb.add(new JButton(new Glyph("play", GREEN, 16)));
					tb.add(new JButton(new Glyph("gear", GREY, 16)));
					tb.add(new JTextField("Search everywhere", 14), "gapleft 8");
					root.add(tb, "dock north");

					JPanel side = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[][grow,fill]"));
					JPanel sh = new JPanel(new MigLayout("insets 3 6 3 4", "[grow][][]"));
					JLabel pl = new JLabel("Project");
					pl.setFont(pl.getFont().deriveFont(Font.BOLD));
					sh.add(pl);
					sh.add(new JButton(new Glyph("up", GREY, 10)));
					sh.add(new JButton(new Glyph("cross", GREY, 10)));
					side.add(sh, "wrap");
					side.add(new JScrollPane(projectTree()));
					root.add(side, "dock west, w 230!");

					JPanel center = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[grow 3,fill][grow 1,fill]"));
					JTabbedPane tabs = new JTabbedPane();
					tabs.setName("tabs");
					tabs.addTab("Main.java", new Glyph("file", BLUE, 12), code("package app;\n\nimport java.util.List;\n\npublic final class Main\n{\n\tpublic static void main(String[] args)\n\t{\n\t\tList<String> words = List.of(args);\n\t\tSystem.out.println(words.size() + \" words\");\n\t}\n}\n"));
					tabs.addTab("Config.java", new Glyph("file", BLUE, 12), code("package app;\n\nrecord Config(String name, int port, boolean verbose)\n{\n\tstatic Config defaults()\n\t{\n\t\treturn new Config(\"app\", 8080, false);\n\t}\n}\n"));
					tabs.addTab("build.gradle", new Glyph("file", GREEN, 12), code("plugins {\n    id 'application'\n}\n\napplication {\n    mainClass = 'app.Main'\n}\n"));
					center.add(tabs, "wrap");

					JPanel problems = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[][grow,fill]"));
					JPanel ph = new JPanel(new MigLayout("insets 3 6 3 6, gap 10"));
					JLabel pt = new JLabel("Problems");
					pt.setFont(pt.getFont().deriveFont(Font.BOLD));
					ph.add(pt);
					ph.add(new JLabel("2 errors", new Glyph("cross", RED, 12), SwingConstants.LEADING));
					ph.add(new JLabel("5 warnings", new Glyph("warn", ORANGE, 12), SwingConstants.LEADING));
					ph.add(new JCheckBox("Current file only"), "gapbefore push");
					problems.add(ph, "wrap");
					JList<String> pl2 = new JList<>(new String[] {
							"Main.java:9  cannot find symbol: Lists",
							"Main.java:10  ';' expected",
							"Config.java:3  parameter 'verbose' is never used",
							"build.gradle:2  plugin version not declared"});
					pl2.setVisibleRowCount(4);
					problems.add(new JScrollPane(pl2));
					center.add(problems);
					root.add(center);

					JPanel status = new JPanel(new MigLayout("insets 2 8 2 8, gap 10", "[grow][][][][][]"));
					status.add(new JLabel("Indexing finished in 2.4 s"));
					status.add(new JSeparator(SwingConstants.VERTICAL), "growy");
					status.add(new JLabel("12:8"));
					status.add(new JLabel("LF"));
					status.add(new JLabel("UTF-8"));
					JProgressBar mem = new JProgressBar(0, 1024);
					mem.setValue(412);
					mem.setStringPainted(true);
					mem.setString("412 of 1024 MB");
					status.add(mem, "w 140!");
					root.add(status, "dock south");
					return root;
				})
				.sizes()
				.step("second tab selected", c -> c.<JTabbedPane>find("tabs").setSelectedIndex(1))
				.step("maximized 1600x1000", c -> c.size(1600, 1000))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario databaseConnection()
	{
		return new Scenario("app-database-connection", DIALOGS, "Database connection",
				"A banner docked north, host and port on one row, an authentication choice, a hidden advanced section, and Test/OK/Cancel/Help by button tags.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 0, hidemode 3", "[grow,fill]", "[]"));
					JPanel banner = new JPanel(new MigLayout("insets 12 16 12 16", "[grow][]", "[][]"));
					banner.setBackground(tint(BLUE, 0.06));
					banner.setOpaque(true);
					JLabel bt = new JLabel("New connection");
					bt.setFont(bt.getFont().deriveFont(Font.BOLD, bt.getFont().getSize2D() + 3f));
					banner.add(bt);
					banner.add(new JLabel(new Glyph("ring", BLUE, 36)), "spany 2, wrap");
					JLabel bd = new JLabel("Enter where the server runs and how to sign in to it.");
					bd.setForeground(GREY);
					banner.add(bd);
					root.add(banner, "dock north");
					root.add(new JSeparator(), "dock north");

					JPanel form = new JPanel(new MigLayout("wrap 2, insets dialog, hidemode 3", "[right][grow,fill]", ""));
					form.add(new JLabel("Name:"));
					form.add(new JTextField("Staging (read only)", 24));
					form.add(new JLabel("Driver:"));
					form.add(combo("PostgreSQL", "MariaDB", "SQLite", "H2"));
					section(form, "Server", true);
					form.add(new JLabel("Host:"));
					form.add(new JTextField("db-staging.internal.example", 20), "split 3");
					form.add(new JLabel("Port:"), "gapleft unrel");
					form.add(new JSpinner(new SpinnerNumberModel(5432, 1, 65535, 1)), "w 80!");
					form.add(new JLabel("Database:"));
					form.add(new JTextField("inventory", 20));
					section(form, "Authentication", true);
					ButtonGroup g = new ButtonGroup();
					JRadioButton pw = new JRadioButton("User and password", true);
					JRadioButton os = new JRadioButton("Operating system account");
					g.add(pw);
					g.add(os);
					form.add(new JLabel("Method:"));
					form.add(pw, "split 2");
					form.add(os);
					form.add(new JLabel("User:"));
					form.add(new JTextField("reporting", 16));
					form.add(new JLabel("Password:"));
					form.add(new JPasswordField("hunter2hunter", 16), "split 2");
					form.add(new JCheckBox("Remember"), "growx 0");
					form.add(new JLabel("URL:"));
					JTextField url = new JTextField("jdbc:postgresql://db-staging.internal.example:5432/inventory");
					url.setEditable(false);
					form.add(url, "wmin 100");
					JCheckBox adv = new JCheckBox("Show advanced options");
					adv.setName("advToggle");
					form.add(adv, "skip");

					JPanel advanced = new JPanel(new MigLayout("wrap 2, insets 0", "[right][grow]"));
					advanced.setName("advanced");
					advanced.setVisible(false);
					advanced.add(new JLabel("Timeout:"));
					advanced.add(new JSpinner(new SpinnerNumberModel(30, 1, 600, 1)), "split 2, w 70!");
					advanced.add(new JLabel("seconds"));
					advanced.add(new JLabel("Encryption:"));
					advanced.add(combo("Prefer", "Require", "Verify full", "Off"), "growx");
					advanced.add(new JLabel("Properties:"), "aligny top");
					JTextArea props = new JTextArea("applicationName=reports\nreadOnly=true\nfetchSize=500", 3, 20);
					advanced.add(new JScrollPane(props), "growx");
					form.add(advanced, "span, growx, gapleft 20");

					JLabel result = new JLabel("<html><b>Connected.</b> PostgreSQL 16.4 answered in 38 ms.</html>", new Glyph("check", GREEN, 14), SwingConstants.LEADING);
					result.setName("result");
					result.setVisible(false);
					form.add(result, "span, left, gaptop para");
					root.add(form, "wrap");

					JPanel buttons = new JPanel(new MigLayout("insets 0 12 12 12", "[grow,fill]"));
					buttons.add(new JButton("Test connection"), "split 5, tag left");
					buttons.add(new JButton("OK"), "tag ok");
					buttons.add(new JButton("Cancel"), "tag cancel");
					buttons.add(new JButton("Help"), "tag help");
					root.add(buttons, "dock south");
					return root;
				})
				.sizes()
				.step("advanced options shown", c -> {
					c.<JCheckBox>find("advToggle").setSelected(true);
					c.<JComponent>find("advanced").setVisible(true);
					c.root.revalidate();
				})
				.step("connection tested", c -> {
					c.<JComponent>find("result").setVisible(true);
					c.root.revalidate();
				})
				.step("preferred size after both", StepContext::sizeToPreferred)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario wizard()
	{
		return new Scenario("app-wizard-page", DIALOGS, "Import wizard",
				"A step indicator of numbered circles joined by separators, two pages of which one is hidden, and Back/Next/Finish/Cancel ordered by tags.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog, hidemode 3", "[grow,fill]", "[][][grow,fill][][]"));
					JPanel steps = new JPanel(new MigLayout("insets 0, gap 6", "", "[]"));
					String[] names = {"Source", "Columns", "Rules", "Review"};
					for (int i = 0; i < names.length; i++) {
						if (i > 0)
							steps.add(new JSeparator(), "w 24:60:, growx, aligny center");
						JLabel s = new JLabel(names[i], new Glyph("num:" + (i + 1), i == 0 ? BLUE : GREY, 22), SwingConstants.LEADING);
						s.setName("step" + i);
						if (i == 0)
							s.setFont(s.getFont().deriveFont(Font.BOLD));
						steps.add(s);
					}
					root.add(steps, "wrap");
					root.add(new JSeparator(), "wrap");

					JPanel pages = new JPanel(new MigLayout("insets 0, hidemode 3, fill", "[grow,fill]", "[grow,fill]"));
					JPanel p1 = new JPanel(new MigLayout("wrap 2, insets 0", "[right][grow,fill]", "[][][][][grow,fill]"));
					p1.setName("page1");
					JLabel h1 = new JLabel("Where does the data come from?");
					h1.setFont(h1.getFont().deriveFont(Font.BOLD, h1.getFont().getSize2D() + 2f));
					p1.add(h1, "span, left, gapbottom 8");
					p1.add(new JLabel("File:"));
					p1.add(new JTextField("/data/exports/customers-2026.csv", 24), "split 2");
					p1.add(new JButton("Browse…"), "growx 0");
					p1.add(new JLabel("Encoding:"));
					p1.add(combo("UTF-8", "ISO-8859-1", "Windows-1252"));
					p1.add(new JLabel("Separator:"));
					ButtonGroup sg = new ButtonGroup();
					String[] seps = {"Comma", "Semicolon", "Tab", "Other:"};
					for (int i = 0; i < seps.length; i++) {
						JRadioButton r = new JRadioButton(seps[i], i == 1);
						sg.add(r);
						p1.add(r, i == 0 ? "split 5" : "");
					}
					p1.add(new JTextField("|", 3), "growx 0");
					p1.add(new JLabel("Preview:"), "aligny top");
					JTextArea prev = new JTextArea("id;name;city;since\n1;Okafor;Lagos;2019\n2;Lindqvist;Umeå;2021\n3;Takahashi;Sapporo;2022", 5, 30);
					prev.setFont(new Font(Font.MONOSPACED, Font.PLAIN, prev.getFont().getSize()));
					p1.add(new JScrollPane(prev));
					pages.add(p1, "wrap");

					JPanel p2 = new JPanel(new MigLayout("wrap 3, insets 0", "[][grow,fill][]", ""));
					p2.setName("page2");
					p2.setVisible(false);
					JLabel h2 = new JLabel("Which columns do you want to import?");
					h2.setFont(h2.getFont().deriveFont(Font.BOLD, h2.getFont().getSize2D() + 2f));
					p2.add(h2, "span, gapbottom 8");
					String[][] cols = {{"id", "Number"}, {"name", "Text"}, {"city", "Text"}, {"since", "Year"}};
					for (String[] col : cols) {
						p2.add(new JCheckBox(col[0], !col[0].equals("since")));
						p2.add(new JTextField(Character.toUpperCase(col[0].charAt(0)) + col[0].substring(1), 14));
						p2.add(combo(col[1], "Text", "Number", "Date"), "sg type");
					}
					p2.add(new JLabel("<html><font color='#7A7F87'>Unticked columns are skipped. Types can be changed later in the table settings.</font></html>"), "span, wmin 0");
					pages.add(p2);
					root.add(pages, "wrap");

					root.add(new JSeparator(), "wrap, gaptop para");
					JButton back = new JButton("< Back");
					back.setEnabled(false);
					back.setName("back");
					root.add(back, "split 4, tag back");
					root.add(new JButton("Next >"), "tag next");
					root.add(new JButton("Finish"), "tag finish");
					root.add(new JButton("Cancel"), "tag cancel");
					return root;
				})
				.sizes()
				.step("next page", c -> {
					c.<JComponent>find("page1").setVisible(false);
					c.<JComponent>find("page2").setVisible(true);
					c.<JComponent>find("back").setEnabled(true);
					JLabel s0 = c.find("step0");
					JLabel s1 = c.find("step1");
					s0.setFont(s1.getFont());
					s0.setIcon(new Glyph("check", GREEN, 22));
					s1.setFont(s1.getFont().deriveFont(Font.BOLD));
					s1.setIcon(new Glyph("num:2", BLUE, 22));
					c.root.revalidate();
				})
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario printDialog()
	{
		return new Scenario("app-print-dialog", DIALOGS, "Print dialog",
				"Titled sections for printer, range, copies, orientation, paper and margins in a two column grid, with radio groups, spinners and combo boxes.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog", "[grow,fill][grow,fill]", "[][grow,fill][grow,fill][]"));
					JPanel printer = titled("Printer", new MigLayout("wrap 3", "[right][grow,fill][]"));
					printer.add(new JLabel("Name:"));
					printer.add(combo("Office laser (2nd floor)", "Photo inkjet", "Save as PDF"));
					printer.add(new JButton("Properties…"));
					printer.add(new JLabel("Status:"));
					printer.add(new JLabel("Ready, 1 job waiting", new Glyph("circle", GREEN, 8), SwingConstants.LEADING), "span 2");
					printer.add(new JLabel("Location:"));
					printer.add(new JLabel("Room 2.14, next to the kitchen"), "span 2");
					printer.add(new JCheckBox("Print to file"), "skip, span 2");
					root.add(printer, "span, wrap");

					JPanel range = titled("Print range", new MigLayout("wrap 1", "[]"));
					ButtonGroup rg = new ButtonGroup();
					JRadioButton all = new JRadioButton("All 24 pages", true);
					JRadioButton pages = new JRadioButton("Pages");
					JRadioButton sel = new JRadioButton("Selection");
					JRadioButton cur = new JRadioButton("Current page");
					for (JRadioButton r : new JRadioButton[] {all, pages, sel, cur})
						rg.add(r);
					range.add(all);
					range.add(pages, "split 4");
					range.add(new JSpinner(new SpinnerNumberModel(1, 1, 24, 1)), "w 56!");
					range.add(new JLabel("to"));
					range.add(new JSpinner(new SpinnerNumberModel(24, 1, 24, 1)), "w 56!");
					range.add(sel);
					range.add(cur);
					root.add(range);

					JPanel copies = titled("Copies", new MigLayout("wrap 2", "[][grow]"));
					copies.add(new JLabel("Number of copies:"));
					copies.add(new JSpinner(new SpinnerNumberModel(2, 1, 99, 1)), "w 60!");
					copies.add(new JLabel(new Glyph("pages", GREY, 40)), "span 2, split 2");
					copies.add(new JCheckBox("Collate", true));
					copies.add(new JCheckBox("Reverse order"), "span 2");
					root.add(copies, "wrap");

					JPanel orient = titled("Orientation", new MigLayout("wrap 2", "[][]"));
					ButtonGroup og = new ButtonGroup();
					JRadioButton port = new JRadioButton("Portrait", true);
					JRadioButton land = new JRadioButton("Landscape");
					og.add(port);
					og.add(land);
					orient.add(new JLabel(new Glyph("page", GREY, 18, 24)));
					orient.add(port);
					orient.add(new JLabel(new Glyph("page", GREY, 24, 18)));
					orient.add(land);
					JPanel paper = titled("Paper", new MigLayout("wrap 2", "[right][grow,fill]"));
					paper.add(new JLabel("Size:"));
					paper.add(combo("A4 (210 × 297 mm)", "Letter", "A5", "Envelope DL"));
					paper.add(new JLabel("Source:"));
					paper.add(combo("Automatic", "Tray 1", "Manual feed"));
					paper.add(new JLabel("Sides:"));
					paper.add(combo("One side", "Both sides, long edge", "Both sides, short edge"));
					JPanel lower = new JPanel(new MigLayout("insets 0", "[][grow,fill]", "[grow,fill]"));
					lower.add(orient, "growy");
					lower.add(paper);
					root.add(lower, "span, wrap");

					JPanel margins = titled("Margins (mm)", new MigLayout("", "[right][60!][right][60!][right][60!][right][60!]"));
					for (String m : new String[] {"Top:", "Bottom:", "Left:", "Right:"}) {
						margins.add(new JLabel(m), "gapleft 8");
						margins.add(new JSpinner(new SpinnerNumberModel(15, 0, 50, 1)));
					}
					root.add(margins, "span, wrap");

					root.add(new JButton("Preview"), "span, split 3, tag left");
					root.add(new JButton("Print"), "tag ok");
					root.add(new JButton("Cancel"), "tag cancel");
					return root;
				})
				.sizes()
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario findReplace()
	{
		return new Scenario("app-find-replace", DIALOGS, "Find and replace",
				"A compact dialog whose replace row is hidden until switched on, options in two columns, a titled direction group and a column of equally sized buttons.",
				() -> {
					JPanel p = new JPanel(new MigLayout("wrap 3, insets dialog, hidemode 3", "[right][grow,fill][fill,sg btn]", "[][][][grow]"));
					p.add(new JLabel("Find:"));
					JComboBox<String> find = new JComboBox<>(new String[] {"layoutContainer", "invalidate", "getBaseline"});
					find.setEditable(true);
					find.setName("find");
					p.add(find, "w 220:260:");
					p.add(new JButton("Find next"));
					JLabel rl = new JLabel("Replace:");
					rl.setName("replLabel");
					rl.setVisible(false);
					p.add(rl);
					JComboBox<String> repl = new JComboBox<>(new String[] {"layout"});
					repl.setEditable(true);
					repl.setName("repl");
					repl.setVisible(false);
					p.add(repl);
					JButton rb = new JButton("Replace");
					rb.setName("replBtn");
					rb.setVisible(false);
					p.add(rb);

					JPanel opts = new JPanel(new MigLayout("wrap 2, insets 0", "[][]"));
					opts.add(new JCheckBox("Match case"));
					opts.add(new JCheckBox("Whole words", true));
					opts.add(new JCheckBox("Regular expression"));
					opts.add(new JCheckBox("Wrap around", true));
					JCheckBox replMode = new JCheckBox("Replace");
					replMode.setName("replMode");
					opts.add(replMode);
					opts.add(new JCheckBox("In selection only"));
					p.add(opts, "skip, spany 2, aligny top, growx 0");
					JButton all = new JButton("Replace all");
					all.setName("replAll");
					all.setVisible(false);
					p.add(all);
					p.add(new JButton("Find all"), "skip");
					JPanel dir = titled("Direction", new MigLayout("insets 2 6 4 6"));
					ButtonGroup g = new ButtonGroup();
					JRadioButton fwd = new JRadioButton("Forward", true);
					JRadioButton bwd = new JRadioButton("Backward");
					g.add(fwd);
					g.add(bwd);
					dir.add(fwd);
					dir.add(bwd);
					p.add(dir, "skip, growx 0, left");
					p.add(new JButton("Close"), "aligny bottom");
					JLabel status = new JLabel("3 matches in Layout.java");
					status.setName("status");
					status.setForeground(GREY);
					p.add(status, "span, left");
					return p;
				})
				.sizes()
				.step("replace mode", c -> {
					c.<JCheckBox>find("replMode").setSelected(true);
					for (String n : new String[] {"replLabel", "repl", "replBtn", "replAll"})
						c.<JComponent>find(n).setVisible(true);
					c.root.revalidate();
				})
				.step("long search term, no match", c -> {
					c.<JComboBox<String>>find("find").getEditor().setItem("protected static int[] calculateBaselineOffsetsForEveryRow");
					JLabel s = c.find("status");
					s.setText("No match. The search reached the end of the document and wrapped around once.");
					s.setForeground(RED);
					s.revalidate();
				})
				.step("preferred size again", StepContext::sizeToPreferred);
	}

	private static Scenario chat()
	{
		return new Scenario("app-chat-window", WINDOWS, "Chat conversation",
				"Message bubbles as nested panels with width-limited HTML text and a wrapping text area, aligned left or right in a scrolled list that tracks the viewport width, with a header and an input row.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[grow,fill]"));
					JPanel head = new JPanel(new MigLayout("insets 8 12 8 8, gap 8", "[][grow][][]", "[][]"));
					head.add(new JLabel(new Glyph("text:LM", PURPLE, 36)), "spany 2");
					JLabel who = new JLabel("Lena Marchetti");
					who.setFont(who.getFont().deriveFont(Font.BOLD, who.getFont().getSize2D() + 2f));
					head.add(who);
					head.add(new JButton(new Glyph("bell", GREY, 16)), "spany 2");
					head.add(new JButton(new Glyph("gear", GREY, 16)), "spany 2, wrap");
					JLabel online = new JLabel("online", new Glyph("circle", GREEN, 8), SwingConstants.LEADING);
					online.setForeground(GREY);
					head.add(online, "skip");
					root.add(head, "dock north");
					root.add(new JSeparator(), "dock north");

					WidthTrackingPanel msgs = new WidthTrackingPanel(new MigLayout("wrap 1, fillx, insets 10 12 10 12, gapy 8", "[grow]"));
					msgs.setName("messages");
					msgs.add(dayDivider("Yesterday"), "growx");
					msgs.add(bubble("Did the venue confirm the projector for Friday?", "18:02", false), "left");
					msgs.add(bubble("Yes! They also offered a second room for the workshop, if we want it. "
							+ "It is a bit smaller, about twenty seats, but it has a whiteboard wall.", "18:05", true), "right");
					msgs.add(dayDivider("Today"), "growx");
					msgs.add(bubble("Perfect. Let's take it. I'll update the schedule and send it round.", "09:12", false), "left");
					JPanel codeBubble = bubbleShell(false);
					JTextArea snippet = new JTextArea("09:30  Welcome\n10:00  Keynote (main hall)\n11:15  Workshop A (room 2)\n13:00  Lunch");
					snippet.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
					snippet.setEditable(false);
					snippet.setOpaque(false);
					snippet.setForeground(Color.BLACK);
					codeBubble.add(snippet, "growx");
					codeBubble.add(stamp("09:14"), "right");
					msgs.add(codeBubble, "left, wmax 80%");
					JPanel wrapBubble = bubbleShell(true);
					JTextArea wrapText = new JTextArea("Looks good. Could the workshop move to 11:30? A few people come by train and the first one only arrives at 11.");
					wrapText.setLineWrap(true);
					wrapText.setWrapStyleWord(true);
					wrapText.setEditable(false);
					wrapText.setOpaque(false);
					wrapText.setForeground(Color.BLACK);
					wrapText.setColumns(22);
					wrapBubble.add(wrapText, "growx, wmin 120");
					wrapBubble.add(stamp("09:20  ✓✓"), "right");
					msgs.add(wrapBubble, "right, wmax 70%");
					JScrollPane sp = new JScrollPane(msgs, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					sp.setBorder(BorderFactory.createEmptyBorder());
					root.add(sp, "w 260:420:, h 200:420:");

					JPanel input = new JPanel(new MigLayout("insets 8, gap 6", "[][grow,fill][]", "[]"));
					input.add(new JButton(new Glyph("file", GREY, 16)), "aligny bottom");
					JTextArea typing = new JTextArea("Sure, I'll ask them", 2, 20);
					typing.setLineWrap(true);
					typing.setWrapStyleWord(true);
					typing.setName("typing");
					input.add(new JScrollPane(typing));
					JButton send = new JButton("Send", new Glyph("play", BLUE, 12));
					input.add(send, "aligny bottom");
					root.add(input, "dock south");
					root.add(new JSeparator(), "dock south");
					return root;
				})
				.sizes()
				.step("a new message arrives", c -> {
					JPanel m = c.find("messages");
					m.add(bubble("<b>Update:</b> the caterer needs the final head count by <u>Wednesday noon</u>. "
							+ "Vegetarian options are included; tell me about any allergies.", "09:31", false), "left");
					m.revalidate();
				})
				.step("long reply typed", c -> {
					JTextArea t = c.find("typing");
					t.setText("Sure, I'll ask them. Two people mentioned nut allergies last time, and one prefers vegan food, so I'll add that straight away.");
					t.revalidate();
				})
				.step("narrow phone-like 320x560", c -> c.size(320, 560))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario fileProperties()
	{
		return new Scenario("app-file-properties", DIALOGS, "File properties",
				"Tabs for general facts, a permissions grid of check boxes and a details sheet, each a MigLayout form, with OK/Cancel/Apply by tags.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog, fill", "[grow,fill]", "[grow,fill][]"));
					JTabbedPane tabs = new JTabbedPane();
					tabs.setName("tabs");

					JPanel gen = new JPanel(new MigLayout("wrap 2, insets 12", "[right][grow,fill]", ""));
					gen.add(new JLabel(new Glyph("file", BLUE, 40)), "right");
					gen.add(new JTextField("annual-budget-2027-draft.xlsx", 22));
					gen.add(new JSeparator(), "span, growx, gapy 6 6");
					String[][] facts = {
							{"Type:", "Spreadsheet (.xlsx)"},
							{"Opens with:", "Tables 7"},
							{"Location:", "/home/shared/finance/budgets/2027"},
							{"Size:", "2.31 MB (2,422,784 bytes)"},
							{"On disk:", "2.32 MB (2,428,928 bytes)"}};
					for (String[] f : facts) {
						gen.add(new JLabel(f[0]));
						gen.add(new JLabel(f[1]));
					}
					gen.add(new JSeparator(), "span, growx, gapy 6 6");
					for (String[] f : new String[][] {{"Created:", "3 March 2026, 09:14"}, {"Modified:", "21 November 2026, 16:40"}, {"Accessed:", "24 November 2026, 08:02"}}) {
						gen.add(new JLabel(f[0]));
						gen.add(new JLabel(f[1]));
					}
					gen.add(new JSeparator(), "span, growx, gapy 6 6");
					gen.add(new JLabel("Attributes:"));
					gen.add(new JCheckBox("Read-only"), "split 3");
					gen.add(new JCheckBox("Hidden"));
					gen.add(new JButton("Advanced…"), "gapbefore push");
					tabs.addTab("General", gen);

					JPanel perm = new JPanel(new MigLayout("wrap 4, insets 12", "[][center,sg p][center,sg p][center,sg p]", ""));
					perm.add(new JLabel("Owner:"));
					perm.add(combo("finance (you)", "root", "backup"), "span 3, growx");
					perm.add(new JLabel("Group:"));
					perm.add(combo("budget-team", "staff", "everyone"), "span 3, growx");
					perm.add(new JSeparator(), "span, growx, gapy 6 6");
					perm.add(new JLabel(""));
					for (String h : new String[] {"Read", "Write", "Execute"}) {
						JLabel l = new JLabel(h);
						l.setFont(l.getFont().deriveFont(Font.BOLD));
						perm.add(l);
					}
					String[] who = {"Owner", "Group", "Others"};
					boolean[][] bits = {{true, true, false}, {true, true, false}, {true, false, false}};
					for (int i = 0; i < who.length; i++) {
						perm.add(new JLabel(who[i]));
						for (int j = 0; j < 3; j++)
							perm.add(new JCheckBox("", bits[i][j]));
					}
					perm.add(new JLabel("<html>Others are everyone with an account on this computer who is <i>not</i> in the group.</html>"), "span, wmin 0, w 10:300:, gaptop para");
					perm.add(new JButton("Apply to enclosed files"), "span, right, gaptop para");
					tabs.addTab("Permissions", perm);

					JPanel det = new JPanel(new MigLayout("wrap 2, insets 12, gapy 2", "[150,fill][grow,fill]", ""));
					String[][] rows = {
							{"#Document", null}, {"Title", "Budget 2027"}, {"Author", "Finance team"}, {"Sheets", "12"}, {"Formulas", "1,408"},
							{"#Origin", null}, {"Application", "Tables 7.2"}, {"Template", "Annual plan"}, {"Last printed", "never"},
							{"#Content", null}, {"Keywords", "budget, plan, draft"}, {"Comments", "3 unresolved"}};
					for (String[] r : rows) {
						if (r[1] == null) {
							JLabel h = new JLabel(r[0].substring(1));
							h.setFont(h.getFont().deriveFont(Font.BOLD));
							det.add(h, "span, gaptop 8");
						} else {
							JLabel k = new JLabel(r[0]);
							k.setForeground(GREY);
							det.add(k, "gapleft 12");
							det.add(new JLabel(r[1]));
						}
					}
					tabs.addTab("Details", det);
					root.add(tabs, "wrap");
					root.add(new JButton("OK"), "split 3, tag ok");
					root.add(new JButton("Cancel"), "tag cancel");
					root.add(new JButton("Apply"), "tag apply");
					return root;
				})
				.sizes()
				.step("permissions tab", c -> c.<JTabbedPane>find("tabs").setSelectedIndex(1))
				.step("details tab", c -> c.<JTabbedPane>find("tabs").setSelectedIndex(2))
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario invoice()
	{
		return new Scenario("app-invoice-entry", FORMS, "Invoice entry",
				"A customer and dates header in two form columns, a line item table that grows, totals right-aligned with a size group, and notes beside them.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog", "[grow,fill]", "[][grow,fill][][]"));
					JLabel title = new JLabel("Invoice INV-2026-0418");
					title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 6f));
					JLabel state = new JLabel("DRAFT");
					state.setForeground(ORANGE);
					state.setFont(state.getFont().deriveFont(Font.BOLD));
					JPanel titleRow = new JPanel(new MigLayout("insets 0", "[]12[grow]"));
					titleRow.add(title);
					titleRow.add(state);
					root.add(titleRow, "wrap");

					JPanel head = new JPanel(new MigLayout("insets 0, wrap 4", "[right][grow,fill]para[right][grow 50,fill]", ""));
					head.add(new JLabel("Customer:"));
					JTextField cust = new JTextField("Hollis & Daughters Joinery", 22);
					cust.setName("customer");
					head.add(cust, "split 2");
					head.add(new JButton("…"), "growx 0");
					head.add(new JLabel("Date:"));
					head.add(new JTextField("24.11.2026", 8));
					head.add(new JLabel("Address:"), "aligny top");
					JTextArea addr = new JTextArea("Unit 4, Mill Lane\nBrackwell BW3 9QT\nUnited Kingdom", 3, 22);
					head.add(new JScrollPane(addr), "spany 2");
					head.add(new JLabel("Due:"));
					head.add(combo("in 14 days", "in 30 days", "on receipt"));
					head.add(new JLabel("Currency:"), "skip");
					head.add(combo("GBP", "EUR", "USD"));
					root.add(head, "wrap, gapbottom para");

					DefaultTableModel m = new DefaultTableModel(new Object[] {"#", "Item", "Qty", "Unit", "Price", "Amount"}, 0);
					m.addRow(new Object[] {"1", "Oak worktop, 3 m, oiled", "2", "pcs", "410.00", "820.00"});
					m.addRow(new Object[] {"2", "Fitting and levelling", "6.5", "h", "48.00", "312.00"});
					m.addRow(new Object[] {"3", "Brass hinges, soft close", "12", "pcs", "9.80", "117.60"});
					m.addRow(new Object[] {"4", "Delivery within 30 km", "1", "flat", "45.00", "45.00"});
					JTable t = new JTable(m);
					t.setName("items");
					t.setPreferredScrollableViewportSize(new Dimension(520, 7 * t.getRowHeight()));
					int[] widths = {24, 220, 50, 44, 70, 80};
					for (int i = 0; i < widths.length; i++)
						t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
					root.add(new JScrollPane(t), "wrap");

					JPanel lineButtons = new JPanel(new MigLayout("insets 0", "[][]unrel[grow]"));
					lineButtons.add(new JButton("Add line"));
					lineButtons.add(new JButton("Remove"));
					JLabel count = new JLabel("4 lines");
					count.setName("count");
					count.setForeground(GREY);
					lineButtons.add(count);
					root.add(lineButtons, "wrap");

					JPanel bottom = new JPanel(new MigLayout("insets 0", "[grow,fill]para[]", "[top]"));
					JPanel notes = titled("Notes", new MigLayout("fill, insets 4", "[grow,fill]", "[grow,fill]"));
					JTextArea na = new JTextArea("Payment by bank transfer. Please quote the invoice number. Thank you for your custom!", 4, 24);
					na.setLineWrap(true);
					na.setWrapStyleWord(true);
					notes.add(new JScrollPane(na));
					bottom.add(notes, "growy");
					JPanel totals = new JPanel(new MigLayout("wrap 2, insets 0", "[right][right,sg amt]", ""));
					totals.add(new JLabel("Subtotal:"));
					totals.add(amount("1,294.60", "subtotal"));
					totals.add(new JLabel("Discount:"));
					totals.add(new JSpinner(new SpinnerNumberModel(5, 0, 100, 1)), "split 2, w 56!");
					totals.add(new JLabel("%"));
					totals.add(new JLabel("VAT 20%:"));
					totals.add(amount("245.97", "vat"));
					totals.add(new JSeparator(), "span, growx");
					JLabel tl = new JLabel("Total:");
					tl.setFont(tl.getFont().deriveFont(Font.BOLD, tl.getFont().getSize2D() + 3f));
					totals.add(tl);
					JLabel total = amount("£ 1,475.84", "total");
					total.setFont(tl.getFont());
					totals.add(total);
					bottom.add(totals);
					root.add(bottom, "wrap, gaptop para");

					root.add(new JButton("Save draft"), "split 3, tag left");
					root.add(new JButton("Send invoice"), "tag ok");
					root.add(new JButton("Cancel"), "tag cancel");
					return root;
				})
				.sizes()
				.step("line added, totals updated", c -> {
					JTable t = c.find("items");
					((DefaultTableModel) t.getModel()).addRow(new Object[] {"5", "Bespoke shelf unit, walnut veneer", "1", "pcs", "1,180.00", "1,180.00"});
					c.<JLabel>find("subtotal").setText("2,474.60");
					c.<JLabel>find("count").setText("5 lines");
					c.<JLabel>find("vat").setText("470.17");
					c.<JLabel>find("total").setText("£ 2,821.04");
					c.root.revalidate();
				})
				.step("long customer name", c -> c.<JTextField>find("customer").setText("Hollis & Daughters Joinery and Restoration Workshop Ltd."))
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("maximized 1600x1000", c -> c.size(1600, 1000));
	}

	private static Scenario dashboard()
	{
		return new Scenario("app-dashboard-cards", WINDOWS, "Dashboard of cards",
				"A header with a pushed period selector over a grid of size-grouped cards with large figures, coloured deltas, progress bars, a sparkline and a ranked list.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 16, wrap 3, gap 12", "[grow,fill,sg card][grow,fill,sg card][grow,fill,sg card]", "[][grow,fill][grow,fill]"));
					JPanel head = new JPanel(new MigLayout("insets 0", "[][grow][][]"));
					JLabel h = new JLabel("Overview");
					h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
					head.add(h);
					JLabel sub = new JLabel("Store front, all regions");
					sub.setForeground(GREY);
					head.add(sub, "gapleft 12, aligny baseline");
					head.add(combo("Last 7 days", "Last 30 days", "This quarter"));
					head.add(new JButton("Refresh"));
					root.add(head, "span");

					root.add(card("Revenue", "€ 48,210", "+12.4%", true, 72, "of monthly target"));
					root.add(card("Orders", "1,284", "+3.1%", true, 55, "of monthly target"));
					root.add(card("Returns", "37", "-8.0%", false, 18, "of last month's"));

					JPanel trend = cardShell("Visitors per day");
					JLabel big = new JLabel("9,412");
					big.setFont(big.getFont().deriveFont(Font.BOLD, 30f));
					big.setName("visitors");
					JPanel figure = new JPanel(new MigLayout("insets 0", "[]6[grow]"));
					figure.add(big);
					JLabel avg = new JLabel("per day, on average");
					avg.setForeground(GREY);
					figure.add(avg);
					trend.add(figure);
					JLabel spark = new JLabel(new Glyph("spark", TEAL, 260, 60));
					spark.setHorizontalAlignment(SwingConstants.LEADING);
					trend.add(spark, "wmin 60");
					trend.add(new JLabel("<html><font color='#7A7F87'>Peak on Saturday, lowest on Tuesday morning.</font></html>"), "wmin 0");
					root.add(trend, "span 2");

					JPanel top = cardShell("Top regions");
					String[] names = {"North", "Coast", "Capital", "Lakes"};
					int[] vals = {41, 27, 19, 13};
					for (int i = 0; i < names.length; i++) {
						top.add(new JLabel(names[i]), "split 3, w 60!");
						JProgressBar pb = new JProgressBar(0, 100);
						pb.setValue(vals[i]);
						top.add(pb, "growx, wmin 40");
						top.add(new JLabel(vals[i] + "%"), "w 36!, right");
					}
					root.add(top);
					return root;
				})
				.sizes()
				.step("figures updated", c -> {
					c.<JLabel>find("visitors").setText("12,906,118");
					c.root.revalidate();
				})
				.step("maximized 1600x1000", c -> c.size(1600, 1000))
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario login()
	{
		return new Scenario("app-login-screen", WINDOWS, "Sign-in screen",
				"A card centred in the window by push gaps, with a logo, fields, a remember/forgot row split by push, a hidden error and a divider with text.",
				() -> {
					JPanel root = new JPanel(new MigLayout("fill, insets 0", "[grow,center]", "[grow,center]"));
					JPanel card = new JPanel(new MigLayout("wrap 1, insets 24 28 24 28, hidemode 3", "[260:300:,fill]", ""));
					card.setBorder(BorderFactory.createLineBorder(new Color(0xC5CAD3), 1, true));
					card.add(new JLabel(new Glyph("star", BLUE, 40)), "center");
					JLabel t = new JLabel("Welcome back");
					t.setFont(t.getFont().deriveFont(Font.BOLD, 20f));
					card.add(t, "center, growx 0");
					JLabel s = new JLabel("Sign in to continue to your workspace");
					s.setForeground(GREY);
					card.add(s, "center, growx 0, gapbottom para");
					card.add(new JLabel("E-mail or user name"));
					card.add(new JTextField("noor.haddad", 20));
					card.add(new JLabel("Password"), "gaptop 6");
					card.add(new JPasswordField("password", 20));
					JLabel err = new JLabel("<html><body style='width:220px'>That password is not right. <b>2 attempts</b> left before the account is locked for 15 minutes.</body></html>", new Glyph("warn", RED, 14), SwingConstants.LEADING);
					err.setForeground(RED);
					err.setName("error");
					err.setVisible(false);
					card.add(err);
					card.add(new JCheckBox("Keep me signed in"), "split 2, growx 0");
					card.add(new JLabel("<html><a href='#'>Forgot password?</a></html>"), "gapbefore push, growx 0");
					JButton go = new JButton("Sign in");
					go.setFont(go.getFont().deriveFont(Font.BOLD));
					card.add(go, "h 32!, gaptop para");
					card.add(new JSeparator(), "split 3, growx, aligny center, gaptop para");
					card.add(new JLabel("or"), "growx 0, gaptop para");
					card.add(new JSeparator(), "growx, aligny center, gaptop para");
					card.add(new JButton("Continue with company account", new Glyph("square", TEAL, 14)));
					root.add(card);
					JLabel foot = new JLabel("Version 3.8.1  ·  Privacy  ·  Help");
					foot.setForeground(GREY);
					root.add(foot, "dock south, center, gapbottom 8");
					return root;
				})
				.sizes()
				.step("wrong password", c -> {
					c.<JComponent>find("error").setVisible(true);
					c.root.revalidate();
				})
				.step("maximized 1600x1000", c -> c.size(1600, 1000))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario calendar()
	{
		return new Scenario("app-calendar-month", WINDOWS, "Month calendar with agenda",
				"A month grid of size-grouped toggle buttons under weekday headers and a navigation row, beside an agenda list of the selected day.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog, gap 16", "[grow,fill][200:240:,fill]", "[grow,fill]"));
					JPanel month = new JPanel(new MigLayout("wrap 7, insets 0, gap 2", "[grow,fill,sg day]", "[][][grow,fill,sg week]"));
					month.add(new JButton(new Glyph("prev", GREY, 12)), "split 4, span, growx 0");
					JLabel ml = new JLabel("November 2026");
					ml.setFont(ml.getFont().deriveFont(Font.BOLD, ml.getFont().getSize2D() + 4f));
					month.add(ml, "growx, gapleft 8");
					month.add(new JButton(new Glyph("next", GREY, 12)), "growx 0");
					month.add(new JButton("Today"), "growx 0");
					for (String d : new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}) {
						JLabel l = new JLabel(d, SwingConstants.CENTER);
						l.setForeground(d.startsWith("S") ? RED : GREY);
						month.add(l, "gaptop 8");
					}
					ButtonGroup g = new ButtonGroup();
					int day = 26;
					boolean current = false;
					for (int i = 0; i < 42; i++) {
						JToggleButton b = new JToggleButton(String.valueOf(day));
						if (!current)
							b.setEnabled(false);
						if (current && (day == 3 || day == 12 || day == 24 || day == 27))
							b.setText("<html><center>" + day + "<br><font color='#2F6FD6'>•</font></center></html>");
						if (current && day == 24)
							b.setSelected(true);
						g.add(b);
						month.add(b, "h 36::, w 36::");
						day++;
						if (!current && day == 32) {
							day = 1;
							current = true;
						} else if (current && day == 31) {
							day = 1;
							current = false;
						}
					}
					root.add(month);

					JPanel agenda = new JPanel(new MigLayout("wrap 2, insets 0", "[][grow,fill]", ""));
					JLabel ah = new JLabel("Tuesday, 24 November");
					ah.setFont(ah.getFont().deriveFont(Font.BOLD));
					agenda.add(ah, "span");
					agenda.add(new JSeparator(), "span, growx");
					String[][] items = {
							{"09:00", "Stand-up", "#2F6FD6"},
							{"11:30", "Design review: onboarding flow", "#7B4FC9"},
							{"13:00", "Lunch with Priya", "#2E9E5B"},
							{"16:00", "Dentist (leave at 15:40)", "#C8372D"}};
					for (String[] it : items) {
						JLabel time = new JLabel(it[0]);
						time.setForeground(GREY);
						agenda.add(time, "aligny top");
						agenda.add(new JLabel("<html><font color='" + it[2] + "'>●</font> " + it[1] + "</html>"), "wmin 0");
					}
					agenda.add(new JButton("New event…"), "span, gaptop para, growx 0, left");
					root.add(agenda, "aligny top");
					return root;
				})
				.sizes()
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("maximized 1600x1000", c -> c.size(1600, 1000))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario musicPlayer()
	{
		return new Scenario("app-music-player-bar", WINDOWS, "Music player bar",
				"A single wide bar: cover art, a two-line title, centred transport buttons over a seek slider with times, and a volume control pushed right.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 8 12 8 12, gap 10", "[][150:220:260][grow,center][][]", "[center]"));
					root.add(new JLabel(new Glyph("square", PURPLE, 52)));
					JPanel info = new JPanel(new MigLayout("insets 0, gap 0, wrap 1", "[grow,fill]"));
					JLabel title = new JLabel("Harbour Lights");
					title.setName("title");
					title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 1f));
					info.add(title, "wmin 0");
					JLabel artist = new JLabel("The Paper Kites of Elm Street");
					artist.setForeground(GREY);
					info.add(artist, "wmin 0");
					root.add(info, "growx");

					JPanel center = new JPanel(new MigLayout("insets 0, gap 4, wrap 1", "[grow,fill]", "[][]"));
					JPanel buttons = new JPanel(new MigLayout("insets 0, gap 6", "push[sg b][sg b][][sg b][sg b]push"));
					buttons.add(new JToggleButton(new Glyph("cross", GREY, 14)));
					buttons.add(new JButton(new Glyph("prev", GREY, 16)));
					JButton play = new JButton(new Glyph("pause", BLUE, 24));
					buttons.add(play);
					buttons.add(new JButton(new Glyph("next", GREY, 16)));
					buttons.add(new JToggleButton(new Glyph("ring", GREY, 14), true));
					center.add(buttons);
					JLabel pos = new JLabel("1:24");
					pos.setForeground(GREY);
					center.add(pos, "split 3, growx 0");
					JSlider seek = new JSlider(0, 237, 84);
					seek.setName("seek");
					center.add(seek, "growx, w 200:420:");
					JLabel len = new JLabel("3:57");
					len.setName("length");
					len.setForeground(GREY);
					center.add(len, "growx 0");
					root.add(center, "growx");

					root.add(new JLabel(new Glyph("speaker", GREY, 18)), "split 2, gapbefore push");
					root.add(new JSlider(0, 100, 65), "w 80:110:");
					root.add(new JToggleButton("Queue"));
					return root;
				})
				.sizes()
				.step("next track with a long title", c -> {
					c.<JLabel>find("title").setText("A Very Long Song Title (Extended Live Version, Recorded at the Old Harbour Hall)");
					c.<JLabel>find("length").setText("12:48");
					c.<JSlider>find("seek").setMaximum(768);
					c.root.revalidate();
				})
				.step("full width 1600x90", c -> c.size(1600, 90))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario checkoutRtl()
	{
		return new Scenario("app-checkout-rtl", I18N, "Checkout form in Arabic (right to left)",
				"A shipping and payment form in Arabic with right-to-left component orientation, a split city/postcode row, payment radios and a titled order summary.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog, gap 16, hidemode 3", "[grow,fill][220::,fill]", "[top]"));
					JPanel form = new JPanel(new MigLayout("wrap 2, insets 0", "[trailing][grow,fill]", ""));
					JLabel h = new JLabel("إتمام الطلب");
					h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
					form.add(h, "span, leading, gapbottom 8");
					section(form, "عنوان الشحن", false);
					form.add(new JLabel("الاسم الكامل:"));
					form.add(new JTextField("سارة الخطيب", 20));
					form.add(new JLabel("البريد الإلكتروني:"));
					form.add(new JTextField("sara@example.org", 20));
					form.add(new JLabel("العنوان:"), "aligny top");
					JTextArea addr = new JTextArea("شارع الحدائق ١٢، الطابق الثالث", 2, 20);
					addr.setLineWrap(true);
					addr.setWrapStyleWord(true);
					addr.setName("address");
					form.add(new JScrollPane(addr));
					form.add(new JLabel("المدينة:"));
					form.add(new JTextField("عمّان", 10), "split 3");
					form.add(new JLabel("الرمز البريدي:"), "gapleft unrel");
					form.add(new JTextField("11181", 6), "growx 0");
					form.add(new JLabel("البلد:"));
					form.add(combo("الأردن", "مصر", "المغرب", "لبنان"));
					section(form, "طريقة الدفع", true);
					ButtonGroup g = new ButtonGroup();
					JRadioButton card = new JRadioButton("بطاقة ائتمان", true);
					JRadioButton cash = new JRadioButton("الدفع عند الاستلام");
					g.add(card);
					g.add(cash);
					form.add(card, "skip, split 2");
					form.add(cash);
					form.add(new JLabel("رقم البطاقة:"));
					form.add(new JTextField("4111 1111 1111 1111", 16));
					form.add(new JLabel("تاريخ الانتهاء:"));
					form.add(new JTextField("08/29", 5), "split 3, growx 0");
					form.add(new JLabel("CVV:"), "gapleft unrel");
					form.add(new JPasswordField("123", 4), "growx 0");
					form.add(new JCheckBox("احفظ هذه البطاقة للطلبات القادمة"), "skip");
					root.add(form);

					JPanel sum = titled("ملخص الطلب", new MigLayout("wrap 2", "[grow][trailing]"));
					String[][] lines = {{"سماعات لاسلكية", "٤٥٫٠٠"}, {"حافظة جلدية", "١٢٫٥٠"}, {"الشحن", "٣٫٠٠"}};
					for (String[] ln : lines) {
						sum.add(new JLabel(ln[0]));
						sum.add(new JLabel(ln[1] + " د.أ"));
					}
					sum.add(new JSeparator(), "span, growx");
					JLabel tl = new JLabel("المجموع");
					tl.setFont(tl.getFont().deriveFont(Font.BOLD, tl.getFont().getSize2D() + 2f));
					sum.add(tl);
					JLabel tv = new JLabel("٦٠٫٥٠ د.أ");
					tv.setFont(tl.getFont());
					sum.add(tv);
					JButton pay = new JButton("ادفع الآن");
					pay.setFont(pay.getFont().deriveFont(Font.BOLD));
					sum.add(pay, "span, growx, gaptop para, h 32!");
					sum.add(new JLabel("<html><body style='width:180px'><font color='#7A7F87'>بالضغط على «ادفع الآن» توافق على شروط البيع.</font></body></html>"), "span");
					root.add(sum);
					root.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					return root;
				})
				.sizes()
				.step("longer address typed", c -> {
					JTextArea a = c.find("address");
					a.setText("شارع الحدائق ١٢، الطابق الثالث، الشقة ٧، بالقرب من دوار الداخلية، جبل الحسين");
					a.revalidate();
				})
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario checkoutCjk()
	{
		return new Scenario("app-checkout-cjk", I18N, "Checkout form in Japanese",
				"A Japanese delivery and payment form with split name fields, a postcode lookup button, delivery date and time combos, and an order summary column.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog, gap 20", "[grow,fill][240::,fill]", "[top]"));
					JPanel form = new JPanel(new MigLayout("wrap 2, insets 0", "[right][grow,fill]", ""));
					JLabel h = new JLabel("ご注文手続き");
					h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
					form.add(h, "span, left, gapbottom 8");
					section(form, "お届け先", false);
					form.add(new JLabel("お名前："));
					form.add(new JTextField("山田", 6), "split 2, sg name");
					form.add(new JTextField("花子", 6), "sg name");
					form.add(new JLabel("フリガナ："));
					JTextField kana = new JTextField("ヤマダ ハナコ", 12);
					kana.setName("kana");
					form.add(kana);
					form.add(new JLabel("郵便番号："));
					form.add(new JTextField("150-0001", 8), "split 2, growx 0");
					form.add(new JButton("住所を検索"));
					form.add(new JLabel("都道府県："));
					form.add(combo("東京都", "大阪府", "北海道", "沖縄県"));
					form.add(new JLabel("市区町村："));
					form.add(new JTextField("渋谷区神宮前", 16));
					form.add(new JLabel("番地・建物名："));
					form.add(new JTextField("1-2-3 さくらハイツ 405", 16));
					form.add(new JLabel("電話番号："));
					form.add(new JTextField("03-1234-5678", 12));
					section(form, "配送とお支払い", true);
					form.add(new JLabel("配送日時："));
					form.add(combo("指定なし", "11月27日（金）", "11月28日（土）"), "split 2");
					form.add(combo("午前中", "14〜16時", "18〜20時"));
					form.add(new JLabel("お支払い方法："), "aligny top");
					ButtonGroup g = new ButtonGroup();
					String[] pays = {"クレジットカード", "コンビニ払い", "代金引換（手数料330円）"};
					for (int i = 0; i < pays.length; i++) {
						JRadioButton r = new JRadioButton(pays[i], i == 0);
						g.add(r);
						form.add(r, i == 0 ? "" : "skip");
					}
					form.add(new JCheckBox("ギフト包装を希望する"), "skip, gaptop 6");
					root.add(form);

					JPanel sum = new JPanel(new MigLayout("wrap 2, insets 12", "[grow][right]"));
					sum.setBorder(BorderFactory.createLineBorder(new Color(0xD5D9DF), 1, true));
					JLabel sh = new JLabel("ご注文内容");
					sh.setFont(sh.getFont().deriveFont(Font.BOLD));
					sum.add(sh, "span, gapbottom 6");
					String[][] items = {{"緑茶ティーバッグ ×2", "¥1,280"}, {"湯呑みセット ×1", "¥3,300"}, {"送料", "¥550"}};
					for (String[] it : items) {
						sum.add(new JLabel(it[0]));
						sum.add(new JLabel(it[1]));
					}
					sum.add(new JSeparator(), "span, growx");
					JLabel tl = new JLabel("合計（税込）");
					tl.setFont(tl.getFont().deriveFont(Font.BOLD, tl.getFont().getSize2D() + 2f));
					sum.add(tl);
					JLabel tv = new JLabel("¥5,130");
					tv.setFont(tl.getFont());
					tv.setForeground(RED);
					sum.add(tv);
					JButton go = new JButton("注文を確定する");
					go.setFont(go.getFont().deriveFont(Font.BOLD));
					sum.add(go, "span, growx, gaptop para, h 34!");
					sum.add(new JLabel("<html><body style='width:190px'><font color='#7A7F87'>ご注文確定後のキャンセルは、発送前に限りマイページから行えます。</font></body></html>"), "span");
					root.add(sum);
					return root;
				})
				.sizes()
				.step("longer reading entered", c -> c.<JTextField>find("kana").setText("ヤマダ ハナコ（タナカ ハナコ様方 お届け）"))
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario propertySheet()
	{
		return new Scenario("app-property-sheet", WINDOWS, "Property sheet",
				"A dense two column property grid without gaps, with bold category rows, bordered cells, mixed editors and a description docked south.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[grow,fill]"));
					JPanel tb = new JPanel(new MigLayout("insets 4 6 4 6, gap 4", "[][]8[grow,fill]"));
					ButtonGroup g = new ButtonGroup();
					JToggleButton cat = new JToggleButton("Categories", true);
					JToggleButton az = new JToggleButton("A–Z");
					g.add(cat);
					g.add(az);
					tb.add(cat);
					tb.add(az);
					tb.add(new JTextField("Filter properties", 10));
					root.add(tb, "dock north");

					WidthTrackingPanel sheet = new WidthTrackingPanel(new MigLayout("wrap 2, insets 0, gap 0", "[150!,fill][grow,fill,120:]", ""));
					sheet.setBackground(UIManager.getColor("TextField.background"));
					propCategory(sheet, "Layout");
					propRow(sheet, "x", spinner(12));
					propRow(sheet, "y", spinner(48));
					propRow(sheet, "width", spinner(320));
					propRow(sheet, "height", spinner(24));
					propRow(sheet, "constraints", cellField("growx, wrap"));
					propCategory(sheet, "Appearance");
					propRow(sheet, "background", colorEditor(new Color(0xF4F7FC), "244, 247, 252"));
					propRow(sheet, "foreground", colorEditor(new Color(0x1F2328), "31, 35, 40"));
					propRow(sheet, "font", fontEditor("Sans Serif, Bold, 13"));
					propRow(sheet, "opaque", checkCell(true));
					propRow(sheet, "border", combo("(none)", "Line", "Etched", "Titled", "Empty 4, 8"));
					propCategory(sheet, "Behaviour");
					propRow(sheet, "enabled", checkCell(true));
					propRow(sheet, "focusable", checkCell(false));
					JTextField tip = cellField("Saves the document under its current name");
					tip.setName("tooltip");
					propRow(sheet, "toolTipText", tip);
					propRow(sheet, "cursor", combo("Default", "Hand", "Text", "Wait"));
					propCategory(sheet, "Accessibility");
					propRow(sheet, "accessibleName", cellField("Save"));
					propRow(sheet, "accessibleDescription", cellField(""));
					JScrollPane sp = new JScrollPane(sheet, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					root.add(sp, "h 200:520:");

					JPanel desc = new JPanel(new MigLayout("wrap 1, insets 6 8 8 8, gap 2", "[grow,fill]"));
					JLabel dn = new JLabel("toolTipText");
					dn.setName("descName");
					dn.setFont(dn.getFont().deriveFont(Font.BOLD));
					desc.add(dn);
					JTextArea dt = new JTextArea("The text shown when the pointer rests on the component.", 2, 20);
					dt.setName("descText");
					dt.setLineWrap(true);
					dt.setWrapStyleWord(true);
					dt.setEditable(false);
					dt.setOpaque(false);
					desc.add(dt, "wmin 0");
					root.add(desc, "dock south");
					root.add(new JSeparator(), "dock south");
					return root;
				})
				.sizes()
				.step("another property selected", c -> {
					c.<JLabel>find("descName").setText("accessibleDescription");
					JTextArea t = c.find("descText");
					t.setText("A longer explanation of the component for assistive technologies such as screen readers. "
							+ "It is read after the accessible name, so it should not repeat it, and it should describe what happens when the component is used.");
					c.root.revalidate();
				})
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario notifications()
	{
		return new Scenario("app-notification-list", WINDOWS, "Notification centre",
				"A scrolled list of notifications with icons, HTML and wrapping text-area messages, times and dismiss buttons, a header with a badge and a pushed link.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[grow,fill]"));
					JPanel head = new JPanel(new MigLayout("insets 10 12 10 8, gap 8", "[][][grow][][]"));
					JLabel t = new JLabel("Notifications");
					t.setFont(t.getFont().deriveFont(Font.BOLD, t.getFont().getSize2D() + 4f));
					head.add(t);
					JLabel badge = new JLabel("3 new");
					badge.setOpaque(true);
					badge.setBackground(BLUE);
					badge.setForeground(Color.WHITE);
					badge.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
					head.add(badge);
					head.add(new JLabel("<html><a href='#'>Mark all as read</a></html>"), "skip");
					head.add(new JButton(new Glyph("gear", GREY, 14)));
					root.add(head, "dock north");
					root.add(new JSeparator(), "dock north");

					WidthTrackingPanel list = new WidthTrackingPanel(new MigLayout("wrap 1, insets 0, gap 0, hidemode 3", "[grow,fill]"));
					list.setName("list");
					list.add(notification("n1", "bell", BLUE, true, "<b>Build 4.2.0-rc3 finished</b><br>All 1,204 tests passed. The installer is ready for the release check.", false, "2 min"));
					list.add(new JSeparator());
					list.add(notification("n2", "user", PURPLE, true, "Ines Oduya mentioned you in “Q1 roadmap”: could you take another look at the dates for the migration before Friday?", true, "1 h"));
					list.add(new JSeparator());
					list.add(notification("n3", "warn", ORANGE, true, "<b>Disk almost full</b><br>Only 2.1 GB are left on <i>/var</i>. Old logs can be removed in the storage settings.", false, "3 h"));
					list.add(new JSeparator());
					list.add(notification("n4", "check", GREEN, false, "Your export “contacts-november.csv” is ready to download. It will be kept for seven days.", true, "Yesterday"));
					list.add(new JSeparator());
					list.add(notification("n5", "star", TEAL, false, "<b>Weekly summary</b><br>14 tasks closed, 3 opened, 2 overdue.", false, "Mon"));
					JScrollPane sp = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					sp.setBorder(BorderFactory.createEmptyBorder());
					root.add(sp, "w 280:400:, h 200:480:");

					JPanel foot = new JPanel(new MigLayout("insets 6", "[grow,center]"));
					foot.add(new JButton("Show earlier notifications"));
					root.add(foot, "dock south");
					return root;
				})
				.sizes()
				.step("one dismissed", c -> {
					c.<JComponent>find("n2").setVisible(false);
					c.root.revalidate();
				})
				.step("new notification on top", c -> {
					JPanel list = c.find("list");
					list.add(new JSeparator(), "", 0);
					list.add(notification("n0", "file", RED, true, "<b>Payment failed</b><br>The card ending in 4421 was declined for the monthly plan. Update it within 5 days to keep your projects.", false, "now"), "", 0);
					list.revalidate();
				})
				.step("narrow 320x480", c -> c.size(320, 480))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario colorPicker()
	{
		return new Scenario("app-color-picker", DIALOGS, "Colour picker",
				"A painted saturation field and hue strip beside slider rows with spinners in a size group, a hex field, old/new swatches and a row of recent colours.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog, gap 10", "[grow,fill][22!][300!,fill]", "[grow,fill][][]"));
					root.add(new ColourField(false), "w 160:240:, h 160:240:");
					root.add(new ColourField(true), "growy");

					JPanel right = new JPanel(new MigLayout("wrap 4, insets 0, gapy 2", "[right][grow,fill][fill,sg spin][]", ""));
					JLabel now = swatch(new Color(0x2F6FD6), 60, 28);
					JLabel was = swatch(new Color(0x5B8F3A), 60, 28);
					right.add(new JLabel("New"), "span, split 4, right");
					right.add(now, "growx 0");
					right.add(was, "growx 0, gapleft 0");
					right.add(new JLabel("Current"), "gapbottom 8");
					String[][] rows = {{"Red:", "47", "255", " "}, {"Green:", "111", "255", " "}, {"Blue:", "214", "255", " "}, {null}, {"Hue:", "217", "359", "°"}, {"Saturation:", "78", "100", "%"}, {"Brightness:", "84", "100", "%"}};
					for (String[] r : rows) {
						if (r[0] == null) {
							right.add(new JSeparator(), "span, growx, gapy 4 4");
							continue;
						}
						right.add(new JLabel(r[0]));
						right.add(new JSlider(0, Integer.parseInt(r[2]), Integer.parseInt(r[1])), "w 80:120:");
						right.add(new JSpinner(new SpinnerNumberModel(Integer.parseInt(r[1]), 0, Integer.parseInt(r[2]), 1)), "w 64!");
						right.add(new JLabel(r[3]), "w 12!");
					}
					right.add(new JSeparator(), "span, growx, gapy 4 4");
					right.add(new JLabel("Hex:"));
					JTextField hex = new JTextField("#2F6FD6", 8);
					hex.setFont(new Font(Font.MONOSPACED, Font.PLAIN, hex.getFont().getSize()));
					right.add(hex, "span 2");
					right.add(new JLabel(" "));
					right.add(new JLabel("Opacity:"));
					right.add(new JSlider(0, 100, 100));
					right.add(new JSpinner(new SpinnerNumberModel(100, 0, 100, 1)));
					right.add(new JLabel("%"));
					root.add(right, "aligny top, wrap");

					JPanel recent = new JPanel(new MigLayout("insets 0, gap 4"));
					recent.add(new JLabel("Recent:"), "gapright 6");
					Color[] cs = {BLUE, GREEN, ORANGE, RED, PURPLE, TEAL, GREY, new Color(0xF2C14E), new Color(0x1F2328), Color.WHITE};
					for (Color c : cs) {
						JButton b = new JButton(new Glyph("circle", c, 16));
						b.setMargin(new Insets(2, 2, 2, 2));
						recent.add(b, "sg sw");
					}
					recent.add(new JButton("+"), "sg sw");
					root.add(recent, "span, wrap");
					root.add(new JButton("Choose"), "span, split 3, tag ok");
					root.add(new JButton("Cancel"), "tag cancel");
					root.add(new JButton(new Glyph("ring", GREY, 14)), "tag left");
					return root;
				})
				.sizes()
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario aboutBox()
	{
		return new Scenario("app-about-box", DIALOGS, "About box",
				"A large icon spanning rows beside a big bold title, version, multi-line HTML text with a fixed width, a scrolled credits area and System info/Close buttons.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 20 24 16 24, wrap 2", "[]16[grow,fill]", "[][][][grow,fill][][]"));
					root.add(new JLabel(new Glyph("star", ORANGE, 72)), "spany 3, aligny top");
					JLabel name = new JLabel("Lanternfish");
					name.setFont(name.getFont().deriveFont(Font.BOLD, 28f));
					root.add(name);
					JLabel ver = new JLabel("Version 5.1.0  (build 20261102, 64-bit)");
					ver.setForeground(GREY);
					root.add(ver);
					root.add(new JLabel("<html><body style='width:330px'>A small, quick notebook for notes, sketches and lists that keeps everything "
							+ "in plain files you own.<br><br>Lanternfish is free software: you may share and change it under the terms of the "
							+ "<a href='#'>Open Notebook Licence 2.0</a>.</body></html>"), "gaptop para");
					JTextArea credits = new JTextArea("Design and code\n    Ottilie Brandvold, Kasimir Nwosu, Anh Thư Lê\n"
							+ "Translations\n    Ingrid Solberg (Norwegian), Paulo Mendes (Portuguese), Yuki Arai (Japanese)\n"
							+ "Icons\n    Drawn by the contributors\n"
							+ "Thanks to everyone who reported a bug or sent a patch.", 6, 40);
					credits.setEditable(false);
					credits.setLineWrap(true);
					credits.setWrapStyleWord(true);
					root.add(new JScrollPane(credits), "span, gaptop para");
					JLabel copy = new JLabel("© 2019–2026 The Lanternfish contributors");
					copy.setFont(copy.getFont().deriveFont(copy.getFont().getSize2D() - 1f));
					copy.setForeground(GREY);
					root.add(copy, "span, center");
					root.add(new JButton("System information"), "span, split 3, tag left");
					root.add(new JButton("Licences"), "tag help2");
					root.add(new JButton("Close"), "tag ok");
					return root;
				})
				.sizes()
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario downloads()
	{
		return new Scenario("app-download-manager", WINDOWS, "Download manager",
				"Rows of downloads in different states, each a nested form with a file icon, name, details, progress bar and state-dependent buttons that are hidden and shown.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[grow,fill]"));
					JPanel top = new JPanel(new MigLayout("insets 8", "[grow,fill][][]"));
					top.add(new JTextField("https://downloads.example.org/tools/mapmaker-3.4.dmg", 30));
					top.add(combo("All", "Active", "Finished", "Failed"));
					top.add(new JButton("Download"));
					root.add(top, "dock north");
					root.add(new JSeparator(), "dock north");

					WidthTrackingPanel list = new WidthTrackingPanel(new MigLayout("wrap 1, insets 0, gap 0", "[grow,fill]"));
					list.add(download("d1", "survey-photos-2026.zip", BLUE, "18.2 MB of 48.0 MB  ·  2.4 MB/s  ·  12 s left", 38, "active"));
					list.add(new JSeparator());
					list.add(download("d2", "field-notes.pdf", RED, "3.1 MB of 4.4 MB  ·  1.1 MB/s  ·  1 s left", 70, "active"));
					list.add(new JSeparator());
					list.add(download("d3", "elevation-model-alps-10m.tif", TEAL, "Paused at 612 MB of 2.1 GB", 29, "paused"));
					list.add(new JSeparator());
					list.add(download("d4", "fonts-bundle.tar.gz", GREY, "Done  ·  22.7 MB", -1, "done"));
					list.add(new JSeparator());
					list.add(download("d5", "nightly-build-x86_64.AppImage", ORANGE, "Failed: the server closed the connection", -1, "failed"));
					JScrollPane sp = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					sp.setBorder(BorderFactory.createEmptyBorder());
					root.add(sp);

					JPanel status = new JPanel(new MigLayout("insets 4 10 4 10", "[grow][]"));
					JLabel st = new JLabel("2 active  ·  3.5 MB/s");
					st.setName("status");
					status.add(st);
					status.add(new JLabel("118 GB free"));
					root.add(status, "dock south");
					root.add(new JSeparator(), "dock south");
					return root;
				})
				.sizes()
				.step("one download completes", c -> {
					c.<JProgressBar>find("d1.bar").setValue(81);
					c.<JLabel>find("d1.details").setText("38.9 MB of 48.0 MB  ·  2.6 MB/s  ·  4 s left");
					c.<JComponent>find("d2.bar").setVisible(false);
					c.<JComponent>find("d2.active").setVisible(false);
					c.<JComponent>find("d2.done").setVisible(true);
					c.<JLabel>find("d2.details").setText("Done  ·  4.4 MB");
					c.<JLabel>find("status").setText("1 active  ·  2.6 MB/s");
					c.root.revalidate();
				})
				.step("maximized 1600x1000", c -> c.size(1600, 1000))
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	private static Scenario contactEditor()
	{
		return new Scenario("app-contact-editor", FORMS, "Contact editor",
				"A photo column beside sections of name fields, a growing list of phone rows with type combos in a size group, an address with a split row and notes.",
				() -> {
					JPanel root = new JPanel(new MigLayout("insets dialog", "[]para[grow,fill]", "[top][]"));
					JPanel photo = new JPanel(new MigLayout("wrap 1, insets 0", "[center]"));
					photo.add(new JLabel(new Glyph("text:OA", TEAL, 96)));
					photo.add(new JButton("Change…"), "sg photo, growx");
					photo.add(new JButton("Remove"), "sg photo, growx");
					root.add(photo);

					JPanel form = new JPanel(new MigLayout("wrap 2, insets 0, hidemode 3", "[right][grow,fill]", ""));
					form.add(new JLabel("First name:"));
					form.add(new JTextField("Oluwaseun", 16));
					form.add(new JLabel("Last name:"));
					form.add(new JTextField("Adeyemi-Clarke", 16));
					form.add(new JLabel("Company:"));
					form.add(new JTextField("Northwind Cartography", 16), "split 3");
					form.add(new JLabel("Title:"), "gapleft unrel");
					form.add(new JTextField("Surveyor", 8));
					section(form, "Phone numbers", true);
					JPanel phones = new JPanel(new MigLayout("wrap 3, insets 0", "[fill,sg ptype][grow,fill][]", ""));
					phones.setName("phones");
					phoneRow(phones, "Mobile", "+44 7700 900461");
					phoneRow(phones, "Work", "+44 20 7946 0958");
					form.add(phones, "span, growx");
					form.add(new JButton("Add phone number", new Glyph("up", GREEN, 10)), "span, left, growx 0");
					section(form, "Address", true);
					form.add(new JLabel("Street:"));
					form.add(new JTextField("14 Juniper Row", 16));
					form.add(new JLabel("Postcode:"));
					form.add(new JTextField("YO1 7HH", 7), "split 3, growx 0");
					form.add(new JLabel("City:"), "gapleft unrel");
					form.add(new JTextField("York", 10));
					form.add(new JLabel("Country:"));
					form.add(combo("United Kingdom", "Ireland", "Nigeria", "Canada"));
					section(form, "Notes", true);
					JTextArea notes = new JTextArea("Prefers e-mail. Met at the coastal mapping conference; interested in the tide-gauge data set.", 3, 30);
					notes.setLineWrap(true);
					notes.setWrapStyleWord(true);
					form.add(new JScrollPane(notes), "span, growx");
					root.add(form, "wrap");

					root.add(new JButton("Delete contact"), "span, split 3, tag left");
					root.add(new JButton("Save"), "tag ok");
					root.add(new JButton("Cancel"), "tag cancel");
					return root;
				})
				.sizes()
				.step("phone row added", c -> {
					JPanel phones = c.find("phones");
					phoneRow(phones, "Home", "+44 1904 555 017");
					phones.revalidate();
				})
				.step("second phone row added at preferred size", c -> {
					JPanel phones = c.find("phones");
					phoneRow(phones, "Fax", "+44 1904 555 018");
					phones.revalidate();
					c.sizeToPreferred();
				})
				.step("fonts +4pt (accessibility)", ScenariosRealistic::biggerFonts)
				.step("shrunk to 300x200", c -> c.size(300, 200));
	}

	// ------------------------------------------------------------------------------------------------ helpers

	private static JComboBox<String> combo(String... items)
	{
		return new JComboBox<>(items);
	}

	/** A bold section caption followed by a separator that fills the rest of the row. */
	private static void section(JPanel p, String text, boolean gapTop)
	{
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		p.add(l, "span, split 2, aligny center" + (gapTop ? ", gaptop para" : ""));
		p.add(new JSeparator(), "growx, aligny center, gapleft rel");
	}

	private static JPanel titled(String title, LayoutManager lm)
	{
		JPanel p = new JPanel(lm);
		p.setBorder(BorderFactory.createTitledBorder(title));
		return p;
	}

	private static JLabel error(String name, String text)
	{
		JLabel l = new JLabel(text, new Glyph("warn", RED, 12), SwingConstants.LEADING);
		l.setForeground(RED);
		l.setName(name);
		l.setVisible(false);
		return l;
	}

	private static JComponent chip(String name, String size)
	{
		JPanel p = new JPanel(new MigLayout("insets 2 6 2 2, gap 4"));
		p.setBorder(BorderFactory.createLineBorder(new Color(0xC5CAD3), 1, true));
		p.add(new JLabel(new Glyph("file", GREY, 12)));
		p.add(new JLabel(name));
		JLabel s = new JLabel(size);
		s.setForeground(GREY);
		p.add(s);
		JButton x = new JButton(new Glyph("cross", GREY, 8));
		x.setMargin(new Insets(1, 1, 1, 1));
		p.add(x);
		return p;
	}

	private static JComponent code(String text)
	{
		JTextArea a = new JTextArea(text, 14, 48);
		a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		a.setTabSize(4);
		JTextArea lines = new JTextArea(lineNumbers(text), 14, 3);
		lines.setFont(a.getFont());
		lines.setEditable(false);
		lines.setForeground(GREY);
		JScrollPane sp = new JScrollPane(a);
		sp.setRowHeaderView(lines);
		return sp;
	}

	private static String lineNumbers(String text)
	{
		StringBuilder sb = new StringBuilder();
		int n = text.split("\n", -1).length;
		for (int i = 1; i <= n; i++)
			sb.append(i).append(i < n ? "\n" : "");
		return sb.toString();
	}

	private static JTree projectTree()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("app");
		DefaultMutableTreeNode src = new DefaultMutableTreeNode("src");
		DefaultMutableTreeNode pkg = new DefaultMutableTreeNode("app");
		pkg.add(new DefaultMutableTreeNode("Main.java"));
		pkg.add(new DefaultMutableTreeNode("Config.java"));
		pkg.add(new DefaultMutableTreeNode("Server.java"));
		src.add(pkg);
		DefaultMutableTreeNode test = new DefaultMutableTreeNode("test");
		test.add(new DefaultMutableTreeNode("MainTest.java"));
		root.add(src);
		root.add(test);
		root.add(new DefaultMutableTreeNode("build.gradle"));
		root.add(new DefaultMutableTreeNode("README.md"));
		JTree t = new JTree(new DefaultTreeModel(root));
		t.expandRow(1);
		t.expandRow(2);
		t.setVisibleRowCount(14);
		return t;
	}

	private static JComponent dayDivider(String text)
	{
		JPanel p = new JPanel(new MigLayout("insets 0", "[grow,fill][][grow,fill]"));
		p.setOpaque(false);
		p.add(new JSeparator(), "aligny center");
		JLabel l = new JLabel(text);
		l.setForeground(GREY);
		p.add(l);
		p.add(new JSeparator(), "aligny center");
		return p;
	}

	private static JPanel bubbleShell(boolean mine)
	{
		JPanel p = new JPanel(new MigLayout("wrap 1, insets 6 10 4 10, gap 2", "[grow,fill]"));
		p.setBackground(mine ? new Color(0xD9E7FB) : new Color(0xECEEF1));
		p.setBorder(BorderFactory.createLineBorder(mine ? new Color(0xB5CDF2) : new Color(0xD5D9DF), 1, true));
		return p;
	}

	/** The panel background of the look and feel, tinted a little towards a colour; works for light and dark themes. */
	private static Color tint(Color c, double f)
	{
		Color bg = UIManager.getColor("Panel.background");
		if (bg == null)
			bg = Color.WHITE;
		return new Color(
				(int) Math.round(bg.getRed() * (1 - f) + c.getRed() * f),
				(int) Math.round(bg.getGreen() * (1 - f) + c.getGreen() * f),
				(int) Math.round(bg.getBlue() * (1 - f) + c.getBlue() * f));
	}

	private static JLabel stamp(String text)
	{
		JLabel l = new JLabel(text);
		l.setForeground(GREY);
		l.setFont(l.getFont().deriveFont(l.getFont().getSize2D() - 2f));
		return l;
	}

	/** A bubble whose HTML text wraps at a fixed width, as chat applications commonly do it. */
	private static JPanel bubble(String html, String time, boolean mine)
	{
		JPanel p = bubbleShell(mine);
		JLabel text = new JLabel("<html><body style='width:220px'>" + html + "</body></html>");
		text.setForeground(Color.BLACK);
		p.add(text, "wmin 0");
		p.add(stamp(mine ? time + "  ✓✓" : time), "right, growx 0");
		return p;
	}

	private static JLabel amount(String text, String name)
	{
		JLabel l = new JLabel(text, SwingConstants.TRAILING);
		l.setName(name);
		return l;
	}

	private static JPanel cardShell(String title)
	{
		JPanel p = new JPanel(new MigLayout("wrap 1, insets 12 14 12 14", "[grow,fill]", ""));
		p.setBorder(BorderFactory.createLineBorder(new Color(0xD5D9DF), 1, true));
		JLabel t = new JLabel(title.toUpperCase());
		t.setForeground(GREY);
		t.setFont(t.getFont().deriveFont(Font.BOLD, t.getFont().getSize2D() - 1f));
		p.add(t);
		return p;
	}

	private static JPanel card(String title, String value, String delta, boolean up, int progress, String caption)
	{
		JPanel p = cardShell(title);
		JLabel v = new JLabel(value);
		v.setFont(v.getFont().deriveFont(Font.BOLD, 28f));
		p.add(v, "split 2");
		Color good = up ? GREEN : RED;
		JLabel d = new JLabel(delta, new Glyph(up ? "up" : "down", good, 10), SwingConstants.LEADING);
		d.setForeground(good);
		p.add(d, "gapleft 8, growx 0");
		JProgressBar pb = new JProgressBar(0, 100);
		pb.setValue(progress);
		p.add(pb, "gaptop 6, wmin 40");
		JLabel c = new JLabel(progress + "% " + caption);
		c.setForeground(GREY);
		p.add(c);
		return p;
	}

	/** A panel in a scroll pane that is always as wide as the viewport, so its content wraps instead of scrolling sideways. */
	private static final class WidthTrackingPanel extends JPanel implements Scrollable
	{
		WidthTrackingPanel(LayoutManager lm)
		{
			super(lm);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize()
		{
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			return Math.max(16, orientation == SwingConstants.VERTICAL ? visibleRect.height - 16 : visibleRect.width - 16);
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight()
		{
			return false;
		}
	}

	private static JSpinner spinner(int v)
	{
		return new JSpinner(new SpinnerNumberModel(v, -9999, 9999, 1));
	}

	private static final Color GRID = new Color(0xD5D9DF);

	private static JTextField cellField(String text)
	{
		JTextField f = new JTextField(text, 8);
		f.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		return f;
	}

	private static JComponent checkCell(boolean on)
	{
		JCheckBox b = new JCheckBox("", on);
		b.setOpaque(false);
		return b;
	}

	private static JComponent colorEditor(Color c, String text)
	{
		JPanel p = new JPanel(new MigLayout("insets 1 4 1 1, gap 4", "[][grow,fill][]"));
		p.setOpaque(false);
		p.add(swatch(c, 14, 14));
		p.add(new JLabel(text));
		JButton b = new JButton("…");
		b.setMargin(new Insets(0, 4, 0, 4));
		p.add(b);
		return p;
	}

	private static JComponent fontEditor(String text)
	{
		JPanel p = new JPanel(new MigLayout("insets 1 4 1 1, gap 4", "[grow,fill][]"));
		p.setOpaque(false);
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		p.add(l);
		JButton b = new JButton("…");
		b.setMargin(new Insets(0, 4, 0, 4));
		p.add(b);
		return p;
	}

	private static void propCategory(JPanel sheet, String name)
	{
		JLabel l = new JLabel(name, new Glyph("down", GREY, 10), SwingConstants.LEADING);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		l.setOpaque(true);
		l.setBackground(tint(GREY, 0.15));
		l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRID), BorderFactory.createEmptyBorder(3, 4, 3, 4)));
		sheet.add(l, "span, growx");
	}

	private static void propRow(JPanel sheet, String name, JComponent editor)
	{
		JLabel l = new JLabel(name);
		l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GRID), BorderFactory.createEmptyBorder(2, 18, 2, 4)));
		sheet.add(l, "growy");
		JPanel cell = new JPanel(new MigLayout("insets 0, fill", "[grow,fill]", "[grow,fill]"));
		cell.setOpaque(false);
		cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRID));
		cell.add(editor);
		sheet.add(cell, "growy");
	}

	private static JLabel swatch(Color c, int w, int h)
	{
		JLabel l = new JLabel();
		l.setOpaque(true);
		l.setBackground(c);
		l.setBorder(BorderFactory.createLineBorder(GREY));
		l.setPreferredSize(new Dimension(w, h));
		l.setMinimumSize(new Dimension(w, h));
		return l;
	}

	private static JComponent notification(String name, String icon, Color color, boolean unread, String text, boolean plainWrapping, String time)
	{
		JPanel p = new JPanel(new MigLayout("insets 10 12 10 8, gap 8 2", "[][grow,fill][]", "[top][]"));
		p.setName(name);
		if (unread) {
			p.setOpaque(true);
			p.setBackground(tint(BLUE, 0.1));
		}
		p.add(new JLabel(new Glyph(icon, color, 22)), "spany 2, aligny top");
		if (plainWrapping) {
			JTextArea a = new JTextArea(text);
			a.setLineWrap(true);
			a.setWrapStyleWord(true);
			a.setEditable(false);
			a.setOpaque(false);
			a.setFont(UIManager.getFont("Label.font"));
			a.setColumns(20);
			p.add(a, "spany 2, wmin 80");
		} else {
			p.add(new JLabel("<html><body style='width:200px'>" + text + "</body></html>"), "spany 2, wmin 0");
		}
		JLabel t = new JLabel(time);
		t.setForeground(GREY);
		p.add(t, "right, wrap");
		JButton x = new JButton(new Glyph("cross", GREY, 10));
		x.setMargin(new Insets(2, 2, 2, 2));
		p.add(x, "skip 2, right, aligny top");
		return p;
	}

	private static JComponent download(String name, String file, Color color, String details, int progress, String state)
	{
		JPanel p = new JPanel(new MigLayout("insets 8 10 8 10, gap 10 2, hidemode 3", "[][grow,fill][]", "[][][]"));
		p.setName(name);
		p.add(new JLabel(new Glyph("file", color, 28)), "spany 3, aligny top");
		JLabel n = new JLabel(file);
		n.setFont(n.getFont().deriveFont(Font.BOLD));
		p.add(n, "wmin 0");
		JPanel active = new JPanel(new MigLayout("insets 0, gap 4"));
		active.setName(name + ".active");
		active.add(new JButton(state.equals("paused") ? "Resume" : "Pause"), "sg dl");
		active.add(new JButton("Cancel"), "sg dl");
		JPanel done = new JPanel(new MigLayout("insets 0, gap 10"));
		done.setName(name + ".done");
		done.add(new JLabel("<html><a href='#'>Open</a></html>"));
		done.add(new JLabel("<html><a href='#'>Show in folder</a></html>"));
		JButton retry = new JButton("Retry");
		retry.setName(name + ".retry");
		active.setVisible(state.equals("active") || state.equals("paused"));
		done.setVisible(state.equals("done"));
		retry.setVisible(state.equals("failed"));
		JPanel actions = new JPanel(new MigLayout("insets 0, flowy, hidemode 3", "[right]"));
		actions.add(active);
		actions.add(done);
		actions.add(retry);
		p.add(actions, "spany 3, aligny center, wrap");
		JProgressBar bar = new JProgressBar(0, 100);
		bar.setName(name + ".bar");
		bar.setValue(Math.max(0, progress));
		bar.setVisible(progress >= 0);
		p.add(bar, "wmin 40");
		JLabel d = new JLabel(details);
		d.setName(name + ".details");
		d.setForeground(state.equals("failed") ? RED : GREY);
		p.add(d, "newline, wmin 0");
		return p;
	}

	private static void phoneRow(JPanel phones, String type, String number)
	{
		phones.add(combo(type, "Mobile", "Work", "Home", "Fax", "Other"));
		phones.add(new JTextField(number, 14));
		JButton x = new JButton(new Glyph("cross", RED, 10));
		phones.add(x);
	}

	/** A painted saturation/brightness square, or (strip) a vertical hue strip, each with a marker at a fixed spot. */
	private static final class ColourField extends JComponent
	{
		private final boolean strip;

		ColourField(boolean strip)
		{
			this.strip = strip;
			setPreferredSize(strip ? new Dimension(22, 200) : new Dimension(220, 200));
			setMinimumSize(strip ? new Dimension(22, 60) : new Dimension(60, 60));
		}

		@Override
		protected void paintComponent(Graphics g0)
		{
			Graphics2D g = (Graphics2D) g0.create();
			int w = getWidth(), h = getHeight();
			if (strip) {
				for (int y = 0; y < h; y++) {
					g.setColor(Color.getHSBColor(y / (float) Math.max(1, h), 1f, 1f));
					g.drawLine(0, y, w, y);
				}
				int my = Math.round(h * 217f / 360f);
				g.setColor(Color.WHITE);
				g.drawRect(0, my - 2, w - 1, 4);
			} else {
				g.setPaint(new GradientPaint(0, 0, Color.WHITE, w, 0, Color.getHSBColor(217f / 360f, 1f, 1f)));
				g.fillRect(0, 0, w, h);
				g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, h, Color.BLACK));
				g.fillRect(0, 0, w, h);
				int mx = Math.round(w * 0.78f), my = Math.round(h * 0.16f);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.WHITE);
				g.drawOval(mx - 6, my - 6, 12, 12);
			}
			g.setColor(GREY);
			g.drawRect(0, 0, w - 1, h - 1);
			g.dispose();
		}
	}

	/** Every explicitly set font of the tree gets 4pt larger, as when a user turns up the font size; then preferred size. */
	private static void biggerFonts(StepContext ctx)
	{
		deriveFonts(ctx.root, 4f);
		ctx.root.revalidate();
		ctx.sizeToPreferred();
	}

	private static void deriveFonts(Component c, float delta)
	{
		if (c.isFontSet() && c.getFont() != null) {
			Font f = c.getFont();
			c.setFont(f.deriveFont(f.getSize2D() + delta));
		}
		if (c instanceof JComponent jc && jc.getBorder() instanceof TitledBorder tb) {
			Font f = tb.getTitleFont() != null ? tb.getTitleFont() : UIManager.getFont("TitledBorder.font");
			if (f != null)
				tb.setTitleFont(f.deriveFont(f.getSize2D() + delta));
		}
		if (c instanceof JTable t)
			t.setRowHeight(t.getFontMetrics(t.getFont()).getHeight() + 4);
		if (c instanceof Container ct)
			for (Component child : ct.getComponents())
				deriveFonts(child, delta);
	}

	/**
	 * Small deterministic icons drawn with Java2D: shapes like a folder, a file, play controls, or a numbered circle
	 * ("num:3") and initials in a circle ("text:AB").
	 */
	private static final class Glyph implements Icon
	{
		private final String kind;
		private final Color color;
		private final int w, h;

		Glyph(String kind, Color color, int size)
		{
			this(kind, color, size, size);
		}

		Glyph(String kind, Color color, int w, int h)
		{
			this.kind = kind;
			this.color = color;
			this.w = w;
			this.h = h;
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

		@Override
		public void paintIcon(Component c, Graphics g0, int x, int y)
		{
			Graphics2D g = (Graphics2D) g0.create();
			g.translate(x, y);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(color);
			float sw = Math.max(1f, Math.min(w, h) / 8f);
			g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			if (kind.startsWith("num:") || kind.startsWith("text:")) {
				g.fillOval(0, 0, w - 1, h - 1);
				String t = kind.substring(kind.indexOf(':') + 1);
				g.setColor(Color.WHITE);
				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(6, h * 9 / 20)));
				FontMetrics fm = g.getFontMetrics();
				g.drawString(t, (w - fm.stringWidth(t)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
				g.dispose();
				return;
			}
			switch (kind) {
				case "circle" -> g.fillOval(0, 0, w - 1, h - 1);
				case "ring" -> g.drawOval((int) sw, (int) sw, w - 1 - 2 * (int) sw, h - 1 - 2 * (int) sw);
				case "square" -> g.fillRoundRect(0, 0, w, h, w / 3, h / 3);
				case "folder" -> {
					g.fillRoundRect(0, h / 8, w * 2 / 5, h / 4, 3, 3);
					g.fillRoundRect(0, h / 4, w, h * 5 / 8, 3, 3);
				}
				case "file", "page" -> {
					Path2D p = new Path2D.Float();
					float d = Math.min(w, h) / 3f;
					if (kind.equals("file")) {
						p.moveTo(w * 0.15, 0);
						p.lineTo(w * 0.85 - d, 0);
						p.lineTo(w * 0.85, d);
						p.lineTo(w * 0.85, h - 1);
						p.lineTo(w * 0.15, h - 1);
						p.closePath();
						g.fill(p);
					} else {
						p.moveTo(1, 1);
						p.lineTo(w - 1 - d, 1);
						p.lineTo(w - 1, 1 + d);
						p.lineTo(w - 1, h - 1);
						p.lineTo(1, h - 1);
						p.closePath();
						g.draw(p);
					}
				}
				case "pages" -> {
					g.drawRect(1, 1, w / 2, h * 2 / 3);
					g.drawRect(w / 4, h / 6, w / 2, h * 2 / 3);
					g.drawRect(w / 2 - 2, h / 3 - 2, w / 2, h * 2 / 3);
				}
				case "play" -> g.fillPolygon(new int[] {w / 5, w - w / 6, w / 5}, new int[] {h / 8, h / 2, h - h / 8}, 3);
				case "pause" -> {
					g.fillRect(w / 5, h / 8, w / 5, h * 3 / 4);
					g.fillRect(w * 3 / 5, h / 8, w / 5, h * 3 / 4);
				}
				case "next" -> {
					g.fillPolygon(new int[] {w / 6, w * 3 / 4, w / 6}, new int[] {h / 6, h / 2, h * 5 / 6}, 3);
					g.fillRect(w * 3 / 4, h / 6, Math.max(2, w / 8), h * 2 / 3);
				}
				case "prev" -> {
					g.fillPolygon(new int[] {w * 5 / 6, w / 4, w * 5 / 6}, new int[] {h / 6, h / 2, h * 5 / 6}, 3);
					g.fillRect(w / 4 - Math.max(2, w / 8), h / 6, Math.max(2, w / 8), h * 2 / 3);
				}
				case "star" -> {
					Path2D p = new Path2D.Float();
					for (int i = 0; i < 10; i++) {
						double r = (i % 2 == 0 ? 0.5 : 0.22) * Math.min(w, h);
						double a = -Math.PI / 2 + i * Math.PI / 5;
						double px = w / 2.0 + r * Math.cos(a), py = h / 2.0 + r * Math.sin(a);
						if (i == 0)
							p.moveTo(px, py);
						else
							p.lineTo(px, py);
					}
					p.closePath();
					g.fill(p);
				}
				case "bell" -> {
					g.fillArc(w / 6, h / 8, w * 2 / 3, h * 3 / 4, 0, 180);
					g.fillRect(w / 6, h / 2 - 1, w * 2 / 3, h / 4);
					g.fillRect(w / 12, h * 3 / 4 - 1, w * 5 / 6, h / 10 + 1);
					g.fillOval(w / 2 - w / 10, h * 5 / 6, w / 5, h / 7);
				}
				case "user" -> {
					g.fillOval(w / 3, 0, w / 3, h * 2 / 5);
					g.fillArc(w / 8, h / 2, w * 3 / 4, h, 0, 180);
				}
				case "check" -> g.drawPolyline(new int[] {w / 6, w * 2 / 5, w * 5 / 6}, new int[] {h / 2, h * 3 / 4, h / 4}, 3);
				case "cross" -> {
					g.drawLine(w / 5, h / 5, w * 4 / 5, h * 4 / 5);
					g.drawLine(w * 4 / 5, h / 5, w / 5, h * 4 / 5);
				}
				case "warn" -> {
					g.fillPolygon(new int[] {w / 2, w - 1, 0}, new int[] {0, h - 1, h - 1}, 3);
					g.setColor(Color.WHITE);
					g.fillRect(w / 2 - Math.max(1, w / 12), h / 3, Math.max(2, w / 6), h / 3);
					g.fillRect(w / 2 - Math.max(1, w / 12), h * 3 / 4, Math.max(2, w / 6), Math.max(1, h / 8));
				}
				case "gear" -> {
					for (int i = 0; i < 8; i++) {
						Graphics2D t = (Graphics2D) g.create();
						t.rotate(i * Math.PI / 4, w / 2.0, h / 2.0);
						t.fillRect(w / 2 - w / 10, 0, w / 5, h);
						t.dispose();
					}
					g.fillOval(w / 6, h / 6, w * 2 / 3, h * 2 / 3);
					g.setColor(Color.WHITE);
					g.fillOval(w * 3 / 8, h * 3 / 8, w / 4, h / 4);
				}
				case "up" -> g.fillPolygon(new int[] {w / 2, w - 1, 0}, new int[] {h / 5, h * 4 / 5, h * 4 / 5}, 3);
				case "down" -> g.fillPolygon(new int[] {0, w - 1, w / 2}, new int[] {h / 5, h / 5, h * 4 / 5}, 3);
				case "speaker" -> {
					g.fillRect(0, h / 3, w / 4, h / 3);
					g.fillPolygon(new int[] {w / 4, w / 2, w / 2, w / 4}, new int[] {h / 3, h / 8, h * 7 / 8, h * 2 / 3}, 4);
					g.drawArc(w / 3, h / 4, w / 2, h / 2, -50, 100);
				}
				case "spark" -> {
					int[] data = {5, 9, 7, 12, 10, 15, 13, 18, 14, 20, 17, 24};
					int[] xs = new int[data.length], ys = new int[data.length];
					for (int i = 0; i < data.length; i++) {
						xs[i] = i * (w - 2) / (data.length - 1) + 1;
						ys[i] = h - 2 - data[i] * (h - 4) / 24;
					}
					g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g.drawPolyline(xs, ys, xs.length);
				}
				default -> g.fillRect(0, 0, w, h);
			}
			g.dispose();
		}
	}

	private ScenariosRealistic() {}
}
