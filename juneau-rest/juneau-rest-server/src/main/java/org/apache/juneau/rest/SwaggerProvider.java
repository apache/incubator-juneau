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

import java.util.*;

import org.apache.juneau.dto.swagger.Swagger;
import org.apache.juneau.rest.annotation.*;

/**
 * Interface for retrieving Swagger on a REST resource.
 */
public interface SwaggerProvider {

	/**
	 * Represents no SwaggerProvider.
	 *
	 * <p>
	 * Used on annotation to indicate that the value should be inherited from the parent class, and
	 * ultimately {@link BasicSwaggerProvider} if not specified at any level.
	 */
	public abstract class Null implements SwaggerProvider {};

	/**
	 * Creator.
	 *
	 * @return A new builder for this object.
	 */
	public static SwaggerProviderBuilder create() {
		return new SwaggerProviderBuilder();
	}

	/**
	 * Returns the Swagger associated with the specified {@link Rest}-annotated class.
	 *
	 * @param context The context of the {@link Rest}-annotated class.
	 * @param locale The request locale.
	 * @return A new {@link Swagger} DTO object.
	 * @throws Exception If an error occurred producing the Swagger.
	 */
	public Swagger getSwagger(RestContext context, Locale locale) throws Exception;

}
