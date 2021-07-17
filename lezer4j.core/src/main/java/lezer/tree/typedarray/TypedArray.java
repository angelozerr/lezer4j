package lezer.tree.typedarray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TypedArray {

	public final int length;

	private ByteBuffer buffer;

	protected TypedArray(int length, int bytesPerElement) {
		this.length = length;
		createBuffer(length * bytesPerElement);
	}

	protected void createBuffer(int capacity) {
		buffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
	}

	protected ByteBuffer getBuffer() {
		return buffer;
	}

}
