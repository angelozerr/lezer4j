package lezer.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lezer.tree.typedarray.Uint16Array;

/// Tree buffers contain (type, start, end, endIndex) quads for each
/// node. In such a buffer, nodes are stored in prefix order (parents
/// before children, with the endIndex of the parent indicating which
/// children belong to it)
public class TreeBuffer extends TreeChild {

	// TODO!!!
	// private final Uint16Array buffer;
	public final Uint16Array buffer;
	public final NodeSet set;

/// Create a tree buffer @internal
	TreeBuffer(
			/// @internal
			Uint16Array buffer,
			/// The total length of the group of nodes in the buffer.
			int length,
			/// @internal
			NodeSet set,
			/// An optional repeat node type associated with the buffer.
			NodeType type) {
		super(type, length);
		this.buffer = buffer;
		this.set = set;
	}

/// @internal
	@Override
	public String toString() {
		List<String> result = new ArrayList<>();
		for (int index = 0; index < this.buffer.length;) {
			result.add(this.childString(index));
			index = this.buffer.get(index + 3);
		}
		return result.stream()//
				.collect(Collectors.joining(","));
	}

/// @internal
	public String childString(int index) {
		int id = this.buffer.get(index), endIndex = this.buffer.get(index + 3);
		NodeType type = this.set.types.get(id);
		String result = type.name;
		// TODO!!!!
		// if (/\W/.test(result) && !type.isError) result = JSON.stringify(result);
		index += 4;
		if (endIndex == index)
			return result;
		List<String> children = new ArrayList<>();
		while (index < endIndex) {
			children.add(this.childString(index));
			index = this.buffer.get(index + 3);
		}
		return result + "(" + children.stream().collect(Collectors.joining(",")) + ")";
	}

/// @internal
	public int findChild(int startIndex, int endIndex, int dir/* : 1 | -1 */, double after) {
		Uint16Array buffer = this.buffer;
		int pick = -1;
		for (int i = startIndex; i != endIndex; i = buffer.get(i + 3)) {
			if (after != After.None) {
				int start = buffer.get(i + 1), end = buffer.get(i + 2);
				if (dir > 0) {
					if (end > after)
						pick = i;
					if (end > after)
						break;
				} else {
					if (start < after)
						pick = i;
					if (end >= after)
						break;
				}
			} else {
				pick = i;
				if (dir > 0)
					break;
			}
		}
		return pick;
	}

}
