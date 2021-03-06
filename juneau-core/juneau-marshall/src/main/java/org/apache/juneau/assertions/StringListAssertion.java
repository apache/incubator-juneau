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
package org.apache.juneau.assertions;

import java.io.*;
import java.util.*;

import org.apache.juneau.internal.*;

/**
 * Used for assertion calls against lists of strings.
 */
@FluentSetters(returns="StringListAssertion")
public class StringListAssertion extends FluentStringListAssertion<StringListAssertion> {

	//-----------------------------------------------------------------------------------------------------------------
	// Constructors
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Creator.
	 *
	 * @param value The object being wrapped.
	 * @return A new {@link StringListAssertion} object.
	 */
	public static StringListAssertion create(List<String> value) {
		return new StringListAssertion(value);
	}

	/**
	 * Creator.
	 *
	 * @param value The object being wrapped.
	 */
	public StringListAssertion(List<String> value) {
		super(value, null);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Fluent setters
	//-----------------------------------------------------------------------------------------------------------------

	// <FluentSetters>

	@Override /* GENERATED - Assertion */
	public StringListAssertion msg(String msg, Object...args) {
		super.msg(msg, args);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public StringListAssertion out(PrintStream value) {
		super.out(value);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public StringListAssertion silent() {
		super.silent();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public StringListAssertion stdout() {
		super.stdout();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public StringListAssertion throwable(Class<? extends java.lang.RuntimeException> value) {
		super.throwable(value);
		return this;
	}

	// </FluentSetters>
}
