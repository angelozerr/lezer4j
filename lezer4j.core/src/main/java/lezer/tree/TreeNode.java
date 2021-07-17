package lezer.tree;

import static lezer.tree.TreeUtils.hasChild;

import java.util.List;

class TreeNode implements SyntaxNode {

	public final Tree node;
	private final int from;
	public final int index;
	public final TreeNode _parent;

	public TreeNode(Tree node, int from, int index, TreeNode _parent) {
		this.node = node;
		this.from = from;
		this.index = index;
		this._parent = _parent;
	}

	@Override
	public NodeType type() {
		return this.node.type;
	}

	@Override
	public String name() {
		return this.node.type.name;
	}

	@Override
	public int from() {
		return this.from;
	}

	@Override
	public int to() {
		return this.from + this.node.length;
	}

	public SyntaxNode nextChild(int i, int dir/* : 1 | -1 */, double after) {
		return nextChild(i, dir, after, false);
	}

	public SyntaxNode nextChild(int i, int dir/* : 1 | -1 */, double after, boolean full) {
		for (TreeNode parent = this;;) {
			List<TreeChild> children = null;
			int e = 0;
			for (children = parent.node.children, e = dir > 0 ? children.size() : -1; i != e; i += dir) {
				List<Integer> positions = parent.node.positions;
				TreeChild next = children.get(i);
				int start = positions.get(i) + parent.from;
				if (after != After.None && (dir < 0 ? start >= after : start + next.length <= after))
					continue;
				if (next instanceof TreeBuffer) {
					TreeBuffer nextBuffer = (TreeBuffer) next;
					int index = nextBuffer.findChild(0, nextBuffer.buffer.length, dir,
							after == After.None ? After.None : after - start);
					if (index > -1)
						return new BufferNode(new BufferContext(parent, nextBuffer, i, start), null, index);
				} else if (full || (!next.type.isAnonymous() || hasChild((Tree) next))) {
					TreeNode inner = new TreeNode((Tree) next, start, i, parent);
					return full || !inner.type().isAnonymous() ? inner
							: inner.nextChild(dir < 0 ? ((Tree) next).children.size() - 1 : 0, dir, after);
				}
			}
			if (full || !parent.type().isAnonymous())
				return null;
			i = parent.index + dir;
			parent = parent._parent;
			if (parent == null)
				return null;
		}
	}

	@Override
	public SyntaxNode firstChild() {
		return this.nextChild(0, 1, After.None);
	}

	@Override
	public SyntaxNode lastChild() {
		return this.nextChild(this.node.children.size() - 1, -1, After.None);
	}

	@Override
	public SyntaxNode childAfter(int pos) {
		return this.nextChild(0, 1, pos);
	}

	@Override
	public SyntaxNode childBefore(int pos) {
		return this.nextChild(this.node.children.size() - 1, -1, pos);
	}

	public TreeNode nextSignificantParent() {
		TreeNode val = this;
		while (val.type().isAnonymous() && val._parent != null)
			val = val._parent;
		return val;
	}

	@Override
	public SyntaxNode parent() {
		return this._parent != null ? this._parent.nextSignificantParent() : null;
	}

	@Override
	public SyntaxNode nextSibling() {
		return this._parent != null ? this._parent.nextChild(this.index + 1, 1, -1) : null;
	}

	@Override
	public SyntaxNode prevSibling() {
		return this._parent != null ? this._parent.nextChild(this.index - 1, -1, -1) : null;
	}

	@Override
	public TreeCursor cursor() {
		return new TreeCursor(this);
	}

	@Override
	public SyntaxNode resolve(int pos, Integer side) {
		return this.cursor().moveTo(pos, side).node();
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

	/*
	 * getChild(type: string | number, before: string | number | null = null, after:
	 * string | number | null = null) { let r = getChildren(this, type, before,
	 * after) return r.length ? r[0] : null; }
	 * 
	 * getChildren(type: string | number, before: string | number | null = null,
	 * after: string | number | null = null) { return getChildren(this, type,
	 * before, after); }
	 */

	/// @internal
	@Override
	public String toString() {
		return this.node.toString();
	}
}
