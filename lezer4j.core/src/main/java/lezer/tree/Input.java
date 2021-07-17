package lezer.tree;

/// This is the interface the parser uses to access the document. It
/// exposes a sequence of UTF16 code units. Most (but not _all_)
/// access, especially through `get`, will be sequential, so
/// implementations can optimize for that.
public interface Input {
/// The end of the stream.
	int length();

/// Get the code unit at the given position. Will return -1 when
/// asked for a point below 0 or beyond the end of the stream.
	int get(int pos);

/// Returns the string between `pos` and the next newline character
/// or the end of the document. Not used by the built-in tokenizers,
/// but can be useful in custom tokenizers or completely custom
/// parsers.
	String lineAfter(int pos);

/// Read part of the stream as a string
	String read(int from, int to);

/// Return a new `Input` over the same data, but with a lower
/// `length`. Used, for example, when nesting grammars to give the
/// inner grammar a narrower view of the input.
	Input clip(int at);
}
