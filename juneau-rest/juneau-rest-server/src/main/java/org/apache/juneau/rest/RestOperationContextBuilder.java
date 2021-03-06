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

import static java.util.Arrays.*;
import static org.apache.juneau.http.HttpHeaders.*;
import static org.apache.juneau.rest.HttpRuntimeException.*;
import static org.apache.juneau.rest.RestOperationContext.*;
import static org.apache.juneau.http.HttpParts.*;

import java.lang.annotation.*;
import java.util.*;
import java.util.function.*;

import org.apache.http.*;
import org.apache.juneau.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.header.*;
import org.apache.juneau.http.response.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.svl.*;
import java.lang.reflect.Method;

/**
 * Builder class for {@link RestOperationContext} objects.
 */
@FluentSetters
public class RestOperationContextBuilder extends BeanContextBuilder {

	RestContext restContext;
	Method restMethod;

	private BeanStore beanStore;
	private Class<? extends RestOperationContext> implClass;

	@Override /* BeanContextBuilder */
	public RestOperationContext build() {
		try {
			ContextProperties cp = getContextProperties();

			Class<? extends RestOperationContext> ic = implClass;
			if (ic == null)
				ic = cp.getClass(RESTOP_contextClass, RestOperationContext.class).orElse(getDefaultImplClass());

			return BeanStore.of(beanStore).addBean(RestOperationContextBuilder.class, this).createBean(ic);
		} catch (Exception e) {
			throw toHttpException(e, InternalServerError.class);
		}
	}

	/**
	 * Specifies the default implementation class if not specified via {@link #implClass(Class)}.
	 *
	 * @return The default implementation class if not specified via {@link #implClass(Class)}.
	 */
	protected Class<? extends RestOperationContext> getDefaultImplClass() {
		return RestOperationContext.class;
	}

	RestOperationContextBuilder(java.lang.reflect.Method method, RestContext context) {

		this.restContext = context;
		this.restMethod = method;
		this.beanStore = context.getRootBeanStore();

		MethodInfo mi = MethodInfo.of(context.getResourceClass(), method);

		try {

			VarResolver vr = context.getVarResolver();
			VarResolverSession vrs = vr.createSession();

			applyAnnotations(mi.getAnnotationList(ConfigAnnotationFilter.INSTANCE), vrs);

		} catch (Exception e) {
			throw toHttpException(e, InternalServerError.class);
		}
	}

	/**
	 * When enabled, append <js>"/*"</js> to path patterns if not already present.
	 *
	 * @return This object (for method chaining).
	 */
	public RestOperationContextBuilder dotAll() {
		set("RestOperationContext.dotAll.b", true);
		return this;
	}


	/**
	 * Specifies a {@link RestOperationContext} implementation subclass to use.
	 *
	 * <p>
	 * When specified, the {@link #build()} method will create an instance of that class instead of the default {@link RestOperationContext}.
	 *
	 * <p>
	 * The subclass must have a public constructor that takes in any of the following arguments:
	 * <ul>
	 * 	<li>{@link RestOperationContextBuilder} - This object.
	 * 	<li>Any beans found in the specified {@link #beanStore(BeanStore) bean store}.
	 * 	<li>Any {@link Optional} beans that may or may not be found in the specified {@link #beanStore(BeanStore) bean store}.
	 * </ul>
	 *
	 * @param implClass The implementation class to build.
	 * @return This object (for method chaining).
	 */
	public RestOperationContextBuilder implClass(Class<? extends RestOperationContext> implClass) {
		this.implClass = implClass;
		return this;
	}

	/**
	 * Specifies a {@link BeanStore} to use when resolving constructor arguments.
	 *
	 * @param beanStore The bean store to use for resolving constructor arguments.
	 * @return This object (for method chaining).
	 */
	public RestOperationContextBuilder beanStore(BeanStore beanStore) {
		this.beanStore = beanStore;
		return this;
	}

