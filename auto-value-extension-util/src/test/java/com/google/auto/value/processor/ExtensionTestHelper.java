package com.google.auto.value.processor;

import com.google.auto.value.extension.AutoValueExtension;
import java.util.Arrays;

public final class ExtensionTestHelper {

    public static AutoValueProcessor newProcessor(AutoValueExtension... extension) {
        return new AutoValueProcessor(Arrays.asList(extension));
    }

    private ExtensionTestHelper() {
        throw new AssertionError("No instances.");
    }
}
