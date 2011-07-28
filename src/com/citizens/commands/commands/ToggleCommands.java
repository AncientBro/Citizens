package com.citizens.commands.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.citizens.Permission;
import com.citizens.commands.CommandHandler;
import com.citizens.economy.EconomyManager;
import com.citizens.npctypes.CitizensNPC;
import com.citizens.npctypes.CitizensNPCManager;
import com.citizens.properties.PropertyManager;
import com.citizens.properties.properties.UtilityProperties;
import com.citizens.resources.npclib.HumanNPC;
import com.citizens.resources.sk89q.Command;
import com.citizens.resources.sk89q.CommandContext;
import com.citizens.resources.sk89q.CommandPermissions;
import com.citizens.resources.sk89q.CommandRequirements;
import com.citizens.resources.sk89q.ServerCommand;
import com.citizens.utils.MessageUtils;
import com.citizens.utils.Messaging;
import com.citizens.utils.PageUtils;
import com.citizens.utils.PageUtils.PageInstance;
import com.citizens.utils.StringUtils;

public class ToggleCommands implements CommandHandler {

	@Command(
			aliases = "toggle",
			usage = "list (page)",
			desc = "view list of toggles",
			modifiers = { "list", "help" },
			min = 1,
			max = 2)
	@ServerCommand()
	@CommandPermissions("toggle.help")
	@CommandRequirements()
	public static void toggleHelp(CommandContext args, CommandSender sender,
			HumanNPC npc) {
		PageInstance instance = PageUtils.newInstance(sender);
		int page = 1;
		if (args.argsLength() == 2) {
			page = args.getInteger(1);
		}
		instance.header(ChatColor.YELLOW
				+ StringUtils.listify(ChatColor.GREEN
						+ "NPC Toggle List <%x/%y>" + ChatColor.YELLOW));
		for (String type : CitizensNPCManager.getTypes().keySet()) {
			instance.push(ChatColor.GREEN
					+ "    - "
					+ StringUtils.wrap(StringUtils.capitalise(type
							.toLowerCase())));
		}
		instance.process(page);
	}

	@CommandRequirements(requireSelected = true, requireOwnership = true)
	@Command(
			aliases = { "toggle", "tog", "t" },
			usage = "[type]",
			desc = "toggle an NPC type",
			modifiers = "*",
			min = 1,
			max = 1)
	public static void toggle(CommandContext args, Player player, HumanNPC npc) {
		String type = args.getString(0).toLowerCase();
		if (!CitizensNPCManager.validType(type)) {
			player.sendMessage(ChatColor.GRAY + "Invalid toggle type.");
			return;
		}
		if (!PropertyManager.npcHasType(npc, type)) {
			buyState(player, npc, CitizensNPCManager.getType(type));
		} else {
			toggleState(player, npc, CitizensNPCManager.getType(type), false);
		}
	}

	@CommandRequirements(requireSelected = true, requireOwnership = true)
	@Command(
			aliases = { "toggle", "tog", "t" },
			usage = "all [on|off]",
			desc = "toggle all NPC types",
			modifiers = "all",
			min = 2,
			max = 2)
	@CommandPermissions("admin.toggleall")
	public static void toggleAll(CommandContext args, Player player,
			HumanNPC npc) {
		if (args.getString(1).equalsIgnoreCase("on")) {
			toggleAll(player, npc, true);
		} else if (args.getString(1).equalsIgnoreCase("off")) {
			toggleAll(player, npc, false);
		}
	}

	/**
	 * Toggles an NPC state.
	 * 
	 * @param player
	 * @param register
	 * @param toggleable
	 */
	private static void toggleState(Player player, HumanNPC npc,
			CitizensNPC type, boolean register) {
		if (register) {
			PropertyManager.get(type.getType());
		}
		if (!npc.isType(type.getType())) {
			npc.addType(type.getType());
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " is now a " + type.getType() + "!");
		} else {
			npc.removeType(type.getType());
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " has stopped being a " + type.getType() + ".");
		}
	}

	/**
	 * Buys an NPC state.
	 * 
	 * @param player
	 * @param toggleable
	 */
	private static void buyState(Player player, HumanNPC npc, CitizensNPC type) {
		String toggle = type.getType();
		if (!Permission.generic(player, "citizens.toggle." + toggle)) {
			Messaging.send(player, npc, MessageUtils.noPermissionsMessage);
			return;
		}
		if (EconomyManager.hasEnough(player,
				UtilityProperties.getPrice(toggle + ".creation"))) {
			double paid = EconomyManager.pay(player,
					UtilityProperties.getPrice(toggle + ".creation"));
			if (paid > 0) {
				Messaging.send(
						player,
						npc,
						MessageUtils.getPaidMessage(player, toggle, toggle
								+ ".creation", npc.getStrippedName(), true));
			}
			toggleState(player, npc, type, true);
		} else {
			Messaging.send(player, npc, MessageUtils.getNoMoneyMessage(player,
					toggle + ".creation"));
		}
	}

	/**
	 * Toggles all types of NPCs
	 * 
	 * @param player
	 * @param npc
	 * @param on
	 */
	private static void toggleAll(Player player, HumanNPC npc, boolean on) {
		if (on) {
			for (CitizensNPC type : npc.types()) {
				if (!npc.isType(type.getType())) {
					toggleState(player, npc, type, false);
				}
			}
		} else {
			for (CitizensNPC type : npc.types()) {
				toggleState(player, npc, type, false);
			}
		}
	}
}