	//----------------------------------------------------------------------------------------------------
	// Properties
	//----------------------------------------------------------------------------------------------------

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Client version pattern matcher.
	 *
	 * <p>
	 * Specifies whether this method can be called based on the client version.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_clientVersion}
	 * </ul>
	 *
	 * @param value The new value for this setting.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder clientVersion(String value) {
		return set(RESTOP_clientVersion, value);
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  REST method context class.
	 *
	 * Allows you to extend the {@link RestOperationContext} class to modify how any of the methods are implemented.
	 *
	 * <p>
	 * The subclass must provide the following:
	 * <ul>
	 * 	<li>A public constructor that takes in one parameter that should be passed to the super constructor:  {@link RestOperationContextBuilder}.
	 * </ul>
	 *
	 * @param value The new value for this setting.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder contextClass(Class<? extends RestOperationContext> value) {
		return set(RESTOP_contextClass, value);
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Debug mode.
	 *
	 * <p>
	 * Enables debugging on this method.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_clientVersion}
	 * </ul>
	 *
	 * @param value The new value for this setting.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder debug(Enablement value) {
		return set(RESTOP_debug, value);
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default form data parameters.
	 *
	 * <p>
	 * Adds a single default form data parameter.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultFormData}
	 * </ul>
	 *
	 * @param name The form data parameter name.
	 * @param value The form data parameter value.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultFormData(String name, Object value) {
		return defaultFormData(basicPart(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default form data parameters.
	 *
	 * <p>
	 * Adds a single default form data parameter.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultFormData}
	 * </ul>
	 *
	 * @param name The form data parameter name.
	 * @param value The form data parameter value supplier.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultFormData(String name, Supplier<?> value) {
		return defaultFormData(basicPart(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default form data parameters.
	 *
	 * <p>
	 * Specifies default values for form data parameters if they're not specified in the request body.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultFormData}
	 * </ul>
	 *
	 * @param values The form data parameters to add.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultFormData(NameValuePair...values) {
		asList(values).stream().forEach(x -> appendTo(RESTOP_defaultFormData, x));
		return this;
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default query parameters.
	 *
	 * <p>
	 * Adds a single default query parameter.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultQuery}
	 * </ul>
	 *
	 * @param name The query parameter name.
	 * @param value The query parameter value.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultQuery(String name, Object value) {
		return defaultQuery(basicPart(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default query parameters.
	 *
	 * <p>
	 * Adds a single default query parameter.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultQuery}
	 * </ul>
	 *
	 * @param name The query parameter name.
	 * @param value The query parameter value supplier.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultQuery(String name, Supplier<?> value) {
		return defaultQuery(basicPart(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default query parameters.
	 *
	 * <p>
	 * Specifies default values for query parameters if they're not specified on the request.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultQuery}
	 * </ul>
	 *
	 * @param values The query parameters to add.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultQuery(NameValuePair...values) {
		asList(values).stream().forEach(x -> appendTo(RESTOP_defaultQuery, x));
		return this;
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default request attributes.
	 *
	 * <p>
	 * Adds a single default request attribute.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultRequestAttributes}
	 * </ul>
	 *
	 * @param name The attribute name.
	 * @param value The attribute value.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultRequestAttribute(String name, Object value) {
		return defaultRequestAttributes(BasicNamedAttribute.of(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default request attributes.
	 *
	 * <p>
	 * Adds a single default request attribute.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultRequestAttributes}
	 * </ul>
	 *
	 * @param name The attribute name.
	 * @param value The attribute value supplier.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultRequestAttribute(String name, Supplier<?> value) {
		return defaultRequestAttributes(BasicNamedAttribute.of(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default request attributes.
	 *
	 * <p>
	 * Adds multiple default request attributes.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultRequestAttributes}
	 * </ul>
	 *
	 * @param values The request attributes to add.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultRequestAttributes(NamedAttribute...values) {
		asList(values).stream().forEach(x -> appendTo(RESTOP_defaultRequestAttributes, x));
		return this;
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default request headers.
	 *
	 * <p>
	 * Adds a single default request header.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultRequestHeaders}
	 * </ul>
	 *
	 * @param name The request header name.
	 * @param value The request header value.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultRequestHeader(String name, String value) {
		return defaultRequestHeaders(stringHeader(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default request headers.
	 *
	 * <p>
	 * Adds a single default request header.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultRequestHeaders}
	 * </ul>
	 *
	 * @param name The request header name.
	 * @param value The request header value supplier.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultRequestHeader(String name, Supplier<String> value) {
		return defaultRequestHeaders(stringHeader(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default request headers.
	 *
	 * <p>
	 * Specifies default values for request headers if they're not passed in through the request.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultRequestHeaders}
	 * </ul>
	 *
	 * @param values The headers to add.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultRequestHeaders(Header...values) {
		asList(values).stream().forEach(x -> appendTo(RESTOP_defaultRequestHeaders, x));
		return this;
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default response headers.
	 *
	 * <p>
	 * Adds a single default response header.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultResponseHeaders}
	 * </ul>
	 *
	 * @param name The response header name.
	 * @param value The response header value.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultResponseHeader(String name, String value) {
		return defaultResponseHeaders(stringHeader(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default response headers.
	 *
	 * <p>
	 * Adds a single default response header.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultResponseHeaders}
	 * </ul>
	 *
	 * @param name The response header name.
	 * @param value The response header value supplier.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultResponseHeader(String name, Supplier<String> value) {
		return defaultResponseHeaders(stringHeader(name, value));
	}

	/**
	 * <i><l>RestOperationContext</l> configuration property:&emsp;</i>  Default response headers.
	 *
	 * <p>
	 * Specifies default values for response headers if they're not set after the Java REST method is called.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_defaultResponseHeaders}
	 * </ul>
	 *
	 * @param values The headers to add.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder defaultResponseHeaders(Header...values) {
		asList(values).stream().forEach(x -> appendTo(RESTOP_defaultResponseHeaders, x));
		return this;
	}

	/**
	 * Configuration property:  HTTP method name.
	 *
	 * <p>
	 * REST method name.
	 *
	 * <p>
	 * Typically <js>"GET"</js>, <js>"PUT"</js>, <js>"POST"</js>, <js>"DELETE"</js>, or <js>"OPTIONS"</js>.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_httpMethod}
	 * </ul>
	 *
	 * @param value The new value for this setting.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder httpMethod(String value) {
		return set(RESTOP_httpMethod, value);
	}

	/**
	 * Configuration property:  Method-level matchers.
	 *
	 * <p>
	 * Associates one or more {@link RestMatcher RestMatchers} with the specified method.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_matchers}
	 * </ul>
	 *
	 * @param values The new values for this setting.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder matchers(RestMatcher...values) {
		return set(RESTOP_matchers, values);
	}

	/**
	 * Configuration property:  Resource method paths.
	 *
	 * <p>
	 * Identifies the URL subpath relative to the servlet class.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestOperationContext#RESTOP_path}
	 * </ul>
	 *
	 * @param values The new values for this setting.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public RestOperationContextBuilder path(String...values) {
		return set(RESTOP_path, values);
	}

	// <FluentSetters>

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder add(Map<String,Object> properties) {
		super.add(properties);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder addTo(String name, Object value) {
		super.addTo(name, value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder appendTo(String name, Object value) {
		super.appendTo(name, value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder apply(ContextProperties copyFrom) {
		super.apply(copyFrom);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder applyAnnotations(java.lang.Class<?>...fromClasses) {
		super.applyAnnotations(fromClasses);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder applyAnnotations(Method...fromMethods) {
		super.applyAnnotations(fromMethods);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder applyAnnotations(AnnotationList al, VarResolverSession r) {
		super.applyAnnotations(al, r);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder debug() {
		super.debug();
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder locale(Locale value) {
		super.locale(value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder mediaType(MediaType value) {
		super.mediaType(value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder prependTo(String name, Object value) {
		super.prependTo(name, value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder putAllTo(String name, Object value) {
		super.putAllTo(name, value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder putTo(String name, String key, Object value) {
		super.putTo(name, key, value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder removeFrom(String name, Object value) {
		super.removeFrom(name, value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder set(String name) {
		super.set(name);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder set(Map<String,Object> properties) {
		super.set(properties);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder set(String name, Object value) {
		super.set(name, value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder timeZone(TimeZone value) {
		super.timeZone(value);
		return this;
	}

	@Override /* GENERATED - ContextBuilder */
	public RestOperationContextBuilder unset(String name) {
		super.unset(name);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder annotations(Annotation...values) {
		super.annotations(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanClassVisibility(Visibility value) {
		super.beanClassVisibility(value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanConstructorVisibility(Visibility value) {
		super.beanConstructorVisibility(value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanFieldVisibility(Visibility value) {
		super.beanFieldVisibility(value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanInterceptor(Class<?> on, Class<? extends org.apache.juneau.transform.BeanInterceptor<?>> value) {
		super.beanInterceptor(on, value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanMapPutReturnsOldValue() {
		super.beanMapPutReturnsOldValue();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanMethodVisibility(Visibility value) {
		super.beanMethodVisibility(value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanProperties(Map<String,Object> values) {
		super.beanProperties(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanProperties(Class<?> beanClass, String properties) {
		super.beanProperties(beanClass, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanProperties(String beanClassName, String properties) {
		super.beanProperties(beanClassName, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesExcludes(Map<String,Object> values) {
		super.beanPropertiesExcludes(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesExcludes(Class<?> beanClass, String properties) {
		super.beanPropertiesExcludes(beanClass, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesExcludes(String beanClassName, String properties) {
		super.beanPropertiesExcludes(beanClassName, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesReadOnly(Map<String,Object> values) {
		super.beanPropertiesReadOnly(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesReadOnly(Class<?> beanClass, String properties) {
		super.beanPropertiesReadOnly(beanClass, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesReadOnly(String beanClassName, String properties) {
		super.beanPropertiesReadOnly(beanClassName, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesWriteOnly(Map<String,Object> values) {
		super.beanPropertiesWriteOnly(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesWriteOnly(Class<?> beanClass, String properties) {
		super.beanPropertiesWriteOnly(beanClass, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beanPropertiesWriteOnly(String beanClassName, String properties) {
		super.beanPropertiesWriteOnly(beanClassName, properties);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beansRequireDefaultConstructor() {
		super.beansRequireDefaultConstructor();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beansRequireSerializable() {
		super.beansRequireSerializable();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder beansRequireSettersForGetters() {
		super.beansRequireSettersForGetters();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder dictionary(Object...values) {
		super.dictionary(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder dictionaryOn(Class<?> on, java.lang.Class<?>...values) {
		super.dictionaryOn(on, values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder disableBeansRequireSomeProperties() {
		super.disableBeansRequireSomeProperties();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder disableIgnoreMissingSetters() {
		super.disableIgnoreMissingSetters();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder disableIgnoreTransientFields() {
		super.disableIgnoreTransientFields();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder disableIgnoreUnknownNullBeanProperties() {
		super.disableIgnoreUnknownNullBeanProperties();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder disableInterfaceProxies() {
		super.disableInterfaceProxies();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public <T> RestOperationContextBuilder example(Class<T> pojoClass, T o) {
		super.example(pojoClass, o);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public <T> RestOperationContextBuilder example(Class<T> pojoClass, String json) {
		super.example(pojoClass, json);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder findFluentSetters() {
		super.findFluentSetters();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder findFluentSetters(Class<?> on) {
		super.findFluentSetters(on);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder ignoreInvocationExceptionsOnGetters() {
		super.ignoreInvocationExceptionsOnGetters();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder ignoreInvocationExceptionsOnSetters() {
		super.ignoreInvocationExceptionsOnSetters();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder ignoreUnknownBeanProperties() {
		super.ignoreUnknownBeanProperties();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder implClass(Class<?> interfaceClass, Class<?> implClass) {
		super.implClass(interfaceClass, implClass);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder implClasses(Map<Class<?>,Class<?>> values) {
		super.implClasses(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder interfaceClass(Class<?> on, Class<?> value) {
		super.interfaceClass(on, value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder interfaces(java.lang.Class<?>...value) {
		super.interfaces(value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder notBeanClasses(Object...values) {
		super.notBeanClasses(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder notBeanPackages(Object...values) {
		super.notBeanPackages(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder propertyNamer(Class<? extends org.apache.juneau.PropertyNamer> value) {
		super.propertyNamer(value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder propertyNamer(Class<?> on, Class<? extends org.apache.juneau.PropertyNamer> value) {
		super.propertyNamer(on, value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder sortProperties() {
		super.sortProperties();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder sortProperties(java.lang.Class<?>...on) {
		super.sortProperties(on);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder stopClass(Class<?> on, Class<?> value) {
		super.stopClass(on, value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder swaps(Object...values) {
		super.swaps(values);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder typeName(Class<?> on, String value) {
		super.typeName(on, value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder typePropertyName(String value) {
		super.typePropertyName(value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder typePropertyName(Class<?> on, String value) {
		super.typePropertyName(on, value);
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder useEnumNames() {
		super.useEnumNames();
		return this;
	}

	@Override /* GENERATED - BeanContextBuilder */
	public RestOperationContextBuilder useJavaBeanIntrospector() {
		super.useJavaBeanIntrospector();
		return this;
	}

	// </FluentSetters>
}