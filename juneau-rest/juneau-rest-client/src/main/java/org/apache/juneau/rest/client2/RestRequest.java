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
package org.apache.juneau.rest.client2;

import static org.apache.juneau.internal.StringUtils.*;
import static org.apache.juneau.httppart.HttpPartType.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.*;

import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.concurrent.*;
import org.apache.http.entity.*;
import org.apache.http.entity.ContentType;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.juneau.*;
import org.apache.juneau.http.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.oapi.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.client2.ext.NameValuePairs;
import org.apache.juneau.rest.client2.ext.SerializedNameValuePair;
import org.apache.juneau.rest.client2.RestCallException;
import org.apache.juneau.rest.client2.ext.*;
import org.apache.juneau.rest.client2.logging.*;
import org.apache.juneau.serializer.*;

/**
 * Represents a request to a remote REST resource.
 *
 * <p>
 * Instances of this class are created by the various creator methods on the {@link RestClient} class.
 *
 * <ul class='seealso'>
 * 	<li class='jc'>{@link RestClient}
 * 	<li class='link'>{@doc juneau-rest-client}
 * </ul>
 */
public final class RestRequest extends BeanSession implements HttpUriRequest, Configurable {

	private static final ContentType TEXT_PLAIN = ContentType.create("text/plain");

	private final RestClient client;                       // The client that created this call.
	private final HttpRequestBase request;                 // The request.
	private RestResponse response;                         // The response.
	List<RestCallInterceptor> interceptors = new ArrayList<>();   // Used for intercepting and altering requests.

	private boolean ignoreErrors;

	private Object input;
	private boolean hasInput;                              // input() was called, even if it's setting 'null'.
	private Serializer serializer;
	private HttpPartSerializer partSerializer;
	private HttpPartSchema requestBodySchema;
	private URIBuilder uriBuilder;
	private NameValuePairs formData;
	private Predicate<Integer> errorCodes;
	private HttpHost target;
	private HttpContext context;

