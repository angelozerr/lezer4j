package lezer.tree;

import static lezer.tree.TreeUtils.hasChild;

import java.util.ArrayList;
import java.util.List;

/// A tree cursor object focuses on a given node in a syntax tree, and
/// allows you to move to adjacent nodes.
public class TreeCursor {
	/// The node's type.
	private NodeType type;

	/// The start source offset of this node.
	private int from;

	/// The end source offset.
	private int to;

	/// @internal
	public TreeNode _tree;
	private BufferContext buffer;
	private final List<Integer> stack;
	private int index;
	private BufferNode bufferNode;
	private final boolean full;

	/// @internal

	public TreeCursor(BufferNode node) {
		this(node, false);
	}

	public TreeCursor(BufferNode node, boolean full) {
		this.stack = new ArrayList<>();
		this._tree = node.context.parent;
		this.buffer = node.context;
		for (BufferNode n = node._parent; n != null; n = n._parent)
			// this.stack.unshift(n.index);
			this.stack.add(0, n.index);
		this.bufferNode = node;
		this.yieldBuf(node.index);
		this.full = full;
	}

	public TreeCursor(TreeNode node) {
		this(node, false);
	}

	public TreeCursor(TreeNode node, boolean full) {
		this.stack = new ArrayList<>();
		this.yieldNode(node);
		this.full = full;
	}

	/// Shorthand for `.type.name`.
	public String name() {
		return this.type.name;
	}

	public NodeType type() {
		return this.type;
	}

	public int from() {
		return this.from;
	}

	public int to() {
		return this.to;
	}

	private boolean yieldNode(TreeNode node) {
		if (node == null)
			return false;
		this._tree = node;
		this.type = node.type();
		this.from = node.from();
		this.to = node.to();
		return true;
	}

	private boolean yieldBuf(int index) {
		return yieldBuf(index, null);
	}

	private boolean yieldBuf(int index, NodeType type) {
		this.index = index;
		int start = this.buffer.start;
		TreeBuffer buffer = this.buffer.buffer;
		this.type = type != null ? type : buffer.set.types.get(buffer.buffer.get(index));
		this.from = start + buffer.buffer.get(index + 1);
		this.to = start + buffer.buffer.get(index + 2);
		return true;
	}

	private boolean yield(Object node /* TreeNode | BufferNode | null */) {
		if (node == null)
			return false;
		if (node instanceof TreeNode) {
			this.buffer = null;
			return this.yieldNode((TreeNode) node);
		}
		BufferNode buffNode = (BufferNode) node;
		this.buffer = buffNode.context;
		return this.yieldBuf(buffNode.index, buffNode.type);
	}

	/// @internal
	@Override
	public String toString() {
		return this.buffer != null ? this.buffer.buffer.childString(this.index) : this._tree.toString();
	}

	/// @internal
	public boolean enter(int dir/* : 1 | -1 */, double after) {
		if (this.buffer == null)
			return this.yield(
					this._tree.nextChild(dir < 0 ? this._tree.node.children.size() - 1 : 0, dir, after, this.full));

		TreeBuffer buffer = this.buffer.buffer;
		int index = buffer.findChild(this.index + 4, buffer.buffer.get(this.index + 3), dir,
				after == After.None ? After.None : after - this.buffer.start);
		if (index < 0)
			return false;
		this.stack.add(this.index);
		return this.yieldBuf(index);
	}

	/// Move the cursor to this node's first child. When this returns
	/// false, the node has no child, and the cursor has not been moved.
	public boolean firstChild() {
		return this.enter(1, After.None);
	}

	/// Move the cursor to this node's last child.
	public boolean lastChild() {
		return this.enter(-1, After.None);
	}

	/// Move the cursor to the first child that starts at or after `pos`.
	public boolean childAfter(int pos) {
		return this.enter(1, pos);
	}

	/// Move to the last child that ends at or before `pos`.
	public boolean childBefore(int pos) {
		return this.enter(-1, pos);
	}

	/// Move the node's parent node, if this isn't the top node.
	public boolean parent() {
		if (this.buffer == null)
			return this.yieldNode(this.full ? this._tree._parent : (TreeNode) this._tree.parent());
		if (!this.stack.isEmpty())
			return this.yieldBuf(this.stack.remove(this.stack.size() - 1));
		TreeNode parent = this.full ? this.buffer.parent : this.buffer.parent.nextSignificantParent();
		this.buffer = null;
		return this.yieldNode(parent);
	}

