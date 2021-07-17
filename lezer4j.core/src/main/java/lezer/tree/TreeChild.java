package lezer.tree;

public class TreeChild {

	public final NodeType type;
	
	public final int length;

	public TreeChild(NodeType type, int length) {
		this.type = type;
		this.length = length;
	}

}
