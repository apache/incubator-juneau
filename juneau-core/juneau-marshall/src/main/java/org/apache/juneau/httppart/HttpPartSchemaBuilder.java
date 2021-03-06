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
package org.apache.juneau.httppart;

import static org.apache.juneau.internal.ClassUtils.*;
import static org.apache.juneau.internal.ExceptionUtils.*;
import static org.apache.juneau.internal.StringUtils.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.jsonschema.annotation.*;
import org.apache.juneau.reflect.*;

/**
 * The builder class for creating {@link HttpPartSchema} objects.
 *
 */
public class HttpPartSchemaBuilder {
	String name, _default;
	Set<Integer> codes;
	Set<String> _enum;
	Boolean allowEmptyValue, exclusiveMaximum, exclusiveMinimum, required, uniqueItems, skipIfEmpty;
	HttpPartCollectionFormat collectionFormat = HttpPartCollectionFormat.NO_COLLECTION_FORMAT;
	HttpPartDataType type = HttpPartDataType.NO_TYPE;
	HttpPartFormat format = HttpPartFormat.NO_FORMAT;
	Pattern pattern;
	Number maximum, minimum, multipleOf;
	Long maxLength, minLength, maxItems, minItems, maxProperties, minProperties;
	Map<String,Object> properties;
	Object items, additionalProperties;
	boolean noValidate;
	Class<? extends HttpPartParser> parser;
	Class<? extends HttpPartSerializer> serializer;

	/**
	 * Instantiates a new {@link HttpPartSchema} object based on the configuration of this builder.
	 *
	 * <p>
	 * This method can be called multiple times to produce new schema objects.
	 *
	 * @return
	 * 	A new {@link HttpPartSchema} object.
	 * 	<br>Never <jk>null</jk>.
	 */
	public HttpPartSchema build() {
		return new HttpPartSchema(this);
	}

	HttpPartSchemaBuilder apply(Class<? extends Annotation> c, ParamInfo mpi) {
		apply(c, mpi.getParameterType().innerType());
		for (Annotation a : mpi.getDeclaredAnnotations())
			if (c.isInstance(a))
				apply(a);
		return this;
	}

	HttpPartSchemaBuilder apply(Class<? extends Annotation> c, Method m) {
		apply(c, m.getGenericReturnType());
		Annotation a = m.getAnnotation(c);
		if (a != null)
			return apply(a);
		return this;
	}

	HttpPartSchemaBuilder apply(Class<? extends Annotation> c, java.lang.reflect.Type t) {
		if (t instanceof Class<?>) {
			ClassInfo ci = ClassInfo.of((Class<?>)t);
			for (Annotation a : ci.getAnnotations(c))
				apply(a);
		} else if (Value.isType(t)) {
			apply(c, Value.getParameterType(t));
		}
		return this;
	}

