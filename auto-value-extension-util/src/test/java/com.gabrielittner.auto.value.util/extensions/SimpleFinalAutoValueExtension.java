package com.gabrielittner.auto.value.util.extensions;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import static com.gabrielittner.auto.value.util.AutoValueUtil.newTypeSpecBuilder;

@AutoService(AutoValueExtension.class)
public class SimpleFinalAutoValueExtension extends AutoValueExtension {

    @Override public boolean applicable(Context context) {
        return true;
    }

    @Override public boolean mustBeFinal(Context context) {
        return true;
    }

    @Override public String generateClass(Context context, String className,
            String classToExtend, boolean isFinal) {
        TypeSpec subclass = newTypeSpecBuilder(context, className, classToExtend, isFinal).build();
        return JavaFile.builder(context.packageName(), subclass)
                .build()
                .toString();
    }
}
