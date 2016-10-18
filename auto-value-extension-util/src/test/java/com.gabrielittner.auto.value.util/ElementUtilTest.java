package com.gabrielittner.auto.value.util;

import com.google.testing.compile.CompilationRule;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class ElementUtilTest {

    @Rule public CompilationRule compilationRule = new CompilationRule();

    private Elements elements;
    private Types types;

    @Before
    public void setUp() {
        this.elements = compilationRule.getElements();
        this.types = compilationRule.getTypes();
    }

    private interface MethodTestInterface1<T> {
        T interface1();
    }

    private interface MethodTestInterface2<T> {
        T interface2();
    }

    private interface MethodTestInterface3<T> {
        T interface3();
    }

    private static class Interface3Impl implements MethodTestInterface3<Double> {
        @Override public Double interface3() { return 0.0; }
    }

    @SuppressWarnings("unused")
    private static abstract class MethodTestClass<T extends String, R extends Float> extends Interface3Impl
        implements MethodTestInterface1<Integer>, MethodTestInterface2<R> {
        void a() { }
        abstract void b(String b);
        static int c() {
            return 0;
        }
        abstract int d(String d);
        static String e(int e) {
            return null;
        }
        abstract T f();
    }

    @Test
    public void methodTests() {
        TypeElement element = elements.getTypeElement(MethodTestClass.class.getCanonicalName());

        assertThat(ElementUtil.getAbstractMethod(elements, element, null, TypeName.VOID)).isNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, null, TypeName.VOID)).isFalse();
        assertThat(ElementUtil.getStaticMethod(element, null, TypeName.VOID)).isNull();
        assertThat(ElementUtil.hasStaticMethod(element, null, TypeName.VOID)).isFalse();

        assertThat(ElementUtil.getAbstractMethod(elements, element, TypeName.get(String.class), TypeName.VOID)).isNotNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, TypeName.get(String.class), TypeName.VOID)).isTrue();
        assertThat(ElementUtil.getStaticMethod(element, TypeName.get(String.class), TypeName.VOID)).isNull();
        assertThat(ElementUtil.hasStaticMethod(element, TypeName.get(String.class), TypeName.VOID)).isFalse();

        assertThat(ElementUtil.getAbstractMethod(elements, element, null, TypeName.INT)).isNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, null, TypeName.INT)).isFalse();
        assertThat(ElementUtil.getStaticMethod(element, null, TypeName.INT)).isNotNull();
        assertThat(ElementUtil.hasStaticMethod(element, null, TypeName.INT)).isTrue();

        assertThat(ElementUtil.getAbstractMethod(elements, element, TypeName.get(String.class), TypeName.INT)).isNotNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, TypeName.get(String.class), TypeName.INT)).isTrue();
        assertThat(ElementUtil.getStaticMethod(element, TypeName.get(String.class), TypeName.INT)).isNull();
        assertThat(ElementUtil.hasStaticMethod(element, TypeName.get(String.class), TypeName.INT)).isFalse();

        assertThat(ElementUtil.getAbstractMethod(elements, element, TypeName.INT, TypeName.get(String.class))).isNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, TypeName.INT, TypeName.get(String.class))).isFalse();
        assertThat(ElementUtil.getStaticMethod(element, TypeName.INT, TypeName.get(String.class))).isNotNull();
        assertThat(ElementUtil.hasStaticMethod(element, TypeName.INT, TypeName.get(String.class))).isTrue();

        ExecutableElement f = getMethodWithName(element, "f");
        TypeMirror fReturn = ElementUtil.getResolvedReturnType(types, element, f);
        assertThat(fReturn).isNotNull();
        assertThat(ClassName.get(fReturn)).isEqualTo(ClassName.get(String.class));

        ExecutableElement iface1 = getMethodWithName(element, "interface1");
        TypeMirror iface1Return = ElementUtil.getResolvedReturnType(types, element, iface1);
        assertThat(iface1Return).isNotNull();
        assertThat(ClassName.get(iface1Return)).isEqualTo(ClassName.get(Integer.class));

        ExecutableElement iface2 = getMethodWithName(element, "interface2");
        TypeMirror iface2Return = ElementUtil.getResolvedReturnType(types, element, iface2);
        assertThat(iface2Return).isNotNull();
        assertThat(ClassName.get(iface2Return)).isEqualTo(ClassName.get(Float.class));

        ExecutableElement iface3 = getMethodWithName(element, "interface3");
        TypeMirror iface3Return = ElementUtil.getResolvedReturnType(types, element, iface3);
        assertThat(iface3Return).isNotNull();
        assertThat(ClassName.get(iface3Return)).isEqualTo(ClassName.get(Double.class));
    }

    @SuppressWarnings("unused")
    private static abstract class MethodModifierTestClass {
        void a() { }
        abstract void b();
        static void c() { }

        public void d() { }
        public abstract void e();
        public static void f() { }
    }

    @Test
    public void hasModifier() {
        TypeElement element = elements.getTypeElement(MethodModifierTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.hasModifier(a, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(a, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(a, Modifier.PUBLIC)).isFalse();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.hasModifier(b, Modifier.ABSTRACT)).isTrue();
        assertThat(ElementUtil.hasModifier(b, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(b, Modifier.PUBLIC)).isFalse();

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.hasModifier(c, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(c, Modifier.STATIC)).isTrue();
        assertThat(ElementUtil.hasModifier(c, Modifier.PUBLIC)).isFalse();

        ExecutableElement d = (ExecutableElement) getElementWithName(elements, "d");
        assertThat(ElementUtil.hasModifier(d, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(d, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(d, Modifier.PUBLIC)).isTrue();

        ExecutableElement e = (ExecutableElement) getElementWithName(elements, "e");
        assertThat(ElementUtil.hasModifier(e, Modifier.ABSTRACT)).isTrue();
        assertThat(ElementUtil.hasModifier(e, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(e, Modifier.PUBLIC)).isTrue();

        ExecutableElement f = (ExecutableElement) getElementWithName(elements, "f");
        assertThat(ElementUtil.hasModifier(f, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(f, Modifier.STATIC)).isTrue();
        assertThat(ElementUtil.hasModifier(f, Modifier.PUBLIC)).isTrue();
    }

    @SuppressWarnings("unused")
    private static class MethodReturnsTestClass {
        void a() { }
        int b() {
            return 0;
        }
        String c() {
            return null;
        }
    }

    @Test
    public void methodReturns() {
        TypeElement element = elements.getTypeElement(MethodReturnsTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.methodReturns(a, TypeName.VOID)).isTrue();
        assertThat(ElementUtil.methodReturns(a, TypeName.INT)).isFalse();
        assertThat(ElementUtil.methodReturns(a, TypeName.get(String.class))).isFalse();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.methodReturns(b, TypeName.VOID)).isFalse();
        assertThat(ElementUtil.methodReturns(b, TypeName.INT)).isTrue();
        assertThat(ElementUtil.methodReturns(b, TypeName.get(String.class))).isFalse();

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.methodReturns(c, TypeName.VOID)).isFalse();
        assertThat(ElementUtil.methodReturns(c, TypeName.INT)).isFalse();
        assertThat(ElementUtil.methodReturns(c, TypeName.get(String.class))).isTrue();
    }

    @SuppressWarnings("unused")
    private static class MethodTakesTestClass {
        void a() { }
        void b(int b) { }
        void c(String c) { }
        void d(int d, int d2) { }
    }

    @Test
    public void methodTakes() {
        TypeElement element = elements.getTypeElement(MethodTakesTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.methodTakes(a, null)).isTrue();
        assertThat(ElementUtil.methodTakes(a, TypeName.INT)).isFalse();
        assertThat(ElementUtil.methodTakes(a, TypeName.get(String.class))).isFalse();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.methodTakes(b, null)).isFalse();
        assertThat(ElementUtil.methodTakes(b, TypeName.INT)).isTrue();
        assertThat(ElementUtil.methodTakes(b, TypeName.get(String.class))).isFalse();

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.methodTakes(c, null)).isFalse();
        assertThat(ElementUtil.methodTakes(c, TypeName.INT)).isFalse();
        assertThat(ElementUtil.methodTakes(c, TypeName.get(String.class))).isTrue();

        ExecutableElement d = (ExecutableElement) getElementWithName(elements, "d");
        assertThat(ElementUtil.methodTakes(d, null)).isFalse();
        assertThat(ElementUtil.methodTakes(d, TypeName.INT)).isFalse();
        assertThat(ElementUtil.methodTakes(d, TypeName.get(String.class))).isFalse();
    }

    @Test
    public void typeExists() {
        ClassName testClassName = ClassName.get(AnnotationTestClass.class);
        assertThat(ElementUtil.typeExists(elements, testClassName)).isTrue();
        ClassName testClass2Name = testClassName.peerClass("TestClass2");
        assertThat(ElementUtil.typeExists(elements, testClass2Name)).isFalse();
    }

    @SuppressWarnings("unused")
    private static abstract class AnnotationTestClass {
        public abstract int a();
        @Annotation1 public abstract int b();
        @Annotation1 @Annotation2("test") public abstract int c();
    }

    @Retention(RUNTIME)
    @Target({METHOD, FIELD})
    private @interface Annotation1 { }

    @Retention(RUNTIME)
    @Target({METHOD, FIELD})
    @SuppressWarnings("unused")
    private @interface Annotation2 {
        String value();
    }

    @Test
    public void annotationWithName() {
        TypeElement element = elements.getTypeElement(AnnotationTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        Element a = getElementWithName(elements, "a");
        assertThat(ElementUtil.hasAnnotationWithName(a, "Annotation1")).isFalse();
        assertThat(ElementUtil.hasAnnotationWithName(a, "Annotation2")).isFalse();

        Element b = getElementWithName(elements, "b");
        assertThat(ElementUtil.hasAnnotationWithName(b, "Annotation1")).isTrue();
        assertThat(ElementUtil.hasAnnotationWithName(b, "Annotation2")).isFalse();

        Element c = getElementWithName(elements, "c");
        assertThat(ElementUtil.hasAnnotationWithName(c, "Annotation1")).isTrue();
        assertThat(ElementUtil.hasAnnotationWithName(c, "Annotation2")).isTrue();
    }

    @Test
    public void buildAnnotationsTest() {
        TypeElement element = elements.getTypeElement(AnnotationTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.buildAnnotations(a)).isEmpty();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.buildAnnotations(b)).containsExactly("Annotation1");

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.buildAnnotations(c)).containsExactly("Annotation1", "Annotation2");
    }

    @Test
    public void annotationValueTest() {
        TypeElement element = elements.getTypeElement(AnnotationTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        Element a = getElementWithName(elements, "a");
        assertThat(ElementUtil.getAnnotationValue(a, Annotation2.class, "value")).isEqualTo(null);

        Element c = getElementWithName(elements, "c");
        assertThat(ElementUtil.getAnnotationValue(c, Annotation2.class, "value")).isEqualTo("test");

        try {
            ElementUtil.getAnnotationValue(c, Annotation2.class, "value2");
        } catch (Throwable throwable) {
            assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
        }
    }

    private Element getElementWithName(List<? extends Element> elements, String name) {
        for (Element element : elements) {
            if (element.getSimpleName().toString().equals(name)) {
                return element;
            }
        }
        return null;
    }

    private ExecutableElement getMethodWithName(TypeElement element, String name) {
        ExecutableElement method = (ExecutableElement) getElementWithName(element.getEnclosedElements(), name);
        if (method != null) {
            return method;
        }

        for (TypeMirror iface : element.getInterfaces()) {
            method = getMethodWithName((TypeElement) types.asElement(iface), name);
            if (method != null) {
                return method;
            }
        }

        return getMethodWithName((TypeElement) types.asElement(element.getSuperclass()), name);
    }
}
