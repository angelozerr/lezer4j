package lezer.tree;

import java.util.function.Function;

/// Each [node type](#tree.NodeType) can have metadata associated with
/// it in props. Instances of this class represent prop names.
public class NodeProp<T> {
/// @internal
public int id;

/// A method that deserializes a value of this prop from a string.
/// Can be used to allow a prop to be directly written in a grammar
/// file. Defaults to raising an error.
private Function<String, T> deserialize;

private static int nextPropID=0;

/// Create a new node prop type. You can optionally pass a
/// `deserialize` function.
public NodeProp(Function<String, T> deserialize) {//{deserialize}: {deserialize?: (str: string) => T} = {}) {
  this.id = nextPropID++;
  this.deserialize = deserialize != null ? deserialize : str -> {
		throw new Error("This node type doesn't define a deserialize function");	
	};
}

/// Create a string-valued node prop whose deserialize function is
/// the identity function.
public static final NodeProp<String> string() { 
	return new NodeProp<String>(str -> str); 
}

/// Create a number-valued node prop whose deserialize function is
/// just `Number`.
public static final NodeProp<Number> number() { 
	return new NodeProp<Number>(str -> Integer.parseInt(str));
}

/// Creates a boolean-valued node prop whose deserialize function
/// returns true for any input.
public static final NodeProp<Boolean> flag() { 
	return new NodeProp<Boolean>(str -> Boolean.TRUE);
}

/// Store a value for this prop in the given object. This can be
/// useful when building up a prop object to pass to the
/// [`NodeType`](#tree.NodeType) constructor. Returns its first
/// argument.
/* TODO!!!
public void set(propObj: {[prop: number]: any}, value: T) {
  propObj[this.id] = value;
  return propObj;
}
*/

/// This is meant to be used with
/// [`NodeSet.extend`](#tree.NodeSet.extend) or
/// [`Parser.withProps`](#lezer.Parser.withProps) to compute prop
/// values for each node type in the set. Takes a [match
/// object](#tree.NodeType^match) or function that returns undefined
/// if the node type doesn't get this prop, and the prop's value if
/// it does.
/* /* TODO!!!
public NodePropSource add(match: {[selector: string]: T} | ((type: NodeType) => T | undefined)) {
  if (typeof match != "function") match = NodeType.match(match);
  return (type) => {
    let result = (match as (type: NodeType) => T | undefined)(type)
    return result === undefined ? null : [this, result];
  }
}
*/

/// Prop that is used to describe matching delimiters. For opening
/// delimiters, this holds an array of node names (written as a
/// space-separated string when declaring this prop in a grammar)
/// for the node types of closing delimiters that match it.
public static final NodeProp<String[]> closedBy = new NodeProp<String[]>(str -> str.split(" "));

/// The inverse of [`openedBy`](#tree.NodeProp^closedBy). This is
/// attached to closing delimiters, holding an array of node names
/// of types of matching opening delimiters.
public static final NodeProp<String[]> openedBy = new NodeProp<String[]>( str -> str.split(" "));

/// Used to assign node types to groups (for example, all node
/// types that represent an expression could be tagged with an
/// `"Expression"` group).
public static final NodeProp<String[]> group = new NodeProp<String[]>(str -> str.split(" "));

}
