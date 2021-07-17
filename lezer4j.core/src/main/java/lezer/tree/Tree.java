package lezer.tree;

import static java.util.Collections.emptyList;
import static lezer.tree.TreeUtils.BalanceBranchFactor;
import static lezer.tree.TreeUtils.CachedNode;
import static lezer.tree.TreeUtils.DefaultBufferLength;
import static lezer.tree.TreeUtils.balanceRange;
import static lezer.tree.TreeUtils.buildTree;

import java.util.List;
import java.util.stream.Collectors;;

/// A piece of syntax tree. There are two ways to approach these
/// trees: the way they are actually stored in memory, and the
/// convenient way.
///
/// Syntax trees are stored as a tree of `Tree` and `TreeBuffer`
/// objects. By packing detail information into `TreeBuffer` leaf
/// nodes, the representation is made a lot more memory-efficient.
///
/// However, when you want to actually work with tree nodes, this
/// representation is very awkward, so most client code will want to
/// use the `TreeCursor` interface instead, which provides a view on
/// some part of this data structure, and can be used to move around
/// to adjacent nodes.
public class Tree extends TreeChild {

	public interface TreeIteratorHandler {

		boolean handle(NodeType type, int from, int to); // : false | void,

	}

	public final List<TreeChild> children;
	public final List<Integer> positions;

	public Integer contextHash;

/// Construct a new tree. You usually want to go through
/// [`Tree.build`](#tree.Tree^build) instead.
	public Tree(NodeType type,
			/// The tree's child nodes. Children small enough to fit in a
			/// `TreeBuffer will be represented as such, other children can be
			/// further `Tree` instances with their own internal structure.
			List<TreeChild> children,
			/// The positions (offsets relative to the start of this tree) of
			/// the children.
			List<Integer> positions,
			/// The total length of this tree
			int length) {
		super(type, length);
		this.children = children;
		this.positions = positions;

	}

/// @internal
@Override
public String toString() {
	String children = this.children.stream()//
	.map(c -> c.toString())
	.collect(Collectors.joining(","));
	return this.type.name == null || this.type.name.isEmpty() ? children :
    //(/\W/.test(this.type.name) && !this.type.isError ? JSON.stringify(this.type.name) : 
		this.type.name +(!children.isEmpty() ? "(" + children + ")" : "");    
}

/// The empty tree
	public static final Tree empty = new Tree(NodeType.none, emptyList(), emptyList(), 0);

/// Get a [tree cursor](#tree.TreeCursor) rooted at this tree. When
/// `pos` is given, the cursor is [moved](#tree.TreeCursor.moveTo)
/// to the given position and side.
	public TreeCursor cursor() {
		return cursor(null, 0);
	}

	public TreeCursor cursor(Integer pos, int side/* : -1 | 0 | 1 = 0 */) {
		TreeNode scope = (pos != null && CachedNode.get(this) != null ? CachedNode.get(this)
				: (TreeNode) this.topNode());
		TreeCursor cursor = new TreeCursor(scope);
		if (pos != null) {
			cursor.moveTo(pos, side);
			CachedNode.put(this, cursor._tree);
		}
		return cursor;
	}

/// Get a [tree cursor](#tree.TreeCursor) that, unlike regular
/// cursors, doesn't skip [anonymous](#tree.NodeType.isAnonymous)
/// nodes.
	public TreeCursor fullCursor() {
		return new TreeCursor((TreeNode) this.topNode(), true);
	}

/// Get a [syntax node](#tree.SyntaxNode) object for the top of the
/// tree.
	public SyntaxNode topNode() {
		return new TreeNode(this, 0, 0, null);
	}

/// Get the [syntax node](#tree.SyntaxNode) at the given position.
/// If `side` is -1, this will move into nodes that end at the
/// position. If 1, it'll move into nodes that start at the
/// position. With 0, it'll only enter nodes that cover the position
/// from both sides.

	public SyntaxNode resolve(int pos) {
		return resolve(pos, 0);
	}

	public SyntaxNode resolve(int pos, int side/* : -1 | 0 | 1 = 0 */) {
		return this.cursor(pos, side).node();
	}

/// Iterate over the tree and its children, calling `enter` for any
/// node that touches the `from`/`to` region (if given) before
/// running over such a node's children, and `leave` (if given) when
/// leaving the node. When `enter` returns `false`, the given node
/// will not have its children iterated over (or `leave` called).
	public void iterate(TreeIteratorHandler enter, // (type: NodeType, from: number, to: number): false | void,
			TreeIteratorHandler leave, // ?(type: NodeType, from: number, to: number): void,
			Integer from, Integer to) {
		// let {enter, leave, from = 0, to = this.length} = spec
		if (from == null) {
			from = 0;
		}
		if (to == null) {
			to = this.length;
		}
		for (TreeCursor c = this.cursor();;) {
			boolean mustLeave = false;
			if (c.from() <= to && c.to() >= from
					&& (c.type().isAnonymous() || enter.handle(c.type(), c.from(), c.to()) != false)) {
				if (c.firstChild())
					continue;
				if (!c.type().isAnonymous())
					mustLeave = true;
			}
			for (;;) {
				if (mustLeave && leave != null)
					leave.handle(c.type(), c.from(), c.to());
				mustLeave = c.type().isAnonymous();
				if (c.nextSibling())
					break;
				if (!c.parent())
					return;
				mustLeave = true;
			}
		}
	}

/// Balance the direct children of this tree.
	private Tree balance() {
		return balance(DefaultBufferLength);
	}

	private Tree balance(int maxBufferLength) {
		return this.children.size() <= BalanceBranchFactor ? this
				: balanceRange(this.type, NodeType.none, this.children, this.positions, 0, this.children.size(), 0,
						maxBufferLength, this.length, 0);
	}

/// Build a tree from a postfix-ordered buffer of node information,
/// or a cursor over such a buffer.
	public static Tree build(BuildData data) {
		return buildTree(data);
	}

}
