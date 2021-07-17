package lezer.tree;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LezerTreeAssertion {

	private static final List<NodeType> types;
	private static final NodeSet nodeSet;
	private static final NodeType repeat;

	public static final Tree anonTree;

	static {
		types = new ArrayList<>();

		String[] names = "T a b c Pa Br".split(" ");
		for (int i = 0; i < names.length; i++) {
			String s = names[i];
			Map<Integer, Object> props = new HashMap<>();
			if (s.matches("^[abc]$")) {
				props.put(NodeProp.group.id, Arrays.asList("atom"));
			}
			types.add(NodeType.define(//
					i, s, props, null, null, null
			// ,
			/// ^[abc]$/.test(s) ? [[NodeProp.group, ["atom"]]] : []
			));
		}

		repeat = NodeType.define(types.size());
		types.add(repeat);
		nodeSet = new NodeSet(types);

		anonTree = new Tree(nodeSet.types.get(0), //
				Arrays.asList(//
						new Tree(NodeType.none, //
								Arrays.asList(//
										new Tree(nodeSet.types.get(1), emptyList(), emptyList(), 1), //
										new Tree(nodeSet.types.get(2), emptyList(), emptyList(), 1)),
								Arrays.asList(0, 1), 2)),
				Arrays.asList(0), 2);
	}

	private static int id(String n) {
		return types.stream().filter(x -> x.name.equals(n)).findFirst().get().id;
	}

	public static Tree mk(String spec) {
		List<Integer> starts = new ArrayList<>();
		List<Integer> buffer = new ArrayList<>();
		for (int pos = 0; pos < spec.length();) {
			Matcher m = Pattern.compile("(?:([abc]+)|([\\[\\(])|([\\]\\)]))").matcher(spec.substring(pos));
			if (m.find()) {
				String letters = m.group(1);
				String open = m.group(2);
				String close = m.group(3);
				// let [m, letters, open, close] =
				// /^(?:([abc]+)|([\[\(])|([\]\)]))/.exec(spec.slice(pos))!;
				if (letters != null) {
					int bufStart = buffer.size();
					for (int i = 0; i < letters.length(); i++) {
						buffer.add(id(letters.charAt(i) + ""));
						buffer.add(pos + i);
						buffer.add(pos + i + 1);
						buffer.add(4);
						if (i > 0) {
							int bufferLength = buffer.size();
							buffer.add(repeat.id);
							buffer.add(pos);
							buffer.add(pos + i + 1);
							buffer.add((bufferLength + 4) - bufStart);
						}
					}
				} else if (open != null) {
					starts.add(buffer.size());
					starts.add(pos);
				} else {
					int bufferLength = buffer.size();
					buffer.add(id(close.equals(")") ? "Pa" : "Br"));
					buffer.add(starts.remove(starts.size() - 1));
					buffer.add(pos + 1);
					buffer.add((bufferLength + 4) - starts.remove(starts.size() - 1));
				}
			}
			pos += m.end(); // length;
		}
		BuildData data = new BuildData(buffer, nodeSet);
		data.setTopID(0);
		data.setMaxBufferLength(10);
		data.setMinRepeatType(repeat.id);
		return Tree.build(data);
	}

	private static Tree _simple = null;

	public static Tree simple() {
		if (_simple == null) {
			_simple = mk("aaaa(bbb[ccc][aaa][()])");
		}
		return _simple;
	}

	private static Tree _recur = null;

	public static Tree recur() {
		if (_recur == null) {
			_recur = mk(build(6));
		}
		return _recur;
	}

	private static String build(int depth) {
		if (depth > 0) {
			String inner = build(depth - 1);
			return "(" + inner + ")[" + inner + "]";
		} else {
			String result = "";
			for (int i = 0; i < 20; i++)
				result += "abc".charAt(i % 3);
			return result;
		}
	}
}