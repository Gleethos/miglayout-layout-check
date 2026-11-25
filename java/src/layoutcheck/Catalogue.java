package layoutcheck;

import java.util.ArrayList;
import java.util.List;

/** Every scenario the harness runs, in a stable order. Each group lives in its own Scenarios* class. */
public final class Catalogue
{
	public static List<Scenario> all()
	{
		List<Scenario> l = new ArrayList<>();
		ScenariosSmoke.add(l);
		ScenariosLayout.add(l);
		ScenariosBaseline.add(l);
		ScenariosRealistic.add(l);
		return l;
	}

	private Catalogue() {}
}
