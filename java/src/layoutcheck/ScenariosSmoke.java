package layoutcheck;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

/** The smallest scenarios: plain captions next to HTML controls, and a plain form; a first check that runs in seconds. */
final class ScenariosSmoke
{
	static void add(List<Scenario> l)
	{
		l.add(new Scenario("smoke-html-captions", "Smoke", "HTML controls next to plain captions",
				"Each row is a plain caption and an HTML control; both should share one baseline.",
				() -> {
					JPanel p = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]", ""));
					p.add(new JLabel("Updates:"));
					p.add(new JCheckBox("<html>Install updates <b>automatically</b></html>"));
					p.add(new JLabel("Range:"));
					p.add(new JRadioButton("<html>The last <b>7</b> days</html>"));
					p.add(new JLabel("Schedule:"));
					p.add(new JLabel("<html>Every <i>day</i></html>"));
					p.add(new JLabel("Action:"));
					p.add(new JButton("<html>Run <i>now</i></html>"));
					return p;
				}));

		l.add(new Scenario("smoke-plain-form", "Smoke", "A plain form",
				"Labels, text fields, a combo box and a spinner, aligned on their baselines.",
				() -> {
					JPanel p = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]", ""));
					p.add(new JLabel("Name:"));
					p.add(new JTextField("Ada Lovelace", 20));
					p.add(new JLabel("Country:"));
					p.add(new JComboBox<>(new String[] {"Austria", "Belgium", "Croatia"}));
					p.add(new JLabel("Age:"));
					p.add(new JSpinner(new SpinnerNumberModel(36, 0, 150, 1)));
					return p;
				}));
	}

	private ScenariosSmoke() {}
}
