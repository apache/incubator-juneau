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
package org.apache.juneau.oapi;

import static org.apache.juneau.httppart.HttpPartCollectionFormat.*;
import static org.apache.juneau.httppart.HttpPartDataType.*;
import static org.apache.juneau.httppart.HttpPartFormat.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.uon.*;

/**
 * Session object that lives for the duration of a single use of {@link OpenApiSerializer}.
 *
 * <p>
 * This class is NOT thread safe.
 * It is typically discarded after one-time use although it can be reused within the same thread.
 */
public class OpenApiSerializerSession extends UonSerializerSession {

	//-------------------------------------------------------------------------------------------------------------------
	// Predefined instances
	//-------------------------------------------------------------------------------------------------------------------

	// Cache these for faster lookup
	private static final BeanContext BC = BeanContext.DEFAULT;
	private static final ClassMeta<byte[]> CM_ByteArray = BC.getClassMeta(byte[].class);
	private static final ClassMeta<String[]> CM_StringArray = BC.getClassMeta(String[].class);
	private static final ClassMeta<Calendar> CM_Calendar = BC.getClassMeta(Calendar.class);
	private static final ClassMeta<Long> CM_Long = BC.getClassMeta(Long.class);
	private static final ClassMeta<Integer> CM_Integer = BC.getClassMeta(Integer.class);
	private static final ClassMeta<Double> CM_Double = BC.getClassMeta(Double.class);
	private static final ClassMeta<Float> CM_Float = BC.getClassMeta(Float.class);
	private static final ClassMeta<Boolean> CM_Boolean = BC.getClassMeta(Boolean.class);

	private static final HttpPartSchema DEFAULT_SCHEMA = HttpPartSchema.DEFAULT;

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final OpenApiSerializer ctx;

