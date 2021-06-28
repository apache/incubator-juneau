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

import org.apache.juneau.annotation.Bean;
import org.apache.juneau.internal.MultiSet;
import org.apache.juneau.utils.ASet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.juneau.internal.BeanPropertyUtils.*;

/**
 * information for Link object.
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Construct using SwaggerBuilder.</jc>
 * 	Contact x = <jsm>contact</jsm>(<js>"API Support"</js>, <js>"http://www.swagger.io/support"</js>, <js>"support@swagger.io"</js>);
 *
 * 	<jc>// Serialize using JsonSerializer.</jc>
 * 	String json = JsonSerializer.<jsf>DEFAULT</jsf>.toString(x);
 *
 * 	<jc>// Or just use toString() which does the same as above.</jc>
 * 	String json = x.toString();
 * </p>
 * <p class='bcode w800'>
 * 	<jc>// Output</jc>
 * 	{
 * 		<js>"name"</js>: <js>"API Support"</js>,
 * 		<js>"url"</js>: <js>"http://www.swagger.io/support"</js>,
 * 		<js>"email"</js>: <js>"support@swagger.io"</js>
 * 	}
 * </p>
 *
 * <h5 class='section'>See Also:</h5>
 * <ul class='doctree'>
 * 	<li class='link'>{@doc juneau-dto.Swagger}
 * </ul>
 */
@Bean(properties="authorizationUrl,tokenUrl,refreshUrl,scopes,*")
public class OAuthFlow extends OpenApiElement {

	private String authorizationUrl;
	private String tokenUrl;
	private String refreshUrl;
	private Map<String,String> scopes;


	/**
	 * Default constructor.
	 */
	public OAuthFlow() {}

	/**
	 * Copy constructor.
	 *
	 * @param copyFrom The object to copy.
	 */
	public OAuthFlow(OAuthFlow copyFrom) {
		super(copyFrom);

		this.authorizationUrl = copyFrom.authorizationUrl;
		this.tokenUrl = copyFrom.tokenUrl;
		this.refreshUrl = copyFrom.refreshUrl;

		if (copyFrom.scopes == null)
			this.scopes = null;
		else
			this.scopes = new LinkedHashMap<>(copyFrom.scopes);
	}

	/**
	 * Make a deep copy of this object.
	 *
	 * @return A deep copy of this object.
	 */
	public OAuthFlow copy() {
		return new OAuthFlow(this);
	}

	/**
	 * Bean property getter:  <property>operationRef</property>.
	 *
	 * <p>
	 * The identifying name of the contact person/organization.
	 *
	 * @return The property value, or <jk>null</jk> if it is not set.
	 */
	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

	/**
	 * Bean property setter:  <property>operationRef</property>.
	 *
	 * <p>
	 * The identifying name of the contact person/organization.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow setAuthorizationUrl(String value) {
		authorizationUrl = value;
		return this;
	}

	/**
	 * Same as {@link #setAuthorizationUrl(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Non-String values will be converted to String using <code>toString()</code>.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow authorizationUrl(Object value) {
		return setAuthorizationUrl(toStringVal(value));
	}

	/**
	 * Bean property getter:  <property>description</property>.
	 *
	 * <p>
	 * The URL pointing to the contact information.
	 *
	 * @return The property value, or <jk>null</jk> if it is not set.
	 */
	public String getTokenUrl() {
		return tokenUrl;
	}

	/**
	 * Bean property setter:  <property>description</property>.
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow setTokenUrl(String value) {
		tokenUrl = value;
		return this;
	}

	/**
	 * Same as {@link #setTokenUrl(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Non-URI values will be converted to URI using <code><jk>new</jk> URI(value.toString())</code>.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow tokenUrl(Object value) {
		return setTokenUrl(toStringVal(value));
	}

	/**
	 * Bean property getter:  <property>externalValue</property>.
	 *
	 * <p>
	 * The email address of the contact person/organization.
	 *
	 * @return The property value, or <jk>null</jk> if it is not set.
	 */
	public String getRefreshUrl() {
		return refreshUrl;
	}

