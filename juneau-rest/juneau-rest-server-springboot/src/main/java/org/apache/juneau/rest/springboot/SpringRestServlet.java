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
package org.apache.juneau.rest.springboot;

import java.util.*;

import javax.inject.*;

import org.apache.juneau.cp.*;
import org.apache.juneau.rest.*;
import org.springframework.context.*;

/**
 * Subclass of a {@link RestServlet} meant for use as deployed top-level REST beans.
 */
public abstract class SpringRestServlet extends RestServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	private Optional<ApplicationContext> appContext;

	@Override /* RestServlet */
	public BeanStore createBeanStore(Optional<BeanStore> parent) {
		return new SpringBeanStore(appContext, parent, this);
	}
}
