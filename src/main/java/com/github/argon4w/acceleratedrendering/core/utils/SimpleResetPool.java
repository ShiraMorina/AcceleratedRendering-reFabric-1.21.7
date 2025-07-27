package com.github.argon4w.acceleratedrendering.core.utils;

import lombok.Getter;

public abstract class SimpleResetPool<T, C> {

	private			final int		size;
	private			final Object[]	pool;
	@Getter private	final C			context;

	private int cursor;

	public SimpleResetPool(int size, C context) {
		this.size		= size;
		this.pool		= new Object[size];
		this.context	= context;

		this.cursor		= 0;

		for (var i = 0; i < this.size; i++) {
			this.pool[i] = create(this.context, i);
		}
	}

	protected abstract T	create	(C context, int i);
	protected abstract void	reset	(T t);
	protected abstract void	delete	(T t);

	@SuppressWarnings("unchecked")
	public T get() {
		if (cursor < size) {
			var t = (T) pool[cursor ++];

			if (test(t)) {
				return t;
			}
		}

		return fail();
	}

	@SuppressWarnings("unchecked")
	public void reset() {
		for (int i = 0; i < cursor; i++) {
			reset((T) pool[i]);
		}

		cursor = 0;
	}

	@SuppressWarnings("unchecked")
	public void delete() {
		for (int i = 0; i < size; i++) {
			delete((T) pool[i]);
		}
	}

	public T fail() {
		return null;
	}

	public boolean test(T t) {
		return true;
	}
}