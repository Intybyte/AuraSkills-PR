package dev.aurelium.auraskills.sponge.menus.contexts;

import dev.aurelium.auraskills.sponge.menus.SourcesMenu.SortType;
import dev.aurelium.slate.context.ContextProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SortTypeContext implements ContextProvider<SortType> {

    @Override
    public Class<SortType> getType() {
        return SortType.class;
    }

    @Nullable
    @Override
    public SortType parse(String menuName, String s) {
        return SortType.valueOf(s.toUpperCase(Locale.ROOT));
    }
}
