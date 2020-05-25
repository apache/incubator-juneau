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
package org.apache.juneau.msgpack;

import static org.junit.Assert.*;
import static org.junit.runners.MethodSorters.*;

import java.util.function.*;

import org.apache.juneau.*;
import org.apache.juneau.msgpack.annotation.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.svl.*;
import org.junit.*;

/**
 * Tests the @MsgPackConfig annotation.
 */
@FixMethodOrder(NAME_ASCENDING)
public class MsgPackConfigAnnotationTest {

	private static void check(String expected, Object o) {
		assertEquals(expected, TO_STRING.apply(o));
	}

	private static final Function<Object,String> TO_STRING = new Function<Object,String>() {
		@Override
		public String apply(Object t) {
			return t.toString();
		}
	};

	static VarResolverSession sr = VarResolver.create().vars(XVar.class).build().createSession();

	//-----------------------------------------------------------------------------------------------------------------
	// Basic tests
	//-----------------------------------------------------------------------------------------------------------------

	 @MsgPackConfig(
		addBeanTypes="$X{true}"
	)
	static class A {}
	static ClassInfo a = ClassInfo.of(A.class);

	@Test
	public void basicSerializer() throws Exception {
		AnnotationList al = a.getAnnotationList();
		MsgPackSerializerSession x = MsgPackSerializer.create().applyAnnotations(al, sr).build().createSession();
		check("true", x.isAddBeanTypes());
	}

	@Test
	public void basicParser() throws Exception {
		AnnotationList al = a.getAnnotationList();
		MsgPackParser.create().applyAnnotations(al, sr).build().createSession();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Annotation with no values.
	//-----------------------------------------------------------------------------------------------------------------

	@MsgPackConfig()
	static class B {}
	static ClassInfo b = ClassInfo.of(B.class);

	@Test
	public void noValuesSerializer() throws Exception {
		AnnotationList al = b.getAnnotationList();
		MsgPackSerializerSession x = MsgPackSerializer.create().applyAnnotations(al, sr).build().createSession();
		check("false", x.isAddBeanTypes());
	}

	@Test
	public void noValuesParser() throws Exception {
		AnnotationList al = b.getAnnotationList();
		MsgPackParser.create().applyAnnotations(al, sr).build().createSession();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// No annotation.
	//-----------------------------------------------------------------------------------------------------------------

	static class C {}
	static ClassInfo c = ClassInfo.of(C.class);

	@Test
	public void noAnnotationSerializer() throws Exception {
		AnnotationList al = c.getAnnotationList();
		MsgPackSerializerSession x = MsgPackSerializer.create().applyAnnotations(al, sr).build().createSession();
		check("false", x.isAddBeanTypes());
	}

	@Test
	public void noAnnotationParser() throws Exception {
		AnnotationList al = c.getAnnotationList();
		MsgPackParser.create().applyAnnotations(al, sr).build().createSession();
	}
}
