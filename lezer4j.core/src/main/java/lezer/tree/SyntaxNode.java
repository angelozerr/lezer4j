package lezer.tree;

import java.util.List;

/// A syntax node provides an immutable pointer at a given node in a
/// tree. When iterating over large amounts of nodes, you may want to
/// use a mutable [cursor](#tree.TreeCursor) instead, which is more
/// efficient.
interface SyntaxNode {
	/// The type of the node.
	NodeType type();

	/// The name of the node (`.type.name`).
	String name();

	/// The start position of the node.
	int from();

	/// The end position of the node.
	int to();

	/// The node's parent node, if any.
	SyntaxNode parent();

	/// The first child, if the node has children.
	SyntaxNode firstChild();

	/// The node's last child, if available.
	SyntaxNode lastChild();

	/// The first child that starts at or after `pos`.
	SyntaxNode childAfter(int pos);

	/// The last child that ends at or before `pos`.
	SyntaxNode childBefore(int pos);

	/// This node's next sibling, if any.
	SyntaxNode nextSibling();

	/// This node's previous sibling.
	SyntaxNode prevSibling();

	/// A [tree cursor](#tree.TreeCursor) starting at this node.
	TreeCursor cursor();

	/// Find the node around, before (if `side` is -1), or after (`side`
	/// is 1) the given position. Will look in parent nodes if the
	/// position is outside this node.
	SyntaxNode resolve(int pos, Integer side /* -1 | 0 | 1 */);

	/// Get the first child of the given type (which may be a [node
	/// name](#tree.NodeProp.name) or a [group
	/// name](#tree.NodeProp^group)). If `before` is non-null, only
	/// return children that occur somewhere after a node with that name
	/// or group. If `after` is non-null, only return children that
	/// occur somewhere before a node with that name or group.
	default SyntaxNode getChild(String type/* : string | number */) {
		return getChild(type, null);
	}

	default SyntaxNode getChild(String type/* : string | number */, String before/* ?: string | number | null */) {
		return getChild(type, before, null);
	}

	SyntaxNode getChild(String type/* : string | number */, String before/* ?: string | number | null */,
			String after/* ?: string | number | null */);

	/// Like [`getChild`](#tree.SyntaxNode.getChild), but return all
	/// matching children, not just the first.
	default List<SyntaxNode> getChildren(String type/* : string | number */) {
		return getChildren(type, null);
	}

	default List<SyntaxNode> getChildren(String type/* : string | number */,
			String before /* ?: string | number | null */) {
		return getChildren(type, before, null);
	}

	List<SyntaxNode> getChildren(String type/* : string | number */, String before /* ?: string | number | null */,
			String after/* ?: string | number | null */);
}
