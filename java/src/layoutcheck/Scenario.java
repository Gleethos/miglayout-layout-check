package layoutcheck;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One GUI to lay out with every MigLayout variant, and the steps to take it through.
 * <p>
 * Each step changes something (a size, a text, a font, ...), lets Swing validate the window the way it would on screen,
 * and then records the bounds of every component and a screenshot. With no steps given, {@link #defaultSteps()} is used:
 * the preferred size, a narrower and a wider size, and the preferred size again.
 */
public final class Scenario
{
	public interface Action { void apply(StepContext ctx) throws Exception; }

	public record Step(String label, Action action) {}

	public final String id;
	public final String group;
	public final String title;
	public final String description;
	public final Supplier<JComponent> builder;
	public final List<Step> steps = new ArrayList<>();

	public Scenario(String id, String group, String title, String description, Supplier<JComponent> builder)
	{
		this.id = id;
		this.group = group;
		this.title = title;
		this.description = description;
		this.builder = builder;
	}

	public Scenario step(String label, Action action)
	{
		steps.add(new Step(label, action));
		return this;
	}

	/** Adds the default size sequence: preferred, narrow, wide, preferred again. */
	public Scenario sizes()
	{
		steps.addAll(defaultSteps());
		return this;
	}

	public List<Step> effectiveSteps()
	{
		return steps.isEmpty() ? defaultSteps() : steps;
	}

	public static List<Step> defaultSteps()
	{
		List<Step> l = new ArrayList<>();
		l.add(new Step("preferred size", StepContext::sizeToPreferred));
		l.add(new Step("narrow (75% wide)", ctx -> ctx.sizeRelative(0.75, 1.0)));
		l.add(new Step("wide and tall (150% x 130%)", ctx -> ctx.sizeRelative(1.5, 1.3)));
		l.add(new Step("preferred size again", StepContext::sizeToPreferred));
		return l;
	}
}
