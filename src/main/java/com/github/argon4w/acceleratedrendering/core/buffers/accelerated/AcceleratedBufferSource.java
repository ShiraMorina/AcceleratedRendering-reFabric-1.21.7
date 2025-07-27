package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.DrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;
import java.util.SequencedMap;

import static org.lwjgl.opengl.GL46.*;

public class AcceleratedBufferSource extends MultiBufferSource.BufferSource implements IAcceleratedBufferSource {

	private final	IBufferEnvironment																						bufferEnvironment;
	private final	AcceleratedBufferSetPool																				acceleratedBufferSetPool;
	private final	SequencedMap<AcceleratedBufferSetPool.BufferSet, Map<RenderType, AcceleratedBufferBuilder>>				acceleratedBuilders;
	private final	SequencedMap<AcceleratedBufferSetPool.BufferSet, Map<RenderType, DrawContextPool.IndirectDrawContext>>	acceleratedDrawContexts;

	private			AcceleratedBufferSetPool.BufferSet																		currentBufferSet;
	private 		boolean																									used;

	public AcceleratedBufferSource(IBufferEnvironment bufferEnvironment) {
		super(null, null);

		this.bufferEnvironment			= bufferEnvironment;
		this.acceleratedBuilders		= new Object2ObjectLinkedOpenHashMap<>	();
		this.acceleratedBufferSetPool	= new AcceleratedBufferSetPool			(this.bufferEnvironment);
		this.acceleratedDrawContexts	= new Object2ObjectLinkedOpenHashMap<>	();

		this.currentBufferSet			= acceleratedBufferSetPool.getBufferSet	();
		this.used						= false;

		this.acceleratedBuilders	.put										(this.currentBufferSet, new Object2ObjectLinkedOpenHashMap<>());
		this.acceleratedDrawContexts.put										(this.currentBufferSet, new Object2ObjectLinkedOpenHashMap<>());
	}

	@Override
	public void endLastBatch() {

	}

	@Override
	public void endBatch() {

	}

	@Override
	public void endBatch(RenderType pRenderType) {

	}

	@Override
	public IBufferEnvironment getBufferEnvironment() {
		return bufferEnvironment;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		var builders		= acceleratedBuilders	.get		(currentBufferSet);
		var builder			= builders				.get		(renderType);

		if (builder != null) {
			return builder;
		}

		var vertexBuffer	= currentBufferSet	.getVertexBuffer	();
		var varyingBuffer	= currentBufferSet	.getVaryingBuffer	();
		var elementSegment	= currentBufferSet	.getElementSegment	();

		if (vertexBuffer == null) {
			currentBufferSet	= acceleratedBufferSetPool	.getBufferSet		();
			builders			= new Object2ObjectLinkedOpenHashMap<>			();

			vertexBuffer	= currentBufferSet				.getVertexBuffer	();
			varyingBuffer	= currentBufferSet				.getVaryingBuffer	();
			elementSegment	= currentBufferSet				.getElementSegment	();

			acceleratedBuilders								.put				(currentBufferSet, builders);
			acceleratedDrawContexts							.put				(currentBufferSet, new Object2ObjectLinkedOpenHashMap<>());
		}

		builder = new AcceleratedBufferBuilder(
				vertexBuffer,
				varyingBuffer,
				elementSegment,
				currentBufferSet,
				renderType
		);

		used = true;
		builders.put(renderType, builder);

		return builder;
	}

	@Override
	public void drawBuffers() {
		if (!used) {
			return;
		}

		for (var bufferSet : acceleratedBuilders.keySet()) {
			var program			= glGetInteger					(GL_CURRENT_PROGRAM);
			var barrier			= 0;
			var builders		= acceleratedBuilders		.get(bufferSet);
			var drawContexts	= acceleratedDrawContexts	.get(bufferSet);

			bufferSet											.prepare				();
			bufferSet											.bindTransformBuffers	();
			bufferEnvironment.selectTransformProgramDispatcher().dispatch				(builders.values());

			for (var renderType : builders.keySet()) {
				var builder			= builders	.get				(renderType);
				var elementSegment	= builder	.getElementSegment	();

				if (builder.isEmpty()) {
					continue;
				}

				var mode		= renderType.mode;
				var drawContext	= bufferSet	.getDrawContext();

				drawContext	.bindComputeBuffers	(elementSegment);
				drawContexts.put				(renderType, drawContext);

				barrier |= bufferEnvironment.selectProcessingProgramDispatcher	(mode)		.dispatch(builder);
				barrier |= bufferEnvironment.selectCullProgramDispatcher		(renderType).dispatch(builder);
			}

			glMemoryBarrier				(barrier);
			glUseProgram				(program);
			BufferUploader.invalidate	();

			bufferSet.bindVertexArray();
			bufferSet.bindDrawBuffers();

			for (var renderType : drawContexts.keySet()) {
				renderType.setupRenderState();

				var drawContext	= drawContexts	.get		(renderType);
				var shader		= RenderSystem	.getShader	();
				var mode		= renderType	.mode;

				shader.setDefaultUniforms(
						mode,
						RenderSystem			.getModelViewMatrix	(),
						RenderSystem			.getProjectionMatrix(),
						Minecraft.getInstance()	.getWindow			()
				);

				shader		.apply				();
				drawContext	.drawElements		(mode);
				shader		.clear				();
				renderType	.clearRenderState	();
			}

			DrawContextPool	.waitBarriers		();
			bufferSet		.resetVertexArray	();
		}
	}

	@Override
	public void clearBuffers() {
		if (!used) {
			return;
		}

		for (var bufferSet : acceleratedBuilders.keySet()) {
			bufferSet.reset			();
			bufferSet.setInFlight	();
		}

		acceleratedBuilders		.clear	();
		acceleratedDrawContexts	.clear	();

		currentBufferSet	= acceleratedBufferSetPool.getBufferSet();
		used				= false;

		acceleratedBuilders		.put	(currentBufferSet, new Object2ObjectLinkedOpenHashMap<>());
		acceleratedDrawContexts	.put	(currentBufferSet, new Object2ObjectLinkedOpenHashMap<>());
	}
}
