package lezer.tree;
/// A parse context is an object providing additional information to the
/// parser. It is passed through to nested parsers.
public interface ParseContext {
  /// A set of fragments from a previous parse to be used for incremental
  /// parsing. These should be aligned with the current document
  /// (through a call to
  /// [`TreeFragment.applyChanges`](#tree.TreeFragment^applyChanges))
  /// if any changes were made since they were produced. The parser
  /// will try to reuse nodes from the fragments in the new parse,
  /// greatly speeding up the parse when it can do so for most of the
  /// document.
	TreeFragment[] fragments();
}
