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
package org.apache.juneau.rest;

import java.io.*;

import javax.servlet.http.*;

import org.apache.juneau.http.response.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.processors.*;

/**
 * Defines the interface for processors that convert POJOs to appropriate HTTP responses.
 *
 * <p>
 * The REST Server API uses the concept of registered response processors for converting objects returned by REST
 * methods or set through {@link RestResponse#setOutput(Object)} into appropriate HTTP responses.
 *
 * <p>
 * Response processors can be associated with REST resources via the following:
 * <ul>
 * 	<li class='ja'>{@link Rest#responseProcessors}
 * 	<li class='jm'>{@link RestContextBuilder#responseProcessors(Class...)}
 * 	<li class='jm'>{@link RestContextBuilder#responseProcessors(ResponseProcessor...)}
 * </ul>
 *
 * <p>
 * By default, REST resources are registered with the following response processors:
 * <ul class='spaced-list'>
 * 	<li class='jc'>{@link ReaderProcessor}
 * 	<li class='jc'>{@link InputStreamProcessor}
 * 	<li class='jc'>{@link HttpResourceProcessor}
 * 	<li class='jc'>{@link HttpEntityProcessor}
 * 	<li class='jc'>{@link DefaultProcessor}
 * </ul>
 *
 * <p>
 * Response processors can be used to process POJOs that cannot normally be handled through Juneau serializers, or
 * because it's simply easier to define response processors for special cases.
 *
 * <p>
 * The following example shows how to create a response processor to handle special <c>Foo</c> objects outside the
 * normal Juneau architecture.
 * <p class='bcode w800'>
 * 	<ja>@Rest</ja>(
 * 		path=<js>"/example"</js>,
 * 		responseHandlers=FooHandler.<jk>class</jk>
 * 	)
 * 	<jk>public class</jk> Example <jk>extends</jk> RestServlet {
 *
 * 		<ja>@RestGet</ja>(<js>"/"</js>)
 * 		<jk>public</jk> Foo test1() {
 * 			<jk>return new</jk> Foo(<js>"123"</js>);
 * 		}
 *
 * 		<jk>public static class</jk> FooHandler <jk>implements</jk> ResponseHandler {
 * 			<ja>@Override</ja>
 * 			<jk>public boolean</jk> handle(RestRequest <jv>req</jv>, RestResponse <jv>res</jv>, Object <jv>output</jv>) <jk>throws</jk> IOException, RestException {
 * 				<jk>if</jk> (<jv>output</jv> <jk>instanceof</jk> Foo) {
 * 					Foo <jv>foo</jv> = (Foo)<jv>output</jv>;
 * 					<jc>// Set some headers and body content.</jc>
 * 					<jv>res</jv>.setHeader(<js>"Foo-ID"</js>, <jv>foo</jv>.getId());
 * 					<jv>res</jv>.getWriter().write(<js>"foo.id="</js> + <jv>foo</jv>.getId());
 * 					<jk>return true</jk>;  <jc>// We handled it.</jc>
 * 				}
 * 				<jk>return false</jk>;  <jc>// We didn't handle it.</jc>
 * 			}
 * 		}
 * 	}
 * </p>
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc RestmReturnTypes}
 * </ul>
 */
public interface ResponseProcessor {

	/**
	 * Process this response if possible.
	 *
	 * @param call The HTTP call.
	 * @return One of the following codes:
	 * 	<ul>
	 * 		<li><c>0</c> - The processor could not handle the request.
	 * 		<li><c>1</c> - The processor was able to fully handle the request.
	 * 		<li><c>2</c> - The processor was able to partially handle the request by replacing the output.
	 * 			The response processors should start over.
	 * 	</ul>
	 * @throws IOException
	 * 	If low-level exception occurred on output stream.
	 * 	Results in a {@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR} error.
	 * @throws BasicHttpException
	 * 	If some other exception occurred.
	 * 	Can be used to provide an appropriate HTTP response code and message.
	 */
	int process(RestCall call) throws IOException, BasicHttpException;
}