	/**
	 * Create a new session using properties specified in the context.
	 *
	 * @param ctx
	 * 	The context creating this session object.
	 * 	The context contains all the configuration settings for this object.
	 * @param args
	 * 	Runtime session arguments.
	 */
	protected OpenApiSerializerSession(OpenApiSerializer ctx, SerializerSessionArgs args) {
		super(ctx, false, args);
		this.ctx = ctx;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Entry point methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Serializer */
	protected void doSerialize(SerializerPipe out, Object o) throws IOException, SerializeException {
		try {
			out.getWriter().write(serialize(HttpPartType.BODY, getSchema(), o));
		} catch (SchemaValidationException e) {
			throw new SerializeException(e);
		}
	}

	@Override /* PartSerializer */
	public String serialize(HttpPartType partType, HttpPartSchema schema, Object value) throws SerializeException, SchemaValidationException {

		schema = ObjectUtils.firstNonNull(schema, DEFAULT_SCHEMA);
		ClassMeta<?> type = getClassMetaForObject(value);
		if (type == null)
			type = object();
		HttpPartDataType t = schema.getType(type);
		HttpPartFormat f = schema.getFormat(type);
		HttpPartCollectionFormat cf = schema.getCollectionFormat();

		String out = null;

		schema.validateOutput(value, ctx);

		if (type.hasMutaterTo(schema.getParsedType()) || schema.getParsedType().hasMutaterFrom(type)) {
			value = toType(value, schema.getParsedType());
			type = schema.getParsedType();
		}

		if (type.isUri()) {
			value = getUriResolver().resolve(value);
			type = string();
		}

		if (value != null) {

			if (t == STRING) {

				if (f == BYTE)
					out = base64Encode(toType(value, CM_ByteArray));
				else if (f == BINARY)
					out = toHex(toType(value, CM_ByteArray));
				else if (f == BINARY_SPACED)
					out = toSpacedHex(toType(value, CM_ByteArray));
				else if (f == DATE)
					out = toIsoDate(toType(value, CM_Calendar));
				else if (f == DATE_TIME)
					out = toIsoDateTime(toType(value, CM_Calendar));
				else if (f == HttpPartFormat.UON)
					out = super.serialize(partType, schema, value);
				else
					out = toType(value, string());

			} else if (t == ARRAY) {

				if (cf == HttpPartCollectionFormat.UON)
					out = super.serialize(partType, null, toList(partType, type, value, schema));
				else {
					List<String> l = new ArrayList<>();

					HttpPartSchema items = schema.getItems();
					ClassMeta<?> vt = getClassMetaForObject(value);

					if (type.isArray()) {
						for (int i = 0; i < Array.getLength(value); i++)
							l.add(serialize(partType, items, Array.get(value, i)));
					} else if (type.isCollection()) {
						for (Object o : (Collection<?>)value)
							l.add(serialize(partType, items, o));
					} else if (vt.hasMutaterTo(String[].class)) {
						String[] ss = toType(value, CM_StringArray);
						for (int i = 0; i < ss.length; i++)
							l.add(serialize(partType, items, ss[i]));
					}

					if (cf == PIPES)
						out = joine(l, '|');
					else if (cf == SSV)
						out = join(l, ' ');
					else if (cf == TSV)
						out = join(l, '\t');
					else
						out = joine(l, ',');
				}

			} else if (t == BOOLEAN) {

				if (f == HttpPartFormat.UON)
					out = super.serialize(partType, null, value);
				else
					out = stringify(toType(value, CM_Boolean));

			} else if (t == INTEGER) {

				if (f == HttpPartFormat.UON)
					out = super.serialize(partType, null, value);
				else if (f == INT64)
					out = stringify(toType(value, CM_Long));
				else
					out = stringify(toType(value, CM_Integer));

			} else if (t == NUMBER) {

				if (f == HttpPartFormat.UON)
					out = super.serialize(partType, null, value);
				else if (f == DOUBLE)
					out = stringify(toType(value, CM_Double));
				else
					out = stringify(toType(value, CM_Float));

			} else if (t == OBJECT) {

				if (f == HttpPartFormat.UON) {
					out = super.serialize(partType, null, value);
				} else if (schema.hasProperties() && type.isMapOrBean()) {
					out = super.serialize(partType, null, toMap(partType, type, value, schema));
				} else {
					out = super.serialize(partType, null, value);
				}

			} else if (t == FILE) {
				throw new SerializeException("File part not supported.");

			} else if (t == NO_TYPE) {
				// This should never be returned by HttpPartSchema.getType(ClassMeta).
				throw new SerializeException("Invalid type.");
			}
		}

		schema.validateInput(out);
		if (out == null)
			out = schema.getDefault();
		if (out == null)
			out = "null";
		return out;
	}

	@SuppressWarnings({ "rawtypes" })
	private Map toMap(HttpPartType partType, ClassMeta<?> type, Object o, HttpPartSchema s) throws SerializeException, SchemaValidationException {
		if (s == null)
			s = DEFAULT_SCHEMA;
		OMap m = new OMap();
		if (type.isBean()) {
			for (BeanPropertyValue p : toBeanMap(o).getValues(isKeepNullProperties())) {
				if (p.getMeta().canRead()) {
					Throwable t = p.getThrown();
					if (t == null)
						m.put(p.getName(), toObject(partType, p.getValue(), s.getProperty(p.getName())));
				}
			}
		} else {
			for (Map.Entry e : (Set<Map.Entry>)((Map)o).entrySet())
				m.put(stringify(e.getKey()), toObject(partType, e.getValue(), s.getProperty(stringify(e.getKey()))));
		}
		if (isSortMaps())
			return sort(m);
		return m;
	}

	@SuppressWarnings("rawtypes")
	private Collection toList(HttpPartType partType, ClassMeta<?> type, Object o, HttpPartSchema s) throws SerializeException, SchemaValidationException {
		if (s == null)
			s = DEFAULT_SCHEMA;
		OList l = new OList();
		HttpPartSchema items = s.getItems();
		if (type.isArray()) {
			for (int i = 0; i < Array.getLength(o); i++)
				l.add(toObject(partType, Array.get(o, i), items));
		} else if (type.isCollection()) {
			for (Object o2 : (Collection<?>)o)
				l.add(toObject(partType, o2, items));
		} else {
			l.add(toObject(partType, o, items));
		}
		if (isSortCollections())
			return sort(l);
		return l;
	}

	@SuppressWarnings("rawtypes")
	private Object toObject(HttpPartType partType, Object o, HttpPartSchema s) throws SerializeException, SchemaValidationException {
		if (o == null)
			return null;
		if (s == null)
			s = DEFAULT_SCHEMA;
		ClassMeta cm = getClassMetaForObject(o);
		HttpPartDataType t = s.getType(cm);
		HttpPartFormat f = s.getFormat(cm);
		HttpPartCollectionFormat cf = s.getCollectionFormat();

		if (t == STRING) {
			if (f == BYTE)
				return base64Encode(toType(o, CM_ByteArray));
			if (f == BINARY)
				return toHex(toType(o, CM_ByteArray));
			if (f == BINARY_SPACED)
				return toSpacedHex(toType(o, CM_ByteArray));
			if (f == DATE)
				return toIsoDate(toType(o, CM_Calendar));
			if (f == DATE_TIME)
				return toIsoDateTime(toType(o, CM_Calendar));
			return o;
		} else if (t == ARRAY) {
			Collection l = toList(partType, getClassMetaForObject(o), o, s);
			if (cf == CSV)
				return joine(l, ',');
			if (cf == PIPES)
				return joine(l, '|');
			if (cf == SSV)
				return join(l, ' ');
			if (cf == TSV)
				return join(l, '\t');
			return l;
		} else if (t == OBJECT) {
			return toMap(partType, getClassMetaForObject(o), o, s);
		}

		return o;
	}

	private <T> T toType(Object in, ClassMeta<T> type) throws SerializeException {
		try {
			return convertToType(in, type);
		} catch (InvalidDataConversionException e) {
			throw new SerializeException(e.getMessage());
		}
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Session */
	public OMap toMap() {
		return super.toMap()
			.a("OpenApiSerializerSession", new DefaultFilteringOMap()
		);
	}
}
