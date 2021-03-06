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
package org.apache.juneau.http.response;

import static org.apache.juneau.http.response.Gone.*;

import java.text.*;

import org.apache.http.*;
import org.apache.juneau.http.*;
import org.apache.juneau.http.annotation.*;

/**
 * Exception representing an HTTP 410 ().
 *
 * <p>
 * Indicates that the resource requested is no longer available and will not be available again.
 * <br>This should be used when a resource has been intentionally removed and the resource should be purged.
 * <br>Upon receiving a 410 status code, the client should not request the resource in the future.
 * <br>Clients such as search engines should remove the resource from their indices.
 * <br>Most use cases do not require clients and search engines to purge the resource, and a <js>"404 Not Found"</js> may be used instead.
 */
@Response(code=STATUS_CODE, description=REASON_PHRASE)
public class Gone extends BasicHttpException {
	private static final long serialVersionUID = 1L;

	/** HTTP status code */
	public static final int STATUS_CODE = 410;

	/** Reason phrase */
	public static final String REASON_PHRASE = "Gone";

	/** Default status line */
	private static final BasicStatusLine STATUS_LINE = BasicStatusLine.create().statusCode(STATUS_CODE).reasonPhrase(REASON_PHRASE).build();

	/** Reusable unmodifiable instance */
	public static final Gone INSTANCE = create().unmodifiable().build();

	/**
	 * Creates a builder for this class.
	 *
	 * @return A new builder bean.
	 */
	public static HttpExceptionBuilder<Gone> create() {
		return new HttpExceptionBuilder<>(Gone.class).statusLine(STATUS_LINE);
	}

	/**
	 * Constructor.
	 *
	 * @param builder The builder containing the settings for this exception.
	 */
	public Gone(HttpExceptionBuilder<?> builder) {
		super(builder);
	}

	/**
	 * Constructor.
	 *
	 * @param cause The caused-by exception.  Can be <jk>null</jk>.
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args The message arguments.
	 */
	public Gone(Throwable cause, String msg, Object...args) {
		this(create().causedBy(cause).message(msg, args));
	}

	/**
	 * Constructor.
	 */
	public Gone() {
		this(create());
	}

	/**
	 * Constructor.
	 *
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args Optional {@link MessageFormat}-style arguments in the message.
	 */
	public Gone(String msg, Object...args) {
		this(create().message(msg, args));
	}

	/**
	 * Constructor.
	 *
	 * @param cause The cause.  Can be <jk>null</jk>.
	 */
	public Gone(Throwable cause) {
		this(create().causedBy(cause));
	}

	/**
	 * Constructor.
	 *
	 * <p>
	 * This is the constructor used when parsing an HTTP response.
	 *
	 * @param response The HTTP response to copy from.  Must not be <jk>null</jk>.
	 * @throws AssertionError If HTTP response status code does not match what was expected.
	 */
	public Gone(HttpResponse response) {
		this(create().copyFrom(response));
		assertStatusCode(response);
	}

	/**
	 * Creates a builder for this class initialized with the contents of this bean.
	 *
	 * @return A new builder bean.
	 */
	@Override /* BasicHttpException */
	public HttpExceptionBuilder<Gone> copy() {
		return new HttpExceptionBuilder<>(this);
	}
}