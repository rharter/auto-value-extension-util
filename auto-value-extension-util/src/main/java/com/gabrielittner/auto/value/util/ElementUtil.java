package com.gabrielittner.auto.value.util;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeParameter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

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

    public static TypeMirror getResolvedReturnType(Types typeUtils, TypeElement type, ExecutableElement method) {
        TypeMirror returnType = method.getReturnType();
        if (returnType.getKind() == TypeKind.TYPEVAR) {
            List<HierarchyElement> hierarchy = getHierarchyUntilClassWithElement(typeUtils, type, method);
            return resolveGenericType(hierarchy, returnType);
        }
        return returnType;
//        if (returnType.getKind() == TypeKind.TYPEVAR) {
//            TypeMirror typeMirror = type.asType();
//            while (typeMirror.getKind() != TypeKind.NONE) {
//                // first see if this class contains the type variable
//                if (type.getEnclosedElements().contains(method)) {
//                    int typeIndex = indexOfParameter(type, returnType.toString());
//                    TypeVariable arg = (TypeVariable) type.getTypeParameters().get(typeIndex).asType();
//                    return arg.getUpperBound();
//                }
//
//                // then check if the type variable comes from an interface
//                for (TypeMirror iface : type.getInterfaces()) {
//                    TypeElement ifaceEl = (TypeElement) typeUtils.asElement(iface);
//                    if (ifaceEl.getEnclosedElements().contains(method)) {
//                        int typeIndex = indexOfParameter(ifaceEl, returnType.toString());
//                        TypeParameterElement var = type.getTypeParameters().get(typeIndex);
//                        if (var.asType().getKind() == TypeKind.TYPEVAR) {
//
//                        }
//                    }
//                    TypeMirror inherited = getResolvedReturnType(typeUtils,
//                        (TypeElement) typeUtils.asElement(iface), method);
//                    if (iface.getKind() == TypeKind.DECLARED) {
//
//                    }
//                    if (inherited != null) {
//                        return inherited;
//                    }
//                }
//
//                // then move on to the super
//                typeMirror = type.getSuperclass();
//                type = (TypeElement) typeUtils.asElement(typeMirror);
//            }
//        }
//        return null;
    }

    private static List<HierarchyElement> getHierarchyUntilClassWithElement(
        Types typeUtils, TypeElement start, Element target) {

        if (start.getEnclosedElements().contains(target)) {
            return new ArrayList<>(Arrays.asList(new HierarchyElement(start, null)));
        }

        for (TypeMirror superType : typeUtils.directSupertypes(start.asType())) {
            TypeElement superTypeElement = (TypeElement) typeUtils.asElement(superType);
            if (superTypeElement.getEnclosedElements().contains(target)) {
                HierarchyElement base = new HierarchyElement(superTypeElement, null);
                HierarchyElement current = new HierarchyElement(start, superType);
                return new ArrayList<>(Arrays.asList(base, current));
            }
        }

        for (TypeMirror superType : typeUtils.directSupertypes(start.asType())) {
            TypeElement superTypeElement = (TypeElement) typeUtils.asElement(superType);
            List<HierarchyElement> result =
                getHierarchyUntilClassWithElement(typeUtils, superTypeElement, target);
            if (result != null) {
                result.add(new HierarchyElement(start, superType));
                return result;
            }
        }
        return null;
    }

    private static TypeMirror resolveGenericType(List<HierarchyElement> hierarchy, TypeMirror type) {
        for (HierarchyElement element : hierarchy) {
            int position = indexOfParameter(element.element, type.toString());
            if (position > -1) {
                TypeMirror arg = ((DeclaredType) element.element.asType()).getTypeArguments().get(position);
                if (type.getKind() == TypeKind.TYPEVAR) {
                    return ((TypeVariable) type).getUpperBound();
                } else {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Couldn't resolve type " + type);
    }

    private static class HierarchyElement {
        private final TypeElement element;
        private final TypeMirror superType;

        private HierarchyElement(TypeElement element, TypeMirror superType) {
            this.element = element;
            this.superType = superType;
        }
    }

    private static int indexOfParameter(TypeElement element, String param) {
        List<? extends TypeParameterElement> params = element.getTypeParameters();
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).getSimpleName().toString().equals(param)) {
                return i;
            }
        }
        return -1;
    }

    private ElementUtil() {
        throw new AssertionError("No instances.");
    }
}
