package org.lucee.extension.search.lucene.query;


public final class Or implements Op {
	private Object left;
	private Object right;

	public Or(Object left, Object right) {
		this.left=left;
		this.right=right;
	}

	@Override
	public String toString() {
		return left+" OR "+right;
	}
}
