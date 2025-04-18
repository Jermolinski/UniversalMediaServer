/*
 * This file is part of Universal Media Server, based on PS3 Media Server.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.util;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 * An {@code enum} for classifying the kind of release.
 *
 * @author Nadahar
 */
public enum VideoClassification {

	/** A video released as a movie/film */
	MOVIE,

	/** A video released as part of a (TV) series */
	SERIES,

	/**
	 * A video released/sent as a TV program that doesn't fit into
	 * {@link #MOVIE} or {@link #SERIES}
	 */
	TV_PROGRAM,

	/** A video that isn't released to the public */
	UNRELEASED;

	@Override
	public String toString() {
		return switch (this) {
			case MOVIE -> "Movie";
			case SERIES -> "Series";
			case TV_PROGRAM -> "TV Program";
			case UNRELEASED -> "Unreleased/Home Made";
			default -> name();
		};
	}

	/**
	 * Tries to parse the {@link VideoClassification} from the specified
	 * {@link String}. This only knows some English terms and shouldn't be
	 * trusted to be very accurate.
	 * <p>
	 * Depending on the use of this method, it might need further refinement.
	 *
	 * @param videoClassification the video kind/classification {@link String}
	 *            to parse.
	 * @return The {@link VideoClassification} type or {@code null}.
	 */
	public static VideoClassification typeOf(String videoClassification) {
		if (StringUtils.isBlank(videoClassification)) {
			return null;
		}
		videoClassification = videoClassification.toLowerCase(Locale.ROOT);
		if (videoClassification.contains("serie") || videoClassification.contains("episode")) {
			return SERIES;
		}
		if (videoClassification.contains("movie") || videoClassification.contains("film")) {
			return MOVIE;
		}
		if (videoClassification.matches(".*\\btv\\b.*")) {
			return TV_PROGRAM;
		}
		if (
			videoClassification.contains("unreleased") ||
			videoClassification.contains("private") ||
			videoClassification.matches(".*home(?:\\s*|-)(?:made|video)")
		) {
			return UNRELEASED;
		}
		return null;
	}
}
