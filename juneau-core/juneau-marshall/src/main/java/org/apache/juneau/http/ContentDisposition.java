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
package org.apache.juneau.http;

import org.apache.juneau.http.annotation.*;

/**
 * Represents a parsed <l>Content-Disposition</l> HTTP request header.
 *
 * <p>
 * In a regular HTTP response, the Content-Disposition response header is a header indicating if the content is expected
 * to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is
 * downloaded and saved locally.
 *
 * <h5 class='figure'>Example</h5>
 * <p class='bcode w800'>
 * 	Content-Disposition: form-data; name="fieldName"; filename="filename.jpg"
 * </p>
 *
 * <h5 class='topic'>RFC2616 Specification</h5>
 *
 * The Expect request-header field is used to indicate that particular server behaviors are required by the client.
 * <p class='bcode w800'>
 *	content-disposition = "Content-Disposition" ":"
 *  	disposition-type *( ";" disposition-parm )
 * 	disposition-type = "attachment" | disp-extension-token
 * 	disposition-parm = filename-parm | disp-extension-parm
 * 	filename-parm = "filename" "=" quoted-string
 *	disp-extension-token = token
 * 	disp-extension-parm = token "=" ( token | quoted-string )
 * </p>
 *
 * <ul class='seealso'>
 * 	<li class='extlink'>{@doc RFC2616}
 * </ul>
 */
@Header("Content-Disposition")
public final class ContentDisposition extends BasicStringHeader {

	/**
	 * Returns a parsed <c>Content-Disposition</c> header.
	 *
	 * @param value The <c>Content-Disposition</c> header string.
	 * @return The parsed <c>Content-Disposition</c> header, or <jk>null</jk> if the string was null.
	 */
	public static ContentDisposition forString(String value) {
		if (value == null)
			return null;
		return new ContentDisposition(value);
	}

	/**
	 * Constructor.
	 *
	 * @param value The value for this header.
	 */
	public ContentDisposition(String value) {
		super("Expect", value);
	}
}
