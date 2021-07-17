package lezer.tree;
class BufferContext {
	public final TreeNode parent;
	public final TreeBuffer buffer;
	public final int index;
	public final int start;

	BufferContext(TreeNode parent,
              TreeBuffer buffer,
              int index,
              int start) {
		this.parent = parent;
		this.buffer = buffer;
		this.index = index;
		this.start = start;
	}
}