package com.citizens.commands.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.citizens.commands.CommandHandler;
import com.citizens.economy.EconomyManager;
import com.citizens.npctypes.blacksmiths.BlacksmithManager;
import com.citizens.resources.npclib.HumanNPC;
import com.citizens.resources.sk89q.Command;
import com.citizens.resources.sk89q.CommandContext;
import com.citizens.resources.sk89q.CommandPermissions;
import com.citizens.resources.sk89q.CommandRequirements;
import com.citizens.resources.sk89q.ServerCommand;
import com.citizens.utils.HelpUtils;
import com.citizens.utils.MessageUtils;
import com.citizens.utils.Messaging;
import com.citizens.utils.StringUtils;

@CommandRequirements(requiredType = "blacksmith")
public class BlacksmithCommands implements CommandHandler {

	@CommandRequirements()
	@Command(
			aliases = "blacksmith",
			usage = "help",
			desc = "view the blacksmith help page",
			modifiers = "help",
			min = 1,
			max = 1)
	@CommandPermissions("blacksmith.use.help")
	@ServerCommand()
	public static void blacksmithHelp(CommandContext args,
			CommandSender sender, HumanNPC npc) {
		HelpUtils.sendBlacksmithHelp(sender);
	}

	@Command(
			aliases = "blacksmith",
			usage = "status",
			desc = "view the status of your in-hand item",
			modifiers = "status",
			min = 1,
			max = 1)
	@CommandPermissions("blacksmith.use.status")
	public static void cost(CommandContext args, Player player, HumanNPC npc) {
		ItemStack item = player.getItemInHand();
		String repairType = "";
		if (BlacksmithManager.validateArmor(item)) {
			repairType = "armorrepair";
		} else if (BlacksmithManager.validateTool(item)) {
			repairType = "toolrepair";
		}
		if (repairType.isEmpty()) {
			Messaging.sendError(player,
					MessageUtils.getMaterialName(item.getTypeId())
							+ " is not a repairable item.");
			return;
		}
		if (EconomyManager.useEconPlugin()) {
			double price = BlacksmithManager.getBlacksmithPrice(player,
					repairType);
			player.sendMessage(ChatColor.GREEN
					+ "Item: "
					+ StringUtils.wrap(MessageUtils.getMaterialName(item
							.getTypeId())));
			player.sendMessage(ChatColor.GREEN + "Cost: "
					+ StringUtils.wrap(EconomyManager.format(price)));
			player.sendMessage(ChatColor.GREEN
					+ "Durability Remaining: "
					+ StringUtils.wrap(Material.getMaterial(item.getTypeId())
							.getMaxDurability() - item.getDurability()));
		} else {
			Messaging.sendError(player,
					"This server is not using an economy system.");
		}
	}
}