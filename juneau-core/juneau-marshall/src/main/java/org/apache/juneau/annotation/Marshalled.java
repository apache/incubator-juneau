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
package org.apache.juneau.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import org.apache.juneau.*;

/**
 * Annotation that can be applied to classes to control how they are marshalled.
 *
 * <p>
 * Can be used in the following locations:
 * <ul>
 * 	<li>Marshalled classes.
 * 	<li><ja>@Rest</ja>-annotated classes and <ja>@RestMethod</ja>-annotated methods when an {@link #on()} value is specified.
 * </ul>
 *
 * <p>
 * This annotation is typically only applied to non-bean classes.  The {@link Bean @Bean} annotation contains equivalent
 * functionality for bean classes.
 */
@Documented
@Target({METHOD,TYPE})
@Retention(RUNTIME)
@Inherited
@Repeatable(MarshalledAnnotation.Array.class)
@PropertyStoreApply(MarshalledAnnotation.Apply.class)
public @interface Marshalled {

	/**
	 * POJO example.
	 *
	 * <p>
	 * Specifies an example of the specified class in Simplified JSON format.
	 *
	 * <p>
	 * Examples are used in cases such as POJO examples in Swagger documents.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Marshalled</ja>(example=<js>"{foo:'bar'}"</js>)
	 * 	<jk>public class</jk> MyClass {...}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Setting applies to specified class and all subclasses.
	 * 	<li>
	 * 		Keys are the class of the example.
	 * 		<br>Values are Simple-JSON representation of that class.
	 * 	<li>
	 * 		POJO examples can also be defined on classes via the following:
	 * 		<ul class='spaced-list'>
	 * 			<li>A static field annotated with {@link Example @Example}.
	 * 			<li>A static method annotated with {@link Example @Example} with zero arguments or one {@link BeanSession} argument.
	 * 			<li>A static method with name <c>example</c> with no arguments or one {@link BeanSession} argument.
	 * 		</ul>
	 * 	<li>
	 * 		Supports {@doc DefaultVarResolver} (e.g. <js>"$C{myConfigVar}"</js>).
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='ja'>{@link Example}
	 * </ul>
	 */
	String example() default "";

	/**
	 * Implementation class.
	 *
	 * <p>
	 * For interfaces and abstract classes this method can be used to specify an implementation class for the
	 * interface/abstract class so that instances of the implementation class are used when instantiated (e.g. during a
	 * parse).
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Marshalled</ja>(implClass=MyInterfaceImpl.<jk>class</jk>)
	 * 	<jk>public class</jk> MyInterface {...}
	 * <p>
	 */
	Class<?> implClass() default Null.class;

	/**
	 * Dynamically apply this annotation to the specified classes.
	 *
	 * <p>
	 * Used in conjunction with {@link BeanContextBuilder#applyAnnotations(Class...)} to dynamically apply an annotation to an existing class.
	 * It is ignored when the annotation is applied directly to classes.
	 *
	 * <p>
	 * The following example shows the equivalent methods for applying the {@link Bean @Bean} annotation to REST methods:
	 * <p class='bpcode w800'>
	 * 	<jc>// Class with explicit annotation.</jc>
	 * 	<ja>@Marshalled</ja>(example=<js>"{foo:'bar'}"</js>)
	 * 	<jk>public class</jk> A {...}
	 *
	 * 	<jc>// Class with annotation applied via @BeanConfig</jc>
	 * 	<jk>public class</jk> B {...}
	 *
	 * 	<jc>// Java REST method with @BeanConfig annotation.</jc>
	 * 	<ja>@RestMethod</ja>(...)
	 * 	<ja>@Marshalled</ja>(on=<js>"B"</js>, example=<js>"{foo:'bar'}"</js>)
	 * 	<jk>public void</jk> doFoo() {...}
	 * </p>
	 *
	 * <h5 class='section'>Valid patterns:</h5>
	 * <ul class='spaced-list'>
	 *  <li>Classes:
	 * 		<ul>
	 * 			<li>Fully qualified:
	 * 				<ul>
	 * 					<li><js>"com.foo.MyClass"</js>
	 * 				</ul>
	 * 			<li>Fully qualified inner class:
	 * 				<ul>
	 * 					<li><js>"com.foo.MyClass$Inner1$Inner2"</js>
	 * 				</ul>
	 * 			<li>Simple:
	 * 				<ul>
	 * 					<li><js>"MyClass"</js>
	 * 				</ul>
	 * 			<li>Simple inner:
	 * 				<ul>
	 * 					<li><js>"MyClass$Inner1$Inner2"</js>
	 * 					<li><js>"Inner1$Inner2"</js>
	 * 					<li><js>"Inner2"</js>
	 * 				</ul>
	 * 		</ul>
	 * 	<li>A comma-delimited list of anything on this list.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc DynamicallyAppliedAnnotations}
	 * </ul>
	 */
	String[] on() default {};

	/**
	 * Dynamically apply this annotation to the specified classes.
	 *
	 * <p>
	 * Identical to {@link #on()} except allows you to specify class objects instead of a strings.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc DynamicallyAppliedAnnotations}
	 * </ul>
	 */
	Class<?>[] onClass() default {};
}