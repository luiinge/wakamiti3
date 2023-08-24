package es.iti.wakamiti.expressions;

import java.util.*;

public sealed class Fragment {

	public abstract sealed class WrapperFragment extends Fragment {}

	public final class OptionalFragment extends WrapperFragment {}

	public final class NegatedFragment extends WrapperFragment {}

	public static abstract sealed class CompositeFragment extends Fragment {

		private List<Fragment> fragments = new LinkedList<>();
		public void add(Fragment fragment) {
			fragments.add(fragment);
		}

	}

	public static final class SequenceFragment extends CompositeFragment {



	}

	public final class ChoiceFragment extends CompositeFragment {}

	public static final class LiteralFragment extends Fragment {
		private String literal;
		LiteralFragment(String literal) {
			this.literal = literal;
		}

	}


	public static final class WildcardFragment extends Fragment {
		public static final WildcardFragment INSTANCE = new WildcardFragment();
	}

	public final class TypedValueFragment extends Fragment {}

}
