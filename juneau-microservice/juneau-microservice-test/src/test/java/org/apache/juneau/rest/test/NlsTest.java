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
package org.apache.juneau.rest.test;

import static org.apache.juneau.rest.test.TestUtils.*;

import org.apache.juneau.dto.swagger.*;
import org.apache.juneau.rest.client.*;
import org.junit.*;

@Ignore // TODO - These are in flux during Swagger development.
public class NlsTest extends RestTestcase {

	private static String URL = "/testNls";
	private RestClient client = TestMicroservice.DEFAULT_CLIENT;

	// ====================================================================================================
	// test1 - Pull labels from annotations only.
	// ====================================================================================================
	@Test
	public void test1() throws Exception {

		Swagger s = client.doOptions(URL + "/test1").getResponse(Swagger.class);
		assertObjectEquals("{title:'Test1.a',description:'Test1.b'}", s.getInfo());
		assertObjectEquals("[{'in':'path',name:'a',type:'string',description:'Test1.d',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'b',type:'string',description:'Test1.e',schema:{description:'java.lang.String',type:'string'}},{'in':'body',type:'string',description:'Test1.f',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'D',type:'string',description:'Test1.g',schema:{description:'java.lang.String',type:'string'}},{'in':'path',name:'a2',type:'string',description:'Test1.h',required:true},{'in':'query',name:'b2',type:'string',description:'Test1.i'},{'in':'header',name:'D2',type:'string',description:'Test1.j'},{'in':'path',name:'e',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'f',schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'g',schema:{description:'java.lang.String',type:'string'}}]", s.getPaths().get("/{a}").get("post").getParameters());
		assertObjectEquals("{'200':{description:'OK',schema:{description:'java.lang.String',type:'string'}},'201':{description:'Test1.l',headers:{bar:{description:'Test1.m',type:'string'}}}}", s.getPaths().get("/{a}").get("post").getResponses());
	}

	// ====================================================================================================
	// test2 - Pull labels from resource bundles only - simple keys.
	// ====================================================================================================
	@Test
	public void test2() throws Exception {

		Swagger s = client.doOptions(URL + "/test2").getResponse(Swagger.class);
		assertObjectEquals("{title:'Test2.a',description:'Test2.b'}", s.getInfo());
		assertObjectEquals("[{'in':'path',name:'a',description:'Test2.d',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'b',description:'Test2.e',schema:{description:'java.lang.String',type:'string'}},{'in':'body',description:'Test2.f',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'D',description:'Test2.g',schema:{description:'java.lang.String',type:'string'}},{'in':'path',name:'a2',description:'Test2.h',required:true},{'in':'query',name:'b2',description:'Test2.i'},{'in':'header',name:'D2',description:'Test2.j'},{'in':'path',name:'e',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'f',schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'g',schema:{description:'java.lang.String',type:'string'}}]", s.getPaths().get("/{a}").get("post").getParameters());
		assertObjectEquals("{'200':{description:'OK2',schema:{description:'java.lang.String',type:'string'}},'201':{description:'Test2.l'}}", s.getPaths().get("/{a}").get("post").getResponses());
	}

	// ====================================================================================================
	// test3 - Pull labels from resource bundles only - keys with class names.
	// ====================================================================================================
	@Test
	public void test3() throws Exception {

		Swagger s = client.doOptions(URL + "/test3").getResponse(Swagger.class);
		assertObjectEquals("{title:'Test3.a',description:'Test3.b'}", s.getInfo());
		assertObjectEquals("[{'in':'path',name:'a',description:'Test3.d',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'b',description:'Test3.e',schema:{description:'java.lang.String',type:'string'}},{'in':'body',description:'Test3.f',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'D',description:'Test3.g',schema:{description:'java.lang.String',type:'string'}},{'in':'path',name:'a2',description:'Test3.h',required:true},{'in':'query',name:'b2',description:'Test3.i'},{'in':'header',name:'D2',description:'Test3.j'},{'in':'path',name:'e',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'f',schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'g',schema:{description:'java.lang.String',type:'string'}}]", s.getPaths().get("/{a}").get("post").getParameters());
		assertObjectEquals("{'200':{description:'OK3',schema:{description:'java.lang.String',type:'string'}},'201':{description:'Test3.l'}}", s.getPaths().get("/{a}").get("post").getResponses());
	}

