package lezer.tree;

import static java.util.Collections.reverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import lezer.tree.typedarray.Uint16Array;

public class TreeUtils {

	public static final int BalanceBranchFactor = 8;

	/// The default maximum length of a `TreeBuffer` node.
	public static final int DefaultBufferLength = 1024;

	// FIXME : remove this static CACHE!!!
	public static final WeakHashMap<Tree, TreeNode> CachedNode = new WeakHashMap<>();

	public static List<SyntaxNode> getChildren(SyntaxNode node, String type /* : string | number */,
			String before/* : string | number | null */, String after/* : string | number | null */) {
		TreeCursor cur = node.cursor();
		List<SyntaxNode> result = new ArrayList<>();
		if (!cur.firstChild())
			return result;
		if (before != null)
			while (!cur.type().is(before))
				if (!cur.nextSibling())
					return result;
		for (;;) {
			if (after != null && cur.type().is(after))
				return result;
			if (cur.type().is(type))
				result.add(cur.node());
			if (!cur.nextSibling())
				return after == null ? result : Collections.emptyList();
		}
	}

	// For trees that need a context hash attached, we're using this
	// kludge which assigns an extra property directly after
	// initialization (creating a single new object shape).
	public static Tree withHash(Tree tree, Integer hash) {
		if (hash != null)
			tree.contextHash = hash;
		return tree;
	}

	private static int takeNode(int parentStart, int minPos, List<TreeChild> children, List<Integer> positions,
			int inRepeat, BufferCursor cursor, int contextHash, List<TreeChild> reused, int minRepeatType,
			int maxBufferLength, List<NodeType> types, NodeSet nodeSet) {
		int id = cursor.id();
		int start = cursor.start();
		int end = cursor.end();
		int size = cursor.size();
		int startPos = start - parentStart;
		if (size < 0) {
			if (size == -1) { // Reused node
				children.add(reused.get(id));
				positions.add(startPos);
			} else { // Context change
				contextHash = id;
			}
			cursor.next();
			return contextHash;
		}

		NodeType type = types.get(id);
		TreeChild node = null;
		BufferSize buffer = null;
		if (end - start <= maxBufferLength && (buffer = findBufferSize(cursor.pos() - minPos, inRepeat, cursor,
				minRepeatType, maxBufferLength)) != null) {
			// Small enough for a buffer, and no reused nodes inside
			Uint16Array data = new Uint16Array(buffer.size - buffer.skip);
			int endPos = cursor.pos() - buffer.size, index = data.length;
			while (cursor.pos() > endPos)
				index = copyToBuffer(buffer.start, data, index, inRepeat, cursor, minRepeatType);
			node = new TreeBuffer(data, end - buffer.start, nodeSet, inRepeat < 0 ? NodeType.none : types.get(inRepeat));
			startPos = buffer.start - parentStart;
		} else { // Make it a node
			int endPos = cursor.pos() - size;
			cursor.next();
			List<TreeChild> localChildren = new ArrayList<>();
			List<Integer> localPositions = new ArrayList<>();
			int localInRepeat = id >= minRepeatType ? id : -1;
			while (cursor.pos() > endPos) {
				if (cursor.id() == localInRepeat)
					cursor.next();
				else {
					contextHash = takeNode(start, endPos, localChildren, localPositions, localInRepeat, cursor,
							contextHash, reused, minRepeatType, maxBufferLength, types, nodeSet);
				}
			}
			reverse(localChildren);
			reverse(localPositions);

			if (localInRepeat > -1 && localChildren.size() > BalanceBranchFactor)
				node = balanceRange(type, type, localChildren, localPositions, 0, localChildren.size(), 0,
						maxBufferLength, end - start, contextHash);
			else
				node = withHash(new Tree(type, localChildren, localPositions, end - start), contextHash);
		}

		children.add(node);
		positions.add(startPos);
		return contextHash;
	}

	private static class BufferSize {

		private int size;
		private int start;
		private int skip;

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getSkip() {
			return skip;
		}

		public void setSkip(int skip) {
			this.skip = skip;
		}

	}

	private static BufferSize findBufferSize(int maxSize, int inRepeat, BufferCursor cursor, int minRepeatType,
			int maxBufferLength) {
		// Scan through the buffer to find previous siblings that fit
		// together in a TreeBuffer, and don't contain any reused nodes
		// (which can't be stored in a buffer).
		// If `inRepeat` is > -1, ignore node boundaries of that type for
		// nesting, but make sure the end falls either at the start
		// (`maxSize`) or before such a node.
		BufferCursor fork = cursor.fork();
		int size = 0, start = 0, skip = 0, minStart = fork.end() - maxBufferLength;
		BufferSize result = new BufferSize(); // {size: 0, start: 0, skip: 0}
		scan: for (int minPos = fork.pos() - maxSize; fork.pos() > minPos;) {
			// Pretend nested repeat nodes of the same type don't exist
			if (fork.id() == inRepeat) {
				// Except that we store the current state as a valid return
				// value.
				result.size = size;
				result.start = start;
				result.skip = skip;
				skip += 4;
				size += 4;
				fork.next();
				continue;
			}
			int nodeSize = fork.size(), startPos = fork.pos() - nodeSize;
			if (nodeSize < 0 || startPos < minPos || fork.start() < minStart)
				break;
			int localSkipped = fork.id() >= minRepeatType ? 4 : 0;
			int nodeStart = fork.start();
			fork.next();
			while (fork.pos() > startPos) {
				if (fork.size() < 0)
					break scan;
				if (fork.id() >= minRepeatType)
					localSkipped += 4;
				fork.next();
			}
			start = nodeStart;
			size += nodeSize;
			skip += localSkipped;
		}
		if (inRepeat < 0 || size == maxSize) {
			result.size = size;
			result.start = start;
			result.skip = skip;
		}
		return result.size > 4 ? result : null;
	}

