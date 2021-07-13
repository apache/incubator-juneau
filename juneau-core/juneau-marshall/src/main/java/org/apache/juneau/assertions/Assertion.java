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

import static org.apache.juneau.internal.ExceptionUtils.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.io.*;
import java.lang.reflect.*;

import org.apache.juneau.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.internal.*;

/**
 * Base class for all assertion objects.
 */
@FluentSetters
public class Assertion {

	private static final Messages MESSAGES = Messages.of(Assertion.class, "Messages");
	static final String
		MSG_parameterCannotBeNull = MESSAGES.getString("parameterCannotBeNull"),
		MSG_causedBy = MESSAGES.getString("causedBy");

	String msg;
	Object[] msgArgs;
	PrintStream out = System.err;
	Class<? extends RuntimeException> throwable;

	/**
	 * Constructor used when this assertion is being created from within another assertion.
	 * @param creator The creator of this assertion.
	 */
	protected Assertion(Assertion creator) {
		if (creator != null) {
			this.msg = creator.msg;
			this.msgArgs = creator.msgArgs;
			this.out = creator.out;
			this.throwable = creator.throwable;
		}
	}

	/**
	 * Allows to to specify the assertion failure message.
	 *
	 * <p>
	 * String can contain <js>"{msg}"</js> to represent the original message.
	 *
	 * @param msg The assertion failure message.
	 * @param args Optional message arguments.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public Assertion msg(String msg, Object...args) {
		this.msg = msg.replace("{msg}", "<<<MSG>>>");
		this.msgArgs = args;
		return this;
	}

	/**
	 * If an error occurs, send the error message to STDOUT.
	 *
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public Assertion stdout() {
		return out(System.out);
	}

	/**
	 * If an error occurs, send the error message to the specified stream.
	 *
	 * @param value The output stream.  Can be <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public Assertion out(PrintStream value) {
		this.out = value;
		return this;
	}

	/**
	 * Suppresses output.
	 *
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public Assertion silent() {
		return out(null);
	}

	/**
	 * If an error occurs, throw this exception when {@link #error(String, Object...)} is called.
	 *
	 * @param value The new value for this setting.
	 * @return This object (for method chaining).
	 */
	@FluentSetter
	public Assertion throwable(Class<? extends RuntimeException> value) {
		this.throwable = value;
		return this;
	}

	/**
	 * Creates a new {@link BasicAssertionError}.
	 *
	 * @param msg The message.
	 * @param args The message arguments.
	 * @return A new {@link BasicAssertionError}.
	 */
	protected BasicAssertionError error(String msg, Object...args) {
		return error(null, msg, args);
	}

	/**
	 * Creates a new {@link BasicAssertionError}.
	 *
	 * @param cause Optional caused-by throwable.
	 * @param msg The message.
	 * @param args The message arguments.
	 * @return A new {@link BasicAssertionError}.
	 */
	protected BasicAssertionError error(Throwable cause, String msg, Object...args) {
		msg = format(msg, args);
		if (this.msg != null)
			msg = format(this.msg, this.msgArgs).replace("<<<MSG>>>", msg);
		if (out != null)
			out.println(msg);
		if (throwable != null) {
			try {
				throw BeanStore.create().build().addBean(Throwable.class, cause).addBean(String.class, msg).createBean(throwable);
			} catch (ExecutableException e) {
				// If we couldn't create requested exception, just throw a RuntimeException.
				throw runtimeException(cause, msg);
			}
		}
		return new BasicAssertionError(cause, msg);
	}

	/**
	 * Convenience method for getting the class name for an object.
	 *
	 * @param o The object to get the class name for.
	 * @return The class name for an object.
	 */
	protected static String className(Object o) {
		return ClassUtils.className(o);
	}

	/**
	 * Convenience method for getting the array class of the specified element type.
	 *
	 * @param c The object to get the class name for.
	 * @return The class name for an object.
	 */
	@SuppressWarnings("unchecked")
	protected static <E> Class<E[]> arrayClass(Class<E> c) {
		return (Class<E[]>)Array.newInstance(c,0).getClass();
	}

	// <FluentSetters>

	// </FluentSetters>
}
