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
package org.apache.juneau.rest.client;

import static org.apache.juneau.httppart.HttpPartCollectionFormat.*;
import static org.apache.juneau.uon.ParamFormat.*;
import static org.junit.runners.MethodSorters.*;

import java.io.*;

import org.apache.juneau.collections.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.mock.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class RestClient_Config_OpenApi_Test {

	@Rest
	public static class A extends BasicRestObject {
		@RestOp(path="/echoBody")
		public Reader postEchoBody(org.apache.juneau.rest.RestRequest req) throws IOException {
			return req.getBody().getReader();
		}
		@RestOp(path="/checkHeader")
		public String[] getHeader(org.apache.juneau.rest.RestRequest req) {
			return req.getRequestHeaders().getAll(req.getHeader("Check")).stream().map(x -> x.getValue()).toArray(String[]::new);
		}
		@RestOp(path="/checkQuery")
		public Reader getQuery(org.apache.juneau.rest.RestRequest req) {
			return new StringReader(req.getQuery().asQueryString());
		}
		@RestOp(path="/checkFormData")
		public Reader postFormData(org.apache.juneau.rest.RestRequest req) {
			return new StringReader(req.getFormData().asQueryString());
		}
	}

	@Test
	public void a01_oapiFormat() throws Exception {
		client().oapiFormat(HttpPartFormat.UON).build().get("/checkQuery").query("Foo","bar baz").run().assertBody().urlDecode().is("Foo='bar baz'");
	}

	@Test
	public void a02_oapiCollectionFormat() throws Exception {
		String[] a = {"bar","baz"};
		RestClient x = client().oapiCollectionFormat(PIPES).build();
		x.get("/checkQuery").query("Foo",a).run().assertBody().urlDecode().is("Foo=bar|baz");
		x.post("/checkFormData").formData("Foo",a).run().assertBody().urlDecode().is("Foo=bar|baz");
		x.get("/checkHeader").header("Check","Foo").header("Foo",a).accept("text/json+simple").run().assertBody().is("['bar|baz']");
	}

	@Test
	public void a03_paramFormat() throws Exception {
		 OMap m = OMap.of(
			"foo","bar",
			"baz",new String[]{"qux","true","123"}
		);
		client().urlEnc().paramFormat(PLAINTEXT).build().post("/echoBody",m).run().assertBody().is("foo=bar&baz=qux,true,123");
		client().urlEnc().paramFormatPlain().build().post("/echoBody",m).run().assertBody().is("foo=bar&baz=qux,true,123");
		client().urlEnc().paramFormat(UON).build().post("/echoBody",m).run().assertBody().is("foo=bar&baz=@(qux,'true','123')");
	}

	//------------------------------------------------------------------------------------------------------------------
	// Helper methods.
	//------------------------------------------------------------------------------------------------------------------

	private static RestClientBuilder client() {
		return MockRestClient.create(A.class);
	}
}
