package net.citizensnpcs.blacksmiths;

import net.citizensnpcs.Economy;
import net.citizensnpcs.properties.properties.UtilityProperties;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.utils.MessageUtils;
import net.citizensnpcs.utils.Messaging;
import net.citizensnpcs.utils.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlacksmithManager {
	private static final String[] materialAddendums = { "misc", "wood", "gold",
			"stone", "iron", "diamond", "leather", "chainmail" };

	// Get the index of the material addendums array based on an item ID
	private static int getBlacksmithIndex(ItemStack item) {
		int id = item.getTypeId();
		if (id == 259 || id == 346 || id == 359) {
			return 0;
		} else if ((id >= 268 && id <= 271) || id == 290) {
			return 1;
		} else if ((id >= 283 && id <= 286) || id == 294
				|| (id >= 314 && id <= 317)) {
			return 2;
		} else if ((id >= 272 && id <= 275) || id == 291) {
			return 3;
		} else if ((id >= 256 && id <= 258) || id == 267 || id == 292
				|| (id >= 306 && id <= 309)) {
			return 4;
		} else if ((id >= 276 && id <= 279) || id == 293
				|| (id >= 310 && id <= 313)) {
			return 5;
		} else if ((id >= 298 && id <= 301)) {
			return 6;
		} else if ((id >= 302 && id <= 305)) {
			return 7;
		}
		return 0;
	}

	// Get the price for a blacksmith operation
	public static double getBlacksmithPrice(Player player, String repairType) {
		ItemStack item = player.getItemInHand();
		short maxDurability = Material.getMaterial(item.getTypeId())
				.getMaxDurability();
		return (maxDurability - (maxDurability - item.getDurability()))
				* UtilityProperties.getPrice("blacksmith." + repairType + "."
						+ materialAddendums[getBlacksmithIndex(item)]);
	}

	// Repair an item
	@SuppressWarnings("deprecation")
	public static void repairItem(Player player, HumanNPC npc, String repairType) {
		ItemStack item = player.getItemInHand();
		if (item.getDurability() > 0) {
			String noPaymentMsg = StringUtils.wrap(npc.getName())
					+ " has repaired your "
					+ StringUtils.wrap(MessageUtils.getMaterialName(item
							.getTypeId()) + ".");
			if (!Economy.useEconPlugin()) {
				player.sendMessage(noPaymentMsg);
			} else if (Economy.hasEnough(player,
					getBlacksmithPrice(player, repairType))) {
				double paid = Economy.pay(player,
						getBlacksmithPrice(player, repairType));
				Economy.pay(npc, getBlacksmithPrice(player, repairType));
				if (paid > 0) {
					player.sendMessage(StringUtils.wrap(npc.getName())
							+ " has repaired your "
							+ StringUtils.wrap(MessageUtils
									.getMaterialName(item.getTypeId()))
							+ " for " + StringUtils.wrap(Economy.format(paid))
							+ ".");
				} else if (paid == 0) {
					player.sendMessage(noPaymentMsg);
				}
			} else {
				Messaging
						.sendError(
								player,
								"You don't have enough to repair your "
										+ MessageUtils.getMaterialName(item
												.getTypeId()));
				return;
			}

			item.setDurability((short) 0);
			player.setItemInHand(item);
			player.updateInventory();
		} else {
			player.sendMessage(ChatColor.GRAY
					+ "Your "
					+ StringUtils.wrap(
							MessageUtils.getMaterialName(item.getTypeId()),
							ChatColor.GRAY) + " is already fully repaired.");
		}
	}
}