// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.http.header;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.apache.juneau.http.annotation.*;

/**
 * Represents a parsed <l>Last-Modified</l> HTTP response header.
 *
 * <p>
 * The last modified date for the requested object (in "HTTP-date" format as defined by RFC 7231).
 *
 * <h5 class='figure'>Example</h5>
 * <p class='bcode w800'>
 * 	Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
 * </p>
 *
 * <h5 class='topic'>RFC2616 Specification</h5>
 *
 * The Last-Modified entity-header field indicates the date and time at which the origin server believes the variant was
 * last modified.
 *
 * <p class='bcode w800'>
 * 	Last-Modified  = "Last-Modified" ":" HTTP-date
 * </p>
 *
 * <p>
 * An example of its use is...
 * <p class='bcode w800'>
 * 	Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
 * </p>
 *
 * <p>
 * The exact meaning of this header field depends on the implementation of the origin server and the nature of the
 * original resource.
 * For files, it may be just the file system last-modified time.
 * For entities with dynamically included parts, it may be the most recent of the set of last-modify times for its
 * component parts.
 * For database gateways, it may be the last-update time stamp of the record.
 * For virtual objects, it may be the last time the internal state changed.
 *
 * <p>
 * An origin server MUST NOT send a Last-Modified date which is later than the server's time of message origination.
 * In such cases, where the resource's last modification would indicate some time in the future, the server MUST replace
 * that date with the message origination date.
 *
 * <p>
 * An origin server SHOULD obtain the Last-Modified value of the entity as close as possible to the time that it
 * generates the Date value of its response.
 * This allows a recipient to make an accurate assessment of the entity's modification time, especially if the entity
 * changes near the time that the response is generated.
 *
 * <p>
 * HTTP/1.1 servers SHOULD send Last-Modified whenever feasible.
 *
 * <ul class='seealso'>
 * 	<li class='extlink'>{@doc RFC2616}
 * </ul>
 */
@Header("Last-Modified")
public class LastModified extends BasicDateHeader {

	private static final long serialVersionUID = 1L;

	/**
	 * Convenience creator.
	 *
	 * @param value
	 * 	The parameter value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li><c>String</c> - An RFC-1123 formated string (e.g. <js>"Sat, 29 Oct 1994 19:43:31 GMT"</js>).
	 * 		<li>{@link ZonedDateTime}
	 * 		<li>{@link Calendar}
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link LastModified} object, or <jk>null</jk> if the value was null.
	 */
	public static LastModified of(Object value) {
		if (value == null)
			return null;
		return new LastModified(value);
	}

	/**
	 * Convenience creator using supplier.
	 *
	 * <p>
	 * Header value is re-evaluated on each call to {@link #getValue()}.
	 *
	 * @param value
	 * 	The parameter value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li><c>String</c> - An RFC-1123 formated string (e.g. <js>"Sat, 29 Oct 1994 19:43:31 GMT"</js>).
	 * 		<li>{@link ZonedDateTime}
	 * 		<li>{@link Calendar}
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link LastModified} object, or <jk>null</jk> if the value was null.
	 */
	public static LastModified of(Supplier<?> value) {
		if (value == null)
			return null;
		return new LastModified(value);
	}

	/**
	 * Constructor.
	 *
	 * @param value
	 * 	The parameter value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li><c>String</c> - An RFC-1123 formated string (e.g. <js>"Sat, 29 Oct 1994 19:43:31 GMT"</js>).
	 * 		<li>{@link ZonedDateTime}
	 * 		<li>{@link Calendar}
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 		<li>A {@link Supplier} of anything on this list.
	 * 	</ul>
	 */
	public LastModified(Object value) {
		super("Last-Modified", value);
	}

	/**
	 * Constructor.
	 *
	 * @param value
	 * 	The parameter value.
	 */
	public LastModified(String value) {
		super("Last-Modified", value);
	}
}