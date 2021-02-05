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
package org.apache.juneau.rest.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static org.apache.juneau.internal.ArrayUtils.*;
import static org.apache.juneau.rest.RestContext.*;
import static org.apache.juneau.rest.RestOperationContext.*;
import static org.apache.juneau.rest.util.RestUtils.*;

import java.lang.annotation.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.http.*;
import org.apache.juneau.http.header.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.svl.*;

/**
 * Utility classes and methods for the {@link RestOp @RestOp} annotation.
 */
public class RestOpAnnotation {


	/** Default value */
	public static final RestOp DEFAULT = create().build();

	/**
	 * Instantiates a new builder for this class.
	 *
	 * @return A new builder object.
	 */
	public static Builder create() {
		return new Builder();
	}

	/**
	 * Builder class for the {@link RestOp} annotation.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link BeanContextBuilder#annotations(Annotation...)}
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	public static class Builder extends TargetedAnnotationMBuilder {

		Class<? extends RestConverter>[] converters = new Class[0];
		Class<? extends RestGuard>[] guards = new Class[0];
		Class<? extends RestMatcher>[] matchers = new Class[0];
		Class<? extends RestOperationContext> contextClass = RestOperationContext.Null.class;
		Class<?>[] encoders=new Class<?>[0], parsers=new Class<?>[0], serializers=new Class<?>[0];
		int priority = 0;
		OpSwagger swagger = OpSwaggerAnnotation.DEFAULT;
		String clientVersion="", debug="", defaultAccept="", defaultCharset="", defaultContentType="", maxInput="", method="", rolesDeclared="", roleGuard="", summary="", value="";
		String[] consumes={}, defaultFormData={}, defaultQuery={}, defaultRequestAttributes={}, defaultRequestHeaders={}, defaultResponseHeaders={}, description={}, path={}, produces={};

		/**
		 * Constructor.
		 */
		public Builder() {
			super(RestOp.class);
		}

		/**
		 * Instantiates a new {@link RestOp @RestOp} object initialized with this builder.
		 *
		 * @return A new {@link RestOp @RestOp} object.
		 */
		public RestOp build() {
			return new Impl(this);
		}

		/**
		 * Sets the {@link RestOp#clientVersion()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder clientVersion(String value) {
			this.clientVersion = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#consumes()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder consumes(String...value) {
			this.consumes = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#contextClass()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder contextClass(Class<? extends RestOperationContext> value) {
			this.contextClass = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#converters()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder converters(Class<? extends RestConverter>...value) {
			this.converters = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#debug()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder debug(String value) {
			this.debug = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultAccept()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultAccept(String value) {
			this.defaultAccept = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultCharset()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultCharset(String value) {
			this.defaultCharset = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultContentType()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultContentType(String value) {
			this.defaultContentType = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultFormData()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultFormData(String...value) {
			this.defaultFormData = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultQuery()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultQuery(String...value) {
			this.defaultQuery = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultRequestAttributes()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultRequestAttributes(String...value) {
			this.defaultRequestAttributes = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultRequestHeaders()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultRequestHeaders(String...value) {
			this.defaultRequestHeaders = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#defaultResponseHeaders()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder defaultResponseHeaders(String...value) {
			this.defaultResponseHeaders = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#description()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder description(String...value) {
			this.description = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#encoders()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder encoders(Class<?>...value) {
			this.encoders = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#guards()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder guards(Class<? extends RestGuard>...value) {
			this.guards = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#matchers()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder matchers(Class<? extends RestMatcher>...value) {
			this.matchers = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#maxInput()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder maxInput(String value) {
			this.maxInput = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#method()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder method(String value) {
			this.method = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#parsers()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder parsers(Class<?>...value) {
			this.parsers = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#path()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder path(String...value) {
			this.path = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#priority()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder priority(int value) {
			this.priority = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#produces()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder produces(String...value) {
			this.produces = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#roleGuard()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder roleGuard(String value) {
			this.roleGuard = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#rolesDeclared()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder rolesDeclared(String value) {
			this.rolesDeclared = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#serializers()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder serializers(Class<?>...value) {
			this.serializers = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#summary()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder summary(String value) {
			this.summary = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#swagger()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder swagger(OpSwagger value) {
			this.swagger = value;
			return this;
		}

		/**
		 * Sets the {@link RestOp#value()} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder value(String value) {
			this.value = value;
			return this;
		}

		// <FluentSetters>

		@Override /* GENERATED - TargetedAnnotationBuilder */
		public Builder on(String...values) {
			super.on(values);
			return this;
		}

