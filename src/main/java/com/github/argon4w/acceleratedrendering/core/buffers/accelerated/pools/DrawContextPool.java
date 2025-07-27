package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.SegmentBuffer;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import com.mojang.blaze3d.vertex.VertexFormat;

import static org.lwjgl.opengl.GL46.*;

public class DrawContextPool extends SimpleResetPool<DrawContextPool.IndirectDrawContext, SegmentBuffer> {

	public DrawContextPool(int size) {
		super(size, new SegmentBuffer(20L * size, size));
	}

	public void bindCommandBuffer() {
		getContext().bind(GL_DRAW_INDIRECT_BUFFER);
	}

	@Override
	protected IndirectDrawContext create(SegmentBuffer buffer, int i) {
		return new IndirectDrawContext(buffer.getSegment(20L));
	}

	@Override
	protected void reset(IndirectDrawContext drawContext) {

	}

	@Override
	protected void delete(IndirectDrawContext drawContext) {

	}

	@Override
	public void delete() {
		super.getContext().delete();
	}

	public static void waitBarriers() {
		glMemoryBarrier(GL_ELEMENT_ARRAY_BARRIER_BIT | GL_COMMAND_BARRIER_BIT);
	}

	public static class IndirectDrawContext {

		private final	long			commandOffset;
		private final	IServerBuffer	commandBuffer;

		private			int				cachedOffset;

		public IndirectDrawContext(IServerBuffer commandBuffer) {
			this.commandOffset	= commandBuffer.getOffset();
			this.commandBuffer	= commandBuffer;
			this.commandBuffer.subData(0, new int[] {0, 1, 0, 0, 0});

			this.cachedOffset	= -1;
		}

		public void bindComputeBuffers(ElementBufferPool.ElementSegment elementSegmentIn) {
			var elementBufferOut	= elementSegmentIn.getBuffer();
			var elementOffset		= elementBufferOut.getOffset();

			if (cachedOffset != elementOffset) {
				cachedOffset = elementOffset;
				commandBuffer.clearInteger(8, elementOffset / 4);
			}

			commandBuffer	.clearInteger	(0,							0);
			commandBuffer	.bindBase		(GL_ATOMIC_COUNTER_BUFFER,	0);
			elementBufferOut.bindBase		(GL_SHADER_STORAGE_BUFFER,	6);
		}

		public void drawElements(VertexFormat.Mode mode) {
			glDrawElementsIndirect(
					mode.asGLMode,
					GL_UNSIGNED_INT,
					commandOffset
			);
		}
	}
}
