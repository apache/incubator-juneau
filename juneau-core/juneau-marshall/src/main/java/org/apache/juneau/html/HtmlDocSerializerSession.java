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
package org.apache.juneau.html;

import static org.apache.juneau.html.HtmlDocSerializer.*;

import java.io.IOException;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.html.annotation.CspHash;
import org.apache.juneau.html.annotation.CspNonce;
import org.apache.juneau.serializer.*;
import org.apache.juneau.svl.*;

/**
 * Context object that lives for the duration of a single serialization of {@link HtmlSerializer} and its subclasses.
 *
 * <p>
 * See {@link Serializer} for details.
 *
 * <p>
 * This class is NOT thread safe.  It is meant to be discarded after one-time use.
 */
public class HtmlDocSerializerSession extends HtmlStrippedDocSerializerSession {

	private static final VarResolver DEFAULT_VR = VarResolver.create().defaultVars().vars(HtmlWidgetVar.class).build();

	private final HtmlDocSerializer ctx;
	private final String[] navlinks, head, header, nav, aside, footer;
	private final AsideFloat asideFloat;
	private final Set<String> style, stylesheet, script;
	private final boolean nowrap;
	private final CspHash cspHash;
	private final CspNonce cspNonce;

	/**
	 * Create a new session using properties specified in the context.
	 *
	 * @param ctx
	 * 	The context creating this session object.
	 * 	The context contains all the configuration settings for this object.
	 * @param args
	 * 	Runtime arguments.
	 */
	protected HtmlDocSerializerSession(HtmlDocSerializer ctx, SerializerSessionArgs args) {
		super(ctx, args);
		this.ctx = ctx;

		SessionProperties sp = getSessionProperties();

		header = sp.get(HTMLDOC_header, String[].class).orElse(ctx.getHeader());
		nav = sp.get(HTMLDOC_nav, String[].class).orElse(ctx.getNav());
		aside = sp.get(HTMLDOC_aside, String[].class).orElse(ctx.getAside());
		asideFloat = sp.get(HTMLDOC_asideFloat, AsideFloat.class).orElse(ctx.getAsideFloat());
		footer = sp.get(HTMLDOC_footer, String[].class).orElse(ctx.getFooter());
		navlinks = sp.get(HTMLDOC_navlinks, String[].class).orElse(ctx.getNavlinks());

		// These can contain dups after variable resolution, so de-dup them with hashsets.
		style = ASet.of(sp.get(HTMLDOC_style, String[].class).orElse(ctx.getStyle()));
		stylesheet = ASet.of(sp.get(HTMLDOC_stylesheet, String[].class).orElse(ctx.getStylesheet()));
		script = ASet.of(sp.get(HTMLDOC_script, String[].class).orElse(ctx.getScript()));
		
		cspHash = sp.get(HTMLDOC_cspHash, CspHash.class).orElse(ctx.getCspHash());
		cspNonce = sp.get(HTMLDOC_cspNonce, CspNonce.class).orElse(ctx.getCspNonce());

		head = sp.get(HTMLDOC_head, String[].class).orElse(ctx.getHead());
		nowrap = sp.get(HTMLDOC_nowrap, boolean.class).orElse(ctx.isNowrap());

		addVarBean(HtmlWidgetMap.class, ctx.getWidgets());
	}

	@Override /* SerializerSession */
	protected VarResolverSession createDefaultVarResolverSession() {
		return DEFAULT_VR.createSession();
	}

	/**
	 * Returns the {@link HtmlDocSerializer#HTMLDOC_navlinks} setting value in this context.
	 *
	 * @return
	 * 	The {@link HtmlDocSerializer#HTMLDOC_navlinks} setting value in this context.
	 * 	<jk>null</jk> if not specified.
	 * 	Never an empty map.
	 */
	public final String[] getNavLinks() {
		return navlinks;
	}

	@Override /* Serializer */
	protected void doSerialize(SerializerPipe out, Object o) throws IOException, SerializeException {

		try (HtmlWriter w = getHtmlWriter(out)) {
			try {
				getTemplate().writeTo(this, w, o);
			} catch (Exception e) {
				throw new SerializeException(e);
			}
		}
	}

