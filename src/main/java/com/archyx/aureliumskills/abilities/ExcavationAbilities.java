package com.archyx.aureliumskills.abilities;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.skills.PlayerSkill;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.SkillLoader;
import com.archyx.aureliumskills.util.LoreUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ExcavationAbilities extends AbilityProvider implements Listener {

	private static final Random r = new Random();
	private final Material[] loadedMaterials;

	public ExcavationAbilities(AureliumSkills plugin) {
		super(plugin, Skill.EXCAVATION);
		//Load materials
		XMaterial[] materials = new XMaterial[]{
				XMaterial.DIRT, XMaterial.GRASS_BLOCK, XMaterial.COARSE_DIRT, XMaterial.PODZOL,
				XMaterial.SAND, XMaterial.RED_SAND, XMaterial.SOUL_SAND, XMaterial.SOUL_SOIL,
				XMaterial.CLAY, XMaterial.GRAVEL, XMaterial.MYCELIUM
		};
		loadedMaterials = new Material[materials.length];
		for (int i = 0; i < loadedMaterials.length; i++) {
			loadedMaterials[i] = materials[i].parseMaterial();
		}
	}

	public void spadeMaster(EntityDamageByEntityEvent event, Player player, PlayerSkill playerSkill) {
		if (OptionL.isEnabled(Skill.EXCAVATION)) {
			if (plugin.getAbilityManager().isEnabled(Ability.SPADE_MASTER)) {
				//Check permission
				if (!player.hasPermission("aureliumskills.excavation")) {
					return;
				}
				if (playerSkill.getAbilityLevel(Ability.SPADE_MASTER) > 0) {
					event.setDamage(event.getDamage() * (1 + (getValue(Ability.SPADE_MASTER, playerSkill) / 100)));
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void biggerScoop(PlayerSkill playerSkill, Block block, Player player) {
		if (isExcavationMaterial(block.getType())) {
			if (r.nextDouble() < (getValue(Ability.BIGGER_SCOOP, playerSkill) / 100)) {
				ItemStack tool = player.getInventory().getItemInMainHand();
				Material mat =  block.getType();
				for (ItemStack item : block.getDrops(tool)) {
					//If silk touch
					if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
						if (mat.equals(XMaterial.GRASS_BLOCK.parseMaterial())) {
							Material grassBlock = XMaterial.GRASS_BLOCK.parseMaterial();
							if (grassBlock != null) {
								block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(grassBlock, 2));
							}
						}
						else if (mat.equals(XMaterial.MYCELIUM.parseMaterial())) {
							Material mycelium = XMaterial.MYCELIUM.parseMaterial();
							if (mycelium != null) {
								block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(mycelium, 2));
							}
						}
						else if (mat.equals(XMaterial.CLAY.parseMaterial())) {
							Material clay = XMaterial.CLAY.parseMaterial();
							if (clay != null) {
								block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(clay, 2));
							}
						}
						if (XMaterial.isNewVersion()) {
							if (mat.equals(XMaterial.PODZOL.parseMaterial())) {
								block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.PODZOL, 2));
							}
						}
						else {
							if (mat.equals(Material.DIRT)) {
								if (block.getData() == 2) {
									block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.DIRT, 2, (short) 2));
								}
							}
						}
					}
					//Drop regular item if not silk touch
					else {
						ItemStack drop = item.clone();
						drop.setAmount(2);
						block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), drop);
					}
				}
			}
		}
	}

	public void metalDetector(Player player, PlayerSkill playerSkill, Block block) {
		if (isExcavationMaterial(block.getType())) {
			if (r.nextDouble() < (getValue(Ability.METAL_DETECTOR, playerSkill) / 100)) {
				int lootTableSize = plugin.getLootTableManager().getLootTable("excavation-rare").getLoot().size();
				if (lootTableSize > 0) {
					Loot loot = plugin.getLootTableManager().getLootTable("excavation-rare").getLoot().get(r.nextInt(lootTableSize));
					// If has item
					if (loot.hasItem()) {
						ItemStack drop = loot.getDrop();
						if (drop != null) {
							block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), drop);
						}
					}
					// If has command
					else if (loot.hasCommand()) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), LoreUtil.replace(loot.getCommand(), "{player}", player.getName()));
					}
				}
			}
		}
	}

	public void luckySpades(Player player, PlayerSkill playerSkill, Block block) {
		if (isExcavationMaterial(block.getType())) {
			if (r.nextDouble() < (getValue(Ability.LUCKY_SPADES, playerSkill) / 100)) {
				int lootTableSize = plugin.getLootTableManager().getLootTable("excavation-epic").getLoot().size();
				if (lootTableSize > 0) {
					Loot loot = plugin.getLootTableManager().getLootTable("excavation-epic").getLoot().get(r.nextInt(lootTableSize));
					// If has item
					if (loot.hasItem()) {
						ItemStack drop = loot.getDrop();
						if (drop != null) {
							block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), drop);
						}
					}
					// If has command
					else if (loot.hasCommand()) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), LoreUtil.replace(loot.getCommand(), "{player}", player.getName()));
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void excavationListener(BlockBreakEvent event) {
		if (OptionL.isEnabled(Skill.ARCHERY)) {
			if (!event.isCancelled()) {
				Player player = event.getPlayer();
				Block block = event.getBlock();
				if (blockAbility(player)) return;
				//Check game mode
				if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
					return;
				}
				//Applies abilities
				if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
					PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
					if (isEnabled(Ability.BIGGER_SCOOP)) {
						if (!block.hasMetadata("skillsPlaced")) {
							biggerScoop(playerSkill, block, player);
						}
					}
					if (isEnabled(Ability.METAL_DETECTOR)) {
						metalDetector(player, playerSkill, block);
					}
					if (isEnabled(Ability.LUCKY_SPADES)) {
						luckySpades(player, playerSkill, block);
					}
				}
			}
		}
	}

	private boolean isExcavationMaterial(Material material) {
		for (Material checkedMaterial : loadedMaterials) {
			if (material == checkedMaterial) {
				return true;
			}
		}
		return false;
	}
}