	/// @internal
	public boolean sibling(int dir /* : 1 | -1 */) {
		if (this.buffer == null)
			return this._tree._parent == null ? false
					: this.yield(this._tree._parent.nextChild(this._tree.index + dir, dir, After.None, this.full));

		TreeBuffer buffer = this.buffer.buffer;
		int d = this.stack.size() - 1;
		if (dir < 0) {
			int parentStart = d < 0 ? 0 : this.stack.get(d) + 4;
			if (this.index != parentStart)
				return this.yieldBuf(buffer.findChild(parentStart, this.index, -1, After.None));
		} else {
			int after = buffer.buffer.get(this.index + 3);
			if (after < (d < 0 ? buffer.buffer.length : buffer.buffer.get(this.stack.get(d) + 3)))
				return this.yieldBuf(after);
		}
		return d < 0 ? this.yield(this.buffer.parent.nextChild(this.buffer.index + dir, dir, After.None, this.full))
				: false;
	}

	/// Move to this node's next sibling, if any.
	public boolean nextSibling() {
		return this.sibling(1);
	}

	/// Move to this node's previous sibling, if any.
	public boolean prevSibling() {
		return this.sibling(-1);
	}

	private boolean atLastNode(int dir/* : 1 | -1 */) {
		int index;
		TreeNode parent;
		BufferContext buffer = this.buffer;
		if (buffer != null) {
			if (dir > 0) {
				if (this.index < buffer.buffer.buffer.length)
					return false;
			} else {
				for (int i = 0; i < this.index; i++)
					if (buffer.buffer.buffer.get(i + 3) < this.index)
						return false;
			}
			index = buffer.index;
			parent = buffer.parent;
			// ;({index, parent} = buffer);
		} else {
			index = this._tree.index;
			parent = this._tree._parent;
			// ({index, _parent: parent} = this._tree)
		}
		for (; parent != null; index = parent.index, parent = parent._parent) {
			for (int i = index + dir, e = dir < 0 ? -1 : parent.node.children.size(); i != e; i += dir) {
				TreeChild child = parent.node.children.get(i);
				if (this.full || !child.type.isAnonymous() || child instanceof TreeBuffer || hasChild((Tree) child))
					return false;
			}
		}
		return true;
	}

	private boolean move(int dir /* : 1 | -1 */) {
		if (this.enter(dir, After.None))
			return true;
		for (;;) {
			if (this.sibling(dir))
				return true;
			if (this.atLastNode(dir) || !this.parent())
				return false;
		}
	}

	/// Move to the next node in a
	/// [pre-order](https://en.wikipedia.org/wiki/Tree_traversal#Pre-order_(NLR))
	/// traversal, going from a node to its first child or, if the
	/// current node is empty, its next sibling or the next sibling of
	/// the first parent node that has one.
	public boolean next() {
		return this.move(1);
	}

	/// Move to the next node in a last-to-first pre-order traveral. A
	/// node is followed by ist last child or, if it has none, its
	/// previous sibling or the previous sibling of the first parent
	/// node that has one.
	public boolean prev() {
		return this.move(-1);
	}

	/// Move the cursor to the innermost node that covers `pos`. If
	/// `side` is -1, it will enter nodes that end at `pos`. If it is 1,
	/// it will enter nodes that start at `pos`.
	public TreeCursor moveTo(int pos, int side /* : -1 | 0 | 1 = 0 */) {
		// Move up to a node that actually holds the position, if possible
		while (this.from == this.to || (side < 1 ? this.from >= pos : this.from > pos)
				|| (side > -1 ? this.to <= pos : this.to < pos))
			if (!this.parent())
				break;

		// Then scan down into child nodes as far as possible
		for (;;) {
			if (side < 0 ? !this.childBefore(pos) : !this.childAfter(pos))
				break;
			if (this.from == this.to || (side < 1 ? this.from >= pos : this.from > pos)
					|| (side > -1 ? this.to <= pos : this.to < pos)) {
				this.parent();
				break;
			}
		}
		return this;
	}

	/// Get a [syntax node](#tree.SyntaxNode) at the cursor's current
	/// position.
	public SyntaxNode node() {
		if (this.buffer == null)
			return this._tree;

		BufferNode cache = this.bufferNode;
		BufferNode result = null;
		int depth = 0;
		if (cache != null && cache.context == this.buffer) {
			scan: for (int index = this.index, d = this.stack.size(); d >= 0;) {
				for (BufferNode c = cache; c != null; c = c._parent)
					if (c.index == index) {
						if (index == this.index)
							return c;
						result = c;
						depth = d + 1;
						break scan;
					}
				index = this.stack.get(--d);
			}
		}
		for (int i = depth; i < this.stack.size(); i++)
			result = new BufferNode(this.buffer, result, this.stack.get(i));
		return this.bufferNode = new BufferNode(this.buffer, result, this.index);
	}

	/// Get the [tree](#tree.Tree) that represents the current node, if
	/// any. Will return null when the node is in a [tree
	/// buffer](#tree.TreeBuffer).
	public Tree tree() {
		return this.buffer != null ? null : this._tree.node;
	}
}
