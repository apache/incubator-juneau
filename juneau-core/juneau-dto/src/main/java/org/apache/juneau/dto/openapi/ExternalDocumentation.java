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
package org.apache.juneau.dto.openapi;

import org.apache.juneau.UriResolver;
import org.apache.juneau.annotation.Bean;
import org.apache.juneau.dto.swagger.SwaggerElement;
import org.apache.juneau.internal.MultiSet;
import org.apache.juneau.internal.StringUtils;
import org.apache.juneau.utils.ASet;

import java.net.URI;
import java.net.URL;
import java.util.Set;

import static org.apache.juneau.internal.BeanPropertyUtils.toStringVal;
import static org.apache.juneau.internal.BeanPropertyUtils.toType;
import static org.apache.juneau.internal.StringUtils.isNotEmpty;

/**
 * Allows referencing an external resource for extended documentation.
 */
@Bean(properties="description,url,*")
public class ExternalDocumentation extends OpenApiElement {

	private String description;
	private URI url;

	/**
	 * Default constructor.
	 */
	public ExternalDocumentation() {}

	/**
	 * Copy constructor.
	 *
	 * @param copyFrom The object to copy.
	 */
	public ExternalDocumentation(ExternalDocumentation copyFrom) {
		super(copyFrom);

		this.description = copyFrom.description;
		this.url = copyFrom.url;
	}

	/**
	 * Make a deep copy of this object.
	 *
	 * @return A deep copy of this object.
	 */
	public ExternalDocumentation copy() {
		return new ExternalDocumentation(this);
	}

	/**
	 * Bean property getter:  <property>description</property>.
	 *
	 * <p>
	 * A short description of the target documentation.
	 *
	 * @return The property value, or <jk>null</jk> if it is not set.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Bean property setter:  <property>description</property>.
	 *
	 * <p>
	 * A short description of the target documentation.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>{@doc GFM} can be used for rich text representation.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public ExternalDocumentation setDescription(String value) {
		description = value;
		return this;
	}

	/**
	 * Same as {@link #setDescription(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Non-String values will be converted to String using <code>toString()</code>.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public ExternalDocumentation description(Object value) {
		return setDescription(toStringVal(value));
	}

	/**
	 * Bean property getter:  <property>url</property>.
	 *
	 * <p>
	 * The URL for the target documentation.
	 *
	 * @return The property value, or <jk>null</jk> if it is not set.
	 */
	public URI getUrl() {
		return url;
	}

	/**
	 * Bean property setter:  <property>url</property>.
	 *
	 * <p>
	 * The URL for the target documentation.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Property value is required.
	 * 	<br>URIs defined by {@link UriResolver} can be used for values.
	 * @return This object (for method chaining).
	 */
	public ExternalDocumentation setUrl(URI value) {
		url = value;
		return this;
	}

	/**
	 * Same as {@link #setUrl(URI)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>URIs defined by {@link UriResolver} can be used for values.
	 * 	<br>Valid types:
	 * 	<ul>
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 			<br>Converted to URI using <code><jk>new</jk> URI(value.toString())</code>.
	 * 		<li>
	 * 	</ul>
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public ExternalDocumentation url(Object value) {
		return setUrl(StringUtils.toURI(value));
	}

	/**
	 * Returns <jk>true</jk> if the url property is not null.
	 *
	 * @return <jk>true</jk> if the url property is not null.
	 */
	public boolean hasUrl() {
		return url != null;
	}

	/**
	 * Returns <jk>true</jk> if the description property is not null or empty.
	 *
	 * @return <jk>true</jk> if the description property is not null or empty.
	 */
	public boolean hasDescription() {
		return isNotEmpty(description);
	}

	@Override /* OpenApiElement */
	public <T> T get(String property, Class<T> type) {
		if (property == null)
			return null;
		switch (property) {
			case "description": return toType(getDescription(), type);
			case "url": return toType(getUrl(), type);
			default: return super.get(property, type);
		}
	}

	@Override /* OpenApiElement */
	public ExternalDocumentation set(String property, Object value) {
		if (property == null)
			return this;
		switch (property) {
			case "description": return description(value);
			case "url": return url(value);
			default:
				super.set(property, value);
				return this;
		}
	}

	@Override /* OpenApiElement */
	public Set<String> keySet() {
		ASet<String> s = new ASet<String>()
			.appendIf(description != null, "description")
			.appendIf(url != null, "url");
		return new MultiSet<>(s, super.keySet());
	}
}