	/**
	 * Calls the parent {@link #doSerialize(SerializerPipe, Object)} method which invokes just the HTML serializer.
	 *
	 * @param out
	 * 	Where to send the output from the serializer.
	 * @param o The object being serialized.
	 * @throws Exception Error occurred during serialization.
	 */
	public void parentSerialize(Object out, Object o) throws Exception {
		try (SerializerPipe pipe = createPipe(out)) {
			super.doSerialize(pipe, o);
		}
	}
	//-----------------------------------------------------------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Configuration property:  Aside section contents.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_aside
	 * @return
	 * 	The overridden contents of the aside section on the HTML page.
	 */
	protected final String[] getAside() {
		return aside;
	}

	/**
	 * Configuration property:  Aside section contents float.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_asideFloat
	 * @return
	 * 	The location of where to place the aside section.
	 */
	protected final AsideFloat getAsideFloat() {
		return asideFloat;
	}

	/**
	 * Configuration property:  CSP hash algorithm name.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_cspHash
	 * @return
	 * 	CSP hash algorithm name.
	 * @since 9.0.0
	 */
	protected final CspHash getCspHash() {
		return cspHash;
	}

	/**
	 * Configuration property:  CSP nonce algorithm name.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_cspNonce
	 * @return
	 * 	CSP nonce algorithm name.
	 * @since 9.0.0
	 */
	protected final CspNonce getCspNonce() {
		return cspNonce;
	}

	/**
	 * Configuration property:  Footer section contents.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_footer
	 * @return
	 * 	The overridden contents of the footer section on the HTML page.
	 */
	protected final String[] getFooter() {
		return footer;
	}

	/**
	 * Configuration property:  Additional head section content.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_head
	 * @return
	 * 	HTML content to add to the head section of the HTML page.
	 */
	protected final String[] getHead() {
		return head;
	}

	/**
	 * Configuration property:  Header section contents.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_header
	 * @return
	 * 	The overridden contents of the header section on the HTML page.
	 */
	protected final String[] getHeader() {
		return header;
	}

	/**
	 * Configuration property:  Nav section contents.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_nav
	 * @return
	 * 	The overridden contents of the nav section on the HTML page.
	 */
	protected final String[] getNav() {
		return nav;
	}

	/**
	 * Configuration property:  Page navigation links.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_navlinks
	 * @return
	 * 	Navigation links to add to the HTML page.
	 */
	protected final String[] getNavlinks() {
		return navlinks;
	}

	/**
	 * Configuration property:  No-results message.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_noResultsMessage
	 * @return
	 * 	The message used when serializing an empty array or empty list.
	 */
	protected final String getNoResultsMessage() {
		return ctx.getNoResultsMessage();
	}

	/**
	 * Configuration property:  Prevent word wrap on page.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_nowrap
	 * @return
	 * 	<jk>true</jk> if <js>"* {white-space:nowrap}"</js> shoudl be added to the CSS instructions on the page to prevent word wrapping.
	 */
	protected final boolean isNowrap() {
		return nowrap;
	}

	/**
	 * Configuration property:  Javascript code.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_script
	 * @return
	 * 	Arbitrary Javascript to add to the HTML page.
	 */
	protected final Set<String> getScript() {
		return script;
	}

	/**
	 * Configuration property:  CSS style code.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_style
	 * @return
	 * 	The CSS instructions to add to the HTML page.
	 */
	protected final Set<String> getStyle() {
		return style;
	}

	/**
	 * Configuration property:  Stylesheet import URLs.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_stylesheet
	 * @return
	 * 	The link to the stylesheet of the HTML page.
	 */
	protected final Set<String> getStylesheet() {
		return stylesheet;
	}

	/**
	 * Configuration property:  HTML document template.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_template
	 * @return
	 * 	The template to use for serializing the page.
	 */
	protected final HtmlDocTemplate getTemplate() {
		return ctx.getTemplate();
	}

	/**
	 * Configuration property:  Page navigation links.
	 *
	 * @see HtmlDocSerializer#HTMLDOC_navlinks
	 * @return
	 * 	Navigation links to add to the HTML page.
	 */
	protected final Collection<HtmlWidget> getWidgets() {
		return ctx.getWidgets().values();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Session */
	public OMap toMap() {
		return super.toMap()
			.a(
				"HtmlDocSerializerSession",
				OMap
					.create()
					.filtered()
					.a("aside", aside)
					.a("head", head)
					.a("header", header)
					.a("footer", footer)
					.a("nav", nav)
					.a("navlinks", navlinks)
					.a("script", script)
					.a("style", style)
					.a("cspHash", cspHash)
					.a("cspNonce", cspNonce)
					.a("stylesheet", stylesheet)
					.a("varResolver", getVarResolver())
			);
	}

}
