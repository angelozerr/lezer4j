package lezer.tree;
/// Interface used to represent an in-progress parse, which can be
/// moved forward piece-by-piece.
public interface PartialParse {
  /// Advance the parse state by some amount.
  Tree advance();
  /// The current parse position.
  int pos();
  /// Get the currently parsed content as a tree, even though the
  /// parse hasn't finished yet.
  Tree forceFinish();
}
