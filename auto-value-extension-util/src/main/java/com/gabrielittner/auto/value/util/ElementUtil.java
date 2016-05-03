package com.gabrielittner.auto.value.util;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Annotation;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import static com.google.auto.common.MoreElements.getLocalAndInheritedMethods;

public final class ElementUtil {

    public static boolean hasStaticMethod(TypeElement cls, TypeName takes, TypeName returns) {
        return getStaticMethod(cls, takes, returns) != null;
    }

    public static ExecutableElement getStaticMethod(TypeElement cls, TypeName takes,
            TypeName returns) {
        for (Element element : cls.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) element;
            if (methodMatches(method, Modifier.STATIC, takes, returns)) {
                return method;
            }
        }
        return null;
    }

    public static boolean hasAbstractMethod(Elements elementUtils, TypeElement cls, TypeName takes,
            TypeName returns) {
        return getAbstractMethod(elementUtils, cls, takes, returns) != null;
    }

    public static ExecutableElement getAbstractMethod(Elements elementUtils,
            TypeElement cls, TypeName takes, TypeName returns) {
        for (ExecutableElement method : getLocalAndInheritedMethods(cls, elementUtils)) {
            if (methodMatches(method, Modifier.ABSTRACT, takes, returns)) {
                return method;
            }
        }
        return null;
    }

    private static boolean methodMatches(ExecutableElement method, Modifier modifier,
            TypeName takes, TypeName returns) {
        return hasModifier(method, modifier) && methodTakes(method, takes)
                && methodReturns(method, returns);
    }

    static boolean hasModifier(ExecutableElement method, Modifier modifier) {
        return method.getModifiers().contains(modifier);
    }

    static boolean methodTakes(ExecutableElement method, TypeName takes) {
        List<? extends VariableElement> parameters = method.getParameters();
        if (takes != null) {
            if (parameters.size() != 1) {
                return false;
            }
            if (!takes.equals(ClassName.get(parameters.get(0).asType()))) {
                return false;
            }
        } else {
            if (parameters.size() > 0) {
                return false;
            }
        }
        return true;
    }

    static boolean methodReturns(ExecutableElement method, TypeName returns) {
        return returns.equals(ClassName.get(method.getReturnType()));
    }

    public static boolean typeExists(Elements elements, ClassName className) {
        String name = className.toString();
        return elements.getTypeElement(name) != null;
    }

    public static boolean hasAnnotationWithName(Element element, String simpleName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            String name = mirror.getAnnotationType().asElement().getSimpleName().toString();
            if (simpleName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static ImmutableSet<String> buildAnnotations(ExecutableElement element) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            builder.add(annotation.getAnnotationType().asElement().getSimpleName().toString());
        }
        return builder.build();
    }

    public static Object getAnnotationValue(Element element, Class<? extends Annotation> clazz,
            String key) {
        Optional<AnnotationMirror> annotation = MoreElements.getAnnotationMirror(element, clazz);
        if (annotation.isPresent()) {
            return AnnotationMirrors.getAnnotationValue(annotation.get(), key).getValue();
        }
        return null;
    }

    private ElementUtil() {
        throw new AssertionError("No instances.");
    }
}
