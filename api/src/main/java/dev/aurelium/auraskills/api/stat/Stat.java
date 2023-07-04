package dev.aurelium.auraskills.api.stat;

import com.google.common.collect.ImmutableList;
import dev.aurelium.auraskills.api.option.Optioned;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.trait.Trait;

import java.util.Locale;

public interface Stat extends Optioned {

    NamespacedId getId();

    boolean isEnabled();

    ImmutableList<Trait> getTraits();

    double getTraitModifier(Trait trait);

    String getDisplayName(Locale locale);

    String getDescription(Locale locale);

    String getColor(Locale locale);

    String getSymbol(Locale locale);

    String name();

}
