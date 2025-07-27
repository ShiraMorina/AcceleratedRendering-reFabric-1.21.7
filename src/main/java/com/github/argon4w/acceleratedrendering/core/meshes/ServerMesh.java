package com.github.argon4w.acceleratedrendering.core.meshes;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.IMeshCollector;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import org.lwjgl.system.MemoryUtil;

import java.util.Map;

@AllArgsConstructor
public class ServerMesh implements IMesh {

	private final int size;
	private final int offset;

	@Override
	public void write(
			IAcceleratedVertexConsumer	extension,
			int							color,
			int							light,
			int							overlay
	) {
		extension.addServerMesh(
				offset,
				size,
				color,
				light,
				overlay
		);
	}

	public static class Builder implements IMesh.Builder {

		public static	final Builder													INSTANCE = new Builder();

		public			final Map<IMemoryLayout<VertexFormatElement>, IServerBuffer>	serverBuffers;

		private Builder() {
			this.serverBuffers = new Object2ObjectOpenHashMap<>();
		}

		@Override
		public IMesh build(IMeshCollector collector) {
			var vertexCount = collector.getVertexCount();

			if (vertexCount == 0) {
				return EmptyMesh.INSTANCE;
			}

			var builder	= collector	.getBuffer	();
			var result	= builder	.build		();

			if (result == null) {
				builder.close();
				return EmptyMesh.INSTANCE;
			}

			var clientBuffer = result						.byteBuffer();
			var serverBuffer = (MappedBuffer) serverBuffers	.get(collector.getLayout());

			if (serverBuffer == null) {
				serverBuffer = new MappedBuffer	(1024L, true);
				serverBuffers.put				(collector.getLayout(), serverBuffer);
			}

			var capacity = clientBuffer.capacity	();
			var position = serverBuffer.getPosition();

			MemoryUtil.memCopy(
					MemoryUtil.memAddress0	(clientBuffer),
					serverBuffer.reserve	(capacity),
					capacity
			);

			builder.close();
			return new ServerMesh(vertexCount, (int) position);
		}

		@Override
		public void close() {
			for (var buffer : serverBuffers.values()) {
				((MappedBuffer) buffer).delete();
			}
		}
	}
}
