package cn.elytra.mod.productive_tinker_io.util;

import net.neoforged.neoforge.common.util.Lazy;

import java.util.function.Function;

public class Utils {

    /**
     * Map the original lazy into another lazy.
     */
    public static <T, R> Lazy<R> mapLazy(Lazy<T> parent, Function<T, R> mapper) {
        return Lazy.of(() -> mapper.apply(parent.get()));
    }

}
