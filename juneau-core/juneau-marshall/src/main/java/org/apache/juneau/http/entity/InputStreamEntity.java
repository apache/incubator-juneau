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
package org.apache.juneau.http.entity;

import static org.apache.juneau.assertions.Assertions.*;
import static org.apache.juneau.internal.IOUtils.*;

import java.io.*;
import java.util.concurrent.atomic.*;

import org.apache.juneau.http.header.*;

/**
 * A streamed, non-repeatable entity that obtains its content from an {@link InputStream}.
 */
public class InputStreamEntity extends AbstractHttpEntity {

	private final InputStream content;
	private final long length;
	private final AtomicReference<byte[]> cache = new AtomicReference<>();

	/**
	 * Creates a new {@link InputStreamEntity} object.
	 *
	 * <p>
	 * Assumes no content type.
	 *
	 * @param content The entity content.  Can be <jk>null<jk>.
	 * @return A new {@link InputStreamEntity} object.
	 */
	public static InputStreamEntity of(InputStream content) {
		return new InputStreamEntity(content, -1, null);
	}

	/**
	 * Creates a new {@link InputStreamEntity} object.
	 *
	 * @param content The entity content.  Can be <jk>null<jk>.
	 * @param contentType The entity content type, or <jk>null</jk> if not specified.
	 * @param length The content length, or <c>-1</c> if not known.
	 * @return A new {@link InputStreamEntity} object.
	 */
	public static InputStreamEntity of(InputStream content, long length, ContentType contentType) {
		return new InputStreamEntity(content, length, contentType);
	}

	/**
	 * Constructor.
	 *
	 * @param content The entity content.  Can be <jk>null</jk>.
	 * @param contentType The entity content type, or <jk>null</jk> if not specified.
	 * @param length The content length, or <c>-1</c> if not known.
	 */
	public InputStreamEntity(InputStream content, long length, ContentType contentType) {
		this.content = content == null ? EMPTY_INPUT_STREAM : content;
		this.length = length;
		setContentType(contentType);
	}

	@Override /* AbstractHttpEntity */
	public String asString() throws IOException {
		return new String(asBytes(), UTF8);
	}

	@Override /* AbstractHttpEntity */
	public byte[] asBytes() throws IOException {
		cache();
		return cache.get();
	}

	@Override /* HttpEntity */
	public boolean isRepeatable() {
		return cache.get() != null;
	}

	@Override /* HttpEntity */
	public long getContentLength() {
		return length;
	}

	@Override /* HttpEntity */
	public InputStream getContent() throws IOException {
		byte[] b = cache.get();
		return b == null ? content : new ByteArrayInputStream(b);
	}

	@Override /* AbstractHttpEntity */
	public InputStreamEntity cache() throws IOException {
		byte[] b = cache.get();
		if (b == null) {
			try (InputStream is = getContent()) {
				b = readBytes(is, (int)length);
			}
			cache.set(b);
		}
		return this;
	}

	/**
	 * Writes bytes from the {@code InputStream} this entity was constructed
	 * with to an {@code OutputStream}.  The content length
	 * determines how many bytes are written.  If the length is unknown ({@code -1}), the
	 * stream will be completely consumed (to the end of the stream).
	 */
	@Override
	public void writeTo(OutputStream out) throws IOException {
		assertArgNotNull("out", out);

		byte[] b = cache.get();
		if (b != null) {
			pipe(b, out, (int)length);
		} else {
			try (InputStream is = getContent()) {
				pipe(is, out, length);
			}
		}
	}

	@Override /* HttpEntity */
	public boolean isStreaming() {
		return cache.get() == null;
	}
}
