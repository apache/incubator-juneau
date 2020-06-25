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

import java.util.*;

import org.apache.juneau.internal.*;

/**
 * Used for assertion calls against {@link Date} objects.
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Validates the specified date is after the current date.</jc>
 * 	<jsm>assertDate</jsm>(myDate).isAfter(<jk>new</jk> Date());
 * </p>
 */
@FluentSetters(returns="DateAssertion")
public class DateAssertion extends FluentDateAssertion<DateAssertion> {

	/**
	 * Creator.
	 *
	 * @param date The date being wrapped.
	 * @return A new {@link DateAssertion} object.
	 */
	public static DateAssertion assertDate(Date date) {
		return new DateAssertion(date);
	}

	/**
	 * Creator.
	 *
	 * @param date The date being wrapped.
	 * @return A new {@link DateAssertion} object.
	 */
	public static DateAssertion create(Date date) {
		return new DateAssertion(date);
	}

	/**
	 * Creator.
	 *
	 * @param date The date being wrapped.
	 */
	protected DateAssertion(Date date) {
		super(date, null);
	}

	@Override
	protected DateAssertion returns() {
		return this;
	}

	// <FluentSetters>

	@Override /* GENERATED - Assertion */
	public DateAssertion msg(String msg, Object...args) {
		super.msg(msg, args);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public DateAssertion stderr() {
		super.stderr();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public DateAssertion stdout() {
		super.stdout();
		return this;
	}

	// </FluentSetters>
}
