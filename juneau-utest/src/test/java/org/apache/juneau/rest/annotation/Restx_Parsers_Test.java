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
package org.apache.juneau.rest.annotation;

import static org.apache.juneau.http.HttpMethod.*;
import static org.apache.juneau.internal.IOUtils.*;
import static org.junit.runners.MethodSorters.*;

import java.io.IOException;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.parser.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.RestRequest;
import org.apache.juneau.rest.client.*;
import org.apache.juneau.rest.mock.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class Restx_Parsers_Test {

	//------------------------------------------------------------------------------------------------------------------
	// Setup
	//------------------------------------------------------------------------------------------------------------------

	public static class PA extends ReaderParser {
		public PA(PropertyStore ps) {
			super(ps, "text/a");
		}
		@Override /* Parser */
		public ReaderParserSession createSession(ParserSessionArgs args) {
			return new ReaderParserSession(args) {
				@Override /* ParserSession */
				@SuppressWarnings("unchecked")
				protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws IOException, ParseException, ExecutableException {
					return (T)("text/a - " + read(pipe.getReader()).trim());
				}
			};
		}
	}

	public static class PB extends ReaderParser {
		public PB(PropertyStore ps) {
			super(ps, "text/b");
		}
		@Override /* Parser */
		public ReaderParserSession createSession(ParserSessionArgs args) {
			return new ReaderParserSession(args) {
				@Override /* ParserSession */
				@SuppressWarnings("unchecked")
				protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws IOException, ParseException, ExecutableException {
					return (T)("text/b - " + read(pipe.getReader()).trim());
				}
			};
		}
	}

	public static class PC extends ReaderParser {
		public PC(PropertyStore ps) {
			super(ps, "text/c");
		}
		@Override /* Parser */
		public ReaderParserSession createSession(ParserSessionArgs args) {
			return new ReaderParserSession(args) {
				@Override /* ParserSession */
				@SuppressWarnings("unchecked")
				protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws IOException, ParseException, ExecutableException {
					return (T)("text/c - " + read(pipe.getReader()).trim());
				}
			};
		}
	}

	public static class PD extends ReaderParser {
		public PD(PropertyStore ps) {
			super(ps, "text/d");
		}
		@Override /* Parser */
		public ReaderParserSession createSession(ParserSessionArgs args) {
			return new ReaderParserSession(args) {
				@Override /* ParserSession */
				@SuppressWarnings("unchecked")
				protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws IOException, ParseException, ExecutableException {
					return (T)("text/d - " + read(pipe.getReader()).trim());
				}
			};
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	// Basic tests
	//------------------------------------------------------------------------------------------------------------------

	@Rest(parsers=PA.class)
	public static class A {
		@RestOp(method=PUT)
		public String a(@Body String in) {
			return in;
		}
		@RestOp(method=PUT, parsers=PB.class)
		public String b(@Body String in) {
			return in;
		}
		@RestOp(method=PUT, parsers={Inherit.class, PB.class,PC.class})
		public String c(@Body String in) {
			return in;
		}
		@RestOp(method=PUT, parsers={Inherit.class, PD.class})
		public String d(@Body String in) {
			return in;
		}
		@RestOp(method=PUT)
		public String e(@Body String in) {
			return in;
		}
	}

	@Test
	public void a01_basic() throws Exception {
		MockRestClient a = MockRestClient.buildLax(A.class);

		a.put("/a", "test1")
			.contentType("text/a")
			.run()
			.assertBody().is("text/a - test1");
		a.put("/a?noTrace=true", "test1")
			.contentType("text/b")
			.run()
			.assertCode().is(415)
			.assertBody().contains(
				"Unsupported media-type in request header 'Content-Type': 'text/b'",
				"Supported media-types: ['text/a"
			);

		a.put("/b", "test2")
			.contentType("text/b")
			.run()
			.assertBody().is("text/b - test2");
		a.put("/b?noTrace=true", "test2")
			.contentType("text/a")
			.run()
			.assertCode().is(415)
			.assertBody().contains(
				"Unsupported media-type in request header 'Content-Type': 'text/a'",
				"Supported media-types: ['text/b']"
			);

		a.put("/c", "test3")
			.contentType("text/a")
			.run()
			.assertBody().is("text/a - test3");
		a.put("/c", "test3")
			.contentType("text/b")
			.run()
			.assertBody().is("text/b - test3");

		a.put("/d", "test4")
			.contentType("text/a")
			.run()
			.assertBody().is("text/a - test4");
		a.put("/d", "test4")
			.contentType("text/d")
			.run()
			.assertBody().is("text/d - test4");

		a.put("/e?noTrace=true", "test1")
			.contentType("text/bad")
			.run()
			.assertCode().is(415)
			.assertBody().contains(
				"Unsupported media-type in request header 'Content-Type': 'text/bad'",
				"Supported media-types: ['text/a"
			);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Test parser inheritance.
	//------------------------------------------------------------------------------------------------------------------

	public static class DummyParser extends ReaderParser {
		public DummyParser(String...consumes) {
			super(PropertyStore.DEFAULT, consumes);
		}
		@Override /* Parser */
		public ReaderParserSession createSession(ParserSessionArgs args) {
			return new ReaderParserSession(args) {
				@Override /* ParserSession */
				protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws IOException, ParseException, ExecutableException {
					return null;
				}
			};
		}
	}

	public static class P1 extends DummyParser{ public P1(PropertyStore ps) {super("text/p1");} }
	public static class P2 extends DummyParser{ public P2(PropertyStore ps) {super("text/p2");} }
	public static class P3 extends DummyParser{ public P3(PropertyStore ps) {super("text/p3");} }
	public static class P4 extends DummyParser{ public P4(PropertyStore ps) {super("text/p4");} }
	public static class P5 extends DummyParser{ public P5(PropertyStore ps) {super("text/p5");} }

	@Rest(parsers={P1.class,P2.class})
	public static class B {}

	@Rest(parsers={P3.class,P4.class,Inherit.class})
	public static class B1 extends B {}

	@Rest
	public static class B2 extends B1 {
		@RestOp
		public OList a(RestRequest req) {
			// Should show ['text/p3','text/p4','text/p1','text/p2']
			return OList.of(req.getConsumes());
		}
		@RestOp(parsers=P5.class)
		public OList b(RestRequest req) {
			// Should show ['text/p5']
			return OList.of(req.getConsumes());
		}
		@RestOp(parsers={P5.class,Inherit.class})
		public OList c(RestRequest req) {
			// Should show ['text/p5','text/p3','text/p4','text/p1','text/p2']
			return OList.of(req.getConsumes());
		}
	}

	@Test
	public void b01_inheritence() throws Exception {
		RestClient b = MockRestClient.build(B2.class);
		b.get("/a").run().assertBody().is("['text/p3','text/p4','text/p1','text/p2']");
		b.get("/b").run().assertBody().is("['text/p5']");
		b.get("/c").run().assertBody().is("['text/p5','text/p3','text/p4','text/p1','text/p2']");
	}

}