		@Override /* GENERATED - TargetedAnnotationTMBuilder */
		public Builder on(java.lang.reflect.Method...value) {
			super.on(value);
			return this;
		}

		// </FluentSetters>
	}

	private static class Impl extends TargetedAnnotationImpl implements RestOp {

		private final Class<? extends RestConverter>[] converters;
		private final Class<? extends RestGuard>[] guards;
		private final Class<? extends RestMatcher>[] matchers;
		private final Class<? extends RestOperationContext> contextClass;
		private final Class<?>[] encoders, parsers, serializers;
		private final int priority;
		private final OpSwagger swagger;
		private final String clientVersion, debug, defaultAccept, defaultCharset, defaultContentType, maxInput, method, rolesDeclared, roleGuard, summary, value;
		private final String[] consumes, defaultFormData, defaultQuery, defaultRequestAttributes, defaultRequestHeaders, defaultResponseHeaders, description, path, produces;

		Impl(Builder b) {
			super(b);
			this.clientVersion = b.clientVersion;
			this.consumes = copyOf(b.consumes);
			this.contextClass = b.contextClass;
			this.converters = copyOf(b.converters);
			this.debug = b.debug;
			this.defaultAccept = b.defaultAccept;
			this.defaultCharset = b.defaultCharset;
			this.defaultContentType = b.defaultContentType;
			this.defaultFormData = copyOf(b.defaultFormData);
			this.defaultQuery = copyOf(b.defaultQuery);
			this.defaultRequestAttributes = copyOf(b.defaultRequestAttributes);
			this.defaultRequestHeaders = copyOf(b.defaultRequestHeaders);
			this.defaultResponseHeaders = copyOf(b.defaultResponseHeaders);
			this.description = copyOf(b.description);
			this.encoders = copyOf(b.encoders);
			this.guards = copyOf(b.guards);
			this.matchers = copyOf(b.matchers);
			this.maxInput = b.maxInput;
			this.method = b.method;
			this.parsers = copyOf(b.parsers);
			this.path = copyOf(b.path);
			this.priority = b.priority;
			this.produces = copyOf(b.produces);
			this.roleGuard = b.roleGuard;
			this.rolesDeclared = b.rolesDeclared;
			this.serializers = copyOf(b.serializers);
			this.summary = b.summary;
			this.swagger = b.swagger;
			this.value = b.value;
			postConstruct();
		}

		@Override /* RestOp */
		public String clientVersion() {
			return clientVersion;
		}

		@Override /* RestOp */
		public String[] consumes() {
			return consumes;
		}

		@Override /* RestOp */
		public Class<? extends RestOperationContext> contextClass() {
			return contextClass;
		}

		@Override /* RestOp */
		public Class<? extends RestConverter>[] converters() {
			return converters;
		}

		@Override /* RestOp */
		public String debug() {
			return debug;
		}

		@Override /* RestOp */
		public String defaultAccept() {
			return defaultAccept;
		}

		@Override /* RestOp */
		public String defaultCharset() {
			return defaultCharset;
		}

		@Override /* RestOp */
		public String defaultContentType() {
			return defaultContentType;
		}

		@Override /* RestOp */
		public String[] defaultFormData() {
			return defaultFormData;
		}

		@Override /* RestOp */
		public String[] defaultQuery() {
			return defaultQuery;
		}

		@Override /* RestOp */
		public String[] defaultRequestAttributes() {
			return defaultRequestAttributes;
		}

		@Override /* RestOp */
		public String[] defaultRequestHeaders() {
			return defaultRequestHeaders;
		}

		@Override /* RestOp */
		public String[] defaultResponseHeaders() {
			return defaultResponseHeaders;
		}

		@Override /* RestOp */
		public String[] description() {
			return description;
		}

		@Override /* RestOp */
		public Class<?>[] encoders() {
			return encoders;
		}

		@Override /* RestOp */
		public Class<? extends RestGuard>[] guards() {
			return guards;
		}

		@Override /* RestOp */
		public Class<? extends RestMatcher>[] matchers() {
			return matchers;
		}

		@Override /* RestOp */
		public String maxInput() {
			return maxInput;
		}

		@Override /* RestOp */
		public String method() {
			return method;
		}

		@Override /* RestOp */
		public Class<?>[] parsers() {
			return parsers;
		}

		@Override /* RestOp */
		public String[] path() {
			return path;
		}

		@Override /* RestOp */
		public int priority() {
			return priority;
		}

		@Override /* RestOp */
		public String[] produces() {
			return produces;
		}

		@Override /* RestOp */
		public String roleGuard() {
			return roleGuard;
		}

		@Override /* RestOp */
		public String rolesDeclared() {
			return rolesDeclared;
		}

		@Override /* RestOp */
		public Class<?>[] serializers() {
			return serializers;
		}

		@Override /* RestOp */
		public String summary() {
			return summary;
		}

		@Override /* RestOp */
		public OpSwagger swagger() {
			return swagger;
		}

		@Override /* RestOp */
		public String value() {
			return value;
		}
	}

	/**
	 * Applies {@link RestOp} annotations to a {@link PropertyStoreBuilder}.
	 */
	public static class Apply extends ConfigApply<RestOp> {

		/**
		 * Constructor.
		 *
		 * @param c The annotation class.
		 * @param vr The resolver for resolving values in annotations.
		 */
		public Apply(Class<RestOp> c, VarResolverSession vr) {
			super(c, vr);
		}

		@Override
		public void apply(AnnotationInfo<RestOp> ai, PropertyStoreBuilder psb, VarResolverSession vr) {
			RestOp a = ai.getAnnotation();

			psb.set(REST_serializers, merge(ConverterUtils.toType(psb.peek(REST_serializers), Object[].class), a.serializers()));
			psb.set(REST_parsers, merge(ConverterUtils.toType(psb.peek(REST_parsers), Object[].class), a.parsers()));
			psb.set(REST_encoders, merge(ConverterUtils.toType(psb.peek(REST_encoders), Object[].class), a.encoders()));
			psb.setIf(a.contextClass() != RestOperationContext.Null.class, RESTOP_contextClass, a.contextClass());
			psb.setIfNotEmpty(REST_produces, stringList(a.produces()));
			psb.setIfNotEmpty(REST_consumes, stringList(a.consumes()));
			stringStream(a.defaultRequestHeaders()).map(x -> BasicHeader.ofPair(x)).forEach(x -> psb.appendTo(RESTOP_defaultRequestHeaders, x));
			stringStream(a.defaultResponseHeaders()).map(x -> BasicHeader.ofPair(x)).forEach(x -> psb.appendTo(RESTOP_defaultResponseHeaders, x));
			stringStream(a.defaultRequestAttributes()).map(x -> BasicNamedAttribute.ofPair(x)).forEach(x -> psb.appendTo(RESTOP_defaultRequestAttributes, x));
			stringStream(a.defaultQuery()).map(x -> BasicNameValuePair.ofPair(x)).forEach(x -> psb.appendTo(RESTOP_defaultQuery, x));
			stringStream(a.defaultFormData()).map(x -> BasicNameValuePair.ofPair(x)).forEach(x -> psb.appendTo(RESTOP_defaultFormData, x));
			psb.appendToIfNotEmpty(REST_defaultRequestHeaders, Accept.of(string(a.defaultAccept())));
			psb.appendToIfNotEmpty(REST_defaultRequestHeaders, ContentType.of(string(a.defaultContentType())));
			psb.prependTo(REST_converters, a.converters());
			psb.prependTo(REST_guards, reverse(a.guards()));
			psb.prependTo(RESTOP_matchers, a.matchers());
			psb.setIfNotEmpty(RESTOP_clientVersion, a.clientVersion());
			psb.setIfNotEmpty(REST_defaultCharset, string(a.defaultCharset()));
			psb.setIfNotEmpty(REST_maxInput, string(a.maxInput()));
			stringStream(a.path()).forEach(x -> psb.prependTo(RESTOP_path, x));
			cdStream(a.rolesDeclared()).forEach(x -> psb.addTo(REST_rolesDeclared, x));
			psb.addToIfNotEmpty(REST_roleGuard, string(a.roleGuard()));
			psb.setIfNotEmpty(RESTOP_httpMethod, string(a.method()));
			psb.setIf(a.priority() != 0, RESTOP_priority, a.priority());
			psb.setIfNotEmpty(RESTOP_debug, string(a.debug()));

			String v = StringUtils.trim(string(a.value()));
			if (v != null) {
				int i = v.indexOf(' ');
				if (i == -1) {
					psb.set(RESTOP_httpMethod, v);
				} else {
					psb.set(RESTOP_httpMethod, v.substring(0, i).trim());
					psb.prependTo(RESTOP_path,  v.substring(i).trim());
				}
			}
		}
	}

	/**
	 * A collection of {@link RestOp @RestOp annotations}.
	 */
	@Documented
	@Target({METHOD,TYPE})
	@Retention(RUNTIME)
	@Inherited
	public static @interface Array {

		/**
		 * The child annotations.
		 */
		RestOp[] value();
	}
}