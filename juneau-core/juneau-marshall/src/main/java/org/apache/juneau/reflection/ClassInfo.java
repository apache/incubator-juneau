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
package org.apache.juneau.reflection;

import static org.apache.juneau.internal.ClassFlags.*;
import static org.apache.juneau.internal.StringUtils.*;
import static org.apache.juneau.internal.CollectionUtils.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.utils.*;

/**
 * Lightweight utility class for introspecting information about a class.
 *
 * <p>
 * Provides various convenience methods for introspecting fields/methods/annotations
 * that aren't provided by the standard Java reflection APIs.
 *
 * <p>
 * Objects are designed to be lightweight to create and threadsafe.
 *
 * <h5 class='figure'>
 * <p class='bpcode w800'>
 * 	<jc>// Wrap our class inside a ClassInfo.</jc>
 * 	ClassInfo ci = ClassInfo.<jsm>of</jsm>(MyClass.<jk>class</jk>);
 *
 * 	<jc>// Get all methods in parent-to-child order, sorted alphabetically per class.</jc>
 * 	<jk>for</jk> (MethodInfo mi : ci.getAllMethodInfos(<jk>true</jk>, <jk>true</jk>)) {
 * 		<jc>// Do something with it.</jc>
 * 	}
 *
 * 	<jc>// Get all class-level annotations in parent-to-child order.</jc>
 * 	<jk>for</jk> (MyAnnotation a : ci.getAnnotations(MyAnnotation.<jk>class</jk>, <jk>true</jk>)) {
 * 		// Do something with it.
 * 	}
 * </p>
 */
@BeanIgnore
public final class ClassInfo {

	private final Type t;
	private final Class<?> c;
	private List<ClassInfo> interfaces;
	private List<MethodInfo> publicMethods, declaredMethods;
	private List<ConstructorInfo> publicConstructors, declaredConstructors;
	private List<FieldInfo> publicFields, declaredFields;
	private Map<Class<?>,Optional<Annotation>> annotationMap, declaredAnnotationMap;

	/**
	 * Constructor.
	 *
	 * @param t The class type.
	 */
	protected ClassInfo(Type t) {
		this.t = t;
		this.c = ClassUtils.toClass(t);
	}

	/**
	 * Constructor.
	 *
	 * @param c The class type.
	 */
	protected ClassInfo(Class<?> c) {
		this.t = c;
		this.c = c;
	}

	/**
	 * Returns a class info wrapper around the specified class type.
	 *
	 * @param t The class type.
	 * @return The constructed class info, or <jk>null</jk> if the type was <jk>null</jk>.
	 */
	public static ClassInfo of(Type t) {
		if (t == null)
			return null;
		return new ClassInfo(t);
	}

	/**
	 * Returns a class info wrapper around the specified class type.
	 *
	 * @param c The class type.
	 * @return The constructed class info, or <jk>null</jk> if the type was <jk>null</jk>.
	 */
	public static ClassInfo of(Class<?> c) {
		if (c == null)
			return null;
		return new ClassInfo(c);
	}

	/**
	 * Same as using the constructor, but operates on an object instance.
	 *
	 * @param o The class instance.
	 * @return The constructed class info.
	 */
	public static ClassInfo of(Object o) {
		if (o == null)
			return null;
		return new ClassInfo(o.getClass());
	}

	/**
	 * Returns the wrapped class as a {@link Type}.
	 *
	 * @return The wrapped class as a {@link Type}.
	 */
	public Type innerType() {
		return t;
	}

