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
package org.apache.juneau.rest.logging;

import static org.apache.juneau.Enablement.*;
import static org.apache.juneau.internal.ClassUtils.*;
import static org.apache.juneau.rest.HttpRuntimeException.*;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;

import javax.servlet.http.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.response.*;
import org.apache.juneau.mstat.*;

/**
 * Builder class for {@link BasicRestLogger} objects.
 */
public class RestLoggerBuilder {

	Logger logger;
	ThrownStore thrownStore;
	List<RestLoggerRule> normalRules = AList.create(), debugRules = AList.create();
	Enablement enabled;
	Predicate<HttpServletRequest> enabledTest;
	RestLoggingDetail requestDetail, responseDetail;
	Level level;
	BeanStore beanStore;
	Class<? extends RestLogger> implClass;

	/**
	 * Creates a new {@link RestLogger} object from this builder.
	 *
	 * <p>
	 * Instantiates an instance of the {@link #implClass(Class) implementation class} or
	 * else {@link BasicRestLogger} if implementation class was not specified.
	 *
	 * @return A new {@link RestLogger} object.
	 */
	public RestLogger build() {
		try {
			Class<? extends RestLogger> ic = isConcrete(implClass) ? implClass : getDefaultImplClass();
			return BeanStore.of(beanStore).addBeans(RestLoggerBuilder.class, this).createBean(ic);
		} catch (Exception e) {
			throw toHttpException(e, InternalServerError.class);
		}
	}

	/**
	 * Specifies the default implementation class if not specified via {@link #implClass(Class)}.
	 *
	 * @return The default implementation class if not specified via {@link #implClass(Class)}.
	 */
	protected Class<? extends RestLogger> getDefaultImplClass() {
		return BasicRestLogger.class;
	}

	/**
	 * Specifies the bean store to use for instantiating the {@link RestLogger} object.
	 *
	 * @param value The new value for this setting.
	 * @return  This object (for method chaining).
	 */
	public RestLoggerBuilder beanStore(BeanStore value) {
		this.beanStore = value;
		return this;
	}

	/**
	 * Specifies a subclass of {@link RestLogger} to create when the {@link #build()} method is called.
	 *
	 * @param value The new value for this setting.
	 * @return  This object (for method chaining).
	 */
	public RestLoggerBuilder implClass(Class<? extends RestLogger> value) {
		this.implClass = value;
		return this;
	}

