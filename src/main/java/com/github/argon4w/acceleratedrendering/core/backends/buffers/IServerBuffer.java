package com.github.argon4w.acceleratedrendering.core.backends.buffers;

public interface IServerBuffer {

	int getOffset       ();
	int getBufferHandle ();
	void bind           (int	target);
	void clearInteger   (long	offset,	int		value);
	void clearBytes     (long	offset,	long	size);
	void subData        (long	offset,	int[]	data);
	void bindBase       (int	target,	int		index);
	void bindRange      (int	target,	int		index,	long	offset,	long	size);
}
