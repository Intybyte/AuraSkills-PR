package dev.aurelium.auraskills.sponge.level;

import dev.aurelium.auraskills.api.event.skill.DamageXpGainEvent;
import dev.aurelium.auraskills.api.event.skill.EntityXpGainEvent;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.source.*;
import dev.aurelium.auraskills.sponge.user.SpongeUser;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.level.LevelManager;
import dev.aurelium.auraskills.common.user.User;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class SpongeLevelManager extends LevelManager {

    private final AuraSkills plugin;
    private final Set<SourceLeveler> levelers;

    public SpongeLevelManager(AuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
        this.levelers = new HashSet<>();
    }

    public void loadXpRequirements() {
        XpRequirementsLoader loader = new XpRequirementsLoader(plugin, xpRequirements);
        loader.load();
    }

    public void registerLevelers() {
        levelers.clear();
        registerLeveler(new AnvilLeveler(plugin));
        registerLeveler(new BlockLeveler(plugin));
        registerLeveler(new BrewingLeveler(plugin));
        registerLeveler(new DamageLeveler(plugin));
        registerLeveler(new EnchantingLeveler(plugin));
        registerLeveler(new EntityLeveler(plugin));
        registerLeveler(new FishingLeveler(plugin));
        registerLeveler(new GrindstoneLeveler(plugin));
        registerLeveler(new ItemConsumeLeveler(plugin));
        registerLeveler(new JumpingLeveler(plugin));
        registerLeveler(new ManaAbilityUseLeveler(plugin));
        registerLeveler(new PotionSplashLeveler(plugin));
        registerLeveler(new StatisticLeveler(plugin));
    }

    private void registerLeveler(SourceLeveler leveler) {
        this.levelers.add(leveler);
        Bukkit.getPluginManager().registerEvents(leveler, plugin);
    }

    @SuppressWarnings("unchecked")
    public <T extends SourceLeveler> T getLeveler(Class<T> levelerClass) {
        for (SourceLeveler leveler : levelers) {
            if (levelerClass.isInstance(leveler)) {
                return (T) leveler;
            }
        }
        // No leveler found
        throw new IllegalArgumentException("Leveler " + levelerClass.getSimpleName() + " is not registered!");
    }

    public void addEntityXp(User user, Skill skill, @NotNull XpSource source, double amount,
                            LivingEntity attacked, Entity damager, EntityEvent originalEvent) {
        if (amount == 0) return; // Ignore if source amount is 0

        double amountToAdd = amount * calculateMultiplier(user, skill);

        EntityXpGainEvent event = new EntityXpGainEvent(SpongeUser.getPlayer(user.toApi()), user.toApi(), skill, source, amountToAdd, attacked, damager, originalEvent);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        addXpRaw(user, skill, event.getAmount(), source);
    }

    public void addDamageXp(User user, Skill skill, @NotNull XpSource source, double amount,
                            DamageCause cause, Entity damager, EntityEvent originalEvent) {
        if (amount == 0) return;

        double amountToAdd = amount * calculateMultiplier(user, skill);

        DamageXpGainEvent event = new DamageXpGainEvent(SpongeUser.getPlayer(user.toApi()), user.toApi(), skill, source, amountToAdd, cause, damager, originalEvent);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        addXpRaw(user, skill, event.getAmount(), source);
    }

    @Override
    public void playLevelUpSound(@NotNull User user) {
        ServerPlayer player = ((SpongeUser) user).getPlayer();
        if (player == null) return;
        try {
            String soundConfig = plugin.configString(Option.LEVELER_SOUND_TYPE);
            SoundType soundType = SoundTypes.registry().value(ResourceKey.minecraft(soundConfig));
            Sound.Source source = Sound.Source.valueOf(plugin.configString(Option.LEVELER_SOUND_CATEGORY));
            float volume = (float) plugin.configDouble(Option.LEVELER_SOUND_VOLUME);
            float pitch = (float) plugin.configDouble(Option.LEVELER_SOUND_PITCH);

            Sound sound = Sound.sound(soundType, source, volume, pitch);

            player.playSound(sound);
        } catch (Exception e) {
            plugin.logger().warn("Error playing level up sound (Check config) Played the default sound instead");
            Sound levelUp = Sound.sound(
                    SoundTypes.ENTITY_PLAYER_LEVELUP.get().key().key(),
                    Sound.Source.MASTER,
                    1f,
                    0.5f
            );

            player.playSound(levelUp);
        }
    }

    @Override
    public void reloadModifiers(User user) {
        ServerPlayer player = ((SpongeUser) user).getPlayer();
        if (player != null) {
            plugin.getModifierManager().reloadPlayer(player);
        }
    }
}