	/**
	 * Specifies the logger to use for logging the request.
	 *
	 * <p>
	 * If not specified, the logger name is determined in the following order:
	 * <ol>
	 * 	<li><js>{@link RestLogger#SP_logger "juneau.restLogger.logger"} system property.
	 * 	<li><js>{@link RestLogger#SP_logger "JUNEAU_RESTLOGGER_LOGGER"} environment variable.
	 * 	<li><js>"global"</js>.
	 * </ol>
	 *
	 * <p>
	 * The {@link BasicRestLogger#getLogger()} method can also be overridden to provide different logic.
	 *
	 * @param value
	 * 	The logger to use for logging the request.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder logger(Logger value) {
		this.logger = value;
		return this;
	}

	/**
	 * Specifies the logger to use for logging the request.
	 *
	 * <p>
	 * Shortcut for calling <c>logger(Logger.<jsm>getLogger</jsm>(value))</c>.
	 *
	 * <p>
	 * If not specified, the logger name is determined in the following order:
	 * <ol>
	 * 	<li><js>{@link RestLogger#SP_logger "juneau.restLogger.logger"} system property.
	 * 	<li><js>{@link RestLogger#SP_logger "JUNEAU_RESTLOGGER_LOGGER"} environment variable.
	 * 	<li><js>"global"</js>.
	 * </ol>
	 *
	 * <p>
	 * The {@link BasicRestLogger#getLogger()} method can also be overridden to provide different logic.
	 *
	 * @param value
	 * 	The logger to use for logging the request.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder logger(String value) {
		this.logger = value == null ? null :Logger.getLogger(value);
		return this;
	}

	/**
	 * Specifies the thrown exception store to use for getting stack trace information (hash IDs and occurrence counts).
	 *
	 * @param value
	 * 	The stack trace store.
	 * 	<br>If <jk>null</jk>, stack trace information will not be logged.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder thrownStore(ThrownStore value) {
		this.thrownStore = value;
		return this;
	}

	/**
	 * Specifies the default logging enablement setting.
	 *
	 * <p>
	 * This specifies the default logging enablement value if not set on the first matched rule or if no rules match.
	 *
	 * <p>
	 * The possible values are:
	 * <ul>
	 * 	<li>{@link Enablement#ALWAYS ALWAYS} (default) - Logging is enabled.
	 * 	<li>{@link Enablement#NEVER NEVER} - Logging is disabled.
	 * 	<li>{@link Enablement#CONDITIONAL CONDITIONALLY} - Logging is enabled if it passes the {@link #enabledTest(Predicate)} test.
	 * </ul>
	 *
	 * <p>
	 * If not specified, the setting is determined via the following:
	 * <ul>
	 * 	<li><js>{@link RestLogger#SP_enabled "juneau.restLogger.enabled"} system property.
	 * 	<li><js>{@link RestLogger#SP_enabled "JUNEAU_RESTLOGGER_ENABLED"} environment variable.
	 * 	<li><js>"ALWAYS"</js>.
	 * </ul>
	 *
	 * <p>
	 * @param value
	 * 	The default enablement flag value.  Can be <jk>null</jk> to use the default.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder enabled(Enablement value) {
		this.enabled = value;
		return this;
	}

	/**
	 * Specifies the default logging enablement test predicate.
	 *
	 * <p>
	 * This specifies the default logging enablement test if not set on the first matched rule or if no rules match.
	 *
	 * <p>
	 * This setting has no effect if the enablement setting is not {@link Enablement#CONDITIONAL CONDITIONALLY}.
	 *
	 * <p>
	 * The default if not specified is <c><jv>x</jv> -> <jk>false</jk></c> (never log).
	 *
	 * @param value
	 * 	The default enablement flag value.  Can be <jk>null</jk> to use the default.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder enabledTest(Predicate<HttpServletRequest> value) {
		this.enabledTest = value;
		return this;
	}

	/**
	 * Shortcut for calling <c>enabled(<jsf>NEVER</jsf>)</c>.
	 *
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder disabled() {
		return this.enabled(NEVER);
	}

	/**
	 * The default level of detail to log on a request.
	 *
	 * <p>
	 * This specifies the default level of request detail if not set on the first matched rule or if no rules match.
	 *
	 * <p>
	 * The possible values are:
	 * <ul>
	 * 	<li>{@link RestLoggingDetail#STATUS_LINE STATUS_LINE} - Log only the status line.
	 * 	<li>{@link RestLoggingDetail#HEADER HEADER} - Log the status line and headers.
	 * 	<li>{@link RestLoggingDetail#ENTITY ENTITY} - Log the status line and headers and body if available.
	 * </ul>
	 *
	 * <p>
	 * If not specified, the setting is determined via the following:
	 * <ul>
	 * 	<li><js>{@link RestLogger#SP_requestDetail "juneau.restLogger.requestDetail"} system property.
	 * 	<li><js>{@link RestLogger#SP_requestDetail "JUNEAU_RESTLOGGER_requestDetail"} environment variable.
	 * 	<li><js>"STATUS_LINE"</js>.
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property, or <jk>null</jk> to use the default.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder requestDetail(RestLoggingDetail value) {
		this.requestDetail = value;
		return this;
	}

	/**
	 * The default level of detail to log on a response.
	 *
	 * <p>
	 * This specifies the default level of response detail if not set on the first matched rule or if no rules match.
	 *
	 * <p>
	 * The possible values are:
	 * <ul>
	 * 	<li>{@link RestLoggingDetail#STATUS_LINE STATUS_LINE} - Log only the status line.
	 * 	<li>{@link RestLoggingDetail#HEADER HEADER} - Log the status line and headers.
	 * 	<li>{@link RestLoggingDetail#ENTITY ENTITY} - Log the status line and headers and body if available.
	 * </ul>
	 *
	 * <p>
	 * If not specified, the setting is determined via the following:
	 * <ul>
	 * 	<li><js>{@link RestLogger#SP_responseDetail "juneau.restLogger.responseDetail"} system property.
	 * 	<li><js>{@link RestLogger#SP_responseDetail "JUNEAU_RESTLOGGER_responseDetail"} environment variable.
	 * 	<li><js>"STATUS_LINE"</js>.
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property, or <jk>null</jk> to use the default.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder responseDetail(RestLoggingDetail value) {
		this.responseDetail = value;
		return this;
	}

	/**
	 * The default logging level to use for logging the request/response.
	 *
	 * <p>
	 * This specifies the default logging level if not set on the first matched rule or if no rules match.
	 *
	 * <p>
	 * If not specified, the setting is determined via the following:
	 * <ul>
	 * 	<li><js>{@link RestLogger#SP_level "juneau.restLogger.level"} system property.
	 * 	<li><js>{@link RestLogger#SP_level "JUNEAU_RESTLOGGER_level"} environment variable.
	 * 	<li><js>"OFF"</js>.
	 * </ul>
	 *
	 * @param value
	 * 	The new value for this property, or <jk>null</jk> to use the default value.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder level(Level value) {
		this.level = value;
		return this;
	}

	/**
	 * Adds logging rules to use when debug mode is not enabled.
	 *
	 * <p>
	 * Logging rules are matched in the order they are added.  The first to match wins.
	 *
	 * @param rules The logging rules to add to the list of rules.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder normalRules(RestLoggerRule...rules) {
		for (RestLoggerRule rule : rules)
			this.normalRules.add(rule);
		return this;
	}

	/**
	 * Adds logging rules to use when debug mode is enabled.
	 *
	 * <p>
	 * Logging rules are matched in the order they are added.  The first to match wins.
	 *
	 * @param rules The logging rules to add to the list of rules.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder debugRules(RestLoggerRule...rules) {
		for (RestLoggerRule rule : rules)
			this.debugRules.add(rule);
		return this;
	}

	/**
	 * Shortcut for adding the same rules as normal and debug rules.
	 *
	 * <p>
	 * Logging rules are matched in the order they are added.  The first to match wins.
	 *
	 * @param rules The logging rules to add to the list of rules.
	 * @return This object (for method chaining).
	 */
	public RestLoggerBuilder rules(RestLoggerRule...rules) {
		return normalRules(rules).debugRules(rules);
	}
}