	private static int copyToBuffer(int bufferStart, Uint16Array buffer, int index, int inRepeat, BufferCursor cursor,
			int minRepeatType) {
		int id = cursor.id();
		int start = cursor.start();
		int end = cursor.end();
		int size = cursor.size();
		cursor.next();
		if (id == inRepeat)
			return index;
		int startIndex = index;
		if (size > 4) {
			int endPos = cursor.pos() - (size - 4);
			while (cursor.pos() > endPos)
				index = copyToBuffer(bufferStart, buffer, index, inRepeat, cursor, minRepeatType);
		}
		if (id < minRepeatType) { // Don't copy repeat nodes into buffers
			buffer.set(--index, startIndex);
			buffer.set(--index, end - bufferStart);
			buffer.set(--index, start - bufferStart);
			buffer.set(--index, id);
		}
		return index;
	}

	public static Tree buildTree(BuildData data) {
		Object buffer = data.getBuffer();
		NodeSet nodeSet = data.getNodeSet();
		int topID = data.getTopID() != null ? data.getTopID() : 0;
		int maxBufferLength = data.getMaxBufferLength() != null ? data.getMaxBufferLength() : DefaultBufferLength;
		List<TreeChild> reused = data.getReused() != null ? data.getReused() : new ArrayList<>();
		int minRepeatType = data.getMinRepeatType() != null ? data.getMinRepeatType() : nodeSet.types.size();

		BufferCursor cursor = buffer instanceof BufferCursor ? (BufferCursor) buffer
				: new FlatBufferCursor((List<Integer>) buffer, ((List<Integer>) buffer).size());

		List<NodeType> types = nodeSet.types;

		int contextHash = 0;

		List<TreeChild> children = new ArrayList<>();
		List<Integer> positions = new ArrayList<>();
		while (cursor.pos() > 0)
			contextHash = takeNode(data.getStart() != null ? data.getStart() : 0, 0, children, positions, -1, cursor,
					contextHash, reused, minRepeatType, maxBufferLength, types, nodeSet);
		int length = data.getLength() != null ? data.getLength() : (!children.isEmpty() ? positions.get(0) + children.get(0).length : 0);
		// data.length ?? (children.length ? positions[0] + children[0].length : 0);
		Collections.reverse(children);
		Collections.reverse(positions);
		return new Tree(types.get(topID), children, positions, length);
	}

	public static Tree balanceRange(NodeType outerType, NodeType innerType, List<TreeChild> children,
			List<Integer> positions, int from, int to, int start, int maxBufferLength, int length, int contextHash) {
		List<TreeChild> localChildren = new ArrayList<>();
		List<Integer> localPositions = new ArrayList<>();
		if (length <= maxBufferLength) {
			for (int i = from; i < to; i++) {
				localChildren.add(children.get(i));
				localPositions.add(positions.get(i) - start);
			}
		} else {
			int maxChild = (int) Math.max(maxBufferLength, Math.ceil(length * 1.5 / BalanceBranchFactor));
			for (int i = from; i < to;) {
				int groupFrom = i, groupStart = positions.get(i);
				i++;
				for (; i < to; i++) {
					int nextEnd = positions.get(i) + children.get(i).length;
					if (nextEnd - groupStart > maxChild)
						break;
				}
				if (i == groupFrom + 1) {
					TreeChild only = children.get(groupFrom);
					if (only instanceof Tree && only.type == innerType && only.length > maxChild << 1) { // Too big,
																											// collapse
						Tree tree = (Tree) only;
						for (int j = 0; j < tree.children.size(); j++) {
							localChildren.add(tree.children.get(j));
							localPositions.add(tree.positions.get(j) + groupStart - start);
						}
						continue;
					}
					localChildren.add(only);
				} else if (i == groupFrom + 1) {
					localChildren.add(children.get(groupFrom));
				} else {
					Tree inner = balanceRange(innerType, innerType, children, positions, groupFrom, i, groupStart,
							maxBufferLength, positions.get(i - 1) + children.get(i - 1).length - groupStart,
							contextHash);
					if (innerType != NodeType.none && !containsType(inner.children, innerType))
						inner = withHash(new Tree(NodeType.none, inner.children, inner.positions, inner.length),
								contextHash);
					localChildren.add(inner);
				}
				localPositions.add(groupStart - start);
			}
		}
		return withHash(new Tree(outerType, localChildren, localPositions, length), contextHash);
	}

	private static boolean containsType(List<TreeChild> nodes, NodeType type) {
		for (TreeChild elt : nodes)
			if (elt.type == type)
				return true;
		return false;
	}

	public static boolean hasChild(Tree tree) {
		return tree.children//
				.stream()//
				.anyMatch(ch -> !ch.type.isAnonymous() || ch instanceof TreeBuffer || hasChild((Tree) ch));
	}
}
