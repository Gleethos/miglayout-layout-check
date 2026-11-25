package layoutcheck;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The look and feels the check can run with, by short name. Run as a program, it prints the names of the ones this
 * platform supports, one per line: the default set of a check.
 */
public final class Lafs
{
	public static final Map<String, String> ALL = new LinkedHashMap<>();
	static {
		ALL.put("metal", "javax.swing.plaf.metal.MetalLookAndFeel");
		ALL.put("nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
		ALL.put("motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		ALL.put("gtk", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		ALL.put("windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		ALL.put("windowsclassic", "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		ALL.put("aqua", "com.apple.laf.AquaLookAndFeel");
		ALL.put("flatlight", "com.formdev.flatlaf.FlatLightLaf");
		ALL.put("flatdark", "com.formdev.flatlaf.FlatDarkLaf");
		ALL.put("flatintellij", "com.formdev.flatlaf.FlatIntelliJLaf");
		ALL.put("flatdarcula", "com.formdev.flatlaf.FlatDarculaLaf");
		ALL.put("flatmaclight", "com.formdev.flatlaf.themes.FlatMacLightLaf");
	}

	public static String className(String shortName)
	{
		String cn = ALL.get(shortName);
		if (cn == null)
			throw new IllegalArgumentException("Unknown look and feel " + shortName + ", known: " + ALL.keySet());
		return cn;
	}

	/** Sets the look and feel; call it on the event dispatch thread. */
	public static void set(String shortName) throws Exception
	{
		UIManager.setLookAndFeel(className(shortName));
	}

	public static void main(String[] args) throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			for (String name : ALL.keySet()) {
				try {
					set(name); // GTK, Windows and Aqua throw where the platform lacks them
					System.out.println(name);
				} catch (Throwable ignored) {
				}
			}
		});
		System.exit(0);
	}

	private Lafs() {}
}
