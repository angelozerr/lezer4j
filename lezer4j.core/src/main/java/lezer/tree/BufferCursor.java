package lezer.tree;
/// This is used by `Tree.build` as an abstraction for iterating over
/// a tree buffer. A cursor initially points at the very last element
/// in the buffer. Every time `next()` is called it moves on to the
/// previous one.
public interface BufferCursor {
  /// The current buffer position (four times the number of nodes
  /// remaining).
  int pos();
  /// The node ID of the next node in the buffer.
  int id();
  /// The start position of the next node in the buffer.
  int start();
  /// The end position of the next node.
  int end();
  /// The size of the next node (the number of nodes inside, counting
  /// the node itself, times 4).
  int size();
  /// Moves `this.pos` down by 4.
  void next();
  /// Create a copy of this cursor.
  BufferCursor fork();
}
