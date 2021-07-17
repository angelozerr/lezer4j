package lezer.tree.typedarray;

import java.nio.ShortBuffer;

public class Uint16Array extends TypedArray {

	public static final int BYTES_PER_ELEMENT = 2;

	private ShortBuffer shortBuffer;

	public Uint16Array(int length) {
		super(length, BYTES_PER_ELEMENT);
		shortBuffer = getBuffer().asShortBuffer();
	}

	public int get(int index) {
		return shortBuffer.get(index);
	}

	public void set(int index, int value) {
		shortBuffer.put(index, (short) value);
	}
}
