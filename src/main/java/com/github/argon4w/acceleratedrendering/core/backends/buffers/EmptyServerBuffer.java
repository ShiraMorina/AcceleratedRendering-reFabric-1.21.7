package com.github.argon4w.acceleratedrendering.core.backends.buffers;

public class EmptyServerBuffer implements IServerBuffer {

	public static final EmptyServerBuffer INSTANCE = new EmptyServerBuffer();

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public int getBufferHandle() {
		return 0;
	}

	@Override
	public void bind(int target) {

	}

	@Override
	public void clearInteger(long offset, int value) {

	}

	@Override
	public void clearBytes(long offset, long size) {

	}

	@Override
	public void subData(long offset, int[] data) {

	}

	@Override
	public void bindBase(int target, int index) {

	}

	@Override
	public void bindRange(
			int		target,
			int		index,
			long	offset,
			long	size
	) {

	}
}
