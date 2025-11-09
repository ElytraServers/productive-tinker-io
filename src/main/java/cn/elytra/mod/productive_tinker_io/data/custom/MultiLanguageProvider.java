package cn.elytra.mod.productive_tinker_io.data.custom;

import com.google.common.collect.ImmutableMap;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class MultiLanguageProvider implements DataProvider {

    private final String modid;
    // locale -> LanguageProvider
    private final Map<String, LanguageProvider> multiLanguageMap;

    public MultiLanguageProvider(PackOutput output, String modid, String... locales) {
        this.modid = modid;
        var mapBuilder = ImmutableMap.<String, LanguageProvider>builder();
        for(String locale : locales) {
            mapBuilder.put(locale, new LanguageProvider(output, modid, locale) {
                @Override
                protected void addTranslations() {
                    // we don't use this callback, because we add them in outer addTranslations().
                }
            });
        }
        this.multiLanguageMap = mapBuilder.build();
    }

    private LanguageProvider getLanguage(String locale) {
        return Objects.requireNonNull(multiLanguageMap.get(locale), "Language " + locale + " is not added to the map when constructing.");
    }

    public abstract void addTranslations();

    @SafeVarargs
    public final void add(String key, Pair<String, String>... localeAndValues) {
        for(Pair<String, String> localeAndValue : localeAndValues) {
            getLanguage(localeAndValue.getKey()).add(key, localeAndValue.getValue());
        }
    }

    @SafeVarargs
    public final void addBlock(Supplier<? extends Block> key, Pair<String, String>... localeAndValues) {
        add(key.get(), localeAndValues);
    }

    @SafeVarargs
    public final void add(Block key, Pair<String, String>... localeAndValues) {
        add(key.getDescriptionId(), localeAndValues);
    }

    @SafeVarargs
    public final void addItem(Supplier<? extends Item> key, Pair<String, String>... localeAndValues) {
        add(key.get(), localeAndValues);
    }

    @SafeVarargs
    public final void add(Item item, Pair<String, String>... localeAndValues) {
        add(item.getDescriptionId(), localeAndValues);
    }

    @SafeVarargs
    public final void addItemStack(Supplier<? extends ItemStack> key, Pair<String, String>... localeAndValues) {
        add(key.get(), localeAndValues);
    }

    @SafeVarargs
    public final void add(ItemStack key, Pair<String, String>... localeAndValues) {
        add(key.getDescriptionId(), localeAndValues);
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        this.addTranslations();
        var futures = multiLanguageMap.values().stream().map(lp -> lp.run(cachedOutput)).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public @NotNull String getName() {
        return "Languages: " + String.join(", ", multiLanguageMap.keySet()) + " for mod: " + this.modid;
    }
}
