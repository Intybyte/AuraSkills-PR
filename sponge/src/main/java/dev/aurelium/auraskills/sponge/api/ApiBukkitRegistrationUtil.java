package dev.aurelium.auraskills.sponge.api;

import dev.aurelium.auraskills.api.AuraSkillsSponge;
import dev.aurelium.auraskills.api.AuraSkillsSpongeProvider;

import java.lang.reflect.Method;

public class ApiBukkitRegistrationUtil {

    private static final Method REGISTER_METHOD;
    private static final Method UNREGISTER_METHOD;

    static {
        try {
            REGISTER_METHOD = AuraSkillsSpongeProvider.class.getDeclaredMethod("register", AuraSkillsSponge.class);
            REGISTER_METHOD.setAccessible(true);

            UNREGISTER_METHOD = AuraSkillsSpongeProvider.class.getDeclaredMethod("unregister");
            UNREGISTER_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void register(AuraSkillsSponge instance) {
        try {
            REGISTER_METHOD.invoke(null, instance);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static void unregister() {
        try {
            UNREGISTER_METHOD.invoke(null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
