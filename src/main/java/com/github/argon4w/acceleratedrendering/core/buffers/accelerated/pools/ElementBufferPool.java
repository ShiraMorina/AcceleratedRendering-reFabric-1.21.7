package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.SegmentBuffer;
import com.github.argon4w.acceleratedrendering.core.utils.MutableSize;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableLong;

public class ElementBufferPool extends SimpleResetPool<ElementBufferPool.ElementSegment, Void> {

	@Getter private	final SegmentBuffer	elementBufferOut;
	private			final MutableLong	elementBufferOutSize;
	private			final MutableLong	elementBufferOutUsedSize;

	public ElementBufferPool(int size) {
		super(size, null);

		this.elementBufferOut			= new SegmentBuffer	(64L * size, size);
		this.elementBufferOutSize		= new MutableLong	(64L * size);
		this.elementBufferOutUsedSize	= new MutableLong	(64L * size);
	}

	public void prepare() {
		elementBufferOut.resizeTo		(elementBufferOutSize.getValue());
		elementBufferOut.clearSegment	();
	}

	@Override
	public void delete() {
		elementBufferOut.delete();
		super			.delete();
	}

	@Override
	public void reset() {
		elementBufferOutUsedSize.setValue	(0L);
		super					.reset		();
	}

	@Override
	protected ElementSegment create(Void value, int i) {
		return new ElementSegment();
	}

	@Override
	protected void reset(ElementSegment elementSegment) {
		elementSegment.reset();
	}

	@Override
	protected void delete(ElementSegment elementSegment) {

	}

	@Override
	public boolean test(ElementSegment elementSegment) {
		return elementBufferOutUsedSize.addAndGet(elementSegment.getSize()) <= (2L * 1024L * 1024L * 1024L);
	}

	@Getter
	public class ElementSegment extends MutableSize {

		private long elementBytes;

		public ElementSegment() {
			super(64L);
			this.elementBytes = 0L;
		}

		@Override
		public void onExpand(long bytes) {
			elementBufferOutSize	.add(bytes);
			elementBufferOutUsedSize.add(bytes);
		}

		public IServerBuffer getBuffer() {
			return elementBufferOut.getSegment(size);
		}

		private void reset() {
			elementBytes = 0L;
		}

		public void countPolygons(int count) {
			elementBytes += count * 4L;

			if (elementBytes > size) {
				resize(elementBytes);
			}
		}
	}
}
