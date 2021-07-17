package lezer.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// Tree fragments are used during [incremental
/// parsing](#lezer.ParseOptions.fragments) to track parts of old
/// trees that can be reused in a new parse. An array of fragments is
/// used to track regions of an old tree whose nodes might be reused
/// in new parses. Use the static
/// [`applyChanges`](#tree.TreeFragment^applyChanges) method to update
/// fragments for document changes.
public class TreeFragment {

	public final int from;

	public final int to;

	public final Tree tree;

	public final int offset;

	private final int open;

	public TreeFragment(
			/// The start of the unchanged range pointed to by this fragment.
			/// This refers to an offset in the _updated_ document (as opposed
			/// to the original tree).
			int from,
			/// The end of the unchanged range.
			int to,
			/// The tree that this fragment is based on.
			Tree tree,
			/// The offset between the fragment's tree and the document that
			/// this fragment can be used against. Add this when going from
			/// document to tree positions, subtract it to go from tree to
			/// document positions.
			int offset, int open) {
		this.from = from;
		this.to = to;
		this.tree = tree;
		this.offset = offset;
		this.open = open;
	}

	public boolean openStart() {
		return (this.open & Open.Start) > 0;
	}

	public boolean openEnd() {
		return (this.open & Open.End) > 0;
	}

	/// Apply a set of edits to an array of fragments, removing or
	/// splitting fragments as necessary to remove edited ranges, and
	/// adjusting offsets for fragments that moved.
	public static List<TreeFragment> applyChanges(List<TreeFragment> fragments, List<ChangedRange> changes) {
		return applyChanges(fragments, changes, 128);
	}

	public static List<TreeFragment> applyChanges(List<TreeFragment> fragments, List<ChangedRange> changes,
			int minGap) {
		if (changes.isEmpty())
			return fragments;
		List<TreeFragment> result = new ArrayList<>();
		int fI = 1;
		TreeFragment nextF = !fragments.isEmpty() ? fragments.get(0) : null;
		for (int cI = 0, pos = 0, off = 0;; cI++) {
			ChangedRange nextC = cI < changes.size() ? changes.get(cI) : null;
			double nextPos = nextC != null ? nextC.fromA : 1e9;
			if (nextPos - pos >= minGap)
				while (nextF != null && nextF.from < nextPos) {
					TreeFragment cut = nextF;
					if (pos >= cut.from || nextPos <= cut.to || off > 0) {
						int fFrom = Math.max(cut.from, pos) - off, fTo = (int) (Math.min(cut.to, nextPos) - off);
						cut = fFrom >= fTo ? null
								: new TreeFragment(fFrom, fTo, cut.tree, cut.offset + off,
										(cI > 0 ? Open.Start : 0) | (nextC != null ? Open.End : 0));
					}
					if (cut != null)
						result.add(cut);
					if (nextF.to > nextPos)
						break;
					nextF = fI < fragments.size() ? fragments.get(fI++) : null;
				}
			if (nextC == null)
				break;
			pos = nextC.toA;
			off = nextC.toA - nextC.toB;
		}
		return result;
	}

	/// Create a set of fragments from a freshly parsed tree, or update
	/// an existing set of fragments by replacing the ones that overlap
	/// with a tree with content from the new tree. When `partial` is
	/// true, the parse is treated as incomplete, and the token at its
	/// end is not included in [`safeTo`](#tree.TreeFragment.safeTo).
	public static List<TreeFragment> addTree(Tree tree, TreeFragment[] fragments, boolean partial) {
		List<TreeFragment> result = new ArrayList<>(
				Arrays.asList(new TreeFragment(0, tree.length, tree, 0, partial ? Open.End : 0)));
		for (TreeFragment f : fragments)
			if (f.to > tree.length)
				result.add(f);
		return result;
	}
}
