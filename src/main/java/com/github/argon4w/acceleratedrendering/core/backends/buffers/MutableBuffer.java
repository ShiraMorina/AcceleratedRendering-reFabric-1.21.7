package com.github.argon4w.acceleratedrendering.core.backends.buffers;

import com.github.argon4w.acceleratedrendering.core.utils.MutableSize;

public class MutableBuffer extends MutableSize implements IServerBuffer {

	private final	int				bits;

	protected		ImmutableBuffer glBuffer;

	public MutableBuffer(long initialSize, int bits) {
		super(initialSize);

		this.bits		= bits;
		this.glBuffer	= new ImmutableBuffer	(this.size,	bits);
		this.glBuffer.clearBytes				(0L,		this.size);
	}

	@Override
	public void doExpand(long size, long bytes) {
		var newSize		= size + bytes;
		var newBuffer	= new ImmutableBuffer(newSize, bits);

		newBuffer.clearBytes(0L, newSize);

		glBuffer.copyTo(newBuffer, size);
		glBuffer.delete();
		glBuffer = newBuffer;
	}

	public long map(int flags) {
		return glBuffer.map(size, flags);
	}

	public void unmap() {
		glBuffer.unmap();
	}

	public void delete() {
		glBuffer.delete();
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public int getBufferHandle() {
		return glBuffer.getBufferHandle();
	}

	@Override
	public void bind(int target) {
		glBuffer.bind(target);
	}

	@Override
	public void clearInteger(long offset, int value) {
		glBuffer.clearInteger(offset, value);
	}

	@Override
	public void clearBytes(long offset, long size) {
		glBuffer.clearBytes(offset, size);
	}

	@Override
	public void subData(long offset, int[] data) {
		glBuffer.subData(offset, data);
	}

	@Override
	public void bindBase(int target, int index) {
		glBuffer.bindBase(target, index);
	}

	@Override
	public void bindRange(
			int		target,
			int		index,
			long	offset,
			long	size
	) {
		glBuffer.bindRange(
				target,
				index,
				offset,
				size
		);
	}
}
