package com.github.argon4w.acceleratedrendering.core.backends.buffers;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class ImmutableBuffer implements IServerBuffer {

	protected final int bufferHandle;

	public ImmutableBuffer(long size, int bits) {
		this.bufferHandle = glCreateBuffers();

		glNamedBufferStorage(
				this.bufferHandle,
				size,
				bits
		);
	}

	public void copyTo(IServerBuffer buffer, long size) {
		glCopyNamedBufferSubData(
				bufferHandle,
				buffer.getBufferHandle(),
				0,
				0,
				size
		);
	}

	public long map(long length, int bits) {
		return nglMapNamedBufferRange(
				bufferHandle,
				0L,
				length,
				bits
		);
	}

	public void flush(long length) {
		glFlushMappedNamedBufferRange(
				bufferHandle,
				0,
				length
		);
	}

	public void unmap() {
		glUnmapNamedBuffer(bufferHandle);
	}

	public void delete() {
		glDeleteBuffers(bufferHandle);
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public int getBufferHandle() {
		return bufferHandle;
	}

	@Override
	public void bind(int target) {
		glBindBuffer(target, bufferHandle);
	}

	@Override
	public void clearInteger(long offset, int value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glClearNamedBufferSubData(
					bufferHandle,
					GL_R32UI,
					offset,
					Integer.BYTES,
					GL_RED_INTEGER,
					GL_UNSIGNED_INT,
					stack
							.malloc(4)
							.putInt(0, value)
			);
		}
	}
	
	@Override
	public void clearBytes(long offset, long size) {
		glClearNamedBufferSubData(
				bufferHandle,
				GL_R8UI,
				offset,
				size,
				GL_RED_INTEGER,
				GL_UNSIGNED_BYTE,
				(ByteBuffer) null
		);
	}

	@Override
	public void subData(long offset, int[] data) {
		glNamedBufferSubData(
				bufferHandle,
				offset,
				data
		);
	}

	@Override
	public void bindBase(int target, int index) {
		glBindBufferBase(
				target,
				index,
				bufferHandle
		);
	}

	@Override
	public void bindRange(
			int		target,
			int		index,
			long	offset,
			long	size
	) {
		glBindBufferRange(
				target,
				index,
				bufferHandle,
				offset,
				size
		);
	}
}
