package dev.aurelium.auraskills.sponge.user;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.antiafk.CheckData;
import dev.aurelium.auraskills.sponge.antiafk.CheckType;
import dev.aurelium.auraskills.sponge.hooks.BukkitLuckPermsHook;
import dev.aurelium.auraskills.sponge.skills.agility.AgilityAbilities;
import dev.aurelium.auraskills.common.api.implementation.ApiSkillsUser;
import dev.aurelium.auraskills.common.user.User;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class SpongeUser extends User {

    @Nullable
    private final org.spongepowered.api.entity.living.player.User player;
    private final AuraSkills plugin;
    // Non-persistent data
    private final Map<CheckType, CheckData> checkData;

    public SpongeUser(UUID uuid, @Nullable org.spongepowered.api.entity.living.player.User player, AuraSkills plugin) {
        super(uuid, plugin);
        this.player = player;
        this.plugin = plugin;
        this.checkData = new HashMap<>();
    }

    @Nullable
    public static Player getPlayer(SkillsUser skillsUser) {
        return ((SpongeUser) ((ApiSkillsUser) skillsUser).getUser()).getPlayer();
    }

    public static SpongeUser getUser(SkillsUser skillsUser) {
        return (SpongeUser) ((ApiSkillsUser) skillsUser).getUser();
    }

    @NotNull
    public CheckData getCheckData(CheckType type) {
        return checkData.computeIfAbsent(type, t -> new CheckData());
    }

    @Nullable
    public org.spongepowered.api.entity.living.player.User getPlayer() {
        return player;
    }

    @Override
    public String getUsername() {
        try {
            var user = Sponge.server().userManager().load(uuid).get();
            if(user.isPresent()) {
                return user.get().name();
            }
            return null;
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    @Override
    public double getPermissionMultiplier(@Nullable Skill skill) {
        if (player == null) {
            return 0.0;
        }
        double multiplier = 0.0;

        if (plugin.getHookManager().isRegistered(BukkitLuckPermsHook.class)
                && plugin.getHookManager().getHook(BukkitLuckPermsHook.class).usePermissionCache()) {
            Set<String> permissions = plugin.getHookManager().getHook(BukkitLuckPermsHook.class).getMultiplierPermissions(player);
            for (String permission : permissions) {
                multiplier += getMultiplierFromPermission(permission, skill);
            }
            return multiplier;
        }

        for (var permission : player.subjectData().allPermissions().values() ) {
            for (var entry : permission.entrySet()) {
                if (!entry.getValue()) continue;
                multiplier += getMultiplierFromPermission(entry.getKey(), skill);
            }
        }

        return multiplier;
    }

    private double getMultiplierFromPermission(String permission, @Nullable Skill skill) {
        final String prefix = "auraskills.multiplier.";
        if (!permission.startsWith(prefix)) {
            return 0.0;
        }

        permission = permission.substring(prefix.length());

        if (isNumeric(permission)) {
            return Double.parseDouble(permission) / 100.0;
        }

        if (skill != null) {
            String namespacedName = skill.toString().toLowerCase(Locale.ROOT) + ".";
            String plainName = skill.name().toLowerCase(Locale.ROOT) + ".";

            if (permission.startsWith(namespacedName)) {
                permission = permission.substring(namespacedName.length());
            } else if (permission.startsWith(plainName)) {
                permission = permission.substring(plainName.length());
            } else {
                return 0.0;
            }

            if (isNumeric(permission)) {
                return Double.parseDouble(permission) / 100.0;
            }
        }

        return 0.0;
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        boolean decimalSeen = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i == 0 && c == '-') {
                if (str.length() == 1) return false; // "-" alone is not a number
                continue;
            }
            if (c == '.') {
                if (decimalSeen || i == 0 || i == str.length() - 1)
                    return false; // Double decimal or leading/trailing decimal
                decimalSeen = true;
            } else if (c < '0' || c > '9') {
                return false; // Non-digit character
            }
        }
        return true;
    }

    @Override
    public boolean hasSkillPermission(Skill skill) {
        if (player == null) return true;

        return player.hasPermission("auraskills.skill." + skill.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void setCommandLocale(Locale locale) {
        if (player != null) {
            plugin.getCommandManager().setPlayerLocale(player, locale);
        }
    }

    @Override
    public int getPermissionJobLimit() {
        if (player == null) return 0;

        final String prefix = "auraskills.jobs.limit.";
        int highestLimit = 0;

        for (var permission : player.subjectData().allPermissions().values() ) {
            for (var entry : permission.entrySet()) {
                String permissionKey = entry.getKey();

                if (!permissionKey.startsWith(prefix)) continue;

                permissionKey = permissionKey.substring(prefix.length());

                if (isNumeric(permissionKey)) {
                    try {
                        int value = Integer.parseInt(permissionKey);
                        if (value > highestLimit) {
                            highestLimit = value;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return highestLimit;
    }

    @Override
    public boolean canSelectJob(@NotNull Skill skill) {
        if (player == null) return true;

        final String prefix = "auraskills.jobs.block.";

        //TODO: Understand better how permissions work in sponge in order to supply the correct stuff if possible and replace all the permissions loops
        for (var permission : player.subjectData().allPermissions().values() ) {
            for (var entry : permission.entrySet()) {
                String permissionKey = entry.getKey();

                if (!permissionKey.startsWith(prefix)) continue;

                String skillName = permissionKey.substring(prefix.length());
                if (skillName.equals(skill.getId().getKey()) || skillName.equals(skill.getId().toString())) {
                    if (entry.getValue()) { // If permission is true, selection is blocked
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void sendMessage(Component component) {
        // Don't send empty messages
        if (plugin.getMessageProvider().componentToString(component).isEmpty()) {
            return;
        }
        if (player != null) {
            plugin.getAudiences().player(player).sendMessage(component);
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        // Remove fleeting
        removeTraitModifier(AgilityAbilities.FLEETING_ID);
    }
}
