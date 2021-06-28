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
import org.apache.juneau.internal.MultiSet;
import org.apache.juneau.utils.ASet;

import java.net.URI;
import java.net.URL;
import java.util.*;

import static org.apache.juneau.internal.BeanPropertyUtils.*;

@Bean(properties="contentType,style,explode,headers,allowReserved,*")
public class Response extends OpenApiElement{

    private String description;
    private Map<String,HeaderInfo> headers;
    private Map<String,MediaType> content;
    private Map<String,Link> links;

    /**
     * Default constructor.
     */
    public Response() { }

    /**
     * Copy constructor.
     *
     * @param copyFrom The object to copy.
     */
    public Response(Response copyFrom) {
        super(copyFrom);

        this.description = copyFrom.description;
        if (copyFrom.headers == null) {
            this.headers = null;
        } else {
            this.headers = new LinkedHashMap<>();
            for (Map.Entry<String,HeaderInfo> e : copyFrom.headers.entrySet())
                this.headers.put(e.getKey(),	e.getValue().copy());
        }

        if (copyFrom.content == null) {
            this.content = null;
        } else {
            this.content = new LinkedHashMap<>();
            for (Map.Entry<String,MediaType> e : copyFrom.content.entrySet())
                this.content.put(e.getKey(),	e.getValue().copy());
        }

        if (copyFrom.links == null) {
            this.links = null;
        } else {
            this.links = new LinkedHashMap<>();
            for (Map.Entry<String,Link> e : copyFrom.links.entrySet())
                this.links.put(e.getKey(),	e.getValue().copy());
        }
    }

    /**
     * Make a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    public Response copy() {
        return new Response(this);
    }

    @Override /* OpenApiElement */
    protected Response strict() {
        super.strict();
        return this;
    }

    /**
     * Bean property getter:  <property>Description</property>.
     *
     * <p>
     * The URL pointing to the contact information.
     *
     * @return The property value, or <jk>null</jk> if it is not set.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Bean property setter:  <property>Description</property>.
     *
     * <p>
     * The value can be of any of the following types: {@link URI}, {@link URL}, {@link String}.
     * <br>Strings must be valid URIs.
     *
     * <p>
     * URIs defined by {@link UriResolver} can be used for values.
     *
     * @param value
     * 	The new value for this property.
     * 	<br>Can be <jk>null</jk> to unset the property.
     * @return This object (for method chaining).
     */
    public Response setDescription(String value) {
        description = value;
        return this;
    }

    /**
     * Same as {@link #setDescription(String)} (String)}.
     *
     * @param value
     * 	The new value for this property.
     * 	<br>Non-URI values will be converted to URI using <code><jk>new</jk> URI(value.toString())</code>.
     * 	<br>Can be <jk>null</jk> to unset the property.
     * @return This object (for method chaining).
     */
    public Response description(Object value) {
        return setDescription(toStringVal(value));
    }

    /**
     * Bean property getter:  <property>headers</property>.
     */
    public Map<String, HeaderInfo> getHeaders() {
        return headers;
    }

    /**
     * Bean property setter:  <property>headers</property>.
     *
     * @param value
     * 	The new value for this property.
     * @return This object (for method chaining).
     */
    public Response setHeaders(Map<String, HeaderInfo> value) {
        headers = newMap(value);
        return this;
    }

    /**
     * Adds one or more values to the <property>headers</property> property.
     *
     * @param value
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response addHeader(Map<String, HeaderInfo> value) {
        headers = addToMap(headers,value);
        return this;
    }

    /**
     * Adds one or more values to the <property>variables</property> property.
     *
     * @param value
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response addHeaders(String keyval, HeaderInfo value) {
        headers = addToMap(headers,keyval,value);
        return this;
    }

    /**
     * Adds a single value to the <property>headers</property> property.
     *
     * @param name variable name.
     * @param value The server variable instance.
     * @return This object (for method chaining).
     */
    public Response header(String name, HeaderInfo value) {
        addHeader(Collections.singletonMap(name, value));
        return this;
    }

