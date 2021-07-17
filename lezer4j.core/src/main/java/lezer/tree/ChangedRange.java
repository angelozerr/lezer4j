package lezer.tree;

/// The [`TreeFragment.applyChanges`](#tree.TreeFragment^applyChanges)
/// method expects changed ranges in this format.
public class ChangedRange {
	/// The start of the change in the start document
	public final int fromA;
	/// The end of the change in the start document
	public final int toA;
	/// The start of the replacement in the new document
	public final int fromB;
	/// The end of the replacement in the new document
	public final int toB;

	public ChangedRange(int fromA, int toA, int fromB, int toB) {
		super();
		this.fromA = fromA;
		this.toA = toA;
		this.fromB = fromB;
		this.toB = toB;
	}

}