package com.sai.lemon.verticles;

import io.vertx.ext.unit.TestContext;

import java.util.function.Supplier;

/**
 * Created by saipkri on 19/04/17.
 */
public final class VerticlesMockUtils {

    public static <T> void withinTryCatch(final TestContext testContext, final Supplier<T> verificationFunction) {
        try {
            verificationFunction.get();
        } catch (Error ex) {
            testContext.fail(ex);
        }
    }
}
