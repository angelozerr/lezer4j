package lezer.tree;

public class StringInput implements Input {

	private final String string;
	private final int length;

	public StringInput(String string) {
		this(string, string.length());
	}

	public StringInput(String string, int length) {
		this.string = string;
		this.length = length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public int get(int pos) {
		return pos < 0 || pos >= this.length ? -1 : this.string.codePointAt(pos);
	}

	@Override
	public String lineAfter(int pos) {
		if (pos < 0)
			return "";
		int end = this.string.indexOf("\n", pos);
		return this.string.substring(pos, end < 0 ? this.length : Math.min(end, this.length));
	}

	@Override
	public String read(int from, int to) {
		return this.string.substring(from, Math.min(this.length, to));
	}

	@Override
	public Input clip(int at) {
		return new StringInput(this.string, at);
	}
}
