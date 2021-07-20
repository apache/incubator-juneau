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
package org.apache.juneau.assertions;

import static org.apache.juneau.assertions.Assertions.*;
import static org.apache.juneau.assertions.AssertionPredicates.*;
import static org.junit.runners.MethodSorters.*;

import java.util.*;
import java.util.function.*;

import org.apache.juneau.json.*;
import org.apache.juneau.serializer.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class ArrayAssertion_Test {

	//------------------------------------------------------------------------------------------------------------------
	// Helpers
	//------------------------------------------------------------------------------------------------------------------

	public static final A A = new A();
	public static class A {
		public int a = 1, b = 2;
		@Override public String toString() { return "a="+a+",b="+b; }
	}

	private <E> ArrayAssertion<E> test(E[] value) {
		return assertArray(value).silent();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Basic tests
	//-----------------------------------------------------------------------------------------------------------------

	@Test
	public void a01_msg() throws Exception {
		assertThrown(()->test(null).msg("Foo {0}", 1).exists()).message().is("Foo 1");
		assertThrown(()->test(null).msg("Foo {0}", 1).throwable(RuntimeException.class).exists()).isExactType(RuntimeException.class).message().is("Foo 1");
	}

	@Test
	public void a02_stdout() throws Exception {
		test(null).stdout();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Transform tests
	//-----------------------------------------------------------------------------------------------------------------

	@Test
	public void ba01a_asString() throws Exception {
		Integer[] x = {1,2}, nil = null;
		test(x).asString().is("[1, 2]");
		test(nil).asString().isNull();
	}

	@Test
	public void ba01b_asString_wSerializer() throws Exception {
		Integer[] x = {1,2}, nil = null;
		WriterSerializer s = SimpleJsonSerializer.DEFAULT;
		test(x).asString(s).is("[1,2]");
		test(nil).asString(s).is("null");
	}

	@Test
	public void ba01c_asString_wPredicate() throws Exception {
		Integer[] x1 = {1,2};
		test(x1).asString(x -> "foo").is("foo");
	}

	@Test
	public void ba02_asJson() throws Exception {
		Integer[] x = {1,2}, nil = null;
		test(x).asJson().is("[1,2]");
		test(nil).asJson().is("null");
	}

	@Test
	public void ba03_asJsonSorted() throws Exception {
		Integer[] x1 = {2,1}, nil = null;
		test(x1).asJsonSorted().is("[1,2]");
		test(nil).asJsonSorted().is("null");
	}

	@Test
	public void ba04_apply() throws Exception {
		Integer[] x1 = {1,2}, x2 = {3,4};
		test(x1).apply(x -> x2).is(x2);
	}

	@Test
	public void bb01_asStrings() throws Exception {
		Integer[] x1 = {1,2}, nil = null;
		test(x1).asStrings().join().is("12");
		test(nil).asStrings().isNull();
	}

	@Test
	public void bb02_asBeanList() throws Exception {
		A[] x = {A,A}, nil = null;
		test(x).asBeanList().property("a").asJson().is("[1,1]");
		test(nil).asBeanList().isNull();
	}

	@Test
	public void bb03_item() throws Exception {
		A[] x = {A,A}, nil = null;
		test(x).item(0).asBean().property("a").is(1);
		test(x).item(-1).isNull();
		test(x).item(2).isNull();
		test(nil).item(0).isNull();
	}

	@Test
	public void bb04a_sorted() throws Exception {
		Integer[] x = {2,3,1}, nil = null;
		test(x).sorted().asJson().is("[1,2,3]");
		test(x).sorted(Comparator.reverseOrder()).asJson().is("[3,2,1]");
		test(x).sorted(null).asJson().is("[1,2,3]");
		test(nil).sorted().isNull();
	}

	@Test
	public void bb04b_sorted_wComparator() throws Exception {
		Integer[] x = {2,3,1}, nil = null;
		test(x).sorted().asJson().is("[1,2,3]");
		test(x).sorted(Comparator.reverseOrder()).asJson().is("[3,2,1]");
		test(x).sorted(null).asJson().is("[1,2,3]");
		test(nil).sorted().isNull();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Test tests
	//-----------------------------------------------------------------------------------------------------------------

	@Test
	public void ca01_exists() throws Exception {
		Integer[] x = {}, nil = null;
		test(x).exists().exists();
		assertThrown(()->test(nil).exists()).message().is("Value was null.");
	}

	@Test
	public void ca02_isNull() throws Exception {
		Integer[] x = {}, nil = null;
		test(nil).isNull();
		assertThrown(()->test(x).isNull()).message().is("Value was not null.");
	}

	@Test
	public void ca03_isNotNull() throws Exception {
		Integer[] x = {}, nil = null;
		test(x).isNotNull();
		assertThrown(()->test(nil).isNotNull()).message().is("Value was null.");
	}

	@Test
	public void ca04a_is_T() throws Exception {
		Integer[] x1 = {null,1,2}, x1a = {null,1,2}, x1b = {null,1,3}, nil = null;
		test(x1).is(x1);
		test(x1).is(x1a);
		test(nil).is(nil);
		assertThrown(()->test(x1).is(x1b)).message().oneLine().is("Unexpected value.Expect='[null, 1, 3]'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(x1).is(nil)).message().oneLine().is("Unexpected value.Expect='null'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(nil).is(x1b)).message().oneLine().is("Unexpected value.Expect='[null, 1, 3]'.Actual='null'.");
	}

	@Test
	public void ca04b_is_predicate() throws Exception {
		Integer[] x1 = {null,1,2};
		test(x1).is(x->x.length==3);
		assertThrown(()->test(x1).is(x->x.length==2)).message().oneLine().is("Unexpected value: '[null, 1, 2]'.");
		assertThrown(()->test(x1).is(ne(x1))).message().oneLine().is("Value unexpectedly matched.Value='[null, 1, 2]'.");
	}

	@Test
	public void ca05_isNot() throws Exception {
		Integer[] x1 = {null,1,2}, x1a = {null,1,2}, x2 = {null,1,3}, nil = null;
		test(x1).isNot(x2);
		test(x1).isNot(nil);
		test(nil).isNot(x1);
		assertThrown(()->test(x1).isNot(x1a)).message().oneLine().is("Unexpected value.Did not expect='[null, 1, 2]'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(nil).isNot(nil)).message().oneLine().is("Unexpected value.Did not expect='null'.Actual='null'.");
	}

	@Test
	public void ca06_isAny() throws Exception {
		Integer[] x1 = {null,1,2}, x1a = {null,1,2}, x2 = {null,1,3}, nil = null;
		test(x1).isAny(x1a, x2);
		assertThrown(()->test(x1).isAny(x2)).message().oneLine().is("Expected value not found.Expect='[[null, 1, 3]]'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(x1).isAny()).message().oneLine().is("Expected value not found.Expect='[]'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(nil).isAny(x2)).message().oneLine().is("Expected value not found.Expect='[[null, 1, 3]]'.Actual='null'.");
	}

	@Test
	public void ca07_isNotAny() throws Exception {
		Integer[] x1 = {null,1,2}, x1a = {null,1,2}, x2 = {null,1,3}, nil = null;
		test(x1).isNotAny(x2);
		test(x1).isNotAny();
		test(nil).isNotAny(x2);
		assertThrown(()->test(x1).isNotAny(x1a)).message().oneLine().is("Unexpected value found.Unexpected='[null, 1, 2]'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(nil).isNotAny(nil)).message().oneLine().is("Unexpected value found.Unexpected='null'.Actual='null'.");
	}

	@Test
	public void ca08_isSame() throws Exception {
		Integer[] x1 = {null,1,2}, x1a = {null,1,2}, nil = null;
		test(x1).isSame(x1);
		test(nil).isSame(nil);
		assertThrown(()->test(x1).isSame(x1a)).message().oneLine().matches("Not the same value.Expect='[null, 1, 2](Integer[]@*)'.Actual='[null, 1, 2](Integer[]@*)'.");
		assertThrown(()->test(nil).isSame(x1a)).message().oneLine().matches("Not the same value.Expect='[null, 1, 2](Integer[]@*)'.Actual='null(null)'.");
		assertThrown(()->test(x1).isSame(nil)).message().oneLine().matches("Not the same value.Expect='null(null)'.Actual='[null, 1, 2](Integer[]@*)'.");
	}

	@Test
	public void ca09_isSameJsonAs() throws Exception {
		Integer[] x1 = {null,1,2}, x1a = {null,1,2}, x2 = {null,1,3}, nil = null;
		test(x1).isSameJsonAs(x1a);
		test(nil).isSameJsonAs(nil);
		assertThrown(()->test(x1a).isSameJsonAs(x2)).message().oneLine().is("Unexpected comparison.Expect='[null,1,3]'.Actual='[null,1,2]'.");
		assertThrown(()->test(nil).isSameJsonAs(x2)).message().oneLine().is("Unexpected comparison.Expect='[null,1,3]'.Actual='null'.");
		assertThrown(()->test(x1).isSameJsonAs(nil)).message().oneLine().is("Unexpected comparison.Expect='null'.Actual='[null,1,2]'.");
	}

	@Test
	public void ca10_isSameSortedJsonAs() throws Exception {
		Integer[] x1 = {1,2}, x1a = {2,1}, x2 = {1,3}, nil = null;  // Note that nulls are not sortable.
		test(x1).isSameSortedJsonAs(x1a);
		test(nil).isSameSortedJsonAs(nil);
		assertThrown(()->test(x1a).isSameSortedJsonAs(x2)).message().oneLine().is("Unexpected comparison.Expect='[1,3]'.Actual='[1,2]'.");
		assertThrown(()->test(nil).isSameSortedJsonAs(x2)).message().oneLine().is("Unexpected comparison.Expect='[1,3]'.Actual='null'.");
		assertThrown(()->test(x1).isSameSortedJsonAs(nil)).message().oneLine().is("Unexpected comparison.Expect='null'.Actual='[1,2]'.");
	}

	@Test
	public void ca11_isSameSerializedAs() throws Exception {
		Integer[] x1 = {null,1,2}, x1a = {null,1,2}, x2 = {null,1,3}, nil = null;
		WriterSerializer s = SimpleJsonSerializer.DEFAULT;
		test(x1).isSameSerializedAs(x1a, s);
		test(nil).isSameSerializedAs(nil, s);
		assertThrown(()->test(x1a).isSameSerializedAs(x2, s)).message().oneLine().is("Unexpected comparison.Expect='[null,1,3]'.Actual='[null,1,2]'.");
		assertThrown(()->test(nil).isSameSerializedAs(x2, s)).message().oneLine().is("Unexpected comparison.Expect='[null,1,3]'.Actual='null'.");
		assertThrown(()->test(x1).isSameSerializedAs(nil, s)).message().oneLine().is("Unexpected comparison.Expect='null'.Actual='[null,1,2]'.");
	}

	@Test
	public void ca12_isType() throws Exception {
		Integer[] x = {}, nil = null;
		test(x).isType(Integer[].class);
		test(x).isType(Object[].class);
		test(x).isType(Object.class);
		assertThrown(()->test(x).isType(String[].class)).message().oneLine().is("Unexpected type.Expect='[Ljava.lang.String;'.Actual='[Ljava.lang.Integer;'.");
		assertThrown(()->test(nil).isType(Integer[].class)).message().oneLine().is("Value was null.");
		assertThrown(()->test(x).isType(null)).message().oneLine().is("Argument 'parent' cannot be null.");
	}

	@Test
	public void ca13_isExactType() throws Exception {
		Integer[] x = {}, nil = null;
		test(x).isExactType(Integer[].class);
		assertThrown(()->test(x).isExactType(String.class)).message().oneLine().is("Unexpected type.Expect='java.lang.String'.Actual='[Ljava.lang.Integer;'.");
		assertThrown(()->test(nil).isExactType(String.class)).message().oneLine().is("Value was null.");
		assertThrown(()->test(x).isExactType(null)).message().oneLine().is("Argument 'parent' cannot be null.");
	}

	@Test
	public void ca14_isString() throws Exception {
		Integer[] x = {null,1,2}, nil = null;
		test(x).isString("[null, 1, 2]");
		test(nil).isString(null);
		assertThrown(()->test(x).isString("bad")).message().oneLine().is("String differed at position 0.Expect='bad'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(x).isString(null)).message().oneLine().is("String differed at position 0.Expect='null'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(nil).isString("bad")).message().oneLine().is("String differed at position 0.Expect='bad'.Actual='null'.");
	}

	@Test
	public void ca15_isJson() throws Exception {
		Integer[] x = {null,1,2}, nil = null;
		test(x).isJson("[null,1,2]");
		test(nil).isJson("null");
		assertThrown(()->test(x).isJson("bad")).message().oneLine().is("String differed at position 0.Expect='bad'.Actual='[null,1,2]'.");
		assertThrown(()->test(x).isJson(null)).message().oneLine().is("String differed at position 0.Expect='null'.Actual='[null,1,2]'.");
		assertThrown(()->test(nil).isJson("bad")).message().oneLine().is("String differed at position 0.Expect='bad'.Actual='null'.");
	}

	@Test
	public void cb01_is_predicates() throws Exception {
		Integer[] x1 = {null,1,2}, nil = null;
		test(x1).is(isNull(),eq("1"),eq("2"));
		test(x1).is(isNull(),eq(1),eq(2));
		assertThrown(()->test(x1).is(isNull(),eq("1"),eq("3"))).message().oneLine().is("Array did not contain expected value at index 2.Value did not match expected.Expect='3'.Actual='2'.");
		assertThrown(()->test(nil).is(isNull(),eq("1"),eq("3"))).message().is("Value was null.");
		test(x1).is((Predicate<Integer>)null,null,null);
	}

	@Test
	public void cb02_any() throws Exception {
		Integer[] x1 = {2,3,1}, nil = null;
		test(x1).any(x -> x .equals(3));
		test(x1).any(eq(3));
		assertThrown(()->test(x1).any(x -> x.equals(4))).message().is("Array did not contain any matching value.\n\tValue='[2, 3, 1]'.");
		assertThrown(()->test(x1).any(null)).message().is("Argument 'test' cannot be null.");
		assertThrown(()->test(nil).any(x->true)).message().is("Value was null.");
	}

	@Test
	public void cb03_all() throws Exception {
		Integer[] x1 = {2,3,1}, nil = null;
		test(x1).all(x -> x < 4);
		assertThrown(()->test(x1).all(x -> x < 3)).message().is("Array contained non-matching value at index 1.\n\tUnexpected value: '3'.");
		assertThrown(()->test(x1).all(ne(3))).message().is("Array contained non-matching value at index 1.\n\tValue unexpectedly matched.\n\tValue='3'.");
		assertThrown(()->test(x1).all(null)).message().is("Argument 'test' cannot be null.");
		assertThrown(()->test(nil).all(x->true)).message().is("Value was null.");
	}

	@Test
	public void cb04_isEmpty() throws Exception {
		String[] x1={}, x2={"foo","bar"}, nil = null;
		test(x1).isEmpty();
		assertThrown(()->test(x2).isEmpty()).message().is("Array was not empty.");
		assertThrown(()->test(nil).isEmpty()).message().is("Value was null.");
	}

	@Test
	public void cb05_isNotEmpty() throws Exception {
		String[] x1={}, x2={"foo","bar"}, nil = null;
		test(x2).isNotEmpty();
		assertThrown(()->test(x1).isNotEmpty()).message().is("Array was empty.");
		assertThrown(()->test(nil).isNotEmpty()).message().is("Value was null.");
	}

	@Test
	public void cb06_contains() throws Exception {
		Integer[] x1 = {null,1,2}, nil = null;
		test(x1).contains(null);
		test(x1).contains(1);
		assertThrown(()->test(x1).contains(3)).message().oneLine().is("Array did not contain expected value.Expect='3'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(nil).contains(3)).message().is("Value was null.");
		assertThrown(()->test(nil).contains(null)).message().is("Value was null.");
	}

	@Test
	public void cb07_doesNotContain() throws Exception {
		Integer[] x1 = {null,1,2}, nil = null;
		test(x1).doesNotContain(3);
		assertThrown(()->test(x1).doesNotContain(1)).message().oneLine().is("Array contained unexpected value.Unexpected='1'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(x1).doesNotContain(null)).message().oneLine().is("Array contained unexpected value.Unexpected='null'.Actual='[null, 1, 2]'.");
		assertThrown(()->test(nil).doesNotContain(3)).message().is("Value was null.");
	}

	@Test
	public void cb08_isSize() throws Exception {
		String[] x1={}, x2={"foo","bar"}, nil = null;
		test(x1).isSize(0);
		test(x2).isSize(2);
		assertThrown(()->test(x1).isSize(2)).message().is("Array did not have the expected size.\n\tExpect=2.\n\tActual=0.");
		assertThrown(()->test(x2).isSize(0)).message().is("Array did not have the expected size.\n\tExpect=0.\n\tActual=2.");
		assertThrown(()->test(nil).isSize(0)).message().is("Value was null.");
	}
}
