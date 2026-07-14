package jenny.werewolf.papermc.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.function.Supplier;

public abstract class Role {
    private static final Map<String, Supplier<Role>> ROLES = new HashMap<>();
    static {
        register("townie", Townie::new);
        register("doctor", Doctor::new);
        register("werewolf", Werewolf::new);
        register("sheriff", Sheriff::new);
    }

    public static void register(String name, Supplier<Role> factory) {
        ROLES.put(name.toLowerCase(), factory);
    }

    public static Set<String> names() {
        return ROLES.keySet();
    }

    public static Role create(String name) {
        return ROLES.get(name.toLowerCase()).get();
    }
    public abstract String getName();
    public abstract void dayAbility(); //trigger on end of day
    public abstract void nightAbility(); //trigger on end of night
    public abstract void immediateAbility(); //trigger immediately, check params within
}

class Townie extends Role {
    public String getName() {
        return "townie";
    }
    @Override
    public void dayAbility() {
        // Townie's ability logic here
    }

    @Override
    public void nightAbility() {
        // Townie's night ability logic here
    }

    @Override
    public void immediateAbility() {
        // Townie's immediate ability logic here
    }
}

class Werewolf extends Role {
    public String getName() {
        return "werewolf";
    }
    @Override
    public void dayAbility() {
        // Werewolf's ability logic here
    }

    @Override
    public void nightAbility() {
        // Werewolf's night ability logic here
    }

    @Override
    public void immediateAbility() {
        // Werewolf's immediate ability logic here
    }
}

class Sheriff extends Role {
    public String getName() {
        return "sheriff";
    }
    @Override
    public void dayAbility() {
        // Sheriff's ability logic here
    }

    @Override
    public void nightAbility() {
        // Sheriff's night ability logic here
    }

    @Override
    public void immediateAbility() {
        // Sheriff's immediate ability logic here
    }
}

class Doctor extends Role {
    public String getName() {
        return "doctor";
    }

    @Override
    public void dayAbility() {
        // Doctor's ability logic here
    }

    @Override
    public void nightAbility() {
        // Doctor's night ability logic here
    }

    @Override
    public void immediateAbility() {
        // Doctor's immediate ability logic here
    }
}