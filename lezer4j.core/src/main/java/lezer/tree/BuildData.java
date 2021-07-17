package lezer.tree;

import java.util.List;

public class BuildData {
	/// The buffer or buffer cursor to read the node data from.
	///
	/// When this is an array, it should contain four values for every
	/// node in the tree.
	///
	/// - The first holds the node's type, as a node ID pointing into
	/// the given `NodeSet`.
	/// - The second holds the node's start offset.
	/// - The third the end offset.
	/// - The fourth the amount of space taken up in the array by this
	/// node and its children. Since there's four values per node,
	/// this is the total number of nodes inside this node (children
	/// and transitive children) plus one for the node itself, times
	/// four.
	///
	/// Parent nodes should appear _after_ child nodes in the array. As
	/// an example, a node of type 10 spanning positions 0 to 4, with
	/// two children, of type 11 and 12, might look like this:
	///
	/// [11, 0, 1, 4, 12, 2, 4, 4, 10, 0, 4, 12]
	private final Object buffer; // : BufferCursor | readonly number[],
	/// The node types to use.
	private final NodeSet nodeSet;
	/// The id of the top node type, if any.
	private Integer topID;
	/// The position the tree should start at. Defaults to 0.
	private Integer start; // ?: number,
	/// The length of the wrapping node. The end offset of the last
	/// child is used when not provided.
	private Integer length; // ?: number,
	/// The maximum buffer length to use. Defaults to
	/// [`DefaultBufferLength`](#tree.DefaultBufferLength).
	private Integer maxBufferLength;// ?: number,
	/// An optional set of reused nodes that the buffer can refer to.
	private List<TreeChild> reused; // ?: (Tree | TreeBuffer)[],
	/// The first node type that indicates repeat constructs in this
	/// grammar.
	private Integer minRepeatType;

	public BuildData(BufferCursor buffer, NodeSet nodeSet) {
		this.buffer = buffer;
		this.nodeSet = nodeSet;
	}
	
	public BuildData(List<Integer> buffer, NodeSet nodeSet) {
		this.buffer = buffer;
		this.nodeSet = nodeSet;
	}

	public Integer getTopID() {
		return topID;
	}

	public void setTopID(Integer topID) {
		this.topID = topID;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getMaxBufferLength() {
		return maxBufferLength;
	}

	public void setMaxBufferLength(Integer maxBufferLength) {
		this.maxBufferLength = maxBufferLength;
	}

	public List<TreeChild> getReused() {
		return reused;
	}

	public void setReused(List<TreeChild> reused) {
		this.reused = reused;
	}

	public Integer getMinRepeatType() {
		return minRepeatType;
	}

	public void setMinRepeatType(Integer minRepeatType) {
		this.minRepeatType = minRepeatType;
	}

	public Object getBuffer() {
		return buffer;
	}

	public NodeSet getNodeSet() {
		return nodeSet;
	}

}
