package lezer.token;

public class Token {

	/// The start of the token. This is set by the parser, and should not
	/// be mutated by the tokenizer.
	public int start = -1;
	/// This starts at -1, and should be updated to a term id when a
	/// matching token is found.
	public int value = -1;
	/// When setting `.value`, you should also set `.end` to the end
	/// position of the token. (You'll usually want to use the `accept`
	/// method.)
	public int end = -1;

	/// Accept a token, setting `value` and `end` to the given values.
	public void accept(int value, int end) {
		this.value = value;
		this.end = end;
	}
}
