package dev.aurelium.auraskills.sponge.trait;

import dev.aurelium.auraskills.api.bukkit.SpongeTraitHandler;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitHandler;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.user.SpongeUser;
import dev.aurelium.auraskills.common.trait.TraitManager;
import dev.aurelium.auraskills.common.user.User;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpongeTraitManager extends TraitManager {

    private final AuraSkills plugin;
    private final Map<Class<?>, SpongeTraitHandler> traitImpls = new HashMap<>();

    public SpongeTraitManager(AuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void registerTraitImplementations() {
        registerTraitImpl(new HpTrait(plugin));
        registerTraitImpl(new HealthRegenTraits(plugin));
        registerTraitImpl(new ManaRegenTrait(plugin));
        registerTraitImpl(new LuckTrait(plugin));
        registerTraitImpl(new DoubleDropTrait(plugin));
        registerTraitImpl(new AttackDamageTrait(plugin));
        registerTraitImpl(new ExperienceBonusTrait(plugin));
        registerTraitImpl(new AnvilDiscountTrait(plugin));
        registerTraitImpl(new MaxManaTrait(plugin));
        registerTraitImpl(new DamageReductionTrait(plugin));
        registerTraitImpl(new CritChanceTrait(plugin));
        registerTraitImpl(new CritDamageTrait(plugin));
        registerTraitImpl(new MovementSpeedTrait(plugin));
        registerTraitImpl(new GatheringLuckTraits(plugin));
    }

    public void registerTraitImpl(SpongeTraitHandler bukkitTrait) {
        traitImpls.put(bukkitTrait.getClass(), bukkitTrait);
        if (bukkitTrait instanceof Listener eventListener) {
            Bukkit.getPluginManager().registerEvents(eventListener, plugin);
        }
    }

    public <T extends SpongeTraitHandler> T getTraitImpl(Class<T> clazz) {
        SpongeTraitHandler traitHandler = traitImpls.get(clazz);
        if (traitHandler != null) {
            return clazz.cast(traitHandler);
        }
        throw new IllegalArgumentException("Trait implementation of type " + clazz.getSimpleName() + " not found!");
    }

    @Nullable
    public SpongeTraitHandler getTraitImpl(Trait trait) {
        for (SpongeTraitHandler traitImpl : traitImpls.values()) {
            for (Trait tr : traitImpl.getTraits()) {
                if (trait.getId().equals(tr.getId())) {
                    return traitImpl;
                }
            }
        }
        return null;
    }

    @Override
    public double getBaseLevel(User user, Trait trait) {
        ServerPlayer player = ((SpongeUser) user).getPlayer();
        if (player == null) return 0.0;

        SpongeTraitHandler traitImpl = getTraitImpl(trait);
        if (traitImpl != null) {
            return traitImpl.getBaseLevel(player, trait);
        } else {
            return 0.0;
        }
    }

    @Override
    public void registerTraitHandler(TraitHandler traitHandler) {
        if (traitHandler instanceof SpongeTraitHandler bukkitTraitHandler) {
            registerTraitImpl(bukkitTraitHandler);
        }
    }

    @Override
    public String getMenuDisplay(Trait trait, double value, Locale locale) {
        SpongeTraitHandler impl = getTraitImpl(trait);
        if (impl != null) {
            return impl.getMenuDisplay(value, trait, locale);
        } else {
            return NumberUtil.format1(value);
        }
    }
}