	// ====================================================================================================
	// test4 - Pull labels from resource bundles only. Values have localized variables to resolve.
	// ====================================================================================================
	@Test
	public void test4() throws Exception {

		Swagger s = client.doOptions(URL + "/test4").getResponse(Swagger.class);
		assertObjectEquals("{title:'baz',description:'baz'}", s.getInfo());
		assertObjectEquals("[{'in':'path',name:'a',description:'baz',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'b',description:'baz',schema:{description:'java.lang.String',type:'string'}},{'in':'body',description:'baz',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'D',description:'baz',schema:{description:'java.lang.String',type:'string'}},{'in':'path',name:'a2',description:'baz',required:true},{'in':'query',name:'b2',description:'baz'},{'in':'header',name:'D2',description:'baz'},{'in':'path',name:'e',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'f',schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'g',schema:{description:'java.lang.String',type:'string'}}]", s.getPaths().get("/{a}").get("post").getParameters());
		assertObjectEquals("{'200':{description:'foobazfoobazfoo',schema:{description:'java.lang.String',type:'string'}},'201':{description:'baz'}}", s.getPaths().get("/{a}").get("post").getResponses());
	}

	// ====================================================================================================
	// test5 - Pull labels from resource bundles only. Values have request variables to resolve.
	// ====================================================================================================
	@Test
	public void test5() throws Exception {

		Swagger s = client.doOptions(URL + "/test5").getResponse(Swagger.class);
		assertObjectEquals("{title:'baz2',description:'baz2'}", s.getInfo());
		assertObjectEquals("[{'in':'path',name:'a',description:'baz2',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'b',description:'baz2',schema:{description:'java.lang.String',type:'string'}},{'in':'body',description:'baz2',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'D',description:'baz2',schema:{description:'java.lang.String',type:'string'}},{'in':'path',name:'a2',description:'baz2',required:true},{'in':'query',name:'b2',description:'baz2'},{'in':'header',name:'D2',description:'baz2'},{'in':'path',name:'e',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'f',schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'g',schema:{description:'java.lang.String',type:'string'}}]", s.getPaths().get("/{a}").get("post").getParameters());
		assertObjectEquals("{'200':{description:'foobaz2foobaz2foo',schema:{description:'java.lang.String',type:'string'}},'201':{description:'baz2'}}", s.getPaths().get("/{a}").get("post").getResponses());
	}

	// ====================================================================================================
	// test6 - Pull labels from annotations only, but annotations contain variables.
	// ====================================================================================================
	@Test
	public void test6() throws Exception {

		Swagger s = client.doOptions(URL + "/test6").getResponse(Swagger.class);
		assertObjectEquals("{title:'baz',description:'baz'}", s.getInfo());
		assertObjectEquals("[{'in':'path',name:'a',description:'baz',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'b',description:'baz',schema:{description:'java.lang.String',type:'string'}},{'in':'body',description:'baz',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'D',description:'baz',schema:{description:'java.lang.String',type:'string'}},{'in':'path',name:'a2',description:'baz',required:true},{'in':'query',name:'b2',description:'baz'},{'in':'header',name:'D2',description:'baz'},{'in':'path',name:'e',required:true,schema:{description:'java.lang.String',type:'string'}},{'in':'query',name:'f',schema:{description:'java.lang.String',type:'string'}},{'in':'header',name:'g',schema:{description:'java.lang.String',type:'string'}}]", s.getPaths().get("/{a}").get("post").getParameters());
		assertObjectEquals("{'200':{description:'OK',schema:{description:'java.lang.String',type:'string'}},'201':{description:'baz',headers:{bar:{description:'baz',type:'string'}}}}", s.getPaths().get("/{a}").get("post").getResponses());
	}
}
