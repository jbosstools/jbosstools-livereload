package org.jboss.tools.livereload.core.internal.util;

/**
 * Convenient utility class to build URL path and make sure {@code /} separators
 * are properly handled.
 */
public class PathBuilder {

	private final StringBuilder path = new StringBuilder();

	/**
	 * Constructor helper
	 * @param pathFragment the initial path fragment to begin with
	 * @return a new instance of the {@link PathBuilder}
	 */
	public static PathBuilder from(final String pathFragment) {
		final PathBuilder pathBuilder = new PathBuilder();
		return pathBuilder.path(pathFragment);
	}

	/**
	 * Adds the given {@code pathFragment} to the current path, while making
	 * sure that a single {@code '/'} character is used to separate the given
	 * {@code pathFragment} from the previous ones.
	 * 
	 * @param pathFragment
	 * @return this {@link PathBuilder}
	 */
	public PathBuilder path(final String pathFragment) {
		if (pathFragment == null) {
			return this;
		}
		if (!pathFragment.startsWith("/")) {
			if (!currentPathEndsWith('/')) {
				this.path.append("/").append(pathFragment);
			} else if (currentPathEndsWith('/')) {
				this.path.append(pathFragment);
			}
		} else {
			if (!currentPathEndsWith('/')) {
				this.path.append(pathFragment);
			} else if (currentPathEndsWith('/') && pathFragment.length() > 1) {
				this.path.append(pathFragment.substring(1));
			} else if (this.path.toString().length() == 0) {
				this.path.append(pathFragment);
			}
		}
		return this;
	}
	
	private boolean currentPathEndsWith(final char lastChar) {
		return this.path.length() > 0 && this.path.charAt(this.path.length() - 1) == lastChar;
	}

	/**
	 * @return a {@link String} representation of the Path 
	 */
	public String build() {
		return this.path.toString();
	}

}