	/**
	 * Constructs a REST call with the specified method name.
	 *
	 * @param client The client that created this request.
	 * @param request The wrapped Apache HTTP client request object.
	 * @param uri The URI for this call.
	 * @throws RestCallException If an exception or non-200 response code occurred during the connection attempt.
	 */
	protected RestRequest(RestClient client, HttpRequestBase request, URI uri) throws RestCallException {
		super(client, BeanSessionArgs.DEFAULT);
		this.client = client;
		this.request = request;
		interceptors(this.client.interceptors);
		this.errorCodes = client.errorCodes;
		this.serializer = client.serializer;
		this.partSerializer = client.getPartSerializer();
		this.uriBuilder = new URIBuilder(uri);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Configuration
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Specifies the serializer to use on this request body.
	 *
	 * <p>
	 * Overrides the serializer specified on the {@link RestClient}.
	 *
	 * @param serializer The serializer used to serialize POJOs to the body of the HTTP request.
	 * @return This object (for method chaining).
	 */
	public RestRequest serializer(Serializer serializer) {
		this.serializer = serializer;
		return this;
	}

	/**
	 * Allows you to override what status codes are considered error codes that would result in a {@link RestCallException}.
	 *
	 * <p>
	 * The default error code predicate is: <code>x -&gt; x &gt;= 400</code>.
	 *
	 * @param value The new predicate for calculating error codes.
	 * @return This object (for method chaining).
	 */
	public RestRequest errorCodes(Predicate<Integer> value) {
		this.errorCodes = value;
		return this;
	}

	/**
	 * Add one or more interceptors for this call only.
	 *
	 * @param interceptors The interceptors to add to this call.
	 * @return This object (for method chaining).
	 * @throws RestCallException If init method on interceptor threw an exception.
	 */
	public RestRequest interceptors(RestCallInterceptor...interceptors) throws RestCallException {
		for (RestCallInterceptor i : interceptors) {
			this.interceptors.add(i);
			try {
				i.onInit(this);
			} catch (Exception e) {
				throw RestCallException.create(e);
			}
		}
		return this;
	}

	/**
	 * Prevent {@link RestCallException RestCallExceptions} from being thrown when HTTP status 400+ is encountered.
	 *
	 * @return This object (for method chaining).
	 */
	public RestRequest ignoreErrors() {
		this.ignoreErrors = true;
		return this;
	}

	/**
	 * Set configuration settings on this request.
	 *
	 * <p>
	 * Use {@link RequestConfig#custom()} to create configuration parameters for the request.
	 *
	 * @param config The new configuration settings for this request.
	 * @return This object (for method chaining).
	 */
	public RestRequest requestConfig(RequestConfig config) {
		setConfig(config);
		return this;
	}

	/**
	 * Adds a {@link RestCallLogger} to the list of interceptors on this class.
	 *
	 * @param level The log level to log events at.
	 * @param log The logger.
	 * @return This object (for method chaining).
	 */
	public RestRequest logTo(Level level, Logger log) {
		try {
			interceptors(new BasicRestCallLogger(level, log));
		} catch (RestCallException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Adds a {@link ConsoleRestCallLogger} to the list of interceptors on this class.
	 *
	 * @return This object (for method chaining).
	 */
	public RestRequest logToConsole() {
		try {
			interceptors(new ConsoleRestCallLogger());
		} catch (RestCallException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Sets <c>Debug: value</c> header on this request.
	 *
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest debug() throws RestCallException {
		header("Debug", true);
		return this;
	}

	/**
	 * Specifies the target host for the request.
	 *
	 * @param target The target host for the request.
	 * 	Implementations may accept <jk>null</jk> if they can still determine a route, for example to a default
	 * 	target or by inspecting the request.
	 * @return This object (for method chaining).
	 */
	public RestRequest target(HttpHost target) {
		this.target = target;
		return this;
	}

	/**
	 * Override the context to use for the execution.
	 *
	 * @param context The context to use for the execution, or <jk>null</jk> to use the default context.
	 * @return This object (for method chaining).
	 */
	public RestRequest context(HttpContext context) {
		this.context = context;
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------
	// URI
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Sets the URI for this request.
	 *
	 * <p>
	 * Can be any of the following types:
	 * <ul>
	 * 	<li>{@link URI}
	 * 	<li>{@link URL}
	 * 	<li>{@link URIBuilder}
	 * 	<li>Anything else converted to a string using {@link Object#toString()}.
	 * </ul>
	 *
	 * <p>
	 * Relative URL strings will be interpreted as relative to the root URL defined on the client.
	 *
	 * @param uri
	 * 	The URL of the remote REST resource.
	 * 	<br>This overrides the URI passed in from the client.
	 * 	<br>Can be any of the following types:
	 * 	<ul class='spaced-list'>
	 * 		<li class='jc'>{@link URIBuilder}
	 * 		<li class='jc'>{@link URI}
	 * 		<li class='jc'>{@link URL}
	 * 		<li class='jc'>{@link String}
	 * 		<li class='jc'>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid URI syntax detected.
	 */
	public RestRequest uri(Object uri) throws RestCallException {
		try {
			if (uri != null)
				uriBuilder = new URIBuilder(client.toURI(uri));
			return this;
		} catch (URISyntaxException e) {
			throw new RestCallException(e);
		}
	}

	/**
	 * Sets the URI scheme.
	 *
	 * @param scheme The new URI host.
	 * @return This object (for method chaining).
	 */
	public RestRequest scheme(String scheme) {
		uriBuilder.setScheme(scheme);
		return this;
	}

	/**
	 * Sets the URI host.
	 *
	 * @param host The new URI host.
	 * @return This object (for method chaining).
	 */
	public RestRequest host(String host) {
		uriBuilder.setHost(host);
		return this;
	}

	/**
	 * Sets the URI port.
	 *
	 * @param port The new URI port.
	 * @return This object (for method chaining).
	 */
	public RestRequest port(int port) {
		uriBuilder.setPort(port);
		return this;
	}

	/**
	 * Sets the URI user info.
	 *
	 * @param userInfo The new URI user info.
	 * @return This object (for method chaining).
	 */
	public RestRequest userInfo(String userInfo) {
		uriBuilder.setUserInfo(userInfo);
		return this;
	}

	/**
	 * Sets the URI user info.
	 *
	 * @param username The new URI username.
	 * @param password The new URI password.
	 * @return This object (for method chaining).
	 */
	public RestRequest userInfo(String username, String password) {
		uriBuilder.setUserInfo(username, password);
		return this;
	}

	/**
	 * Sets the URI fragment.
	 *
	 * @param fragment The URI fragment.  The value is expected to be unescaped and may contain non ASCII characters.
	 * @return This object (for method chaining).
	 */
	public RestRequest fragment(String fragment) {
		uriBuilder.setFragment(fragment);
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Path
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Replaces a path parameter of the form <js>"{name}"</js> in the URL.
	 *
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @param serializer The serializer to use for serializing the value to a string.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, then the {@link HttpPartSerializer} defined on the client is used ({@link OpenApiSerializer} by default).
	 * 	</ul>
	 * @param schema The schema object that defines the format of the output.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, defaults to {@link HttpPartSchema#DEFAULT}.
	 * 		<li>Only used if serializer is schema-aware (e.g. {@link OpenApiSerializer}).
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	@SuppressWarnings("unchecked")
	public RestRequest path(String name, Object value, HttpPartSerializer serializer, HttpPartSchema schema) throws RestCallException {
		serializer = (serializer == null ? partSerializer : serializer);
		boolean isMulti = isEmpty(name) || "*".equals(name) || value instanceof NameValuePairs;
		if (! isMulti) {
			path(new SerializedNameValuePair(name, value, PATH, serializer, schema, false));
		} else if (value instanceof NameValuePairs) {
			for (NameValuePair p : (NameValuePairs)value)
				path(p);
		} else if (value instanceof Map) {
			for (Map.Entry<String,Object> p : ((Map<String,Object>) value).entrySet()) {
				String n = p.getKey();
				Object v = p.getValue();
				HttpPartSchema s = schema == null ? null : schema.getProperty(n);
				path(new SerializedNameValuePair(n, v, PATH, serializer, s, false));
			}
		} else if (isBean(value)) {
			return path(name, toBeanMap(value), serializer, schema);
		} else if (value != null) {
			throw new RestCallException("Invalid name ''{0}'' passed to path(name,value) for data type ''{1}''", name, className(value));
		}
		return this;
	}

	/**
	 * Replaces a path parameter of the form <js>"{name}"</js> in the URL.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}"</js>)
	 * 		.path(<js>"foo"</js>, <js>"bar"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(String name, Object value) throws RestCallException {
		return path(name, value, null, null);
	}

	/**
	 * Replaces a path parameter of the form <js>"{name}"</js> in the URL.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}"</js>)
	 * 		.path(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param param The path parameter.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(NameValuePair param) throws RestCallException {
		String path = uriBuilder.getPath();
		String name = param.getName(), value = param.getValue();
		String var = "{" + name + "}";
		if (path.indexOf(var) == -1 && ! name.equals("/*"))
			throw new RestCallException("Path variable {"+name+"} was not found in path.");
		String p = null;
		if (name.equals("/*"))
			p = path.replaceAll("\\/\\*$", value);
		else
			p = path.replace(var, String.valueOf(value));
		uriBuilder.setPath(p);
		return this;
	}

	/**
	 * Replaces path parameters of the form <js>"{name}"</js> in the URL.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}"</js>)
	 * 		.path(<jk>new</jk> ObjectMap(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The path parameters.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(ObjectMap params) throws RestCallException {
		return path((Map<String,Object>)params);
	}

	/**
	 * Replaces path parameters of the form <js>"{name}"</js> in the URL.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}"</js>)
	 * 		.path(AMap.<jsm>create</jsm>().append(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The path parameters.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(Map<String,Object> params) throws RestCallException {
		for (Map.Entry<String,Object> e : params.entrySet())
			path(e.getKey(), e.getValue(), null, null);
		return this;
	}

	/**
	 * Replaces path parameters of the form <js>"{name}"</js> in the URL.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}"</js>)
	 * 		.path(<jk>new</jk> NameValuePairs(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The path parameters.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(NameValuePairs params) throws RestCallException {
		for (NameValuePair p : params)
			path(p);
		return this;
	}

	/**
	 * Replaces path parameters of the form <js>"{name}"</js> in the URL.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}"</js>)
	 * 		.path(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The path parameters.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(NameValuePair...params) throws RestCallException {
		for (NameValuePair p : params)
			path(p);
		return this;
	}

	/**
	 * Replaces path parameters of the form <js>"{name}"</js> in the URL using a bean with key/value properties.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}"</js>)
	 * 		.path(<jk>new</jk> MyBean())
	 * 		.run();
	 * </p>
	 *
	 * @param bean The path bean.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(Object bean) throws RestCallException {
		return path(toBeanMap(bean));
	}

	/**
	 * Replaces path parameters of the form <js>"{name}"</js> in the URL using free-form key/value pairs.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<js>"/{foo}/{bar}"</js>)
	 * 		.path(<js>"foo"</js>,<js>"val1"</js>,<js>"bar"</js>,<js>"val2"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param pairs The path key/value pairs.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest path(Object...pairs) throws RestCallException {
		if (pairs.length % 2 != 0)
			throw new RestCallException("Odd number of parameters passed into path(Object...)");
		for (int i = 0; i < pairs.length; i+=2)
			path(new SerializedNameValuePair(stringify(pairs[i]), pairs[i+1], PATH, partSerializer, null, false));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Query
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Adds a query parameter to the URI.
	 *
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @param skipIfEmpty Don't add the parameter if the value is empty.
	 * @param serializer The serializer to use for serializing the value to a string.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, then the {@link HttpPartSerializer} defined on the client is used ({@link OpenApiSerializer} by default).
	 * 	</ul>
	 * @param schema The schema object that defines the format of the output.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, defaults to {@link HttpPartSchema#DEFAULT}.
	 * 		<li>Only used if serializer is schema-aware (e.g. {@link OpenApiSerializer}).
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	@SuppressWarnings("unchecked")
	public RestRequest query(String name, Object value, boolean skipIfEmpty, HttpPartSerializer serializer, HttpPartSchema schema) throws RestCallException {
		serializer = (serializer == null ? partSerializer : serializer);
		boolean isMulti = isEmpty(name) || "*".equals(name) || value instanceof NameValuePairs;
		if (! isMulti) {
			query(new SerializedNameValuePair(name, value, QUERY, serializer, schema, skipIfEmpty));
		} else if (value instanceof NameValuePairs) {
			for (NameValuePair p : (NameValuePairs)value)
				query(p, skipIfEmpty);
		} else if (value instanceof Map) {
			for (Map.Entry<String,Object> p : ((Map<String,Object>)value).entrySet()) {
				String n = p.getKey();
				Object v = p.getValue();
				HttpPartSchema s = schema == null ? null : schema.getProperty(n);
				query(new SerializedNameValuePair(n, v, QUERY, serializer, s, skipIfEmpty));
			}
		} else if (isBean(value)) {
			query(name, toBeanMap(value), skipIfEmpty, serializer, schema);
		} else if (value instanceof Reader) {
			query((Reader)value);
		} else if (value instanceof InputStream) {
			query((InputStream)value);
		} else if (value instanceof CharSequence) {
			query(value.toString());
		} else {
			throw new RestCallException("Invalid name ''{0}'' passed to query(name,value,skipIfEmpty) for data type ''{1}''", name, className(value));
		}
		return this;
	}

	/**
	 * Adds a query parameter to the URI.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<js>"foo"</js>, <js>"bar"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(String name, Object value) throws RestCallException {
		return query(name, value, false, null, null);
	}

	/**
	 * Adds a query parameter to the URI.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param param The query parameter.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(NameValuePair param) throws RestCallException {
		return query(param, false);
	}

	/**
	 * Adds a query parameter to the URI.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>""</js>), <jk>true</jk>)  <jc>// Will be skipped</jc>
	 * 		.run();
	 * </p>
	 *
	 * @param param The query parameter.
	 * @param skipIfEmpty Don't add the parameter if the value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(NameValuePair param, boolean skipIfEmpty) throws RestCallException {
		String v = param.getValue();
		if (canAdd(v, skipIfEmpty))
			uriBuilder.addParameter(param.getName(), v);
		return this;
	}

	/**
	 * Adds query parameters to the URI.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> ObjectMap(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The query parameters.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(ObjectMap params) throws RestCallException {
		return query((Map<String,Object>)params);
	}

	/**
	 * Adds query parameters to the URI.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(AMap.<jsm>create</jsm>().append(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The query parameters.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(Map<String,Object> params) throws RestCallException {
		for (Map.Entry<String,Object> e : params.entrySet())
			query(e.getKey(), e.getValue(), false, null, null);
		return this;
	}

	/**
	 * Adds query parameters to the URI.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> NameValuePairs(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The query parameters.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(NameValuePairs params) throws RestCallException {
		for (NameValuePair p : params)
			query(p);
		return this;
	}

	/**
	 * Adds query parameters to the URI.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The query parameters.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(NameValuePair...params) throws RestCallException {
		for (NameValuePair p : params)
			query(p);
		return this;
	}

	/**
	 * Adds query parameters to the URI using a bean with key/value properties.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> MyBean())
	 * 		.run();
	 * </p>
	 *
	 * @param bean The query bean.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(Object bean) throws RestCallException {
		return query(toBeanMap(bean));
	}

	/**
	 * Adds query parameters to the URI query using free-form key/value pairs..
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<js>"key1"</js>,<js>"val1"</js>,<js>"key2"</js>,<js>"val2"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param pairs The query key/value pairs.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest query(Object...pairs) throws RestCallException {
		if (pairs.length % 2 != 0)
			throw new RestCallException("Odd number of parameters passed into query(Object...)");
		for (int i = 0; i < pairs.length; i+=2)
			query(new SerializedNameValuePair(stringify(pairs[i]), pairs[i+1], QUERY, partSerializer, null, false));
		return this;
	}

	/**
	 * Sets a custom URI query.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<js>"foo=bar&amp;baz=qux"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param query The new URI query string.
	 * @return This object (for method chaining).
	 */
	public RestRequest query(String query) {
		uriBuilder.setCustomQuery(query);
		return this;
	}

	/**
	 * Sets a custom URI query from a reader.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> StringReader(<js>"foo=bar&amp;baz=qux"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param query The new URI query string.
	 * @return This object (for method chaining).
	 * @throws RestCallException IO exception from reader occurred.
	 */
	public RestRequest query(Reader query) throws RestCallException {
		try {
			uriBuilder.setCustomQuery(IOUtils.read(query));
		} catch (IOException e) {
			throw new RestCallException(e);
		}
		return this;
	}

	/**
	 * Sets a custom URI query from an input stream.
	 *
	 * @param query The new URI query string.
	 * @return This object (for method chaining).
	 * @throws RestCallException IO exception from stream occurred.
	 */
	public RestRequest query(InputStream query) throws RestCallException {
		try {
			uriBuilder.setCustomQuery(IOUtils.read(query));
		} catch (IOException e) {
			throw new RestCallException(e);
		}
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Form data
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Adds a form-data parameter to the request body.
	 *
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @param skipIfEmpty Don't add the parameter if the value is empty.
	 * @param serializer The serializer to use for serializing the value to a string.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, then the {@link HttpPartSerializer} defined on the client is used ({@link OpenApiSerializer} by default).
	 * 	</ul>
	 * @param schema The schema object that defines the format of the output.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, defaults to {@link HttpPartSchema#DEFAULT}.
	 * 		<li>Only used if serializer is schema-aware (e.g. {@link OpenApiSerializer}).
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	@SuppressWarnings("unchecked")
	public RestRequest formData(String name, Object value, boolean skipIfEmpty, HttpPartSerializer serializer, HttpPartSchema schema) throws RestCallException {
		serializer = (serializer == null ? partSerializer : serializer);
		boolean isMulti = isEmpty(name) || "*".equals(name) || value instanceof NameValuePairs;
		if (! isMulti) {
			formData(new SerializedNameValuePair(name, value, FORMDATA, serializer, schema, skipIfEmpty));
		} else if (value instanceof NameValuePairs) {
			for (NameValuePair p : (NameValuePairs)value)
				formData(p, skipIfEmpty);
		} else if (value instanceof Map) {
			for (Map.Entry<String,Object> p : ((Map<String,Object>)value).entrySet()) {
				String n = p.getKey();
				Object v = p.getValue();
				HttpPartSchema s = schema == null ? null : schema.getProperty(n);
				formData(new SerializedNameValuePair(n, v, FORMDATA, serializer, s, skipIfEmpty));
			}
		} else if (isBean(value)) {
			formData(name, toBeanMap(value), skipIfEmpty, serializer, schema);
		} else if (value instanceof Reader) {
			formData((Reader)value);
		} else if (value instanceof InputStream) {
			formData((InputStream)value);
		} else if (value instanceof CharSequence) {
			formData((CharSequence)value);
		} else {
			throw new FormattedRuntimeException("Invalid name ''{0}'' passed to formData(name,value,skipIfEmpty) for data type ''{1}''", name, className(value));
		}
		return this;
	}

	/**
	 * Adds a form-data parameter to the request body.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<js>"foo"</js>, <js>"bar"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(String name, Object value) throws RestCallException {
		return formData(name, value, false, null, null);
	}

	/**
	 * Adds a form-data parameter to the request body.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param param The form-data parameter.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(NameValuePair param) throws RestCallException {
		return formData(param, false);
	}

	/**
	 * Adds a form-data parameter to the request body.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>""</js>), <jk>true</jk>)  <jc>// Will be skipped</jc>
	 * 		.run();
	 * </p>
	 *
	 * @param param The form-data parameter.
	 * @param skipIfEmpty Don't add the parameter if the value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(NameValuePair param, boolean skipIfEmpty) throws RestCallException {
		String v = param.getValue();
		if (canAdd(v, skipIfEmpty)) {
			if (formData == null)
				formData = new NameValuePairs();
			formData.add(new BasicNameValuePair(param.getName(), v));
		}
		return this;
	}

	/**
	 * Adds form-data parameters to the request body.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<jk>new</jk> ObjectMap(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The form-data parameters.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(ObjectMap params) throws RestCallException {
		return formData((Map<String,Object>)params);
	}

	/**
	 * Adds form-data parameters to the request body.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(AMap.<jsm>create</jsm>().append(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The form-data parameters.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(Map<String,Object> params) throws RestCallException {
		for (Map.Entry<String,Object> e : params.entrySet())
			formData(e.getKey(), e.getValue());
		return this;
	}

	/**
	 * Adds form-data parameters to the request body.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<jk>new</jk> NameValuePairs(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The form-data parameters.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(NameValuePairs params) throws RestCallException {
		for (NameValuePair p : params)
			formData(p);
		return this;
	}

	/**
	 * Adds form-data parameters to the request body.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<jk>new</jk> NameValuePair(<js>"foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param params The form-data parameters.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(NameValuePair...params) throws RestCallException {
		for (NameValuePair p : params)
			formData(p);
		return this;
	}

	/**
	 * Adds form-data parameters to the request body using a bean with key/value properties.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<jk>new</jk> MyBean())
	 * 		.run();
	 * </p>
	 *
	 * @param bean The form-data bean.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(Object bean) throws RestCallException {
		return formData(toBeanMap(bean));
	}

	/**
	 * Adds form-data parameters to the request body using free-form key/value pairs.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<js>"key1"</js>,<js>"val1"</js>,<js>"key2"</js>,<js>"val2"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param pairs The form-data key/value pairs.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest formData(Object...pairs) throws RestCallException {
		if (pairs.length % 2 != 0)
			throw new RestCallException("Odd number of parameters passed into formData(Object...)");
		for (int i = 0; i < pairs.length; i+=2)
			formData(new SerializedNameValuePair(stringify(pairs[i]), pairs[i+1], FORMDATA, partSerializer, null, false));
		return this;
	}

	/**
	 * Sets the body of a URL-encoded form post from a string.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<js>"foo=bar&amp;baz=qux"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param body The body of the request.
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	public RestRequest formData(CharSequence body) throws RestCallException {
		contentType("application/x-www-form-urlencoded");
		try {
			body(new StringEntity(body.toString()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * Sets the body of a URL-encoded form post from a reader.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.formPost(<jsf>URL</jsf>)
	 * 		.formData(<jk>new</jk> StringReader(<js>"foo=bar&amp;baz=qux"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param body The body of the request.
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	public RestRequest formData(Reader body) throws RestCallException {
		contentType("application/x-www-form-urlencoded");
		body(body);
		return this;
	}

	/**
	 * Sets the body of a URL-encoded form post from an input stream.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.query(<jk>new</jk> FileInputStream(<jsf>PATH</jsf>))
	 * 		.run();
	 * </p>
	 *
	 * @param body The body of the request.
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	public RestRequest formData(InputStream body) throws RestCallException {
		contentType("application/x-www-form-urlencoded");
		body(body);
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Request body
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Sets the body of this request.
	 *
	 * @param input
	 * 	The input to be sent to the REST resource (only valid for PUT/POST/PATCH) requests.
	 * 	<br>Can be of the following types:
	 * 	<ul class='spaced-list'>
	 * 		<li class='jc'>
	 * 			{@link Reader} - Raw contents of {@code Reader} will be serialized to remote resource.
	 * 		<li class='jc'>
	 * 			{@link InputStream} - Raw contents of {@code InputStream} will be serialized to remote resource.
	 * 		<li class='jc'>
	 * 			{@link ReaderResource} - Raw contents of {@code Reader} will be serialized to remote resource.  Additional headers and media type will be set on request.
	 * 		<li class='jc'>
	 * 			{@link StreamResource} - Raw contents of {@code InputStream} will be serialized to remote resource.  Additional headers and media type will be set on request.
	 * 		<li class='jc'>
	 * 			{@link Object} - POJO to be converted to text using the {@link Serializer} registered with the
	 * 			{@link RestClient}.
	 * 		<li class='jc'>
	 * 			{@link HttpEntity} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
	 * 		<li class='jc'>
	 * 			{@link NameValuePairs} - Converted to a URL-encoded FORM post.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException If a retry was attempted, but the entity was not repeatable.
	 */
	public RestRequest body(Object input) throws RestCallException {
		this.input = input;
		this.hasInput = true;
		this.formData = null;
		return this;
	}

	/**
	 * Sets the body of this request.
	 *
	 * @param input
	 * 	The input to be sent to the REST resource (only valid for PUT/POST/PATCH) requests.
	 * 	<br>Can be of the following types:
	 * 	<ul class='spaced-list'>
	 * 		<li class='jc'>
	 * 			{@link Reader} - Raw contents of {@code Reader} will be serialized to remote resource.
	 * 		<li class='jc'>
	 * 			{@link InputStream} - Raw contents of {@code InputStream} will be serialized to remote resource.
	 * 		<li class='jc'>
	 * 			{@link ReaderResource} - Raw contents of {@code Reader} will be serialized to remote resource.  Additional headers and media type will be set on request.
	 * 		<li class='jc'>
	 * 			{@link StreamResource} - Raw contents of {@code InputStream} will be serialized to remote resource.  Additional headers and media type will be set on request.
	 * 		<li class='jc'>
	 * 			{@link Object} - POJO to be converted to text using the {@link Serializer} registered with the
	 * 			{@link RestClient}.
	 * 		<li class='jc'>
	 * 			{@link HttpEntity} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
	 * 		<li class='jc'>
	 * 			{@link NameValuePairs} - Converted to a URL-encoded FORM post.
	 * 	</ul>
	 * @param schema The schema object that defines the format of the output.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, defaults to {@link HttpPartSchema#DEFAULT}.
	 * 		<li>Only used if serializer is schema-aware (e.g. {@link OpenApiSerializer}).
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException If a retry was attempted, but the entity was not repeatable.
	 */
	public RestRequest body(Object input, HttpPartSchema schema) throws RestCallException {
		this.input = input;
		this.hasInput = true;
		this.formData = null;
		this.requestBodySchema = schema;
		return this;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Headers
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Sets a header on the request.
	 *
	 * @param name The header name.
	 * @param value The header value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @param skipIfEmpty Don't add the header if the value is empty.
	 * @param serializer The serializer to use for serializing the value to a string.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, then the {@link HttpPartSerializer} defined on the client is used ({@link OpenApiSerializer} by default).
	 * 	</ul>
	 * @param schema The schema object that defines the format of the output.
	 * 	<ul>
	 * 		<li>If <jk>null</jk>, defaults to {@link HttpPartSchema#DEFAULT}.
	 * 		<li>Only used if serializer is schema-aware (e.g. {@link OpenApiSerializer}).
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	@SuppressWarnings("unchecked")
	public RestRequest header(String name, Object value, boolean skipIfEmpty, HttpPartSerializer serializer, HttpPartSchema schema) throws RestCallException {
		serializer = (serializer == null ? partSerializer : serializer);
		boolean isMulti = isEmpty(name) || "*".equals(name) || value instanceof NameValuePairs;
		if (! isMulti) {
			header(new SerializedNameValuePair(name, value, HEADER, serializer, schema, skipIfEmpty));
		} else if (value instanceof NameValuePairs) {
			for (NameValuePair p : (NameValuePairs)value)
				header(p, skipIfEmpty);
		} else if (value instanceof Map) {
			for (Map.Entry<String,Object> p : ((Map<String,Object>)value).entrySet()) {
				String n = p.getKey();
				Object v = p.getValue();
				HttpPartSchema s = schema == null ? null : schema.getProperty(n);
				header(new SerializedNameValuePair(n, v, HEADER, serializer, s, skipIfEmpty));
			}
		} else if (isBean(value)) {
			return header(name, toBeanMap(value), skipIfEmpty, serializer, schema);
		} else {
			throw new RestCallException("Invalid name ''{0}'' passed to header(name,value,skipIfEmpty) for data type ''{1}''", name, className(value));
		}
		return this;
	}

	/**
	 * Sets a header on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.header(<js>"Foo"</js>, <js>"bar"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param name The header name.
	 * @param value The header value.
	 * 	<ul>
	 * 		<li>Can be any POJO.
	 * 		<li>Converted to a string using the specified part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest header(String name, Object value) throws RestCallException {
		return header(name, value, false, null, null);
	}

	/**
	 * Sets a header on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.header(<jk>new</jk> BasicHeader(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param header The header to set.
	 * @return This object (for method chaining).
	 */
	public RestRequest header(Header header) {
		return header(header, false);
	}

	/**
	 * Sets a header on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.header(<jk>new</jk> BasicHeader(<js>"Foo"</js>, <js>""</js>), <jk>true</jk>) <jc>// Will be skipped</jc>
	 * 		.run();
	 * </p>
	 *
	 * @param header The header to set.
	 * @param skipIfEmpty Don't add the header if the value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public RestRequest header(Header header, boolean skipIfEmpty) {
		String v = header.getValue();
		if (! (skipIfEmpty && isEmpty(v)))
			addHeader(header);
		return this;
	}

	/**
	 * Sets a header on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.header(<jk>new</jk> NameValuePair(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param header The header to set.
	 * @return This object (for method chaining).
	 */
	public RestRequest header(NameValuePair header) {
		return header(header, false);
	}

	/**
	 * Sets a header on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.header(<jk>new</jk> NameValuePair(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param header
	 * 	The header to set.
	 * @param skipIfEmpty
	 * 	Don't add the header if the value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public RestRequest header(NameValuePair header, boolean skipIfEmpty) {
		String v = header.getValue();
		if (canAdd(v, skipIfEmpty))
			addHeader(header.getName(), v);
		return this;
	}

	/**
	 * Sets a header on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.header(<jk>new</jk> Accept(<js>"Content-Type"</js>, <js>"application/json"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param header The header to set.
	 * @return This object (for method chaining).
	 * @throws RestCallException Error occurred.
	 */
	public RestRequest header(HttpHeader header) throws RestCallException {
		return header(header.getName(), header.getValue());
	}

	/**
	 * Adds multiple headers to the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(<jk>new</jk> BasicHeader(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param headers The header to set.
	 * @return This object (for method chaining).
	 */
	public RestRequest headers(Header...headers) {
		for (Header h : headers)
			header(h);
		return this;
	}

	/**
	 * Adds multiple headers to the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(<jk>new</jk> ObjectMap(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param headers The header pairs.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest headers(ObjectMap headers) throws RestCallException {
		return headers((Map<String,Object>)headers);
	}

	/**
	 * Sets multiple headers on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(AMap.<jsm>create</jsm>().append(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param headers The header pairs.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest headers(Map<String,Object> headers) throws RestCallException {
		for (Map.Entry<String,Object> e : headers.entrySet())
			header(e.getKey(), e.getValue(), false, null, null);
		return this;
	}

	/**
	 * Sets multiple headers on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(<jk>new</jk> NameValuePairs(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param headers The header pairs.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest headers(NameValuePairs headers) throws RestCallException {
		for (NameValuePair p : headers)
			header(p);
		return this;
	}

	/**
	 * Sets multiple headers on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(<jk>new</jk> NameValuePair(<js>"Foo"</js>, <js>"bar"</js>))
	 * 		.run();
	 * </p>
	 *
	 * @param headers The header pairs.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest headers(NameValuePair...headers) throws RestCallException {
		for (NameValuePair p : headers)
			header(p);
		return this;
	}

	/**
	 * Sets multiple headers on the request using a bean with key/value properties.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(<jk>new</jk> MyBean())
	 * 		.run();
	 * </p>
	 *
	 * @param bean The header bean.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest headers(Object bean) throws RestCallException {
		return headers(toBeanMap(bean));
	}

	/**
	 * Sets multiple headers on the request using freeform key/value pairs.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(<js>"Header1"</js>,<js>"val1"</js>,<js>"Header2"</js>,<js>"val2"</js>)
	 * 		.run();
	 * </p>
	 *
	 * @param pairs The header key/value pairs.
	 * 	<ul>
	 * 		<li>Values can be any POJO.
	 * 		<li>Values converted to a string using the configured part serializer.
	 * 		<li>Values are converted to strings at runtime to allow them to be modified externally.
	 * 	</ul>
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest headers(Object...pairs) throws RestCallException {
		if (pairs.length % 2 != 0)
			throw new RestCallException("Odd number of parameters passed into headers(Object...)");
		for (int i = 0; i < pairs.length; i+=2)
			header(new SerializedNameValuePair(stringify(pairs[i]), pairs[i+1], HEADER, partSerializer, null, false));
		return this;
	}

	/**
	 * Sets multiple headers on the request using header beans.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.headers(
	 * 			<jk>new</jk> AcceptEncoding(<js>"gzip"</js>),
	 * 			<jk>new</jk> AcceptLanguage(<js>"da, en-gb;q=0.8, en;q=0.7"</js>)
	 * 		)
	 * 		.run();
	 * </p>
	 *
	 * @param headers
	 * 	The headers.
	 * 	The header values are converted to strings using the configured {@link HttpPartSerializer}.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest headers(HttpHeader...headers) throws RestCallException {
		for (HttpHeader h : headers)
			addHeader(h.getName(), stringify(h.getValue()));
		return this;
	}

	/**
	 * Sets the value for the <c>Accept</c> request header.
	 *
	 * <p>
	 * This overrides the media type specified on the parser, but is overridden by calling
	 * <code>header(<js>"Accept"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest accept(Object value) throws RestCallException {
		return header("Accept", value);
	}

	/**
	 * Sets the value for the <c>Accept-Charset</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Accept-Charset"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest acceptCharset(Object value) throws RestCallException {
		return header("Accept-Charset", value);
	}

	/**
	 * Sets the value for the <c>Accept-Encoding</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Accept-Encoding"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest acceptEncoding(Object value) throws RestCallException {
		return header("Accept-Encoding", value);
	}

	/**
	 * Sets the value for the <c>Accept-Language</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Accept-Language"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest acceptLanguage(Object value) throws RestCallException {
		return header("Accept-Language", value);
	}

	/**
	 * Sets the value for the <c>Authorization</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Authorization"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest authorization(Object value) throws RestCallException {
		return header("Authorization", value);
	}

	/**
	 * Sets the value for the <c>Cache-Control</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Cache-Control"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest cacheControl(Object value) throws RestCallException {
		return header("Cache-Control", value);
	}

	/**
	 * Sets the value for the <c>Connection</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Connection"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest connection(Object value) throws RestCallException {
		return header("Connection", value);
	}

	/**
	 * Sets the value for the <c>Content-Length</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Content-Length"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest contentLength(Object value) throws RestCallException {
		return header("Content-Length", value);
	}

	/**
	 * Sets the value for the <c>Content-Type</c> request header.
	 *
	 * <p>
	 * This overrides the media type specified on the serializer, but is overridden by calling
	 * <code>header(<js>"Content-Type"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest contentType(Object value) throws RestCallException {
		return header("Content-Type", value);
	}

	/**
	 * Sets the value for the <c>Date</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Date"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest date(Object value) throws RestCallException {
		return header("Date", value);
	}

	/**
	 * Sets the value for the <c>Expect</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Expect"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest expect(Object value) throws RestCallException {
		return header("Expect", value);
	}

	/**
	 * Sets the value for the <c>Forwarded</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Forwarded"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest forwarded(Object value) throws RestCallException {
		return header("Forwarded", value);
	}

	/**
	 * Sets the value for the <c>From</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"From"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest from(Object value) throws RestCallException {
		return header("From", value);
	}

	/**
	 * Sets the value for the <c>Host</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Host"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest host(Object value) throws RestCallException {
		return header("Host", value);
	}

	/**
	 * Sets the value for the <c>If-Match</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"If-Match"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest ifMatch(Object value) throws RestCallException {
		return header("If-Match", value);
	}

	/**
	 * Sets the value for the <c>If-Modified-Since</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"If-Modified-Since"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest ifModifiedSince(Object value) throws RestCallException {
		return header("If-Modified-Since", value);
	}

	/**
	 * Sets the value for the <c>If-None-Match</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"If-None-Match"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest ifNoneMatch(Object value) throws RestCallException {
		return header("If-None-Match", value);
	}

	/**
	 * Sets the value for the <c>If-Range</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"If-Range"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest ifRange(Object value) throws RestCallException {
		return header("If-Range", value);
	}

	/**
	 * Sets the value for the <c>If-Unmodified-Since</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"If-Unmodified-Since"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest ifUnmodifiedSince(Object value) throws RestCallException {
		return header("If-Unmodified-Since", value);
	}

	/**
	 * Sets the value for the <c>Max-Forwards</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Max-Forwards"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest maxForwards(Object value) throws RestCallException {
		return header("Max-Forwards", value);
	}

	/**
	 * Sets the value for the <c>Origin</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Origin"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest origin(Object value) throws RestCallException {
		return header("Origin", value);
	}

	/**
	 * Sets the value for the <c>Pragma</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Pragma"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest pragma(Object value) throws RestCallException {
		return header("Pragma", value);
	}

	/**
	 * Sets the value for the <c>Proxy-Authorization</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Proxy-Authorization"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest proxyAuthorization(Object value) throws RestCallException {
		return header("Proxy-Authorization", value);
	}

	/**
	 * Sets the value for the <c>Range</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Range"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest range(Object value) throws RestCallException {
		return header("Range", value);
	}

	/**
	 * Sets the value for the <c>Referer</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Referer"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest referer(Object value) throws RestCallException {
		return header("Referer", value);
	}

	/**
	 * Sets the value for the <c>TE</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"TE"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest te(Object value) throws RestCallException {
		return header("TE", value);
	}

	/**
	 * Sets the value for the <c>User-Agent</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"User-Agent"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest userAgent(Object value) throws RestCallException {
		return header("User-Agent", value);
	}

	/**
	 * Sets the value for the <c>Upgrade</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Upgrade"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest upgrade(Object value) throws RestCallException {
		return header("Upgrade", value);
	}

	/**
	 * Sets the value for the <c>Via</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Via"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest via(Object value) throws RestCallException {
		return header("Via", value);
	}

	/**
	 * Sets the value for the <c>Warning</c> request header.
	 *
	 * <p>
	 * This is a shortcut for calling <code>header(<js>"Warning"</js>, value);</code>
	 *
	 * @param value The new header value.
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest warning(Object value) throws RestCallException {
		return header("Warning", value);
	}

	/**
	 * Sets the client version by setting the value for the <js>"X-Client-Version"</js> header.
	 *
	 * @param value The version string (e.g. <js>"1.2.3"</js>)
	 * @return This object (for method chaining).
	 * @throws RestCallException Invalid input.
	 */
	public RestRequest clientVersion(Object value) throws RestCallException {
		return header("X-Client-Version", value);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Execution methods.
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Runs this request and returns the resulting response object.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jk>try</jk> {
	 * 		<jk>int</jk> rc = client.get(<jsf>URL</jsf>).execute().getResponseStatus();
	 * 		<jc>// Succeeded!</jc>
	 * 	} <jk>catch</jk> (RestCallException e) {
	 * 		<jc>// Failed!</jc>
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>Calling this method multiple times will return the same original response object.
	 * 	<li>You must close the returned object if you do not consume the response or execute a method that consumes
	 * 		the response.
	 * 	<li>If you are only interested in the response code, use the {@link #complete()} method which will automatically
	 * 		consume the response so that you don't need to call {@link InputStream#close()} on the response body.
	 * </ul>
	 *
	 * @return The response object.
	 * @throws RestCallException If an exception or non-200 response code occurred during the connection attempt.
	 */
	public RestResponse run() throws RestCallException {
		if (response != null)
			return response;

		try {
			HttpEntityEnclosingRequestBase request2 = request instanceof HttpEntityEnclosingRequestBase ? (HttpEntityEnclosingRequestBase)request : null;

			request.setURI(uriBuilder.build());

			if (hasInput || formData != null) {

				if (hasInput && formData != null)
					throw new RestCallException("Both input and form-data found on same request.");

				if (request2 == null)
					throw new RestCallException(0, "Method does not support content entity.", getMethod(), getURI(), null);

				HttpEntity entity = null;
				if (formData != null)
					entity = new UrlEncodedFormEntity(formData);
				else if (input instanceof NameValuePairs)
					entity = new UrlEncodedFormEntity((NameValuePairs)input);
				else if (input instanceof HttpEntity)
					entity = (HttpEntity)input;
				else if (input instanceof Reader)
					entity = new StringEntity(IOUtils.read((Reader)input), getRequestContentType(TEXT_PLAIN));
				else if (input instanceof InputStream)
					entity = new InputStreamEntity((InputStream)input, getRequestContentType(ContentType.APPLICATION_OCTET_STREAM));
				else if (input instanceof ReaderResource) {
					ReaderResource r = (ReaderResource)input;
					contentType(r.getMediaType());
					headers(r.getHeaders());
					entity = new StringEntity(IOUtils.read(r.getContents()), getRequestContentType(TEXT_PLAIN));
				}
				else if (input instanceof StreamResource) {
					StreamResource r = (StreamResource)input;
					contentType(r.getMediaType());
					headers(r.getHeaders());
					entity = new InputStreamEntity(r.getContents(), getRequestContentType(ContentType.APPLICATION_OCTET_STREAM));
				}
				else if (serializer != null)
					entity = new SerializedHttpEntity(input, serializer, requestBodySchema);
				else if (partSerializer != null)
					entity = new StringEntity(partSerializer.serialize((HttpPartSchema)null, input), getRequestContentType(TEXT_PLAIN));
				else
					entity = new StringEntity(getBeanContext().getClassMetaForObject(input).toString(input), getRequestContentType(TEXT_PLAIN));

				request2.setEntity(entity);
			}

			try {
				if (request2 != null)
					response = new RestResponse(client, this, client.execute(target, request2, context));
				else
					response = new RestResponse(client, this, client.execute(target, this.request, context));
			} catch (Exception e) {
				throw e;
			}

			for (RestCallInterceptor rci : interceptors)
				rci.onConnect(this, response);

			if (response.getStatusCode() == 0)
				throw new RestCallException("HttpClient returned a null response");

			String method = getMethod();
			int sc = response.getStatusCode();

			if (errorCodes.test(sc) && ! ignoreErrors) {
				throw new RestCallException(sc, response.getReasonPhrase(), method, getURI(), response.getBody().asAbbreviatedString(1000))
					.setServerException(response.getStringHeader("Exception-Name"), response.getStringHeader("Exception-Message"), response.getStringHeader("Exception-Trace"))
					.setRestResponse(response);
			}

		} catch (Exception e) {
			if (response != null)
				response.close();
			throw e instanceof RestCallException ? (RestCallException)e : new RestCallException(e).setRestResponse(response);
		}

		return this.response;
	}

	/**
	 * Same as {@link #run()} but allows you to run the call asynchronously.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	Future&lt;RestResponse&gt; f = client.get(<jsf>URL</jsf>).runFuture();
	 * 	<jc>// Do some other stuff</jc>
	 * 	<jk>try</jk> {
	 * 		String body = f.get().getBody().asString();
	 * 		<jc>// Succeeded!</jc>
	 * 	} <jk>catch</jk> (RestCallException e) {
	 * 		<jc>// Failed!</jc>
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>Use the {@link RestClientBuilder#executorService(ExecutorService, boolean)} method to customize the
	 * 		executor service used for creating {@link Future Futures}.
	 * </ul>
	 *
	 * @return The HTTP status code.
	 * @throws RestCallException If the executor service was not defined.
	 */
	public Future<RestResponse> runFuture() throws RestCallException {
		return client.getExecutorService(true).submit(
			new Callable<RestResponse>() {
				@Override /* Callable */
				public RestResponse call() throws Exception {
					return run();
				}
			}
		);
	}

	/**
	 * Same as {@link #run()} but immediately calls {@link RestResponse#consume()} to clean up the response.
	 *
	 * <p>
	 * Use this method if you're only interested in the status line of the response and not the response entity.
	 * Attempts to call any of the methods on the response object that retrieve the body (e.g. {@link RestResponseBody#asReader()}
	 * will cause a {@link RestCallException} to be thrown.
	 *
	 * <ul class='notes'>
	 * 	<li>You do not need to execute {@link InputStream#close()} on the response body to consume the response.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 *  <jc>// Get the response code.
	 *  // No need to call close() on the RestResponse object.</jc>
	 *  <jk>int</jk> rc = client.get(<jsf>URL</jsf>).complete().getResponseCode();
	 * </p>
	 *
	 * @return The response object.
	 * @throws RestCallException If an exception or non-200 response code occurred during the connection attempt.
	 */
	public RestResponse complete() throws RestCallException {
		return run().consume();
	}

	/**
	 * Same as {@link #complete()} but allows you to run the call asynchronously.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	Future&lt;RestResponse&gt; f = client.get(<jsf>URL</jsf>).completeFuture();
	 * 	<jc>// Do some other stuff</jc>
	 * 	<jk>int</jk> rc = f.get().getResponseStatus();
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>Use the {@link RestClientBuilder#executorService(ExecutorService, boolean)} method to customize the
	 * 		executor service used for creating {@link Future Futures}.
	 * 	<li>You do not need to execute {@link InputStream#close()} on the response body to consume the response.
	 * </ul>
	 *
	 * @return The HTTP status code.
	 * @throws RestCallException If the executor service was not defined.
	 */
	public Future<RestResponse> completeFuture() throws RestCallException {
		return client.getExecutorService(true).submit(
			new Callable<RestResponse>() {
				@Override /* Callable */
				public RestResponse call() throws Exception {
					return complete();
				}
			}
		);
	}

	/**
	 * Returns <jk>true</jk> if this request has a body.
	 *
	 * @return <jk>true</jk> if this request has a body.
	 */
	public boolean hasHttpEntity() {
		return request instanceof HttpEntityEnclosingRequestBase;
	}

	/**
	 * Returns the body of this request.
	 *
	 * @return The body of this request, or <jk>null</jk> if it doesn't have a body.
	 */
	public HttpEntity getHttpEntity() {
		return (request instanceof HttpEntityEnclosingRequestBase ? ((HttpEntityEnclosingRequestBase)request).getEntity() : null);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// HttpRequestBase pass-through methods.
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Sets the actual request configuration.
	 *
	 * @param value The new value.
	 * @return This object (for method chaining).
	 */
	public RestRequest setConfig(RequestConfig value) {
		request.setConfig(value);
		return this;
	}

	/**
	 * Sets {@link Cancellable} for the ongoing operation.
	 *
	 * @param cancellable The cancellable object.
	 * @return This object (for method chaining).
	 */
	public RestRequest setCancellable(Cancellable cancellable) {
		request.setCancellable(cancellable);
		return this;
	}

	/**
	 * Sets the protocol version for this request.
	 *
	 * @param version The protocol version for this request.
	 */
	public void setProtocolVersion(ProtocolVersion version) {
		request.setProtocolVersion(version);
	}

	/**
	 * Used in combination with {@link #setCancellable(Cancellable)}.
	 */
	public void completed() {
		request.completed();
	}

	// -----------------------------------------------------------------------------------------------------------------
	// HttpUriRequest pass-through methods.
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the HTTP method this request uses, such as GET, PUT, POST, or other.
	 *
	 * @return The HTTP method this request uses, such as GET, PUT, POST, or other.
	 */
	@Override /* HttpUriRequest */
	public String getMethod() {
		return request.getMethod();
	}

	/**
	 * Returns the original request URI.
	 *
	 * <ul class='notes'>
	 * 	<li>URI remains unchanged in the course of request execution and is not updated if the request is redirected to another location.
	 * </ul>
	 *
	 * @return The original request URI.
	 */
	@Override /* HttpUriRequest */
	public URI getURI() {
		return request.getURI();
	}

	/**
	 * Aborts this http request. Any active execution of this method should return immediately.
	 *
	 * If the request has not started, it will abort after the next execution.
	 * <br>Aborting this request will cause all subsequent executions with this request to fail.
	 */
	@Override /* HttpUriRequest */
	public void abort() throws UnsupportedOperationException {
		request.abort();
	}

	@Override /* HttpUriRequest */
	public boolean isAborted() {
		return request.isAborted();
	}

	/**
	 * Returns the request line of this request.
	 *
	 * @return The request line.
	 */
	@Override /* HttpRequest */
	public RequestLine getRequestLine() {
		return request.getRequestLine();
	}

	/**
	 * Returns the protocol version this message is compatible with.
	 *
	 * @return The protocol version.
	 */
	@Override /* HttpMessage */
	public ProtocolVersion getProtocolVersion() {
		return request.getProtocolVersion();
	}

	/**
	 * Checks if a certain header is present in this message.
	 *
	 * Header values are ignored.
	 *
	 * @param name The header name to check for.
	 * @return <jk>true</jk> if at least one header with this name is present.
	 */
	@Override /* HttpMessage */
	public boolean containsHeader(String name) {
		return request.containsHeader(name);
	}

	/**
	 * Returns all the headers with a specified name of this message.
	 *
	 * Header values are ignored.
	 * <br>Headers are ordered in the sequence they will be sent over a connection.
	 *
	 * @param name The name of the headers to return.
	 * @return The headers whose name property equals name.
	 */
	@Override /* HttpMessage */
	public Header[] getHeaders(String name) {
		return request.getHeaders(name);
	}

	/**
	 * Returns the first header with a specified name of this message.
	 *
	 * Header values are ignored.
	 * <br>If there is more than one matching header in the message the first element of {@link #getHeaders(String)} is returned.
	 * <br>If there is no matching header in the message <jk>null</jk> is returned.
	 *
	 * @param name The name of the header to return.
	 * @return The first header whose name property equals name or <jk>null</jk> if no such header could be found.
	 */
	@Override /* HttpMessage */
	public Header getFirstHeader(String name) {
		return request.getFirstHeader(name);
	}

	/**
	 * Returns the last header with a specified name of this message.
	 *
	 * Header values are ignored.
	 * <br>If there is more than one matching header in the message the last element of {@link #getHeaders(String)} is returned.
	 * <br>If there is no matching header in the message null is returned.
	 *
	 * @param name The name of the header to return.
	 * @return The last header whose name property equals name or <jk>null</jk> if no such header could be found.
	 */
	@Override /* HttpMessage */
	public Header getLastHeader(String name) {
		return request.getLastHeader(name);
	}

	/**
	 * Returns all the headers of this message.
	 *
	 * Headers are ordered in the sequence they will be sent over a connection.
	 *
	 * @return All the headers of this message
	 */
	@Override /* HttpMessage */
	public Header[] getAllHeaders() {
		return request.getAllHeaders();
	}

	/**
	 * Adds a header to this message.
	 *
	 * The header will be appended to the end of the list.
	 *
	 * <ul class='notes'>
	 * 	<li>{@link #header(Header)} is an equivalent method and the preferred method for fluent-style coding.
	 * </ul>
	 *
	 * @param header The header to append.
	 */
	@Override /* HttpMessage */
	public void addHeader(Header header) {
		request.addHeader(header);
	}

	/**
	 * Adds a header to this message.
	 *
	 * The header will be appended to the end of the list.
	 *
	 * <ul class='notes'>
	 * 	<li>{@link #header(String,Object)} is an equivalent method and the preferred method for fluent-style coding.
	 * </ul>
	 *
	 * @param name The name of the header.
	 * @param value The value of the header.
	 */
	@Override /* HttpMessage */
	public void addHeader(String name, String value) {
		request.addHeader(name, value);
	}

	/**
	 * Overwrites the first header with the same name.
	 *
	 * The new header will be appended to the end of the list, if no header with the given name can be found.
	 *
	 * @param header The header to set.
	 */
	@Override /* HttpMessage */
	public void setHeader(Header header) {
		request.setHeader(header);
	}

	/**
	 * Overwrites the first header with the same name.
	 *
	 * The new header will be appended to the end of the list, if no header with the given name can be found.
	 *
	 * @param name The name of the header.
	 * @param value The value of the header.
	 */
	@Override /* HttpMessage */
	public void setHeader(String name, String value) {
		request.setHeader(name, value);
	}

	/**
	 * Overwrites all the headers in the message.
	 *
	 * @param headers The array of headers to set.
	 */
	@Override /* HttpMessage */
	public void setHeaders(Header[] headers) {
		request.setHeaders(headers);
	}

	/**
	 * Removes a header from this message.
	 *
	 * @param header The header to remove.
	 */
	@Override /* HttpMessage */
	public void removeHeader(Header header) {
		request.removeHeader(header);
	}

	/**
	 * Removes all headers with a certain name from this message.
	 *
	 * @param name The name of the headers to remove.
	 */
	@Override /* HttpMessage */
	public void removeHeaders(String name) {
		request.removeHeaders(name);
	}

	/**
	 * Returns an iterator of all the headers.
	 *
	 * @return Iterator that returns {@link Header} objects in the sequence they are sent over a connection.
	 */
	@Override /* HttpMessage */
	public HeaderIterator headerIterator() {
		return request.headerIterator();
	}

	/**
	 * Returns an iterator of the headers with a given name.
	 *
	 * @param name the name of the headers over which to iterate, or <jk>null</jk> for all headers.
	 * @return Iterator that returns {@link Header} objects with the argument name in the sequence they are sent over a connection.
	 */
	@Override /* HttpMessage */
	public HeaderIterator headerIterator(String name) {
		return request.headerIterator(name);
	}

	/**
	 * Returns the parameters effective for this message as set by {@link #setParams(HttpParams)}.
	 *
	 * @return The parameters effective for this message as set by {@link #setParams(HttpParams)}.
	 * @deprecated Use constructor parameters of configuration API provided by HttpClient.
	 */
	@Override /* HttpMessage */
	@Deprecated
	public HttpParams getParams() {
		return request.getParams();
	}

	/**
	 * Provides parameters to be used for the processing of this message.
	 *
	 * @param params The parameters.
	 * @deprecated Use constructor parameters of configuration API provided by HttpClient.
	 */
	@Override /* HttpMessage */
	@Deprecated
	public void setParams(HttpParams params) {
		request.setParams(params);
	}

	/**
	 * Returns the actual request configuration.
	 *
	 * @return The actual request configuration.
	 */
	@Override /* Configurable */
	public RequestConfig getConfig() {
		return request.getConfig();
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Utility methods
	// -----------------------------------------------------------------------------------------------------------------

	private BeanContext getBeanContext() {
		BeanContext bc = serializer;
		if (bc == null)
			bc = BeanContext.DEFAULT;
		return bc;
	}

	private ContentType getRequestContentType(ContentType def) {
		Header h = request.getFirstHeader("Content-Type");
		if (h != null) {
			String s = h.getValue();
			if (! isEmpty(s))
				return ContentType.create(s);
		}
		return def;
	}

	@Override
	public ObjectMap getProperties() {
		return super.getProperties();
	}

	/**
	 * Specifies that the following value can be added as an HTTP part.
	 */
	private boolean canAdd(Object value, boolean skipIfEmpty) {
		if (value == null)
			return false;
		if (ObjectUtils.isEmpty(value) && skipIfEmpty)
			return false;
		return true;
	}

	private static String className(Object o) {
		return ClassInfo.of(o).getFullName();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Session */
	public ObjectMap toMap() {
		return super.toMap()
			.append("RestCall", new DefaultFilteringObjectMap()
				.append("client", client)
				.append("hasInput", hasInput)
				.append("ignoreErrors", ignoreErrors)
				.append("interceptors", interceptors)
				.append("partSerializer", partSerializer)
				.append("requestBodySchema", requestBodySchema)
				.append("response", response)
				.append("serializer", serializer)
			);
	}
}