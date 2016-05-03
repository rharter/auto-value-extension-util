package com.gabrielittner.auto.value.util;

import com.gabrielittner.auto.value.util.extensions.CallingConstructorAutoValueExtension;
import com.gabrielittner.auto.value.util.extensions.SimpleAutoValueExtension;
import com.gabrielittner.auto.value.util.extensions.SimpleFinalAutoValueExtension;
import com.google.testing.compile.JavaFileObjects;
import java.util.Collections;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.auto.value.processor.ExtensionTestHelper.newProcessor;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AutoValueCursorExtensionTest {

    @Test public void simpleFinal() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "@AutoValue public abstract class Test {\n"
                + "  public abstract int a();\n"
                + "}\n"
        );

        JavaFileObject expected = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "  AutoValue_Test(int a) {\n"
                + "    super(a);\n"
                + "  }\n"
                + "}\n"
        );

        assertAbout(javaSources()).that(Collections.singletonList(source))
                .processedWith(newProcessor(new SimpleFinalAutoValueExtension()))
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test public void simpleAbstract() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "@AutoValue public abstract class Test {\n"
                + "  public abstract int a();\n"
                + "}\n"
        );
        JavaFileObject expected = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "abstract class $AutoValue_Test extends $$AutoValue_Test {\n"
                + "  AutoValue_Test(int a) {\n"
                + "    super(a);\n"
                + "  }\n"
                + "}\n"
        );
        JavaFileObject expected2 = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "  AutoValue_Test(int a) {\n"
                + "    super(a);\n"
                + "  }\n"
                + "}\n"
        );

        assertAbout(javaSources()).that(Collections.singletonList(source))
                .processedWith(newProcessor(new SimpleFinalAutoValueExtension(),
                        new SimpleAutoValueExtension()))
                .compilesWithoutError()
                .and()
                .generatesSources(expected, expected2);
    }

    @Test public void genericClass() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "@AutoValue public abstract class Test<T> {\n"
                + "  public abstract T t();\n"
                + "}\n"
        );

        JavaFileObject expected = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "final class AutoValue_Test<T> extends $AutoValue_Test<T> {\n"
                + "  AutoValue_Test(T t) {\n"
                + "    super(t);\n"
                + "  }\n"
                + "}\n"
        );

        assertAbout(javaSources()).that(Collections.singletonList(source))
                .processedWith(newProcessor(new SimpleAutoValueExtension()))
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    @Test public void callingFinalClassConstructor() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "@AutoValue public abstract class Test {\n"
                + "  public abstract int a();\n"
                + "  public abstract String b();\n"
                + "  public abstract boolean c();\n"
                + "}\n"
        );

        JavaFileObject expected = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n\n"
                + "\n"
                + "import java.lang.String;"
                + "\n"
                + "abstract class $AutoValue_Test extends $$AutoValue_Test {\n"
                + "  $AutoValue_Test(int a, String b, boolean c) {\n"
                + "    super(a, b, c);\n"
                + "  }\n"
                + "\n"
                + "  Test test() {\n"
                + "    return new AutoValue_Test(a(), b(), c());\n"
                + "  }\n"
                + "}\n"
        );

        assertAbout(javaSources()).that(Collections.singletonList(source))
                .processedWith(newProcessor(new CallingConstructorAutoValueExtension(),
                        new SimpleFinalAutoValueExtension()))
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
