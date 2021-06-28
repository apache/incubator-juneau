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
import org.apache.juneau.dto.swagger.Items;
import org.apache.juneau.internal.MultiSet;
import org.apache.juneau.utils.ASet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.apache.juneau.internal.BeanPropertyUtils.*;


@Bean(properties="enum,default,description,*")
public class ServerVariable extends OpenApiElement {

    private List<Object> _enum;
    private String _default;
    private String description;

    /**
     * Default constructor.
     */
    public ServerVariable() {}

    /**
     * Copy constructor.
     *
     * @param copyFrom The object to copy.
     */
    public ServerVariable(ServerVariable copyFrom) {
        super(copyFrom);

        this._enum = newList(copyFrom._enum);
        this._default = copyFrom._default;
        this.description = copyFrom.description;
    }

    /**
     * Make a deep copy of this object.
     * @return A deep copy of this object.
     */
    public ServerVariable copy() {
        return new ServerVariable(this);
    }

    @Override /* OpenApiElement */
    protected ServerVariable strict() {
        super.strict();
        return this;
    }

    /**
     * Bean property getter:  <property>enum</property>.
     *
     * <h5 class='section'>See Also:</h5>
     * <ul>
     * 	<li class='extlink'>{@doc JsonSchemaValidation}
     * </ul>
     *
     * @return The property value, or <jk>null</jk> if it is not set.
     */
    public List<Object> getEnum() {
        return _enum;
    }

    /**
     * Bean property setter:  <property>enum</property>.
     *
     * <h5 class='section'>See Also:</h5>
     * <ul>
     * 	<li class='extlink'>{@doc JsonSchemaValidation}
     * </ul>
     *
     * @param value
     * 	The new value for this property.
     * 	<br>Can be <jk>null</jk> to unset the property.
     * @return This object (for method chaining).
     */
    public ServerVariable setEnum(Collection<Object> value) {
        _enum = newList(value);
        return this;
    }

    /**
     * Adds one or more values to the <property>enum</property> property.
     *
     * @param values
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public ServerVariable addEnum(Collection<Object> values) {
        _enum = addToList(_enum, values);
        return this;
    }

    /**
     * Adds one or more values to the <property>enum</property> property.
     *
     * @param values
     * 	The values to add to this property.
     * 	<br>Valid types:
     * 	<ul>
     * 		<li><code>Object</code>
     * 		<li><code>Collection&lt;Object&gt;</code>
     * 		<li><code>String</code> - JSON array representation of <code>Collection&lt;Object&gt;</code>
     * 			<h5 class='figure'>Example:</h5>
     * 			<p class='bcode w800'>
     * 	_enum(<js>"['foo','bar']"</js>);
     * 			</p>
     * 		<li><code>String</code> - Individual values
     * 			<h5 class='figure'>Example:</h5>
     * 			<p class='bcode w800'>
     * 	_enum(<js>"foo"</js>, <js>"bar"</js>);
     * 			</p>
     * 	</ul>
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public ServerVariable _enum(Object...values) {
        _enum = addToList(_enum, values, Object.class);
        return this;
    }

    /**
     * Bean property getter:  <property>default</property>.
     *
     * <p>
     * Declares the value of the item that the server will use if none is provided.
     *
     * <h5 class='section'>Notes:</h5>
     * <ul class='spaced-list'>
     * 	<li>
     * 		<js>"default"</js> has no meaning for required items.
     * 	<li>
     * 		Unlike JSON Schema this value MUST conform to the defined <code>type</code> for the data type.
     * </ul>
     *
     * <h5 class='section'>See Also:</h5>
     * <ul>
     * 	<li class='extlink'>{@doc JsonSchemaValidation}
     * </ul>
     *
     * @return The property value, or <jk>null</jk> if it is not set.
     */
    public String getDefault() {
        return _default;
    }

