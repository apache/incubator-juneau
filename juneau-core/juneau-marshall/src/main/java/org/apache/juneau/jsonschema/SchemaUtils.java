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
package org.apache.juneau.jsonschema;

import static org.apache.juneau.internal.ClassUtils.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.util.*;

import org.apache.juneau.collections.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.parser.*;

/**
 * Utilities for working with the schema annotations.
 */
public class SchemaUtils {

	/**
	 * Concatenates and parses a string array as a JSON object.
	 *
	 * @param ss The array to concatenate and parse.
	 * @return The parsed contents.
	 * @throws ParseException Invalid JSON encountered.
	 */
	public static OMap parseMap(String[] ss) throws ParseException {
		if (ss.length == 0)
			return null;
		String s = joinnl(ss);
		if (s.isEmpty())
			return null;
		if (! isJsonObject(s, true))
			s = "{" + s + "}";
		return OMap.ofJson(s);
	}

	/**
	 * Parses a generic object as JSON and converts it to an {@link OMap}.
	 *
	 * @param o The object to convert.
	 * @return The parsed contents.
	 * @throws ParseException Invalid JSON encountered.
	 */
	public static OMap parseMap(Object o) throws ParseException {
		if (o == null)
			return null;
		if (o instanceof String[])
			o = joinnl((String[])o);
		if (o instanceof String) {
			String s = o.toString();
			if (s.isEmpty())
				return null;
			if ("IGNORE".equalsIgnoreCase(s))
				return OMap.of("ignore", true);
			if (! isJsonObject(s, true))
				s = "{" + s + "}";
			return OMap.ofJson(s);
		}
		if (o instanceof OMap)
			return (OMap)o;
		throw new ParseException("Unexpected data type ''{0}''.  Expected OMap or String.", className(o));
	}

	/**
	 * Concatenates and parses a string array as JSON array or comma-delimited list.
	 *
	 * @param ss The array to concatenate and parse.
	 * @return The parsed contents.
	 * @throws ParseException Invalid JSON encountered.
	 */
	public static Set<String> parseSet(String[] ss) throws ParseException {
		if (ss.length == 0)
			return null;
		String s = joinnl(ss);
		if (s.isEmpty())
			return null;
		Set<String> set = ASet.of();
		for (Object o : StringUtils.parseListOrCdl(s))
			set.add(o.toString());
		return set;
	}

	/**
	 * Joins an array of strings with newlines.
	 *
	 * @param s The array to join.
	 * @return A new joined string.
	 */
	public static String joinnl(String[]...s) {
		for (String[] ss : s) {
			if (ss.length != 0)
			return StringUtils.joinnl(ss).trim();
		}
		return "";
	}
}
