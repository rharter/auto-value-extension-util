package com.gabrielittner.auto.value.util;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.TypeName;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

/**
 * A Property of the AutoValue annotated class.
 *
 * This convenience class wraps the {@link ExecutableElement} for properties provided by the
 * AutoValueExtension.Context to make accessing values easier.
 *
 * It is recommended that you get the {@link #humanName} directly from the properties returned
 * from the AutoValueExtension.Context#properties method.
 *
 * <pre>
 * ImmutableList.Builder<Property> values = ImmutableList.builder();
 * for (Map.Entry<String, ExecutableElement> entry : context.properties().entrySet()) {
 *   values.add(new Property(entry.getKey(), entry.getValue()));
 * }
 * return values.build();
 * </pre>
 */
public class Property {
  private final String methodName;
  private final String humanName;
  private final ExecutableElement element;
  private final TypeName type;
  private final ImmutableSet<String> annotations;

  public Property(String humanName, ExecutableElement element) {
    this.methodName = element.getSimpleName().toString();
    this.humanName = humanName;
    this.element = element;
    type = TypeName.get(element.getReturnType());
    annotations = ElementUtil.buildAnnotations(element);
  }

  /**
   * The method name of the property.
   */
  public String methodName() {
    return methodName;
  }

  /**
   * The human readable name of the property.  If all properties use {@code get} or {@code is}
   * prefixes, this name will be different from {@link #methodName()}.
   */
  public String humanName() {
    return humanName;
  }

  /**
   * The underlying ExecutableElement representing the get method of the property.
   */
  public ExecutableElement element() {
    return element;
  }

  /**
   * The return type of the property.
   */
  public TypeName type() {
    return type;
  }

  /**
   * The set of annotations present on the original property.
   */
  public Set<String> annotations() {
    return annotations;
  }

  /**
   * True if the property can be null.
   */
  public Boolean nullable() {
    return annotations.contains("Nullable");
  }
}
