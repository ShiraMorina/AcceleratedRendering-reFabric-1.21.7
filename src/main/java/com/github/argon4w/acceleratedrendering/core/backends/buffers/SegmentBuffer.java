package com.github.argon4w.acceleratedrendering.core.backends.buffers;

import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import org.apache.commons.lang3.mutable.MutableLong;

import static org.lwjgl.opengl.GL46.GL_DYNAMIC_STORAGE_BIT;

public class SegmentBuffer extends MutableBuffer {

	private final MutableLong segmentOffset;
	private final SegmentPool segmentPool;

	public SegmentBuffer(long initialSize, int segments) {
		super(initialSize, GL_DYNAMIC_STORAGE_BIT);

		this.segmentOffset	= new MutableLong(0);
		this.segmentPool	= new SegmentPool(segments);
	}

	@Override
	public void delete() {
		super		.delete();
		segmentPool	.delete();
	}

	public void clearSegment() {
		segmentOffset	.setValue(0);
		segmentPool		.reset();
	}

	public Segment getSegment(long size) {
		return segmentPool.get(size);
	}

	public long getSegmentOffset(long size) {
		return segmentOffset.getAndAdd(size);
	}

	public long getSegmentOffset() {
		return segmentOffset.getValue();
	}

	public class SegmentPool extends SimpleResetPool<Segment, Void> {

		public SegmentPool(int size) {
			super(size, null);
		}

		public Segment get(long size) {
			return get().set(segmentOffset.getAndAdd(size), size);
		}

		@Override
		protected Segment create(Void context, int i) {
			return new Segment();
		}

		@Override
		protected void reset(Segment segment) {
			segment.reset();
		}

		@Override
		protected void delete(Segment segment) {
			segment.reset();
		}

		@Override
		public Segment fail() {
			throw new IllegalStateException("Segment pool is empty.");
		}
	}

	public class Segment implements IServerBuffer {

		private long offset;
		private long size;

		private Segment() {
			this.offset	= -1;
			this.size	= -1;
		}

		public void reset() {
			this.offset	= -1;
			this.size	= -1;
		}

		public Segment set(long offset, long size) {
			this.offset	= offset;
			this.size	= size;

			return this;
		}

		@Override
		public int getOffset() {
			return (int) offset;
		}

		@Override
		public int getBufferHandle() {
			return SegmentBuffer.this.getBufferHandle();
		}

		@Override
		public void bind(int target) {
			SegmentBuffer.this.bind(target);
		}

		@Override
		public void subData(long offset, int[] data) {
			SegmentBuffer.this.subData(offset + this.offset, data);
		}

		@Override
		public void clearInteger(long offset, int value) {
			SegmentBuffer.this.clearInteger(offset + this.offset, value);
		}

		@Override
		public void clearBytes(long offset, long size) {
			SegmentBuffer.this.clearBytes(offset + this.offset, size);
		}

		@Override
		public void bindBase(int target, int index) {
			SegmentBuffer.this.bindRange(
					target,
					index,
					offset,
					size
			);
		}

		@Override
		public void bindRange(
				int		target,
				int		index,
				long	offset,
				long	size
		) {
			SegmentBuffer.this.bindRange(
					target,
					index,
					offset + this.offset,
					size
			);
		}
	}
}
