package lezer.tree;

import java.util.List;

class FlatBufferCursor implements BufferCursor {

	private final List<Integer> buffer;
	private int index;

	FlatBufferCursor(List<Integer> buffer, int index) {
		this.buffer = buffer;
		this.index = index;
	}

	@Override
	public int id() {
		return this.buffer.get(this.index - 4);
	}

	@Override
	public int start() {
		return this.buffer.get(this.index - 3);
	}

	@Override
	public int end() {
		return this.buffer.get(this.index - 2);
	}

	@Override
	public int size() {
		return this.buffer.get(this.index - 1);
	}

	@Override
	public int pos() {
		return this.index;
	}

	@Override
	public void next() {
		this.index -= 4;
	}

	@Override
	public BufferCursor fork() {
		return new FlatBufferCursor(this.buffer, this.index);
	}
}
