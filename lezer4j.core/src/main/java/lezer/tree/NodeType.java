package lezer.tree;

import java.util.Collections;
import java.util.Map;

/// Each node in a syntax tree has a node type associated with it.
public class NodeType {

	private static final Map<Integer, Object> noProps = Collections.emptyMap();

	public final String name;
	public final Map<Integer, Object> props;
	public final int id;
	public final int flags;
	
/// @internal
	NodeType(
			/// The name of the node type. Not necessarily unique, but if the
			/// grammar was written properly, different node types with the
			/// same name within a node set should play the same semantic
			/// role.
			String name,
			/// @internal
			Map<Integer, Object> props, // : {readonly [prop: number]: any},
			/// The id of this node in its set. Corresponds to the term ids
			/// used in the parser.
			int id,
			/// @internal
			int flags) {
		this.name = name;
		this.props = props;
		this.id = id;
		this.flags = flags;
	}

	public static NodeType define(int id) {
		return define(id, null, null, null, null, null);
	}

	public static NodeType define(
			/// The ID of the node type. When this type is used in a
			/// [set](#tree.NodeSet), the ID must correspond to its index in
			/// the type array.
			int id,
			/// The name of the node type. Leave empty to define an anonymous
			/// node.
			String name,
			/// [Node props](#tree.NodeProp) to assign to the type. The value
			/// given for any given prop should correspond to the prop's type.
			Map<Integer, Object> props, // FIXME!!! ?: readonly ([NodeProp<any>, any] | NodePropSource)[],
			/// Whether is is a [top node](#tree.NodeType.isTop).
			Boolean top,
			/// Whether this node counts as an [error
			/// node](#tree.NodeType.isError).
			Boolean error,
			/// Whether this node is a [skipped](#tree.NodeType.isSkipped)
			/// node.
			Boolean skipped) {
		//let props = spec.props && spec.props.length ? Object.create(null) : noProps
			    int flags = (top != null && top ? NodeFlag.Top : 0) | (skipped != null && skipped ? NodeFlag.Skipped : 0) |
			      (error != null && error ? NodeFlag.Error : 0) | (name == null ? NodeFlag.Anonymous : 0);
			      NodeType type = new NodeType(name != null ? name :  "", props, id, flags);
			    /*if (spec.props) for (let src of spec.props) {
			      if (!Array.isArray(src)) src = src(type)!
			      if (src) src[0].set(props, src[1])
			    }*/
			    return type;
	}

/// Retrieves a node prop for this type. Will return `undefined` if
/// the prop isn't present on this node.
// TODO!!!
// prop<T>(prop: NodeProp<T>): T | undefined { return this.props[prop.id] }

/// True when this is the top node of a grammar.
	public boolean isTop() {
		return (this.flags & NodeFlag.Top) > 0;
	}

/// True when this node is produced by a skip rule.
	public boolean isSkipped() {
		return (this.flags & NodeFlag.Skipped) > 0;
	}

/// Indicates whether this is an error node.
	public boolean isError() {
		return (this.flags & NodeFlag.Error) > 0;
	}

/// When true, this node type doesn't correspond to a user-declared
/// named node, for example because it is used to cache repetition.
	public boolean isAnonymous() {
		return (this.flags & NodeFlag.Anonymous) > 0;
	}

/// Returns true when this node's name or one of its
/// [groups](#tree.NodeProp^group) matches the given string.
	public boolean is(String name) {
		if (this.name == name)
			return true;
		// TODO!!!
		/*
		 * let group = this.prop(NodeProp.group); return group ? group.indexOf(name) >
		 * -1 : false;
		 */
		return false;
	}

	public boolean is(int name) {
		return this.id == name;
	}

/// An empty dummy node type to use when no actual type is available.
	public static final NodeType none = new NodeType("", /* Object.create(null) */ null, 0, NodeFlag.Anonymous);

/// Create a function from node types to arbitrary values by
/// specifying an object whose property names are node or
/// [group](#tree.NodeProp^group) names. Often useful with
/// [`NodeProp.add`](#tree.NodeProp.add). You can put multiple
/// names, separated by spaces, in a single property name to map
/// multiple node names to a single value.

// TODO!!!

	/*
	 * static match<T>(map: {[selector: string]: T}): (node: NodeType) => T |
	 * undefined { let direct = Object.create(null) for (let prop in map) for (let
	 * name of prop.split(" ")) direct[name] = map[prop] return (node: NodeType) =>
	 * { for (let groups = node.prop(NodeProp.group), i = -1; i < (groups ?
	 * groups.length : 0); i++) { let found = direct[i < 0 ? node.name : groups![i]]
	 * if (found) return found } } }
	 */
}
