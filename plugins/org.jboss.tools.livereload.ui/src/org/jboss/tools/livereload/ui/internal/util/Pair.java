package org.jboss.tools.livereload.ui.internal.util;
public class Pair<A, B> {

	public static <P, Q> Pair<P, Q> makePair(P p, Q q) {
		return new Pair<P, Q>(p, q);
	}

	public final A left;
	public final B right;

	public Pair(A a, B b) {
		this.left = a;
		this.right = b;
	}

	


}