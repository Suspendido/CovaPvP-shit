package me.keano.azurite.utils;

import com.lunarclient.apollo.module.glow.GlowModule;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 13/8/2025
 * Project: Zeus
 */

public final class ApolloGlowRuntime {
    private ApolloGlowRuntime() {}

    private static Class<?> tryLoadRecipientsClass() {
        String[] candidates = {
                "com.lunarclient.apollo.common.Recipients",
                "com.lunarclient.apollo.recipients.Recipients"
        };
        for (String name : candidates) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    private static Object recipientsEveryone(Class<?> recCls) throws Exception {
        String[] factories = {"ofEveryone", "ofAll", "all"};
        for (String mName : factories) {
            try {
                Method m = recCls.getMethod(mName);
                return m.invoke(null);
            } catch (NoSuchMethodException ignored) {}
        }
        try {
            Method builder = recCls.getMethod("builder");
            Object b = builder.invoke(null);
            Method build = b.getClass().getMethod("build");
            return build.invoke(b);
        } catch (NoSuchMethodException ignored) {}
        throw new NoSuchMethodException("Not found method 'Recipients everyone'.");
    }

    public static void overrideGlow(GlowModule module, UUID target, Color color) throws Exception {
        Class<?> recCls = tryLoadRecipientsClass();
        if (recCls != null) {
            try {
                Method m = module.getClass().getMethod("overrideGlow", recCls, UUID.class, Color.class);
                m.invoke(module, recipientsEveryone(recCls), target, color);
                return;
            } catch (NoSuchMethodException ignored) {}
        }
        try {
            Method m = module.getClass().getMethod("overrideGlow", UUID.class, Color.class);
            m.invoke(module, target, color);
            return;
        } catch (NoSuchMethodException ignored) {}

        throw new NoSuchMethodException("Not found override glow on method GlowModule.");
    }

    public static void resetGlow(GlowModule module, UUID target) throws Exception {
        Class<?> recCls = tryLoadRecipientsClass();
        if (recCls != null) {
            try {
                Method m = module.getClass().getMethod("resetGlow", recCls, UUID.class);
                m.invoke(module, recipientsEveryone(recCls), target);
                return;
            } catch (NoSuchMethodException ignored) {}
        }
        try {
            Method m = module.getClass().getMethod("resetGlow", UUID.class);
            m.invoke(module, target);
            return;
        } catch (NoSuchMethodException ignored) {}

        throw new NoSuchMethodException("Not found compatible GlowModule.");
    }
}
