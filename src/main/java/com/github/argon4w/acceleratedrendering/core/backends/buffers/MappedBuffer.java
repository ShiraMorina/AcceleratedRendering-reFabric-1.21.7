package com.github.argon4w.acceleratedrendering.core.backends.buffers;

import lombok.Getter;

import static org.lwjgl.opengl.GL46.*;

public class MappedBuffer extends MutableBuffer implements IClientBuffer {

	public static 	final 	int 	AUTO_FLUSH_BITS		= GL_DYNAMIC_STORAGE_BIT	| GL_MAP_PERSISTENT_BIT	| GL_MAP_WRITE_BIT	| GL_MAP_COHERENT_BIT;
	public static 	final 	int 	VERB_FLUSH_BITS		= GL_DYNAMIC_STORAGE_BIT	| GL_MAP_PERSISTENT_BIT	| GL_MAP_WRITE_BIT;

	public static 	final 	int 	AUTO_FLUSH_MAP_BITS	= GL_MAP_WRITE_BIT			| GL_MAP_PERSISTENT_BIT	| GL_MAP_COHERENT_BIT;
	public static 	final 	int 	VERB_FLUSH_MAP_BITS	= GL_MAP_WRITE_BIT			| GL_MAP_PERSISTENT_BIT	| GL_MAP_FLUSH_EXPLICIT_BIT;

	private 		final 	int 	mapBits;

			protected 		long 	address;
	@Getter protected 		long	position;

	public int meshCount;

	public MappedBuffer(long initialSize, boolean autoFlush) {
		super(initialSize, autoFlush ? AUTO_FLUSH_BITS : VERB_FLUSH_BITS);

		this.mapBits	= autoFlush ? AUTO_FLUSH_MAP_BITS : VERB_FLUSH_MAP_BITS;
		this.address	= map();
		this.position	= 0L;
	}

	public MappedBuffer(long initialSize) {
		this(initialSize, false);
	}

	@Override
	public long reserve(long bytes) {
		var position	=	this.position;
		this.position	+=	bytes;

		if (this.position <= size) {
			return address + position;
		}

		resize(this.position);
		return address + position;
	}

	@Override
	public void beforeExpand() {
		unmap();
	}

	@Override
	public void afterExpand() {
		address = map();
	}

	@Override
	public void bind(int target) {
		throw new IllegalStateException("Buffer is mapped.");
	}

	public void flush() {
		glBuffer.flush(position);
	}

	public long map() {
		return map(mapBits);
	}

	public void reset() {
		position = 0;
		meshCount = 0;
	}
}