    /**
     * Adds one or more values to the <property>headers</property> property.
     *
     * @param values
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response headers(Object...values) {
        headers = addToMap(headers, values, String.class, HeaderInfo.class);
        return this;
    }

    public Response headers(Object value) {
        return setHeaders((HashMap<String,HeaderInfo>)value);
    }

    /**
     * Bean property getter:  <property>headers</property>.
     */
    public Map<String, MediaType> getContent() {
        return content;
    }

    /**
     * Bean property setter:  <property>content</property>.
     *
     * @param value
     * 	The new value for this property.
     * @return This object (for method chaining).
     */
    public Response setContent(Map<String, MediaType> value) {
        content = newMap(value);
        return this;
    }

    /**
     * Adds one or more values to the <property>content</property> property.
     *
     * @param value
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response addContent(Map<String, MediaType> value) {
        content = addToMap(content,value);
        return this;
    }

    /**
     * Adds one or more values to the <property>variables</property> property.
     *
     * @param value
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response addContent(String keyval, MediaType value) {
        content = addToMap(content,keyval,value);
        return this;
    }

    /**
     * Adds a single value to the <property>headers</property> property.
     *
     * @param name variable name.
     * @param value The server variable instance.
     * @return This object (for method chaining).
     */
    public Response content(String name, MediaType value) {
        addContent(Collections.singletonMap(name, value));
        return this;
    }

    /**
     * Adds one or more values to the <property>headers</property> property.
     *
     * @param values
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response content(Object...values) {
        content = addToMap(content, values, String.class, MediaType.class);
        return this;
    }

    public Response content(Object value) {
        return setContent((HashMap<String,MediaType>)value);
    }

    /**
     * Bean property getter:  <property>link</property>.
     */
    public Map<String, Link> getLinks() {
        return links;
    }

    /**
     * Bean property setter:  <property>Link</property>.
     *
     * @param value
     * 	The new value for this property.
     * @return This object (for method chaining).
     */
    public Response setLinks(Map<String, Link> value) {
        links = newMap(value);
        return this;
    }

    /**
     * Adds one or more values to the <property>headers</property> property.
     *
     * @param value
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response addLink(Map<String, Link> value) {
        links = addToMap(links,value);
        return this;
    }

    /**
     * Adds one or more values to the <property>variables</property> property.
     *
     * @param value
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response addLink(String keyval, Link value) {
        links = addToMap(links,keyval,value);
        return this;
    }

    /**
     * Adds a single value to the <property>headers</property> property.
     *
     * @param name variable name.
     * @param value The server variable instance.
     * @return This object (for method chaining).
     */
    public Response links(String name, Link value) {
        addLink(Collections.singletonMap(name, value));
        return this;
    }

    /**
     * Adds one or more values to the <property>headers</property> property.
     *
     * @param values
     * 	The values to add to this property.
     * 	<br>Ignored if <jk>null</jk>.
     * @return This object (for method chaining).
     */
    public Response links(Object...values) {
        links = addToMap(links, values, String.class, Link.class);
        return this;
    }

    public Response links(Object value) {
        return setLinks((HashMap<String,Link>)value);
    }


    @Override /* OpenApiElement */
    public <T> T get(String property, Class<T> type) {
        if (property == null)
            return null;
        switch (property) {
            case "description": return toType(getDescription(), type);
            case "content": return toType(getContent(), type);
            case "headers": return toType(getHeaders(), type);
            case "links": return toType(getLinks(), type);
            default: return super.get(property, type);
        }
    }

    @Override /* OpenApiElement */
    public Response set(String property, Object value) {
        if (property == null)
            return this;
        switch (property) {
            case "description": return description(value);
            case "headers": return headers(value);
            case "content": return content(value);
            case "links": return links(value);
            default:
                super.set(property, value);
                return this;
        }
    }

    @Override /* OpenApiElement */
    public Set<String> keySet() {
        ASet<String> s = new ASet<String>()
                .appendIf(description != null, "description")
                .appendIf(headers != null, "headers")
                .appendIf(content != null, "content")
                .appendIf(links != null, "links");
        return new MultiSet<>(s, super.keySet());
    }
}
