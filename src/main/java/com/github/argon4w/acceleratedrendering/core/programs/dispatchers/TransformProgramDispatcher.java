package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

import static org.lwjgl.opengl.GL46.GL_SHADER_STORAGE_BUFFER;

public class TransformProgramDispatcher {

	private static	final int				GROUP_SIZE = 128;

	private			final ComputeProgram	program;
	private			final Uniform			vertexCountUniform;
	private			final Uniform			vertexOffsetUniform;

	public TransformProgramDispatcher(ResourceLocation key) {
		this.program				= ComputeShaderProgramLoader.getProgram(key);
		this.vertexCountUniform		= program					.getUniform("vertexCount");
		this.vertexOffsetUniform	= program					.getUniform("vertexOffset");
	}

	public void dispatch(Collection<AcceleratedBufferBuilder> builders) {
		program.useProgram();

		for (var builder : builders) {
			var vertexCount		= builder.getVertexCount	();
			var vertexBuffer	= builder.getVertexBuffer	();
			var varyingBuffer	= builder.getVaryingBuffer	();

			vertexBuffer	.flush();
			varyingBuffer	.flush();

			vertexBuffer	.bindBase(GL_SHADER_STORAGE_BUFFER, 0);
			varyingBuffer	.bindBase(GL_SHADER_STORAGE_BUFFER, 3);

			vertexCountUniform	.uploadUnsignedInt	(vertexCount);
			vertexOffsetUniform	.uploadUnsignedInt	(builder.getVertexOffset());

			program				.dispatch			((vertexCount + GROUP_SIZE - 1) / GROUP_SIZE);
		}

		program.resetProgram();
		program.waitBarriers();
	}
}