	/**
	 * Apply the specified annotation to this schema.
	 *
	 * @param a The annotation to apply.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder apply(Annotation a) {
		if (a instanceof Body)
			apply((Body)a);
		else if (a instanceof Header)
			apply((Header)a);
		else if (a instanceof FormData)
			apply((FormData)a);
		else if (a instanceof Query)
			apply((Query)a);
		else if (a instanceof Path)
			apply((Path)a);
		else if (a instanceof Response)
			apply((Response)a);
		else if (a instanceof ResponseHeader)
			apply((ResponseHeader)a);
		else if (a instanceof HasQuery)
			apply((HasQuery)a);
		else if (a instanceof HasFormData)
			apply((HasFormData)a);
		else if (a instanceof Schema)
			apply((Schema)a);
		else
			throw runtimeException("HttpPartSchemaBuilder.apply(@{0}) not defined", className(a));
		return this;
	}

	HttpPartSchemaBuilder apply(Body a) {
		required(a.required() || a.r());
		allowEmptyValue(! (a.required() || a.r()));
		apply(a.schema());
		return this;
	}

	HttpPartSchemaBuilder apply(Header a) {
		name(firstNonEmpty(a.name(), a.n(), a.value()));
		required(a.required() || a.r());
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		allowEmptyValue(a.allowEmptyValue() || a.aev());
		items(a.items());
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		_default(joinnlOrNull(a._default(), a.df()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		skipIfEmpty(a.skipIfEmpty() || a.sie());
		parser(a.parser());
		serializer(a.serializer());
		return this;
	}

	HttpPartSchemaBuilder apply(ResponseHeader a) {
		name(firstNonEmpty(a.name(), a.n(), a.value()));
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		items(a.items());
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		_default(joinnlOrNull(a._default(), a.df()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		allowEmptyValue(false);
		serializer(a.serializer());
		return this;
	}

	HttpPartSchemaBuilder apply(FormData a) {
		name(firstNonEmpty(a.name(), a.n(), a.value()));
		required(a.required() || a.r());
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		allowEmptyValue(a.allowEmptyValue() || a.aev());
		items(a.items());
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		_default(joinnlOrNull(a._default(), a.df()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		skipIfEmpty(a.skipIfEmpty() || a.sie());
		parser(a.parser());
		serializer(a.serializer());
		return this;
	}

	HttpPartSchemaBuilder apply(Query a) {
		name(firstNonEmpty(a.name(), a.n(), a.value()));
		required(a.required() || a.r());
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		allowEmptyValue(a.allowEmptyValue() || a.aev());
		items(a.items());
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		_default(joinnlOrNull(a._default(), a.df()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		skipIfEmpty(a.skipIfEmpty() || a.sie());
		parser(a.parser());
		serializer(a.serializer());
		return this;
	}

	HttpPartSchemaBuilder apply(Path a) {
		name(firstNonEmpty(a.name(), a.n(), a.value()));
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		items(a.items());
		allowEmptyValue(a.allowEmptyValue() || a.aev());
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		parser(a.parser());
		serializer(a.serializer());

		// Path remainder always allows empty value.
		if (startsWith(name, '/')) {
			allowEmptyValue();
			required(false);
		} else {
			required(a.required() && a.r());
		}

		return this;
	}

	HttpPartSchemaBuilder apply(Response a) {
		codes(a.value());
		codes(a.code());
		required(false);
		allowEmptyValue(true);
		serializer(a.serializer());
		parser(a.parser());
		apply(a.schema());
		return this;
	}

	HttpPartSchemaBuilder apply(Items a) {
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		items(a.items());
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		_default(joinnlOrNull(a._default(), a.df()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		return this;
	}

	HttpPartSchemaBuilder apply(SubItems a) {
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		items(HttpPartSchema.toOMap(a.items()));
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		_default(joinnlOrNull(a._default(), a.df()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		return this;
	}

	HttpPartSchemaBuilder apply(Schema a) {
		type(firstNonEmpty(a.type(), a.t()));
		format(firstNonEmpty(a.format(), a.f()));
		items(a.items());
		collectionFormat(firstNonEmpty(a.collectionFormat(), a.cf()));
		_default(joinnlOrNull(a._default(), a.df()));
		maximum(toNumber(a.maximum(), a.max()));
		exclusiveMaximum(a.exclusiveMaximum() || a.emax());
		minimum(toNumber(a.minimum(), a.min()));
		exclusiveMinimum(a.exclusiveMinimum() || a.emin());
		maxLength(firstNmo(a.maxLength(), a.maxl()));
		minLength(firstNmo(a.minLength(), a.minl()));
		pattern(firstNonEmpty(a.pattern(), a.p()));
		maxItems(firstNmo(a.maxItems(), a.maxi()));
		minItems(firstNmo(a.minItems(), a.mini()));
		uniqueItems(a.uniqueItems() || a.ui());
		_enum(toSet(a._enum(), a.e()));
		multipleOf(toNumber(a.multipleOf(), a.mo()));
		maxProperties(firstNmo(a.maxProperties(), a.maxp()));
		minProperties(firstNmo(a.minProperties(), a.minp()));
		properties(HttpPartSchema.toOMap(a.properties()));
		additionalProperties(HttpPartSchema.toOMap(a.additionalProperties()));
		return this;
	}

	HttpPartSchemaBuilder apply(HasQuery a) {
		name(firstNonEmpty(a.name(), a.n(), a.value()));
		return this;
	}

	HttpPartSchemaBuilder apply(HasFormData a) {
		name(firstNonEmpty(a.name(), a.n(), a.value()));
		return this;
	}

	HttpPartSchemaBuilder apply(OMap m) {
		if (m != null && ! m.isEmpty()) {
			_default(m.getString("default"));
			_enum(HttpPartSchema.toSet(m.getString("enum")));
			allowEmptyValue(m.getBoolean("allowEmptyValue"));
			exclusiveMaximum(m.getBoolean("exclusiveMaximum"));
			exclusiveMinimum(m.getBoolean("exclusiveMinimum"));
			required(m.getBoolean("required"));
			uniqueItems(m.getBoolean("uniqueItems"));
			collectionFormat(m.getString("collectionFormat"));
			type(m.getString("type"));
			format(m.getString("format"));
			pattern(m.getString("pattern"));
			maximum(m.get("maximum", Number.class));
			minimum(m.get("minimum", Number.class));
			multipleOf(m.get("multipleOf", Number.class));
			maxItems(m.get("maxItems", Long.class));
			maxLength(m.get("maxLength", Long.class));
			maxProperties(m.get("maxProperties", Long.class));
			minItems(m.get("minItems", Long.class));
			minLength(m.get("minLength", Long.class));
			minProperties(m.get("minProperties", Long.class));

			items(m.getMap("items"));
			properties(m.getMap("properties"));
			additionalProperties(m.getMap("additionalProperties"));

			apply(m.getMap("schema", null));
		}
		return this;
	}

	/**
	 * <mk>name</mk> field.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder name(String value) {
		if (isNotEmpty(value))
			name = value;
		return this;
	}

	/**
	 * Synonym for {@link #name(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder n(String value) {
		return name(value);
	}

	/**
	 * <mk>httpStatusCode</mk> key.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerResponsesObject Responses}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if <jk>null</jk> or an empty array.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder codes(int[] value) {
		if (value != null && value.length != 0)
			for (int v : value)
				code(v);
		return this;
	}

	/**
	 * <mk>httpStatusCode</mk> key.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerResponsesObject Responses}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <c>0</c>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder code(int value) {
		if (value != 0) {
			if (codes == null)
				codes = new TreeSet<>();
			codes.add(value);
		}
		return this;
	}

	/**
	 * <mk>required</mk> field.
	 *
	 * <p>
	 * Determines whether the parameter is mandatory.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder required(Boolean value) {
		required = resolve(value, required);
		return this;
	}

	/**
	 * Synonym for {@link #required(Boolean)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder r(Boolean value) {
		return required(value);
	}

	/**
	 * <mk>required</mk> field.
	 *
	 * <p>
	 * Determines whether the parameter is mandatory.
	 *
	 * <p>
	 * Same as {@link #required(Boolean)} but takes in a boolean value as a string.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder required(String value) {
		required = resolve(value, required);
		return this;
	}

	/**
	 * Synonym for {@link #required(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder r(String value) {
		return required(value);
	}

	/**
	 * <mk>required</mk> field.
	 *
	 * <p>
	 * Shortcut for calling <code>required(<jk>true</jk>);</code>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder required() {
		return required(true);
	}

	/**
	 * Synonym for {@link #required()}.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder r() {
		return required();
	}

	/**
	 * <mk>type</mk> field.
	 *
	 * <p>
	 * The type of the parameter.
	 *
	 * <p>
	 * The possible values are:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		<js>"string"</js>
	 * 		<br>Parameter must be a string or a POJO convertible from a string.
	 * 	<li>
	 * 		<js>"number"</js>
	 * 		<br>Parameter must be a number primitive or number object.
	 * 		<br>If parameter is <c>Object</c>, creates either a <c>Float</c> or <c>Double</c> depending on the size of the number.
	 * 	<li>
	 * 		<js>"integer"</js>
	 * 		<br>Parameter must be a integer/long primitive or integer/long object.
	 * 		<br>If parameter is <c>Object</c>, creates either a <c>Short</c>, <c>Integer</c>, or <c>Long</c> depending on the size of the number.
	 * 	<li>
	 * 		<js>"boolean"</js>
	 * 		<br>Parameter must be a boolean primitive or object.
	 * 	<li>
	 * 		<js>"array"</js>
	 * 		<br>Parameter must be an array or collection.
	 * 		<br>Elements must be strings or POJOs convertible from strings.
	 * 		<br>If parameter is <c>Object</c>, creates an {@link OList}.
	 * 	<li>
	 * 		<js>"object"</js>
	 * 		<br>Parameter must be a map or bean.
	 * 		<br>If parameter is <c>Object</c>, creates an {@link OMap}.
	 * 		<br>Note that this is an extension of the OpenAPI schema as Juneau allows for arbitrarily-complex POJOs to be serialized as HTTP parts.
	 * 	<li>
	 * 		<js>"file"</js>
	 * 		<br>This type is currently not supported.
	 * </ul>
	 *
	 * <p>
	 * If the type is not specified, it will be auto-detected based on the parameter class type.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerSecuritySchemeObject SecurityScheme}
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='extlink'>{@doc ExtSwaggerDataTypes}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder type(String value) {
		try {
			if (isNotEmpty(value))
				type = HttpPartDataType.fromString(value);
		} catch (Exception e) {
			throw new ContextRuntimeException("Invalid value ''{0}'' passed in as type value.  Valid values: {1}", value, HttpPartDataType.values());
		}
		return this;
	}

	/**
	 * Synonym for {@link #type(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder t(String value) {
		return type(value);
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.STRING)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tString() {
		type = HttpPartDataType.STRING;
		return this;
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.NUMBER)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tNumber() {
		type = HttpPartDataType.NUMBER;
		return this;
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.INTEGER)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tInteger() {
		type = HttpPartDataType.INTEGER;
		return this;
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.BOOLEAN)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tBoolean() {
		type = HttpPartDataType.BOOLEAN;
		return this;
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.ARRAY)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tArray() {
		type = HttpPartDataType.ARRAY;
		return this;
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.OBJECT)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tObject() {
		type = HttpPartDataType.OBJECT;
		return this;
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.FILE)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tFile() {
		type = HttpPartDataType.FILE;
		return this;
	}

	/**
	 * Shortcut for <c>type(HttpPartDataType.NO_TYPE)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder tNone() {
		type = HttpPartDataType.NO_TYPE;
		return this;
	}

	/**
	 * <mk>type</mk> field.
	 *
	 * <p>
	 * The type of the parameter.
	 *
	 * <p>
	 * The possible values are:
	 * <ul class='javatree'>
	 * 	<li class='jc'>{@link HttpPartDataType}
	 * 	<ul>
	 * 		<li class='jf'>
	 * 			{@link HttpPartDataType#STRING STRING}
	 * 			<br>Parameter must be a string or a POJO convertible from a string.
	 * 			<li>
	 * 			{@link HttpPartDataType#NUMBER NUMBER}
	 * 			<br>Parameter must be a number primitive or number object.
	 * 			<br>If parameter is <c>Object</c>, creates either a <c>Float</c> or <c>Double</c> depending on the size of the number.
	 * 		<li class='jf'>
	 * 			{@link HttpPartDataType#INTEGER INTEGER}
	 * 			<br>Parameter must be a integer/long primitive or integer/long object.
	 * 			<br>If parameter is <c>Object</c>, creates either a <c>Short</c>, <c>Integer</c>, or <c>Long</c> depending on the size of the number.
	 * 		<li class='jf'>
	 * 			{@link HttpPartDataType#BOOLEAN BOOLEAN}
	 * 			<br>Parameter must be a boolean primitive or object.
	 * 		<li class='jf'>
	 * 			{@link HttpPartDataType#ARRAY ARRAY}
	 * 			<br>Parameter must be an array or collection.
	 * 			<br>Elements must be strings or POJOs convertible from strings.
	 * 			<br>If parameter is <c>Object</c>, creates an {@link OList}.
	 * 		<li class='jf'>
	 * 			{@link HttpPartDataType#OBJECT OBJECT}
	 * 			<br>Parameter must be a map or bean.
	 * 			<br>If parameter is <c>Object</c>, creates an {@link OMap}.
	 * 			<br>Note that this is an extension of the OpenAPI schema as Juneau allows for arbitrarily-complex POJOs to be serialized as HTTP parts.
	 * 		<li class='jf'>
	 * 			{@link HttpPartDataType#FILE FILE}
	 * 			<br>This type is currently not supported.
	 * 	</ul>
	 * </ul>
	 *
	 * <p>
	 * If the type is not specified, it will be auto-detected based on the parameter class type.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerSecuritySchemeObject SecurityScheme}
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='extlink'>{@doc ExtSwaggerDataTypes}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder type(HttpPartDataType value) {
		this.type = value;
		return this;
	}

	/**
	 * Synonym for {@link #type(HttpPartDataType)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder t(HttpPartDataType value) {
		return type(value);
	}

	/**
	 * <mk>format</mk> field.
	 *
	 * <p>
	 * The extending format for the previously mentioned {@doc ExtSwaggerParameterTypes parameter type}.
	 *
	 * <p>
	 * The possible values are:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		<js>"int32"</js> - Signed 32 bits.
	 * 		<br>Only valid with type <js>"integer"</js>.
	 * 	<li>
	 * 		<js>"int64"</js> - Signed 64 bits.
	 * 		<br>Only valid with type <js>"integer"</js>.
	 * 	<li>
	 * 		<js>"float"</js> - 32-bit floating point number.
	 * 		<br>Only valid with type <js>"number"</js>.
	 * 	<li>
	 * 		<js>"double"</js> - 64-bit floating point number.
	 * 		<br>Only valid with type <js>"number"</js>.
	 * 	<li>
	 * 		<js>"byte"</js> - BASE-64 encoded characters.
	 * 		<br>Only valid with type <js>"string"</js>.
	 * 		<br>Parameters of type POJO convertible from string are converted after the string has been decoded.
	 * 	<li>
	 * 		<js>"binary"</js> - Hexadecimal encoded octets (e.g. <js>"00FF"</js>).
	 * 		<br>Only valid with type <js>"string"</js>.
	 * 		<br>Parameters of type POJO convertible from string are converted after the string has been decoded.
	 * 	<li>
	 * 		<js>"binary-spaced"</js> - Hexadecimal encoded octets, spaced (e.g. <js>"00 FF"</js>).
	 * 		<br>Only valid with type <js>"string"</js>.
	 * 		<br>Parameters of type POJO convertible from string are converted after the string has been decoded.
	 * 	<li>
	 * 		<js>"date"</js> - An <a href='http://xml2rfc.ietf.org/public/rfc/html/rfc3339.html#anchor14'>RFC3339 full-date</a>.
	 * 		<br>Only valid with type <js>"string"</js>.
	 * 	<li>
	 * 		<js>"date-time"</js> - An <a href='http://xml2rfc.ietf.org/public/rfc/html/rfc3339.html#anchor14'>RFC3339 date-time</a>.
	 * 		<br>Only valid with type <js>"string"</js>.
	 * 	<li>
	 * 		<js>"password"</js> - Used to hint UIs the input needs to be obscured.
	 * 		<br>This format does not affect the serialization or parsing of the parameter.
	 * 	<li>
	 * 		<js>"uon"</js> - UON notation (e.g. <js>"(foo=bar,baz=@(qux,123))"</js>).
	 * 		<br>Only valid with type <js>"object"</js>.
	 * 		<br>If not specified, then the input is interpreted as plain-text and is converted to a POJO directly.
	 * </ul>
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='extlink'>{@doc ExtSwaggerDataTypeFormats}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or an empty string.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder format(String value) {
		try {
			if (isNotEmpty(value))
				format = HttpPartFormat.fromString(value);
		} catch (Exception e) {
			throw new ContextRuntimeException("Invalid value ''{0}'' passed in as format value.  Valid values: {1}", value, HttpPartFormat.values());
		}
		return this;
	}

	/**
	 * Synonym for {@link #format(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder f(String value) {
		return format(value);
	}

	/**
	 * <mk>format</mk> field.
	 *
	 * <p>
	 * The extending format for the previously mentioned {@doc ExtSwaggerParameterTypes parameter type}.
	 *
	 * <p>
	 * The possible values are:
	 * <ul class='javatree'>
	 * 	<ul class='jc'>{@link HttpPartFormat}
	 * 	<ul>
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#INT32 INT32} - Signed 32 bits.
	 * 			<br>Only valid with type <js>"integer"</js>.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#INT64 INT64} - Signed 64 bits.
	 * 			<br>Only valid with type <js>"integer"</js>.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#FLOAT FLOAT} - 32-bit floating point number.
	 * 			<br>Only valid with type <js>"number"</js>.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#DOUBLE DOUBLE} - 64-bit floating point number.
	 * 			<br>Only valid with type <js>"number"</js>.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#BYTE BYTE} - BASE-64 encoded characters.
	 * 			<br>Only valid with type <js>"string"</js>.
	 * 			<br>Parameters of type POJO convertible from string are converted after the string has been decoded.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#BINARY BINARY} - Hexadecimal encoded octets (e.g. <js>"00FF"</js>).
	 * 			<br>Only valid with type <js>"string"</js>.
	 * 			<br>Parameters of type POJO convertible from string are converted after the string has been decoded.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#BINARY_SPACED BINARY_SPACED} - Hexadecimal encoded octets, spaced (e.g. <js>"00 FF"</js>).
	 * 			<br>Only valid with type <js>"string"</js>.
	 * 			<br>Parameters of type POJO convertible from string are converted after the string has been decoded.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#DATE DATE} - An <a href='http://xml2rfc.ietf.org/public/rfc/html/rfc3339.html#anchor14'>RFC3339 full-date</a>.
	 * 			<br>Only valid with type <js>"string"</js>.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#DATE_TIME DATE_TIME} - An <a href='http://xml2rfc.ietf.org/public/rfc/html/rfc3339.html#anchor14'>RFC3339 date-time</a>.
	 * 			<br>Only valid with type <js>"string"</js>.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#PASSWORD PASSWORD} - Used to hint UIs the input needs to be obscured.
	 * 			<br>This format does not affect the serialization or parsing of the parameter.
	 * 		<li class='jf'>
	 * 			{@link HttpPartFormat#UON UON} - UON notation (e.g. <js>"(foo=bar,baz=@(qux,123))"</js>).
	 * 			<br>Only valid with type <js>"object"</js>.
	 * 			<br>If not specified, then the input is interpreted as plain-text and is converted to a POJO directly.
	 * 	</ul>
	 * </ul>
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='extlink'>{@doc ExtSwaggerDataTypeFormats}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder format(HttpPartFormat value) {
		format = value;
		return this;
	}

	/**
	 * Synonym for {@link #format(HttpPartFormat)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder f(HttpPartFormat value) {
		return format(value);
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.INT32)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fInt32() {
		format = HttpPartFormat.INT32;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.INT64)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fInt64() {
		format = HttpPartFormat.INT64;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.FLOAT)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fFloat() {
		format = HttpPartFormat.FLOAT;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.DOUBLE)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fDouble() {
		format = HttpPartFormat.DOUBLE;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.BYTE)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fByte() {
		format = HttpPartFormat.BYTE;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.BINARY)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fBinary() {
		format = HttpPartFormat.BINARY;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.BINARY_SPACED)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fBinarySpaced() {
		format = HttpPartFormat.BINARY_SPACED;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.DATE)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fDate() {
		format = HttpPartFormat.DATE;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.DATE_TIME)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fDateTime() {
		format = HttpPartFormat.DATE_TIME;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.PASSWORD)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fPassword() {
		format = HttpPartFormat.PASSWORD;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.UON)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fUon() {
		format = HttpPartFormat.UON;
		return this;
	}

	/**
	 * Shortcut for <c>format(HttpPartFormat.NO_FORMAT)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder fNone() {
		format = HttpPartFormat.NO_FORMAT;
		return this;
	}

	/**
	 * <mk>allowEmptyValue</mk> field.
	 *
	 * <p>
	 * Sets the ability to pass empty-valued parameters.
	 * <br>This is valid only for either query or formData parameters and allows you to send a parameter with a name only or an empty value.
	 * <br>The default value is <jk>false</jk>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder allowEmptyValue(Boolean value) {
		allowEmptyValue = resolve(value, allowEmptyValue);
		return this;
	}

	/**
	 * Synonym for {@link #allowEmptyValue(Boolean)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder aev(Boolean value) {
		return allowEmptyValue(value);
	}

	/**
	 * <mk>allowEmptyValue</mk> field.
	 *
	 * <p>
	 * Same as {@link #allowEmptyValue(Boolean)} but takes in a string boolean value.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder allowEmptyValue(String value) {
		allowEmptyValue = resolve(value, allowEmptyValue);
		return this;
	}

	/**
	 * Synonym for {@link #allowEmptyValue(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder aev(String value) {
		return allowEmptyValue(value);
	}

	/**
	 * <mk>allowEmptyValue</mk> field.
	 *
	 * <p>
	 * Shortcut for calling <code>allowEmptyValue(<jk>true</jk>);</code>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder allowEmptyValue() {
		return allowEmptyValue(true);
	}

	/**
	 * Synonym for {@link #allowEmptyValue()}.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder aev() {
		return allowEmptyValue(true);
	}

	/**
	 * <mk>items</mk> field.
	 *
	 * <p>
	 * Describes the type of items in the array.
	 * <p>
	 * Required if <c>type</c> is <js>"array"</js>.
	 * <br>Can only be used if <c>type</c> is <js>"array"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder items(HttpPartSchemaBuilder value) {
		if (value != null)
			this.items = value;
		return this;
	}

	/**
	 * Synonym for {@link #items(HttpPartSchemaBuilder)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder i(HttpPartSchemaBuilder value) {
		return items(value);
	}

	/**
	 * <mk>items</mk> field.
	 *
	 * <p>
	 * Describes the type of items in the array.
	 * <p>
	 * Required if <c>type</c> is <js>"array"</js>.
	 * <br>Can only be used if <c>type</c> is <js>"array"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder items(HttpPartSchema value) {
		if (value != null)
			this.items = value;
		return this;
	}

	/**
	 * Synonym for {@link #items(HttpPartSchema)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder i(HttpPartSchema value) {
		return items(value);
	}

	HttpPartSchemaBuilder items(OMap value) {
		if (value != null && ! value.isEmpty())
			items = HttpPartSchema.create().apply(value);
		return this;
	}

	HttpPartSchemaBuilder items(Items value) {
		if (! ItemsAnnotation.empty(value))
			items = HttpPartSchema.create().apply(value);
		return this;
	}

	HttpPartSchemaBuilder items(SubItems value) {
		if (! SubItemsAnnotation.empty(value))
			items = HttpPartSchema.create().apply(value);
		return this;
	}


	/**
	 * <mk>collectionFormat</mk> field.
	 *
	 * <p>
	 * Determines the format of the array if <c>type</c> <js>"array"</js> is used.
	 * <br>Can only be used if <c>type</c> is <js>"array"</js>.
	 *
	 * <br>Possible values are:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		<js>"csv"</js> (default) - Comma-separated values (e.g. <js>"foo,bar"</js>).
	 * 	<li>
	 * 		<js>"ssv"</js> - Space-separated values (e.g. <js>"foo bar"</js>).
	 * 	<li>
	 * 		<js>"tsv"</js> - Tab-separated values (e.g. <js>"foo\tbar"</js>).
	 * 	<li>
	 * 		<js>"pipes</js> - Pipe-separated values (e.g. <js>"foo|bar"</js>).
	 * 	<li>
	 * 		<js>"multi"</js> - Corresponds to multiple parameter instances instead of multiple values for a single instance (e.g. <js>"foo=bar&amp;foo=baz"</js>).
	 * 	<li>
	 * 		<js>"uon"</js> - UON notation (e.g. <js>"@(foo,bar)"</js>).
	 * 	<li>
	 * </ul>
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * <p>
	 * Note that for collections/arrays parameters with POJO element types, the input is broken into a string array before being converted into POJO elements.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder collectionFormat(String value) {
		try {
			if (isNotEmpty(value))
				this.collectionFormat = HttpPartCollectionFormat.fromString(value);
		} catch (Exception e) {
			throw new ContextRuntimeException("Invalid value ''{0}'' passed in as collectionFormat value.  Valid values: {1}", value, HttpPartCollectionFormat.values());
		}
		return this;
	}

	/**
	 * Synonym for {@link #collectionFormat(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cf(String value) {
		return collectionFormat(value);
	}

	/**
	 * <mk>collectionFormat</mk> field.
	 *
	 * <p>
	 * Determines the format of the array if <c>type</c> <js>"array"</js> is used.
	 * <br>Can only be used if <c>type</c> is <js>"array"</js>.
	 *
	 * <br>Possible values are:
	 * <ul class='javatree'>
	 * 	<ul class='jc'>{@link HttpPartCollectionFormat}
	 * 	<ul>
	 * 		<li>
	 * 			{@link HttpPartCollectionFormat#CSV CSV} (default) - Comma-separated values (e.g. <js>"foo,bar"</js>).
	 * 		<li>
	 * 			{@link HttpPartCollectionFormat#SSV SSV} - Space-separated values (e.g. <js>"foo bar"</js>).
	 * 		<li>
	 * 			{@link HttpPartCollectionFormat#TSV TSV} - Tab-separated values (e.g. <js>"foo\tbar"</js>).
	 * 		<li>
	 * 			{@link HttpPartCollectionFormat#PIPES PIPES} - Pipe-separated values (e.g. <js>"foo|bar"</js>).
	 * 		<li>
	 * 			{@link HttpPartCollectionFormat#MULTI MULTI} - Corresponds to multiple parameter instances instead of multiple values for a single instance (e.g. <js>"foo=bar&amp;foo=baz"</js>).
	 * 		<li>
	 * 			{@link HttpPartCollectionFormat#UONC UONC} - UON collection notation (e.g. <js>"@(foo,bar)"</js>).
	 * 	</ul>
	 * </ul>
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * <p>
	 * Note that for collections/arrays parameters with POJO element types, the input is broken into a string array before being converted into POJO elements.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder collectionFormat(HttpPartCollectionFormat value) {
		collectionFormat = value;
		return this;
	}

	/**
	 * Synonym for {@link #collectionFormat(HttpPartCollectionFormat)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cf(HttpPartCollectionFormat value) {
		return collectionFormat(value);
	}

	/**
	 * Shortcut for <c>collectionFormat(HttpPartCollectionFormat.CSV)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cfCsv() {
		return collectionFormat(HttpPartCollectionFormat.CSV);
	}

	/**
	 * Shortcut for <c>collectionFormat(HttpPartCollectionFormat.SSV)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cfSsv() {
		return collectionFormat(HttpPartCollectionFormat.SSV);
	}

	/**
	 * Shortcut for <c>collectionFormat(HttpPartCollectionFormat.TSV)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cfTsv() {
		return collectionFormat(HttpPartCollectionFormat.TSV);
	}

	/**
	 * Shortcut for <c>collectionFormat(HttpPartCollectionFormat.PIPES)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cfPipes() {
		return collectionFormat(HttpPartCollectionFormat.PIPES);
	}

	/**
	 * Shortcut for <c>collectionFormat(HttpPartCollectionFormat.MULTI)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cfMulti() {
		return collectionFormat(HttpPartCollectionFormat.MULTI);
	}

	/**
	 * Shortcut for <c>collectionFormat(HttpPartCollectionFormat.UONC)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cfUon() {
		return collectionFormat(HttpPartCollectionFormat.UONC);
	}

	/**
	 * Shortcut for <c>collectionFormat(HttpPartCollectionFormat.NO_COLLECTION_FORMAT)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder cfNone() {
		return collectionFormat(HttpPartCollectionFormat.NO_COLLECTION_FORMAT);
	}

	/**
	 * <mk>default</mk> field.
	 *
	 * <p>
	 * Declares the value of the parameter that the server will use if none is provided, for example a "count" to control the number of results per page might default to 100 if not supplied by the client in the request.
	 * <br>(Note: "default" has no meaning for required parameters.)
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder _default(String value) {
		if (value != null)
			this._default = value;
		return this;
	}

	/**
	 * Synonym for {@link #_default(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder df(String value) {
		return _default(value);
	}

	/**
	 * <mk>maximum</mk> field.
	 *
	 * <p>
	 * Defines the maximum value for a parameter of numeric types.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"integer"</js>, <js>"number"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maximum(Number value) {
		if (value != null)
			this.maximum = value;
		return this;
	}

	/**
	 * Synonym for {@link #maximum(Number)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder max(Number value) {
		return maximum(value);
	}

	/**
	 * <mk>exclusiveMaximum</mk> field.
	 *
	 * <p>
	 * Defines whether the maximum is matched exclusively.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"integer"</js>, <js>"number"</js>.
	 * <br>If <jk>true</jk>, must be accompanied with <c>maximum</c>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder exclusiveMaximum(Boolean value) {
		exclusiveMaximum = resolve(value, exclusiveMaximum);
		return this;
	}

	/**
	 * Synonym for {@link #exclusiveMaximum(Boolean)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder emax(Boolean value) {
		return exclusiveMaximum(value);
	}

	/**
	 * <mk>exclusiveMaximum</mk> field.
	 *
	 * <p>
	 * Same as {@link #exclusiveMaximum(Boolean)} but takes in a string boolean value.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder exclusiveMaximum(String value) {
		exclusiveMaximum = resolve(value, exclusiveMaximum);
		return this;
	}

	/**
	 * Synonym for {@link #exclusiveMaximum(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder emax(String value) {
		return exclusiveMaximum(value);
	}

	/**
	 * <mk>exclusiveMaximum</mk> field.
	 *
	 * <p>
	 * Shortcut for calling <code>exclusiveMaximum(<jk>true</jk>);</code>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder exclusiveMaximum() {
		return exclusiveMaximum(true);
	}

	/**
	 * Synonym for {@link #exclusiveMaximum()}.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder emax() {
		return exclusiveMaximum();
	}

	/**
	 * <mk>minimum</mk> field.
	 *
	 * <p>
	 * Defines the minimum value for a parameter of numeric types.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"integer"</js>, <js>"number"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minimum(Number value) {
		if (value != null)
			this.minimum = value;
		return this;
	}

	/**
	 * Synonym for {@link #minimum(Number)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder min(Number value) {
		return minimum(value);
	}

	/**
	 * <mk>exclusiveMinimum</mk> field.
	 *
	 * <p>
	 * Defines whether the minimum is matched exclusively.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"integer"</js>, <js>"number"</js>.
	 * <br>If <jk>true</jk>, must be accompanied with <c>minimum</c>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder exclusiveMinimum(Boolean value) {
		exclusiveMinimum = resolve(value, exclusiveMinimum);
		return this;
	}

	/**
	 * Synonym for {@link #exclusiveMinimum(Boolean)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder emin(Boolean value) {
		return exclusiveMinimum(value);
	}

	/**
	 * <mk>exclusiveMinimum</mk> field.
	 *
	 * <p>
	 * Same as {@link #exclusiveMinimum(Boolean)} but takes in a string boolean value.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder exclusiveMinimum(String value) {
		exclusiveMinimum = resolve(value, exclusiveMinimum);
		return this;
	}

	/**
	 * Synonym for {@link #exclusiveMinimum(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder emin(String value) {
		return exclusiveMinimum(value);
	}

	/**
	 * <mk>exclusiveMinimum</mk> field.
	 *
	 * <p>
	 * Shortcut for calling <code>exclusiveMinimum(<jk>true</jk>);</code>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder exclusiveMinimum() {
		return exclusiveMinimum(true);
	}

	/**
	 * Synonym for {@link #exclusiveMinimum()}.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder emin() {
		return exclusiveMinimum();
	}

	/**
	 * <mk>maxLength</mk> field.
	 *
	 * <p>
	 * A string instance is valid against this keyword if its length is less than, or equal to, the value of this keyword.
	 * <br>The length of a string instance is defined as the number of its characters as defined by <a href='https://tools.ietf.org/html/rfc4627'>RFC 4627</a>.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"string"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or <c>-1</c>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxLength(Long value) {
		maxLength = resolve(value, maxLength);
		return this;
	}

	/**
	 * Synonym for {@link #maxLength(Long)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxl(Long value) {
		return maxLength(value);
	}

	/**
	 * <mk>maxLength</mk> field.
	 *
	 * <p>
	 * Same as {@link #maxLength(Long)} but takes in a string number.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxLength(String value) {
		maxLength = resolve(value, maxLength);
		return this;
	}

	/**
	 * Synonym for {@link #maxLength(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxl(String value) {
		return maxLength(value);
	}

	/**
	 * <mk>minLength</mk> field.
	 *
	 * <p>
	 * A string instance is valid against this keyword if its length is greater than, or equal to, the value of this keyword.
	 * <br>The length of a string instance is defined as the number of its characters as defined by <a href='https://tools.ietf.org/html/rfc4627'>RFC 4627</a>.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"string"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or <c>-1</c>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minLength(Long value) {
		minLength = resolve(value, minLength);
		return this;
	}

	/**
	 * Synonym for {@link #minLength(Long)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minl(Long value) {
		return minLength(value);
	}

	/**
	 * <mk>minLength</mk> field.
	 *
	 * <p>
	 * Same as {@link #minLength(Long)} but takes in a string number.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minLength(String value) {
		minLength = resolve(value, minLength);
		return this;
	}

	/**
	 * Synonym for {@link #minLength(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minl(String value) {
		return minLength(value);
	}

	/**
	 * <mk>pattern</mk> field.
	 *
	 * <p>
	 * A string input is valid if it matches the specified regular expression pattern.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"string"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder pattern(String value) {
		try {
			if (isNotEmpty(value))
				this.pattern = Pattern.compile(value);
		} catch (Exception e) {
			throw new ContextRuntimeException(e, "Invalid value {0} passed in as pattern value.  Must be a valid regular expression.", value);
		}
		return this;
	}

	/**
	 * Synonym for {@link #pattern(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder p(String value) {
		return pattern(value);
	}

	/**
	 * <mk>maxItems</mk> field.
	 *
	 * <p>
	 * An array or collection is valid if its size is less than, or equal to, the value of this keyword.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"array"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or <c>-1</c>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxItems(Long value) {
		maxItems = resolve(value, maxItems);
		return this;
	}

	/**
	 * Synonym for {@link #maxItems(Long)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxi(Long value) {
		return maxItems(value);
	}

	/**
	 * <mk>maxItems</mk> field.
	 *
	 * <p>
	 * Same as {@link #maxItems(Long)} but takes in a string number.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxItems(String value) {
		maxItems = resolve(value, maxItems);
		return this;
	}

	/**
	 * Synonym for {@link #maxItems(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxi(String value) {
		return maxItems(value);
	}

	/**
	 * <mk>minItems</mk> field.
	 *
	 * <p>
	 * An array or collection is valid if its size is greater than, or equal to, the value of this keyword.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"array"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or <c>-1</c>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minItems(Long value) {
		minItems = resolve(value, minItems);
		return this;
	}

	/**
	 * Synonym for {@link #minItems(Long)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder mini(Long value) {
		return minItems(value);
	}

	/**
	 * <mk>minItems</mk> field.
	 *
	 * <p>
	 * Same as {@link #minItems(Long)} but takes in a string number.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minItems(String value) {
		minItems = resolve(value, minItems);
		return this;
	}

	/**
	 * Synonym for {@link #minItems(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder mini(String value) {
		return minItems(value);
	}

	/**
	 * <mk>uniqueItems</mk> field.
	 *
	 * <p>
	 * If <jk>true</jk>, the input validates successfully if all of its elements are unique.
	 *
	 * <p>
	 * <br>If the parameter type is a subclass of {@link Set}, this validation is skipped (since a set can only contain unique items anyway).
	 * <br>Otherwise, the collection or array is checked for duplicate items.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"array"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder uniqueItems(Boolean value) {
		uniqueItems = resolve(value, uniqueItems);
		return this;
	}

	/**
	 * Synonym for {@link #uniqueItems(Boolean)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder ui(Boolean value) {
		return uniqueItems(value);
	}

	/**
	 * <mk>uniqueItems</mk> field.
	 *
	 * <p>
	 * Same as {@link #uniqueItems(Boolean)} but takes in a string boolean.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty..
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder uniqueItems(String value) {
		uniqueItems = resolve(value, uniqueItems);
		return this;
	}

	/**
	 * Synonym for {@link #uniqueItems(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder ui(String value) {
		return uniqueItems(value);
	}

	/**
	 * <mk>uniqueItems</mk> field.
	 *
	 * <p>
	 * Shortcut for calling <code>uniqueItems(<jk>true</jk>);</code>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder uniqueItems() {
		return uniqueItems(true);
	}

	/**
	 * Synonym for {@link #uniqueItems()}.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder ui() {
		return uniqueItems();
	}

	/**
	 * <mk>skipIfEmpty</mk> field.
	 *
	 * <p>
	 * Identifies whether an item should be skipped during serialization if it's empty.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder skipIfEmpty(Boolean value) {
		skipIfEmpty = resolve(value, skipIfEmpty);
		return this;
	}

	/**
	 * Synonym for {@link #skipIfEmpty(Boolean)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder sie(Boolean value) {
		return skipIfEmpty(value);
	}

	/**
	 * <mk>skipIfEmpty</mk> field.
	 *
	 * <p>
	 * Same as {@link #skipIfEmpty(Boolean)} but takes in a string boolean.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder skipIfEmpty(String value) {
		skipIfEmpty = resolve(value, skipIfEmpty);
		return this;
	}

	/**
	 * Synonym for {@link #skipIfEmpty(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder sie(String value) {
		return skipIfEmpty(value);
	}

	/**
	 * Identifies whether an item should be skipped if it's empty.
	 *
	 * <p>
	 * Shortcut for calling <code>skipIfEmpty(<jk>true</jk>);</code>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder skipIfEmpty() {
		return skipIfEmpty(true);
	}

	/**
	 * Synonym for {@link #skipIfEmpty()}.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder sie() {
		return skipIfEmpty();
	}

	/**
	 * <mk>enum</mk> field.
	 *
	 * <p>
	 * If specified, the input validates successfully if it is equal to one of the elements in this array.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or an empty set.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder _enum(Set<String> value) {
		if (value != null && ! value.isEmpty())
			this._enum = value;
		return this;
	}

	/**
	 * Synonym for {@link #_enum(Set)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder e(Set<String> value) {
		return _enum(value);
	}

	/**
	 * <mk>_enum</mk> field.
	 *
	 * <p>
	 * Same as {@link #_enum(Set)} but takes in a var-args array.
	 *
	 * @param values
	 * 	The new values for this property.
	 * 	<br>Ignored if value is empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder _enum(String...values) {
		return _enum(ASet.of(values));
	}

	/**
	 * Synonym for {@link #_enum(String...)}.
	 *
	 * @param values
	 * 	The new values for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder e(String...values) {
		return _enum(values);
	}

	/**
	 * <mk>multipleOf</mk> field.
	 *
	 * <p>
	 * A numeric instance is valid if the result of the division of the instance by this keyword's value is an integer.
	 *
	 * <p>
	 * Only allowed for the following types: <js>"integer"</js>, <js>"number"</js>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerParameterObject Parameter}
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * 	<li>{@doc ExtSwaggerItemsObject Items}
	 * 	<li>{@doc ExtSwaggerHeaderObject Header}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder multipleOf(Number value) {
		if (value != null)
			this.multipleOf = value;
		return this;
	}

	/**
	 * Synonym for {@link #multipleOf(Number)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder mo(Number value) {
		return multipleOf(value);
	}

	/**
	 * <mk>mapProperties</mk> field.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or <c>-1</c>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxProperties(Long value) {
		maxProperties = resolve(value, maxProperties);
		return this;
	}

	/**
	 * Synonym for {@link #maxProperties(Long)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxp(Long value) {
		return maxProperties(value);
	}

	/**
	 * <mk>mapProperties</mk> field.
	 *
	 * <p>
	 * Same as {@link #maxProperties(Long)} but takes in a string number.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxProperties(String value) {
		maxProperties = resolve(value, maxProperties);
		return this;
	}

	/**
	 * Synonym for {@link #maxProperties(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder maxp(String value) {
		return maxProperties(value);
	}

	/**
	 * <mk>minProperties</mk> field.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minProperties(Long value) {
		minProperties = resolve(value, minProperties);
		return this;
	}

	/**
	 * Synonym for {@link #minProperties(Long)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minp(Long value) {
		return minProperties(value);
	}

	/**
	 * <mk>minProperties</mk> field.
	 *
	 * <p>
	 * Same as {@link #minProperties(Long)} but takes in a string boolean.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minProperties(String value) {
		minProperties = resolve(value, minProperties);
		return this;
	}

	/**
	 * Synonym for {@link #minProperties(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder minp(String value) {
		return minProperties(value);
	}

	/**
	 * <mk>properties</mk> field.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param key
	 *	The property name.
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder property(String key, HttpPartSchemaBuilder value) {
		if ( key != null && value != null) {
			if (properties == null)
				properties = new LinkedHashMap<>();
			properties.put(key, value);
		}
		return this;
	}

	/**
	 * <mk>properties</mk> field.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param key
	 *	The property name.
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder property(String key, HttpPartSchema value) {
		if ( key != null && value != null) {
			if (properties == null)
				properties = new LinkedHashMap<>();
			properties.put(key, value);
		}
		return this;
	}

	/**
	 * Shortcut for <c>property(key, value)</c>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param key
	 *	The property name.
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder p(String key, HttpPartSchemaBuilder value) {
		return property(key, value);
	}

	/**
	 * Shortcut for <c>property(key, value)</c>.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param key
	 *	The property name.
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder p(String key, HttpPartSchema value) {
		return property(key, value);
	}

	private HttpPartSchemaBuilder properties(OMap value) {
		if (value != null && ! value.isEmpty())
		for (Map.Entry<String,Object> e : value.entrySet())
			property(e.getKey(), HttpPartSchema.create().apply((OMap)e.getValue()));
		return this;
	}

	/**
	 * <mk>additionalProperties</mk> field.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder additionalProperties(HttpPartSchemaBuilder value) {
		if (value != null)
			additionalProperties = value;
		return this;
	}

	/**
	 * <mk>additionalProperties</mk> field.
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder additionalProperties(HttpPartSchema value) {
		if (value != null)
			additionalProperties = value;
		return this;
	}

	/**
	 * Shortcut for <c>additionalProperties(value)</c>
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder ap(HttpPartSchemaBuilder value) {
		return additionalProperties(value);
	}

	/**
	 * Shortcut for <c>additionalProperties(value)</c>
	 *
	 * <p>
	 * Applicable to the following Swagger schema objects:
	 * <ul>
	 * 	<li>{@doc ExtSwaggerSchemaObject Schema}
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or empty.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder ap(HttpPartSchema value) {
		return additionalProperties(value);
	}

	private HttpPartSchemaBuilder additionalProperties(OMap value) {
		if (value != null && ! value.isEmpty())
			additionalProperties = HttpPartSchema.create().apply(value);
		return this;
	}

	/**
	 * Identifies the part serializer to use for serializing this part.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or {@link HttpPartSerializer.Null}.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder serializer(Class<? extends HttpPartSerializer> value) {
		if (value != null && value != HttpPartSerializer.Null.class)
			serializer = value;
		return this;
	}

	/**
	 * Identifies the part parser to use for parsing this part.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Ignored if value is <jk>null</jk> or {@link HttpPartParser.Null}.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder parser(Class<? extends HttpPartParser> value) {
		if (value != null && value != HttpPartParser.Null.class)
			parser = value;
		return this;
	}

	/**
	 * Disables Swagger schema usage validation checking.
	 *
	 * @param value Specify <jk>true</jk> to prevent {@link ContextRuntimeException} from being thrown if invalid Swagger usage was detected.
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder noValidate(Boolean value) {
		if (value != null)
			this.noValidate = value;
		return this;
	}

	/**
	 * Disables Swagger schema usage validation checking.
	 *
	 * <p>
	 * Shortcut for calling <code>noValidate(<jk>true</jk>);</code>.
	 *
	 * @return This object (for method chaining).
	 */
	public HttpPartSchemaBuilder noValidate() {
		return noValidate(true);
	}

	private Boolean resolve(String newValue, Boolean oldValue) {
		return isEmpty(newValue) ? oldValue : Boolean.valueOf(newValue);
	}

	private Boolean resolve(Boolean newValue, Boolean oldValue) {
		return newValue == null ? oldValue : newValue;
	}

	private Long resolve(String newValue, Long oldValue) {
		return isEmpty(newValue) ? oldValue : Long.parseLong(newValue);
	}

	private Long resolve(Long newValue, Long oldValue) {
		return (newValue == null || newValue == -1) ? oldValue : newValue;
	}

	private Set<String> toSet(String[]...s) {
		return HttpPartSchema.toSet(s);
	}

	private Number toNumber(String...s) {
		return HttpPartSchema.toNumber(s);
	}

	private Long firstNmo(Long...l) {
		for (Long ll : l)
			if (ll != null && ll != -1)
				return ll;
		return null;
	}

	private String joinnlOrNull(String[]...s) {
		for (String[] ss : s)
			if (ss.length > 0)
				return joinnl(ss);
		return null;
	}
}