    /**
     * Bean property setter:  <property>default</property>.
     *
     * <p>
     * Declares the value of the item that the server will use if none is provided.
     *
     * <h5 class='section'>Notes:</h5>
     * <ul class='spaced-list'>
     * 	<li>
     * 		<js>"default"</js> has no meaning for required items.
     * 	<li>
     * 		Unlike JSON Schema this value MUST conform to the defined <code>type</code> for the data type.
     * </ul>
     *
     * <h5 class='section'>See Also:</h5>
     * <ul>
     * 	<li class='extlink'>{@doc JsonSchemaValidation}
     * </ul>
     *
     * @param value
     * 	The new value for this property.
     * 	<br>Can be <jk>null</jk> to unset the property.
     * @return This object (for method chaining).
     */
    public ServerVariable setDefault(String value) {
        _default = value;
        return this;
    }

    /**
     * Same as {@link #setDefault(String)}.
     *
     * @param value
     * 	The new value for this property.
     * 	<br>Can be <jk>null</jk> to unset the property.
     * @return This object (for method chaining).
     */
    public ServerVariable _default(Object value) {
        return setDefault(toStringVal(value));
    }

    /**
     * Returns <jk>true</jk> if the defualt property is not null or empty.
     *
     * @return <jk>true</jk> if the defualt property is not null or empty.
     */
    public boolean hasDefault() {
        return ! _default.isEmpty();
    }

    /**
     * Bean property getter:  <property>description</property>.
     *
     * <p>
     * Declares the value of the item that the server will use if none is provided.
     *
     * <h5 class='section'>Notes:</h5>
     * <ul class='spaced-list'>
     * 	<li>
     * 		<js>"description"</js> has no meaning for required items.
     * 	<li>
     * 		Unlike JSON Schema this value MUST conform to the defined <code>type</code> for the data type.
     * </ul>
     *
     * <h5 class='section'>See Also:</h5>
     * <ul>
     * 	<li class='extlink'>{@doc JsonSchemaValidation}
     * </ul>
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Bean property setter:  <property>description</property>.
     *
     * <p>
     * Declares the value of the item that the server will use if none is provided.
     *
     * <h5 class='section'>Notes:</h5>
     * <ul class='spaced-list'>
     * 	<li>
     * 		<js>"description"</js> has no meaning for required items.
     * 	<li>
     * 		Unlike JSON Schema this value MUST conform to the defined <code>type</code> for the data type.
     * </ul>
     *
     * <h5 class='section'>See Also:</h5>
     * <ul>
     * 	<li class='extlink'>{@doc JsonSchemaValidation}
     * </ul>
     *
     * @param value
     * 	The new value for this property.
     * @return This object (for method chaining).
     */
    public ServerVariable setDescription(String value) {
        description = value;
        return this;
    }

    /**
     * Same as {@link #setDescription(String)}.
     *
     * @param value
     * 	The new value for this property.
     * @return This object (for method chaining).
     */
    public ServerVariable description(Object value) {
        return setDefault(toStringVal(value));
    }

    @Override /* OpenApiElement */
    public <T> T get(String property, Class<T> type) {
        if (property == null)
            return null;
        switch (property) {
            case "enum": return toType(getEnum(), type);
            case "default": return toType(getDefault(), type);
            case "description": return toType(getDescription(), type);
            default: return super.get(property, type);
        }
    }

    @Override /* OpenApiElement */
    public ServerVariable set(String property, Object value) {
        if (property == null)
            return this;
        switch (property) {
            case "default": return _default(value);
            case "enum": return setEnum(null)._enum(value);
            case "description": return description(value);
            default:
                super.set(property, value);
                return this;
        }
    }

    @Override /* OpenApiElement */
    public Set<String> keySet() {
        ASet<String> s = new ASet<String>()
                .appendIf(_enum != null, "enum")
                .appendIf(_default != null,"default" )
                .appendIf(description != null, "description");
        return new MultiSet<>(s, super.keySet());


    }
}