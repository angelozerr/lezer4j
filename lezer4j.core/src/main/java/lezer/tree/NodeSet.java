package lezer.tree;

import java.util.List;

/// A node set holds a collection of node types. It is used to
/// compactly represent trees by storing their type ids, rather than a
/// full pointer to the type object, in a number array. Each parser
/// [has](#lezer.Parser.nodeSet) a node set, and [tree
/// buffers](#tree.TreeBuffer) can only store collections of nodes
/// from the same set. A set can have a maximum of 2**16 (65536)
/// node types in it, so that the ids fit into 16-bit typed array
/// slots.
public class NodeSet {

	public final List<NodeType> types;

/// Create a set with the given types. The `id` property of each
/// type should correspond to its position within the array.
	public NodeSet(
			/// The node types in this set, by id.
			List<NodeType> types) {
		this.types = types;
		for (int i = 0; i < types.size(); i++) {
			if (types.get(i).id != i) {
				throw new RangeError("Node type ids should correspond to array positions when creating a node set");
			}
		}
	}

/// Create a copy of this set with some node properties added. The
/// arguments to this method should be created with
/// [`NodeProp.add`](#tree.NodeProp.add).
// TODO!!!
	
/*public NodeSet extend(NodePropSource ...props) {
  List<NodeType> newTypes = new ArrayList<>();
  for (NodeType type of this.types) {
    let newProps = null;
    for (let source of props) {
      let add = source(type);
      if (add) {
        if (!newProps) newProps = Object.assign({}, type.props);
        add[0].set(newProps, add[1]);
      }
    }
    newTypes.push(newProps ? new NodeType(type.name, newProps, type.id, type.flags) : type);
  }
  return new NodeSet(newTypes);
}*/

}
