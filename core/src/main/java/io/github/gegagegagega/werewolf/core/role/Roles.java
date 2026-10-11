package io.github.gegagegagega.werewolf.core.role;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Registry of assignable roles by id. */
public final class Roles {
    private static final Map<String, Supplier<Role>> ROLES = new LinkedHashMap<>();

    static {
        register("townie", Townie::new);
        register("doctor", Doctor::new);
        register("werewolf", Werewolf::new);
        register("sheriff", Sheriff::new);
        register("milkman", Milkman::new);
        register("fool", Fool::new);
    }

    private Roles() {
    }

    public static void register(String id, Supplier<Role> factory) {
        ROLES.put(id.toLowerCase(Locale.ROOT), factory);
    }

    public static Set<String> ids() {
        return Collections.unmodifiableSet(ROLES.keySet());
    }

    public static boolean exists(String id) {
        return id != null && ROLES.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public static Role create(String id) {
        Supplier<Role> factory = ROLES.get(id.toLowerCase(Locale.ROOT));
        if (factory == null) {
            throw new IllegalArgumentException("Unknown role: " + id);
        }
        return factory.get();
    }
}