	/**
	 * Bean property setter:  <property>externalValue</property>.
	 *
	 * <p>
	 * The email address of the contact person/organization.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>MUST be in the format of an email address.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow setRefreshUrl(String value) {
		refreshUrl = value;
		return this;
	}

	/**
	 * Same as {@link #setRefreshUrl(String)}.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Non-String values will be converted to String using <code>toString()</code>.
	 * 	<br>MUST be in the format of an email address.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow refreshUrl(Object value) {
		return setRefreshUrl(toStringVal(value));
	}

	/**
	 * Bean property getter:  <property>examples</property>.
	 *
	 * <p>
	 * An example of the response message.
	 *
	 * @return The property value, or <jk>null</jk> if it is not set.
	 */
	public Map<String,String> getScopes() {
		return scopes;
	}

	/**
	 * Bean property setter:  <property>examples</property>.
	 *
	 * <p>
	 * An example of the response message.
	 *
	 * @param value
	 * 	The new value for this property.
	 * 	<br>Keys must be MIME-type strings.
	 * 	<br>Can be <jk>null</jk> to unset the property.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow setScopes(Map<String,String> value) {
		scopes = newMap(value);
		return this;
	}

	/**
	 * Adds one or more values to the <property>examples</property> property.
	 *
	 * @param values
	 * 	The values to add to this property.
	 * 	<br>Ignored if <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow addScopes(Map<String,String> values) {
		scopes = addToMap(scopes, values);
		return this;
	}

	/**
	 * Adds a single value to the <property>examples</property> property.
	 *
	 * @param name The mime-type string.
	 * @param descriptiom The example.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow scopes(String name, String descriptiom) {
		scopes = addToMap(scopes, name, descriptiom);
		return this;
	}

	/**
	 * Adds one or more values to the <property>examples</property> property.
	 *
	 * @param values
	 * 	The values to add to this property.
	 * 	<br>Valid types:
	 * 	<ul>
	 * 		<li><code>Map&lt;String,Object&gt;</code>
	 * 		<li><code>String</code> - JSON object representation of <code>Map&lt;String,Object&gt;</code>
	 * 			<h5 class='figure'>Example:</h5>
	 * 			<p class='bcode w800'>
	 * 	examples(<js>"{'text/json':{foo:'bar'}}"</js>);
	 * 			</p>
	 * 	</ul>
	 * 	<br>Ignored if <jk>null</jk>.
	 * @return This object (for method chaining).
	 */
	public OAuthFlow scopes(Object...values) {
		scopes = addToMap(scopes, values, String.class, String.class);
		return this;
	}


	@Override /* OpenApiElement */
	public <T> T get(String property, Class<T> type) {
		if (property == null)
			return null;
		switch (property) {
			case "refreshUrl": return toType(getRefreshUrl(), type);
			case "tokenUrl": return toType(getTokenUrl(), type);
			case "authorizationUrl": return toType(getAuthorizationUrl(), type);
			case "scopes": return toType(getScopes(), type);
			default: return super.get(property, type);
		}
	}

	@Override /* OpenApiElement */
	public OAuthFlow set(String property, Object value) {
		if (property == null)
			return this;
		switch (property) {
			case "authorizationUrl": return authorizationUrl(value);
			case "tokenUrl": return tokenUrl(value);
			case "refreshUrl": return refreshUrl(value);
			case "scopes": return scopes(value);
			default:
				super.set(property, value);
				return this;
		}
	}

	@Override /* OpenApiElement */
	public Set<String> keySet() {
		ASet<String> s = new ASet<String>()
			.appendIf(authorizationUrl != null, "authorizationUrl")
			.appendIf(tokenUrl != null, "tokenUrl")
			.appendIf(refreshUrl != null, "refreshUrl")
			.appendIf(scopes != null, "scopes");
		return new MultiSet<>(s, super.keySet());
	}
}
