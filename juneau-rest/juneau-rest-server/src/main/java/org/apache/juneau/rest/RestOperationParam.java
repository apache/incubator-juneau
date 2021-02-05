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

import org.apache.juneau.rest.annotation.*;

/**
 * REST java method parameter resolver.
 *
 * <p>
 * Used to resolve parameter values when invoking {@link RestOp}-annotated methods.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// A simple parameter resolver that resolves TimeZone parameters.</jc>
 * 	<jk>public class</jk> TimeZoneParam <jk>implements</jk> RestParam {
 *
 * 		<jc>// Implementers must provide a static creator method that returns a RestParam if it's
 * 		// applicable to the specified parameter.</jc>
 * 		<jk>public static</jk> TimeZoneParam <jsm>create</jsm>(ParamInfo <jv>paramInfo</jv>) {
 * 			<jk>if</jk> (<jv>paramInfo</jv>.isType(TimeZone.<jk>class</jk>))
 * 				<jk>return new</jk> TimeZoneParam();
 * 			<jk>return null</jk>;
 * 		}
 *
 * 		<jk>protected</jk> TimeZoneParam() {}
 *
 * 		<ja>@Override</ja>
 * 		<jk>public</jk> Object resolve(RestCall <jv>call</jv>) <jk>throws</jk> Exception {
 * 			<jk>return</jk> <jv>call</jv>.getRestRequest().getHeaders().getTimeZone();
 * 		}
 * 	}
 * </p>
 *
 * <ul class='seealso'>
 * 	<li class='jf'>{@link RestContext#REST_restOperationParams}
 * 	<li class='link'>{@doc RestmParameters}
 * </ul>
 */
public interface RestOperationParam {

	/**
	 * Resolves the parameter object.
	 *
	 * @param call The rest call.
	 * @return The resolved object.
	 * @throws Exception Generic error occurred.
	 */
	public Object resolve(RestCall call) throws Exception;
}