	/**
	 * Returns the wrapped class as a {@link Class}.
	 *
	 * @return The wrapped class as a {@link Class}, or <jk>null</jk> if it's not a class (e.g. it's a {@link ParameterizedType}).
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> inner() {
		return (Class<T>)c;
	}

	/**
	 * If this class is a parameterized {@link Value} type, returns the parameterized type.
	 *
	 * @return The parameterized type, or this object if this class is not a parameterized {@link Value} type.
	 */
	public ClassInfo resolved() {
		if (Value.isType(t))
			return of(Value.getParameterType(t));
		return this;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Parent classes and interfaces.
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the parent class wrapped in {@link ClassInfo}.
	 *
	 * @return The parent class wrapped in {@link ClassInfo}, or <jk>null</jk> if the class has no parent.
	 */
	public ClassInfo getParent() {
		return of(c.getSuperclass());
	}

	/**
	 * Returns a list of interfaces defined on this class wrapped in {@link ClassInfo}.
	 *
	 * <p>
	 * Does not include interfaces defined on parent classes.
	 *
	 * @return
	 * 	An unmodifiable list of interfaces defined on this class wrapped in {@link ClassInfo}.
	 */
	public List<ClassInfo> getInterfaces() {
		if (interfaces == null) {
			Class<?>[] ii = c.getInterfaces();
			List<ClassInfo> l = new ArrayList<>(ii.length);
			for (Class<?> i : ii)
				l.add(of(i));
			interfaces = unmodifiableList(l);
		}
		return interfaces;
	}

	/**
	 * Returns a list including this class and all parent classes in child-to-parent order.
	 *
	 * <p>
	 * Does not include interfaces.
	 *
	 * @return An unmodifiable list including this class and all parent classes in child-to-parent order.
	 */
	public List<ClassInfo> getParents() {
		return getParents(false, false);
	}

	/**
	 * Returns a list including this class and all parent classes and interfaces.
	 *
	 * <h5 class='figure'>Examples</h5>
	 * <p class='bpcode'>
	 * 	<jc>// Class hierarchy</jc>
	 * 	<jk>interface</jk> I1 {}
	 * 	<jk>interface</jk> I2 <jk>extends</jk> I11 {}
	 * 	<jk>interface</jk> I3 {}
	 * 	<jk>interface</jk> I4 {}
	 * 	<jk>class</jk> CA <jk>implements</jk> I1, I2 {}
	 * 	<jk>class</jk> CB <jk>extends</jk> CA <jk>implements</jk> I3 {}
	 * 	<jk>class</jk> CC <jk>extends</jk> CB {}
	 *
	 * 	<jc>// Examples</jc>
	 * 	ClassInfo cc = ClassInfo.<jsm>of</jsm>(CC.<jk>class</jk>);
	 *
	 * 	<jc>// Parent last, no interfaces.
	 * 	cc.getParentInfos();             // CC,CB,CA
	 * 	cc.getParentInfos(false,false);  // CC,CB,CA
	 *
	 * 	<jc>// Parent first, no interfaces.
	 * 	cc.getParentInfos(true,false);   // CA,CB,CC
	 *
	 * 	<jc>// Parent last, include interfaces.
	 * 	cc.getParentInfos(false,true);   // CC,CB,I3,CA,I1,I2
	 *
	 * 	<jc>// Parent first, include interfaces.
	 * 	cc.getParentInfos(true,true);    // I2,I1,CA,I3,CB,CC
	 * </p>
	 *
	 * @param parentFirst
	 * 	If <jk>true</jk>, results are ordered parent-first.
	 * @param includeInterfaces
	 * 	If <jk>true</jk>, results include interfaces.
	 * @return An unmodifiable list including this class and all parent classes and interfaces.
	 */
	public List<ClassInfo> getParents(boolean parentFirst, boolean includeInterfaces) {
		return unmodifiableList(findParents(new ArrayList<>(), c, parentFirst, includeInterfaces));
	}

	private static List<ClassInfo> findParents(List<ClassInfo> l, Class<?> c, boolean parentFirst, boolean includeInterfaces) {
		if (parentFirst) {
			if (hasSuperclass(c))
				findParents(l, c.getSuperclass(), parentFirst, includeInterfaces);
			if (includeInterfaces)
				for (Class<?> i : iterable(c.getInterfaces(), true))
					l.add(of(i));
			l.add(of(c));
		} else {
			l.add(of(c));
			if (includeInterfaces)
				for (Class<?> i : c.getInterfaces())
					l.add(of(i));
			if (hasSuperclass(c))
				findParents(l, c.getSuperclass(), parentFirst, includeInterfaces);
		}
		return l;
	}

	private static boolean hasSuperclass(Class<?> c) {
		if (c.getSuperclass() == Object.class)
			return false;
		if (c.getSuperclass() == null)
			return false;
		return true;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns all public methods on this class.
	 *
	 * <p>
	 * Methods defined on the {@link Object} class are excluded from the results.
	 *
	 * @return All public methods on this class in alphabetical order.
	 */
	public List<MethodInfo> getPublicMethods() {
		if (publicMethods == null) {
			Method[] mm = c.getMethods();
			List<MethodInfo> l = new ArrayList<>(mm.length);
			for (Method m : mm)
				if (m.getDeclaringClass() != Object.class)
					l.add(MethodInfo.of(this, m));
			l.sort(null);
			publicMethods = Collections.unmodifiableList(l);
		}
		return publicMethods;
	}

	/**
	 * Returns all methods declared on this class.
	 *
	 * @return All methods declared on this class in alphabetical order.
	 */
	public List<MethodInfo> getDeclaredMethods() {
		if (declaredMethods == null) {
			Method[] mm = c.getDeclaredMethods();
			List<MethodInfo> l = new ArrayList<>(mm.length);
			for (Method m : mm)
				if (! "$jacocoInit".equals(m.getName())) // Jacoco adds its own simulated methods.
					l.add(MethodInfo.of(this, m));
			l.sort(null);
			declaredMethods = Collections.unmodifiableList(l);
		}
		return declaredMethods;
	}

	/**
	 * Returns all declared methods on this class and all parent classes in child-to-parent order.
	 *
	 * @return All declared methods on this class and all parent classes in child-to-parent order in
	 * 	alphabetical order per class.
	 */
	public List<MethodInfo> getAllMethods() {
		return getAllMethods(false);
	}

	/**
	 * Returns all declared methods on this class and all parent classes in child-to-parent order.
	 *
	 * @param parentFirst If <jk>true</jk>, methods on parent classes are listed first.
	 * @return All declared methods on this class and all parent classes in alphabetical order per class.
	 */
	public List<MethodInfo> getAllMethods(boolean parentFirst) {
		return findAllMethods(parentFirst);
	}

	private List<MethodInfo> findAllMethods(boolean parentFirst) {
		List<MethodInfo> l = new ArrayList<>();
		for (ClassInfo c : getParents(parentFirst, true))
			c.appendDeclaredMethods(l);
		return l;
	}

	private List<MethodInfo> appendDeclaredMethods(List<MethodInfo> l) {
		l.addAll(getDeclaredMethods());
		return l;
	}


	//-----------------------------------------------------------------------------------------------------------------
	// Special methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Finds the public static "fromString" method on this class.
	 *
	 * <p>
	 * Looks for the following method names:
	 * <ul>
	 * 	<li><code>fromString</code>
	 * 	<li><code>fromValue</code>
	 * 	<li><code>valueOf</code>
	 * 	<li><code>parse</code>
	 * 	<li><code>parseString</code>
	 * 	<li><code>forName</code>
	 * 	<li><code>forString</code>
	 * </ul>
	 *
	 * @return The static method, or <jk>null</jk> if it couldn't be found.
	 */
	public MethodInfo getFromStringMethod() {
		for (MethodInfo m : getPublicMethods())
			if (m.isAll(STATIC, PUBLIC, NOT_DEPRECATED)
					&& m.hasReturnType(c)
					&& m.hasArgs(String.class)
					&& isOneOf(m.getName(), "create","fromString","fromValue","valueOf","parse","parseString","forName","forString"))
				return m;
		return null;
	}

	/**
	 * Find the public static creator method on this class.
	 *
	 * <p>
	 * Looks for the following method names:
	 * <ul>
	 * 	<li><code>create</code>
	 * 	<li><code>from</code>
	 * 	<li><code>fromIC</code>
	 * </ul>
	 *
	 * @param ic The argument type.
	 * @return The static method, or <jk>null</jk> if it couldn't be found.
	 */
	public MethodInfo getStaticCreateMethod(Class<?> ic) {
		for (MethodInfo m : getPublicMethods()) {
			if (m.isAll(STATIC, PUBLIC, NOT_DEPRECATED) && m.hasReturnType(c) && m.hasArgs(ic)) {
				String n = m.getName();
				if (isOneOf(n, "create","from") || (n.startsWith("from") && n.substring(4).equals(ic.getSimpleName()))) {
					return m;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the <code>public static Builder create()</code> method on this class.
	 *
	 * @return The <code>public static Builder create()</code> method on this class, or <jk>null</jk> if it doesn't exist.
	 */
	public MethodInfo getBuilderCreateMethod() {
		for (MethodInfo m : getDeclaredMethods())
			if (m.isAll(PUBLIC, STATIC) && m.hasName("create") && (!m.hasReturnType(void.class)))
				return m;
		return null;
	}

	/**
	 * Returns the <code>T build()</code> method on this class.
	 *
	 * @return The <code>T build()</code> method on this class, or <jk>null</jk> if it doesn't exist.
	 */
	public MethodInfo getBuilderBuildMethod() {
		for (MethodInfo m : getDeclaredMethods())
			if (m.isAll(NOT_STATIC) && m.hasName("build") && (!m.hasArgs()) && (!m.hasReturnType(void.class)))
				return m;
		return null;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Constructors
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns all the public constructors defined on this class.
	 *
	 * @return All public constructors defined on this class.
	 */
	public List<ConstructorInfo> getPublicConstructors() {
		if (publicConstructors == null) {
			Constructor<?>[] cc = c.getConstructors();
			List<ConstructorInfo> l = new ArrayList<>(cc.length);
			for (Constructor<?> ccc : cc)
				l.add(ConstructorInfo.of(this, ccc));
			l.sort(null);
			publicConstructors = Collections.unmodifiableList(l);
		}
		return publicConstructors;
	}

	/**
	 * Returns all the constructors defined on this class.
	 *
	 * @return All constructors defined on this class.
	 */
	public List<ConstructorInfo> getDeclaredConstructors() {
		if (declaredConstructors == null) {
			Constructor<?>[] cc = c.getDeclaredConstructors();
			List<ConstructorInfo> l = new ArrayList<>(cc.length);
			for (Constructor<?> ccc : cc)
				l.add(ConstructorInfo.of(this, ccc));
			l.sort(null);
			declaredConstructors = Collections.unmodifiableList(l);
		}
		return declaredConstructors;
	}

	/**
	 * Finds the public constructor that can take in the specified arguments.
	 *
	 * @param args The argument types we want to pass into the constructor.
	 * @return
	 * 	The constructor, or <jk>null</jk> if a public constructor could not be found that takes in the specified
	 * 	arguments.
	 */
	public ConstructorInfo getPublicConstructor(Class<?>...args) {
		return getConstructor(Visibility.PUBLIC, false, args);
	}

	/**
	 * Finds the public constructor that can take in the specified arguments.
	 *
	 * @param args The arguments we want to pass into the constructor.
	 * @return
	 * 	The constructor, or <jk>null</jk> if a public constructor could not be found that takes in the specified
	 * 	arguments.
	 */
	public ConstructorInfo getPublicConstructor(Object...args) {
		return getConstructor(Visibility.PUBLIC, false, ClassUtils.getClasses(args));
	}

	/**
	 * Finds the public constructor that can take in the specified arguments using fuzzy-arg matching.
	 *
	 * @param args The arguments we want to pass into the constructor.
	 * @return
	 * 	The constructor, or <jk>null</jk> if a public constructor could not be found that takes in the specified
	 * 	arguments.
	 */
	public ConstructorInfo getPublicConstructorFuzzy(Object...args) {
		return getConstructor(Visibility.PUBLIC, true, ClassUtils.getClasses(args));
	}

	/**
	 * Finds a constructor with the specified parameters without throwing an exception.
	 *
	 * @param vis The minimum visibility.
	 * @param argTypes
	 * 	The argument types in the constructor.
	 * 	Can be subtypes of the actual constructor argument types.
	 * @return The matching constructor, or <jk>null</jk> if constructor could not be found.
	 */
	public ConstructorInfo getConstructor(Visibility vis, Class<?>...argTypes) {
		return getConstructor(vis, false, argTypes);
	}

	private ConstructorInfo getConstructor(Visibility vis, boolean fuzzyArgs, Class<?>...argTypes) {
		if (fuzzyArgs) {
			int bestCount = -1;
			ConstructorInfo bestMatch = null;
			for (ConstructorInfo n : getDeclaredConstructors()) {
				if (vis.isVisible(n.inner())) {
					int m = ClassUtils.fuzzyArgsMatch(n.getParameterTypes(), argTypes);
					if (m > bestCount) {
						bestCount = m;
						bestMatch = n;
					}
				}
			}
			return bestMatch;
		}

		final boolean isMemberClass = isMemberClass() && ! isStatic();
		for (ConstructorInfo n : getDeclaredConstructors()) {
			Class<?>[] paramTypes = n.getParameterTypes();
			if (isMemberClass)
				paramTypes = Arrays.copyOfRange(paramTypes, 1, paramTypes.length);
			if (ClassUtils.argsMatch(paramTypes, argTypes) && vis.isVisible(n.inner()))
				return n;
		}

		return null;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Special constructors
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Locates the no-arg constructor for this class.
	 *
	 * <p>
	 * Constructor must match the visibility requirements specified by parameter 'v'.
	 * If class is abstract, always returns <jk>null</jk>.
	 * Note that this also returns the 1-arg constructor for non-static member classes.
	 *
	 * @param v The minimum visibility.
	 * @return The constructor, or <jk>null</jk> if no no-arg constructor exists with the required visibility.
	 */
	public ConstructorInfo getNoArgConstructor(Visibility v) {
		if (isAbstract())
			return null;
		boolean isMemberClass = isMemberClass() && ! isStatic();
		for (ConstructorInfo cc : getDeclaredConstructors())
			if (cc.hasNumArgs(isMemberClass ? 1 : 0) && cc.isVisible(v))
				return cc.transform(v);
		return null;
	}

	/**
	 * Locates the public no-arg constructor for this class.
	 *
	 * <p>
	 * If class is abstract, always returns <jk>null</jk>.
	 * Note that this also returns the 1-arg constructor for non-static member classes.
	 *
	 * @return The constructor, or <jk>null</jk> if no public no-arg constructor exists.
	 */
	public ConstructorInfo getPublicNoArgConstructor() {
		return getNoArgConstructor(Visibility.PUBLIC);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Fields
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns all public fields on this class.
	 *
	 * <p>
	 * Hidden fields are excluded from the results.
	 *
	 * @return All public fields on this class in alphabetical order.
	 */
	public List<FieldInfo> getPublicField() {
		if (publicFields == null) {
			Map<String,FieldInfo> m = new LinkedHashMap<>();
			for (ClassInfo c : getParents(false, false))
				c.appendDeclaredPublicFields(m);
			List<FieldInfo> l = new ArrayList<>(m.values());
			l.sort(null);
			publicFields = Collections.unmodifiableList(l);
		}
		return publicFields;
	}

	/**
	 * Returns all fields declared on this class.
	 *
	 * @return All fields declared on this class in alphabetical order.
	 */
	public List<FieldInfo> getDeclaredField() {
		if (declaredFields == null) {
			Field[] ff = c.getDeclaredFields();
			List<FieldInfo> l = new ArrayList<>(ff.length);
			for (Field f : ff)
				if (! "$jacocoData".equals(f.getName()))
					l.add(FieldInfo.of(this, f));
			l.sort(null);
			declaredFields = Collections.unmodifiableList(l);
		}
		return declaredFields;
	}

	/**
	 * Returns all declared fields on this class and all parent classes in child-to-parent order.
	 *
	 * @return All declared fields on this class and all parent classes in child-to-parent order in
	 * 	alphabetical order per class.
	 */
	public List<FieldInfo> getAllFields() {
		return getAllFields(false);
	}

	/**
	 * Returns all declared fields on this class and all parent classes in child-to-parent order.
	 *
	 * @param parentFirst If <jk>true</jk>, fields on parent classes are listed first.
	 * @return All declared fields on this class and all parent classes in alphabetical order per class.
	 */
	public List<FieldInfo> getAllFields(boolean parentFirst) {
		return findAllFields(parentFirst);
	}

	private List<FieldInfo> findAllFields(boolean parentFirst) {
		List<FieldInfo> l = new ArrayList<>();
		for (ClassInfo c : getParents(parentFirst, true))
			c.appendDeclaredFields(l);
		return l;
	}

	private List<FieldInfo> appendDeclaredFields(List<FieldInfo> l) {
		l.addAll(getDeclaredField());
		return l;
	}

	private Map<String,FieldInfo> appendDeclaredPublicFields(Map<String,FieldInfo> m) {
		for (FieldInfo f : getDeclaredField()) {
			String fn = f.getName();
			if (f.isPublic() && ! (m.containsKey(fn) || "$jacocoData".equals(fn)))
					m.put(f.getName(), f);
		}
		return m;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Annotations
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Finds the annotation of the specified type defined on this class or parent class/interface.
	 *
	 * <p>
	 * If the annotation cannot be found on the immediate class, searches methods with the same
	 * signature on the parent classes or interfaces.
	 * <br>The search is performed in child-to-parent order.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @return
	 * 	The annotation if found, or <jk>null</jk> if not.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(Class<T> a) {
		Optional<Annotation> o = annotationMap().get(a);
		if (o == null) {
			o = Optional.ofNullable(findAnnotation(a));
			annotationMap().put(a, o);
		}
		return o.isPresent() ? (T)o.get() : null;
	}

	/**
	 * Returns <jk>true</jk> if this class has the specified annotation.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @return
	 * 	The <jk>true</jk> if annotation if found.
	 */
	public boolean hasAnnotation(Class<? extends Annotation> a) {
		return getAnnotation(a) != null;
	}

	/**
	 * Returns all annotations of the specified type defined on the specified class or parent classes/interfaces.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @return
	 * 	A list of all matching annotations found in child-to-parent order, or an empty list if none found.
	 */
	public <T extends Annotation> List<T> getAnnotations(Class<T> a) {
		return getAnnotations(a, false);
	}

	/**
	 * Identical to {@link #getAnnotations(Class)} but optionally returns the list in reverse (parent-to-child) order.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @param parentFirst If <jk>true</jk>, results are in parent-to-child order.
	 * @return
	 * 	A list of all matching annotations found or an empty list if none found.
	 */
	public <T extends Annotation> List<T> getAnnotations(Class<T> a, boolean parentFirst) {
		return appendAnnotations(new ArrayList<>(), a, parentFirst);
	}

	/**
	 * Returns the specified annotation only if it's been declared on the specified class.
	 *
	 * <p>
	 * More efficient than calling {@link Class#getAnnotation(Class)} since it doesn't recursively look for the class
	 * up the parent chain.
	 *
	 * @param <T> The annotation class type.
	 * @param a The annotation class.
	 * @return The annotation, or <jk>null</jk> if not found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> a) {
		Optional<Annotation> o = declaredAnnotationMap().get(a);
		if (o == null) {
			o = Optional.ofNullable(findDeclaredAnnotation(a));
			declaredAnnotationMap().put(a, o);
		}
		return o.isPresent() ? (T)o.get() : null;
	}

	/**
	 * Finds and appends the specified annotation on the specified class and superclasses/interfaces to the specified
	 * list.
	 *
	 * <p>
	 * Results are ordered in child-to-parent order.
	 *
	 * @param l The list of annotations.
	 * @param a The annotation.
	 * @return The same list.
	 */
	public <T extends Annotation> List<T> appendAnnotations(List<T> l, Class<T> a) {
		return appendAnnotations(l, a, false);
	}

	/**
	 * Finds and appends the specified annotation on the specified class and superclasses/interfaces to the specified
	 * list.
	 *
	 * @param l The list of annotations.
	 * @param a The annotation.
	 * @param parentFirst If <jk>true</jk>, results are ordered in parent-to-child order.
	 * @return The same list.
	 */
	public <T extends Annotation> List<T> appendAnnotations(List<T> l, Class<T> a, boolean parentFirst) {
		if (c != null) {
			if (parentFirst) {
				for (Class<?> c2 : c.getInterfaces())
					of(c2).appendAnnotations(l, a, true);

				ClassInfo sci = of(c.getSuperclass());
				if (sci != null)
					sci.appendAnnotations(l, a, true);

				if (c.getPackage() != null)
					addIfNotNull(l, c.getPackage().getAnnotation(a));

				addIfNotNull(l, getDeclaredAnnotation(a));
			} else {
				addIfNotNull(l, getDeclaredAnnotation(a));

				if (c.getPackage() != null)
					addIfNotNull(l, c.getPackage().getAnnotation(a));

				ClassInfo sci = of(c.getSuperclass());
				if (sci != null)
					sci.appendAnnotations(l, a, false);

				for (Class<?> c2 : c.getInterfaces())
					of(c2).appendAnnotations(l, a, false);
			}
		}
		return l;
	}

	/**
	 * Same as getAnnotations(Class) except returns the annotations with the accompanying class.
	 *
	 * <p>
	 * Results are ordered child-to-parent.
	 *
	 * @param <T> The annotation class type.
	 * @param a The annotation class type.
	 * @return The found matches, or an empty list if annotation was not found.
	 */
	public <T extends Annotation> List<ClassAnnotation<T>> getClassAnnotations(Class<T> a) {
		return getClassAnnotations(a, false);
	}

	/**
	 * Same as {@link #getClassAnnotations(Class)} except optionally returns results in parent-to-child order.
	 *
	 * @param <T> The annotation class type.
	 * @param a The annotation class type.
	 * @param parentFirst If <jk>true</jk>, annotations are returned in parent-to-child order.
	 * @return The found matches, or an empty list if annotation was not found.
	 */
	public <T extends Annotation> List<ClassAnnotation<T>> getClassAnnotations(Class<T> a, boolean parentFirst) {
		return appendClassAnnotations(new ArrayList<>(), a, parentFirst);
	}

	private <T extends Annotation> List<ClassAnnotation<T>> appendClassAnnotations(List<ClassAnnotation<T>> l, Class<T> a, boolean parentFirst) {
		if (c != null) {
			if (parentFirst) {
				for (Class<?> c2 : c.getInterfaces())
					of(c2).appendClassAnnotations(l, a, true);

				ClassInfo sci = of(c.getSuperclass());
				if (sci != null)
					sci.appendClassAnnotations(l, a, true);

				T t2 = getDeclaredAnnotation(a);
				if (t2 != null)
					l.add(ClassAnnotation.of(this, t2));

			} else {
				T t2 = getDeclaredAnnotation(a);
				if (t2 != null)
					l.add(ClassAnnotation.of(this, t2));

				ClassInfo sci = of(c.getSuperclass());
				if (sci != null)
					sci.appendClassAnnotations(l, a, false);

				for (Class<?> c2 : c.getInterfaces())
					of(c2).appendClassAnnotations(l, a, false);
			}
		}
		return l;
	}

	private <T extends Annotation> T findAnnotation(Class<T> a) {
		if (c != null) {
			T t2 = getDeclaredAnnotation(a);
			if (t2 != null)
				return t2;

			ClassInfo sci = getParent();
			if (sci != null) {
				t2 = sci.getAnnotation(a);
				if (t2 != null)
					return t2;
			}

			for (ClassInfo c2 : getInterfaces()) {
				t2 = c2.getAnnotation(a);
				if (t2 != null)
					return t2;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T findDeclaredAnnotation(Class<T> a) {
		for (Annotation a2 : c.getDeclaredAnnotations())
			if (a2.annotationType() == a)
				return (T)a2;
		return null;
	}

	private synchronized Map<Class<?>,Optional<Annotation>> annotationMap() {
		if (annotationMap == null)
			annotationMap = new ConcurrentHashMap<>();
		return annotationMap;
	}

	private synchronized Map<Class<?>,Optional<Annotation>> declaredAnnotationMap() {
		if (declaredAnnotationMap == null)
			declaredAnnotationMap = new ConcurrentHashMap<>();
		return declaredAnnotationMap;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Characteristics
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns <jk>true</jk> if all specified flags are applicable to this class.
	 *
	 * @param flags The flags to test for.
	 * @return <jk>true</jk> if all specified flags are applicable to this class.
	 */
	public boolean isAll(ClassFlags...flags) {
		for (ClassFlags f : flags) {
			switch (f) {
				case DEPRECATED:
					if (isNotDeprecated())
						return false;
					break;
				case NOT_DEPRECATED:
					if (isDeprecated())
						return false;
					break;
				case PUBLIC:
					if (isNotPublic())
						return false;
					break;
				case NOT_PUBLIC:
					if (isPublic())
						return false;
					break;
				case STATIC:
					if (isNotStatic())
						return false;
					break;
				case NOT_STATIC:
					if (isStatic())
						return false;
					break;
				case ABSTRACT:
					if (isNotAbstract())
						return false;
					break;
				case NOT_ABSTRACT:
					if (isAbstract())
						return false;
					break;
				case HAS_ARGS:
				case HAS_NO_ARGS:
				case TRANSIENT:
				case NOT_TRANSIENT:
				default:
					break;

			}
		}
		return true;
	}

	/**
	 * Returns <jk>true</jk> if all specified flags are applicable to this class.
	 *
	 * @param flags The flags to test for.
	 * @return <jk>true</jk> if all specified flags are applicable to this class.
	 */
	public boolean isAny(ClassFlags...flags) {
		for (ClassFlags f : flags) {
			switch (f) {
				case DEPRECATED:
					if (isDeprecated())
						return true;
					break;
				case NOT_DEPRECATED:
					if (isNotDeprecated())
						return true;
					break;
				case PUBLIC:
					if (isPublic())
						return true;
					break;
				case NOT_PUBLIC:
					if (isNotPublic())
						return true;
					break;
				case STATIC:
					if (isStatic())
						return true;
					break;
				case NOT_STATIC:
					if (isNotStatic())
						return true;
					break;
				case ABSTRACT:
					if (isAbstract())
						return true;
					break;
				case NOT_ABSTRACT:
					if (isNotAbstract())
						return true;
					break;
				case TRANSIENT:
				case NOT_TRANSIENT:
				case HAS_ARGS:
				case HAS_NO_ARGS:
				default:
					break;

			}
		}
		return false;
	}

	/**
	 * Returns <jk>true</jk> if this class has the {@link Deprecated @Deprecated} annotation on it.
	 *
	 * @return <jk>true</jk> if this class has the {@link Deprecated @Deprecated} annotation on it.
	 */
	public boolean isDeprecated() {
		return c.isAnnotationPresent(Deprecated.class);
	}

	/**
	 * Returns <jk>true</jk> if this class doesn't have the {@link Deprecated @Deprecated} annotation on it.
	 *
	 * @return <jk>true</jk> if this class doesn't have the {@link Deprecated @Deprecated} annotation on it.
	 */
	public boolean isNotDeprecated() {
		return ! c.isAnnotationPresent(Deprecated.class);
	}

	/**
	 * Returns <jk>true</jk> if this class is public.
	 *
	 * @return <jk>true</jk> if this class is public.
	 */
	public boolean isPublic() {
		return Modifier.isPublic(c.getModifiers());
	}

	/**
	 * Returns <jk>true</jk> if this class is not public.
	 *
	 * @return <jk>true</jk> if this class is not public.
	 */
	public boolean isNotPublic() {
		return ! Modifier.isPublic(c.getModifiers());
	}

	/**
	 * Returns <jk>true</jk> if this class is public.
	 *
	 * @return <jk>true</jk> if this class is public.
	 */
	public boolean isStatic() {
		return Modifier.isStatic(c.getModifiers());
	}

	/**
	 * Returns <jk>true</jk> if this class is not static.
	 *
	 * @return <jk>true</jk> if this class is not static.
	 */
	public boolean isNotStatic() {
		return ! Modifier.isStatic(c.getModifiers());
	}

	/**
	 * Returns <jk>true</jk> if this class is abstract.
	 *
	 * @return <jk>true</jk> if this class is abstract.
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(c.getModifiers());
	}

	/**
	 * Returns <jk>true</jk> if this class is not abstract.
	 *
	 * @return <jk>true</jk> if this class is not abstract.
	 */
	public boolean isNotAbstract() {
		return ! Modifier.isAbstract(c.getModifiers());
	}

	/**
	 * Returns <jk>true</jk> if this class is a member class.
	 *
	 * @return <jk>true</jk> if this class is a member class.
	 */
	public boolean isMemberClass() {
		return c.isMemberClass();
	}

	/**
	 * Identifies if the specified visibility matches this constructor.
	 *
	 * @param v The visibility to validate against.
	 * @return <jk>true</jk> if this visibility matches the modifier attribute of this constructor.
	 */
	public boolean isVisible(Visibility v) {
		return v.isVisible(c);
	}

	/**
	 * Returns <jk>true</jk> if this is a primitive class.
	 *
	 * @return <jk>true</jk> if this is a primitive class.
	 */
	public boolean isPrimitive() {
		return c.isPrimitive();
	}

	/**
	 * Returns <jk>true</jk> if this is not a primitive class.
	 *
	 * @return <jk>true</jk> if this is not a primitive class.
	 */
	public boolean isNotPrimitive() {
		return ! c.isPrimitive();
	}

	/**
	 * Returns the underlying class name.
	 *
	 * @return The underlying class name.
	 */
	public String getName() {
		return c.getName();
	}

	/**
	 * Returns the simple name of the underlying class.
	 *
	 * <p>
	 * Returns either {@link Class#getSimpleName()} or {@link Type#getTypeName()} depending on whether
	 * this is a class or type.
	 *
	 * @return The simple name of the underlying class;
	 */
	public String getSimpleName() {
		return c != null ? c.getSimpleName() : t.getTypeName();
	}

	/**
	 * Returns the short name of the underlying class.
	 *
	 * <p>
	 * Similar to {@link #getSimpleName()} but also renders local or member class name prefixes.
	 *
	 * @return The short name of the underlying class.
	 */
	public String getShortName() {
		if (t instanceof Class) {
			if (c.isLocalClass())
				return of(c.getEnclosingClass()).getShortName() + '.' + c.getSimpleName();
			if (c.isMemberClass())
				return of(c.getDeclaringClass()).getShortName() + '.' + c.getSimpleName();
			return c.getSimpleName();
		}
		if (t instanceof ParameterizedType) {
			StringBuilder sb = new StringBuilder();
			ParameterizedType pt = (ParameterizedType)t;
			sb.append(of(pt.getRawType()).getShortName());
			sb.append("<");
			boolean first = true;
			for (Type t2 : pt.getActualTypeArguments()) {
				if (! first)
					sb.append(',');
				first = false;
				sb.append(of(t2).getShortName());
			}
			sb.append(">");
			return sb.toString();
		}
		return null;

	}

	/**
	 * Shortcut for calling <code><jsm>getReadableClassName</jsm>(c.getName())</code>
	 *
	 * @return A readable class type name, or <jk>null</jk> if parameter is <jk>null</jk>.
	 */
	public String getReadableName() {
		return ClassUtils.getReadableClassName(c != null ? c.getName() : t.getTypeName());
	}


	//-----------------------------------------------------------------------------------------------------------------
	// Primitive wrappers.
	//-----------------------------------------------------------------------------------------------------------------


	private static final Map<Class<?>, Class<?>>
		pmap1 = new HashMap<>(),
		pmap2 = new HashMap<>();
	static {
		pmap1.put(boolean.class, Boolean.class);
		pmap1.put(byte.class, Byte.class);
		pmap1.put(short.class, Short.class);
		pmap1.put(char.class, Character.class);
		pmap1.put(int.class, Integer.class);
		pmap1.put(long.class, Long.class);
		pmap1.put(float.class, Float.class);
		pmap1.put(double.class, Double.class);
		pmap2.put(Boolean.class, boolean.class);
		pmap2.put(Byte.class, byte.class);
		pmap2.put(Short.class, short.class);
		pmap2.put(Character.class, char.class);
		pmap2.put(Integer.class, int.class);
		pmap2.put(Long.class, long.class);
		pmap2.put(Float.class, float.class);
		pmap2.put(Double.class, double.class);
	}

	/**
	 * Returns <jk>true</jk> if the {@link #getPrimitiveWrapper()} method returns a value.
	 *
	 * @return <jk>true</jk> if the {@link #getPrimitiveWrapper()} method returns a value.
	 */
	public boolean hasPrimitiveWrapper() {
		return pmap1.containsKey(c);
	}

	/**
	 * If this class is a primitive (e.g. <code><jk>int</jk>.<jk>class</jk></code>) returns it's wrapper class
	 * (e.g. <code>Integer.<jk>class</jk></code>).
	 *
	 * @return The wrapper class, or <jk>null</jk> if class is not a primitive.
	 */
	public Class<?> getPrimitiveWrapper() {
		return pmap1.get(c);
	}

	/**
	 * If this class is a primitive wrapper (e.g. <code><jk>Integer</jk>.<jk>class</jk></code>) returns it's
	 * primitive class (e.g. <code>int.<jk>class</jk></code>).
	 *
	 * @return The primitive class, or <jk>null</jk> if class is not a primitive wrapper.
	 */
	public Class<?> getPrimitiveForWrapper() {
		return pmap2.get(c);
	}

	/**
	 * If this class is a primitive (e.g. <code><jk>int</jk>.<jk>class</jk></code>) returns it's wrapper class
	 * (e.g. <code>Integer.<jk>class</jk></code>).
	 *
	 * @return The wrapper class if it's primitive, or the same class if class is not a primitive.
	 */
	public Class<?> getWrapperIfPrimitive() {
		if (! c.isPrimitive())
			return c;
		return pmap1.get(c);
	}

	/**
	 * Same as {@link #getWrapperIfPrimitive()} but wraps it in a {@link ClassInfo}.
	 *
	 * @return The wrapper class if it's primitive, or the same class if class is not a primitive.
	 */
	public ClassInfo getWrapperInfoIfPrimitive() {
		if (! c.isPrimitive())
			return this;
		return of(pmap1.get(c));
	}

	/**
	 * Returns the default value for this primitive class.
	 *
	 * @return The default value, or <jk>null</jk> if this is not a primitive class.
	 */
	public Object getPrimitiveDefault() {
		return primitiveDefaultMap.get(c);
	}

	private static final Map<Class<?>,Object> primitiveDefaultMap = Collections.unmodifiableMap(
		new AMap<Class<?>,Object>()
			.append(Boolean.TYPE, false)
			.append(Character.TYPE, (char)0)
			.append(Short.TYPE, (short)0)
			.append(Integer.TYPE, 0)
			.append(Long.TYPE, 0l)
			.append(Float.TYPE, 0f)
			.append(Double.TYPE, 0d)
			.append(Byte.TYPE, (byte)0)
			.append(Boolean.class, false)
			.append(Character.class, (char)0)
			.append(Short.class, (short)0)
			.append(Integer.class, 0)
			.append(Long.class, 0l)
			.append(Float.class, 0f)
			.append(Double.class, 0d)
			.append(Byte.class, (byte)0)
	);

	/**
	 * Returns <jk>true</jk> if this class is a parent of <code>child</code>.
	 *
	 * @param child The child class.
	 * @param strict If <jk>true</jk> returns <jk>false</jk> if the classes are the same.
	 * @return <jk>true</jk> if this class is a parent of <code>child</code>.
	 */
	public boolean isParentOf(Class<?> child, boolean strict) {
		return c.isAssignableFrom(child) && ((!strict) || ! c.equals(child));
	}

	/**
	 * Returns <jk>true</jk> if this class is a parent or the same as <code>child</code>.
	 *
	 * @param child The child class.
	 * @return <jk>true</jk> if this class is a parent or the same as <code>child</code>.
	 */
	public boolean isParentOf(Class<?> child) {
		return isParentOf(child, false);
	}

	/**
	 * Returns <jk>true</jk> if this class is a parent or the same as <code>child</code>.
	 *
	 * @param child The child class.
	 * @return <jk>true</jk> if this class is a parent or the same as <code>child</code>.
	 */
	public boolean isParentOf(Type child) {
		if (child instanceof Class)
			return isParentOf((Class<?>)child);
		return false;
	}

	/**
	 * Returns <jk>true</jk> if this class is a child of <code>parent</code>.
	 *
	 * @param parent The parent class.
	 * @param strict If <jk>true</jk> returns <jk>false</jk> if the classes are the same.
	 * @return <jk>true</jk> if this class is a parent of <code>child</code>.
	 */
	public boolean isChildOf(Class<?> parent, boolean strict) {
		return parent.isAssignableFrom(c) && ((!strict) || ! c.equals(parent));
	}

	/**
	 * Returns <jk>true</jk> if this class is a child or the same as <code>parent</code>.
	 *
	 * @param parent The parent class.
	 * @return <jk>true</jk> if this class is a child or the same as <code>parent</code>.
	 */
	public boolean isChildOf(Class<?> parent) {
		return isChildOf(parent, false);
	}

	/**
	 * Returns <jk>true</jk> if this class is a child or the same as any of the <code>parents</code>.
	 *
	 * @param parents The parents class.
	 * @return <jk>true</jk> if this class is a child or the same as any of the <code>parents</code>.
	 */
	public boolean isChildOfAny(Class<?>...parents) {
		for (Class<?> p : parents)
			if (isChildOf(p))
				return true;
		return false;
	}

	/**
	 * Returns <jk>true</jk> if this class is a child or the same as <code>parent</code>.
	 *
	 * @param parent The parent class.
	 * @return <jk>true</jk> if this class is a parent or the same as <code>parent</code>.
	 */
	public boolean isChildOf(Type parent) {
		if (parent instanceof Class)
			return isChildOf((Class<?>)parent);
		return false;
	}

	/**
	 * Checks for equality with the specified class.
	 *
	 * @param c The class to check equality with.
	 * @return <jk>true</jk> if the specified class is the same as this one.
	 */
	public boolean is(Class<?> c) {
		return this.c.equals(c);
	}

	/**
	 * Shortcut for calling {@link Class#newInstance()} on the underlying class.
	 *
	 * @return A new instance of the underlying class
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public Object newInstance() throws InstantiationException, IllegalAccessException {
		return c.newInstance();
	}

	/**
	 * Returns <jk>true</jk> if this class is an interface.
	 *
	 * @return <jk>true</jk> if this class is an interface.
	 */
	public boolean isInterface() {
		return c.isInterface();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Parameter types.
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Finds the real parameter type of the specified class.
	 *
	 * @param index The zero-based index of the parameter to resolve.
	 * @param oc The class we're trying to resolve the parameter type for.
	 * @return The resolved real class.
	 */
	public Class<?> getParameterType(int index, Class<?> oc) {

		// We need to make up a mapping of type names.
		Map<Type,Type> typeMap = new HashMap<>();
		while (c != oc.getSuperclass()) {
			extractTypes(typeMap, oc);
			oc = oc.getSuperclass();
		}

		Type gsc = oc.getGenericSuperclass();

		// Not actually a parameterized type.
		if (! (gsc instanceof ParameterizedType))
			return Object.class;

		ParameterizedType opt = (ParameterizedType)gsc;
		Type actualType = opt.getActualTypeArguments()[index];

		if (typeMap.containsKey(actualType))
			actualType = typeMap.get(actualType);

		if (actualType instanceof Class) {
			return (Class<?>)actualType;

		} else if (actualType instanceof GenericArrayType) {
			Class<?> cmpntType = (Class<?>)((GenericArrayType)actualType).getGenericComponentType();
			return Array.newInstance(cmpntType, 0).getClass();

		} else if (actualType instanceof TypeVariable) {
			TypeVariable<?> typeVariable = (TypeVariable<?>)actualType;
			List<Class<?>> nestedOuterTypes = new LinkedList<>();
			for (Class<?> ec = oc.getEnclosingClass(); ec != null; ec = ec.getEnclosingClass()) {
				try {
					Class<?> outerClass = oc.getClass();
					nestedOuterTypes.add(outerClass);
					Map<Type,Type> outerTypeMap = new HashMap<>();
					extractTypes(outerTypeMap, outerClass);
					for (Map.Entry<Type,Type> entry : outerTypeMap.entrySet()) {
						Type key = entry.getKey(), value = entry.getValue();
						if (key instanceof TypeVariable) {
							TypeVariable<?> keyType = (TypeVariable<?>)key;
							if (keyType.getName().equals(typeVariable.getName()) && isInnerClass(keyType.getGenericDeclaration(), typeVariable.getGenericDeclaration())) {
								if (value instanceof Class)
									return (Class<?>)value;
								typeVariable = (TypeVariable<?>)entry.getValue();
							}
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			throw new FormattedRuntimeException("Could not resolve type: {0}", actualType);
		} else {
			throw new FormattedRuntimeException("Invalid type found in resolveParameterType: {0}", actualType);
		}
	}

	private static boolean isInnerClass(GenericDeclaration od, GenericDeclaration id) {
		if (od instanceof Class && id instanceof Class) {
			Class<?> oc = (Class<?>)od;
			Class<?> ic = (Class<?>)id;
			while ((ic = ic.getEnclosingClass()) != null)
				if (ic == oc)
					return true;
		}
		return false;
	}

	private static void extractTypes(Map<Type,Type> typeMap, Class<?> c) {
		Type gs = c.getGenericSuperclass();
		if (gs instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType)gs;
			Type[] typeParameters = ((Class<?>)pt.getRawType()).getTypeParameters();
			Type[] actualTypeArguments = pt.getActualTypeArguments();
			for (int i = 0; i < typeParameters.length; i++) {
				if (typeMap.containsKey(actualTypeArguments[i]))
					actualTypeArguments[i] = typeMap.get(actualTypeArguments[i]);
				typeMap.put(typeParameters[i], actualTypeArguments[i]);
			}
		}
	}

	@Override
	public String toString() {
		return t.toString();
	}

}
