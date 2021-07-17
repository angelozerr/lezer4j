package lezer.tree;

import java.util.List;

class BufferNode implements SyntaxNode {

	public final BufferContext context;
	public final NodeType type;
	public final BufferNode _parent;
	public final int index;

	BufferNode(BufferContext context, BufferNode _parent, int index) {
		this.context = context;
		this._parent = _parent;
		this.index = index;
		this.type = context.buffer.set.types.get(context.buffer.buffer.get(index));
	}

	@Override
	public NodeType type() {
		return this.type;
	}

	@Override
	public String name() {
		return this.type.name;
	}

	@Override
	public int from() {
		return this.context.start + this.context.buffer.buffer.get(this.index + 1);
	}

	@Override
	public int to() {
		return this.context.start + this.context.buffer.buffer.get(this.index + 2);
	}

	private BufferNode child(int dir/* : 1 | -1 */, double after) {
		TreeBuffer buffer = this.context.buffer;
		int index = buffer.findChild(this.index + 4, buffer.buffer.get(this.index + 3), dir,
				after == After.None ? After.None : after - this.context.start);
		return index < 0 ? null : new BufferNode(this.context, this, index);
	}

	@Override
	public SyntaxNode firstChild() {
		return this.child(1, After.None);
	}

	@Override
	public SyntaxNode lastChild() {
		return this.child(-1, After.None);
	}

	@Override
	public SyntaxNode childAfter(int pos) {
		return this.child(1, pos);
	}

	@Override
	public SyntaxNode childBefore(int pos) {
		return this.child(-1, pos);
	}

	@Override
	public SyntaxNode parent() {
		return this._parent != null ? this._parent : this.context.parent.nextSignificantParent();
	}

	private SyntaxNode externalSibling(int dir/* : 1 | -1 */) {
		return this._parent != null ? null : this.context.parent.nextChild(this.context.index + dir, dir, -1);
	}

	@Override
	public SyntaxNode nextSibling() {
		TreeBuffer buffer = this.context.buffer;
		int after = buffer.buffer.get(this.index + 3);
		if (after < (this._parent != null ? buffer.buffer.get(this._parent.index + 3) : buffer.buffer.length))
			return new BufferNode(this.context, this._parent, after);
		return this.externalSibling(1);
	}

	@Override
	public SyntaxNode prevSibling() {
		TreeBuffer buffer = this.context.buffer;
		int parentStart = this._parent != null ? this._parent.index + 4 : 0;
		if (this.index == parentStart)
			return this.externalSibling(-1);
		return new BufferNode(this.context, this._parent, buffer.findChild(parentStart, this.index, -1, After.None));
	}

	@Override
	public TreeCursor cursor() {
		return new TreeCursor(this);
	}

	@Override
	public SyntaxNode resolve(int pos, Integer side) {
		return this.cursor().moveTo(pos, side).node();
	}

	/// @internal
	@Override
	public String toString() {
		return this.context.buffer.childString(this.index);
	}

	@Override
	public SyntaxNode getChild(String type, String before, String after) {
		List<SyntaxNode> r = TreeUtils.getChildren(this, type, before, after);
		return !r.isEmpty() ? r.get(0) : null;
	}

	@Override
	public List<SyntaxNode> getChildren(String type, String before, String after) {
		return TreeUtils.getChildren(this, type, before, after);
	}
}
