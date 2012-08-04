package net.citizensnpcs.npcdata;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.event.NPCCreateEvent.NPCCreateReason;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.resources.npclib.NPCManager;
import net.citizensnpcs.utils.InventoryUtils;
import net.citizensnpcs.utils.InventoryUtils.Armor;
import net.citizensnpcs.utils.MessageUtils;
import net.citizensnpcs.utils.Messaging;
import net.citizensnpcs.utils.StringUtils;
import net.citizensnpcs.waypoints.Waypoint;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

public class NPCDataManager {
    public static final Map<Player, Integer> equipmentEditors = Maps.newHashMap();
    public static final Map<Integer, Deque<String>> NPCTexts = new MapMaker().makeMap();
    // TODO: make editors an interface.
    public static final Map<Player, PathEditingSession> pathEditors = Maps.newHashMap();
    public static final Map<String, Integer> selectedNPCs = new MapMaker().makeMap();

    // Adds items to an npc so that they are visible.
    public static void addItems(HumanNPC npc, List<ItemData> items) {
        if (items != null) {
            npc.setItemInHand(items.get(0).getID() == 0 ? null : items.get(0).createStack());
            for (int i = 0; i < items.size() - 1; i++) {
                Armor.getArmor(i).set(npc.getInventory(),
                        items.get(i + 1).getID() == 0 ? null : items.get(i + 1).createStack());
            }
            npc.getNPCData().setItems(items);
        }
    }

    // Adds to an npc's text.
    public static void addText(int UID, String text) {
        Deque<String> texts = NPCDataManager.getText(UID);
        if (texts == null) {
            texts = new ArrayDeque<String>();
        }
        texts.add(text);
        NPCDataManager.setText(UID, texts);
    }

    public static void deselectNPC(Player player) {
        selectedNPCs.remove(player.getName());
    }

    // equip an NPC based on a player's item-in-hand
    @SuppressWarnings("deprecation")
    private static void equip(Player player, HumanNPC npc) {
        // TODO: cleanup
        ItemStack hand = player.getItemInHand();
        PlayerInventory npcInv = npc.getInventory();
        List<ItemData> items = Lists.newArrayList();
        items.add(new ItemData(npc.getItemInHand().getTypeId(), npc.getItemInHand().getDurability()));
        for (Armor armor : Armor.values()) {
            ItemStack slot = armor.get(npcInv);
            if (slot == null)
                slot = new ItemStack(Material.AIR, (short) 0);
            items.add(new ItemData(slot.getTypeId(), slot.getDurability()));
        }
        List<ItemStack> toAdd = Lists.newArrayList();
        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            boolean found = false;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getID() != 0) {
                    toAdd.add(items.get(i).createStack());
                    found = true;
                }
                items.set(i, new ItemData(0, (short) 0));
            }
            player.sendMessage(found ? StringUtils.wrap(npc.getName()) + " is now naked. Here are the items!"
                    : ChatColor.GRAY + "There were no items to take.");
        } else {
            int itemID = hand.getTypeId();
            String error = npc.getName() + " is already equipped with " + MessageUtils.getMaterialName(itemID) + ".";
            String slot = "";
            if (player.isSneaking()) {
                if (Material.getMaterial(items.get(0).getID()) == Material.getMaterial(itemID)) {
                    Messaging.sendError(player, error);
                    return;
                }
                slot = "item-in-hand";
                if (npc.getItemInHand().getType() != Material.AIR) {
                    toAdd.add(items.get(0).createStack());
                }
                items.set(0, new ItemData(hand.getTypeId(), hand.getDurability()));
            } else {
                Armor armor = Armor.getArmorSlot(itemID);
                if (armor != null) {
                    ItemStack armorItem = armor.get(npcInv);
                    if (armorItem != null && armorItem.getType() == Material.getMaterial(itemID)) {
                        Messaging.sendError(player, error);
                        return;
                    }
                    slot = armor.name().toLowerCase();
                    if (armorItem != null && armorItem.getType() != Material.AIR) {
                        toAdd.add(items.get(armor.getSlot() + 1).createStack());
                    }
                    items.set(armor.getSlot() + 1, new ItemData(hand.getTypeId(), hand.getDurability()));
                } else {
                    if (Material.getMaterial(items.get(0).getID()) == Material.getMaterial(itemID)) {
                        Messaging.sendError(player, error);
                        return;
                    }
                    slot = "item-in-hand";
                    if (npc.getItemInHand().getType() != Material.AIR) {
                        toAdd.add(items.get(0).createStack());
                    }
                    items.set(0, new ItemData(hand.getTypeId(), hand.getDurability()));
                }
            }
            player.sendMessage(StringUtils.wrap(npc.getName() + "'s ") + slot + " was set to "
                    + StringUtils.wrap(MessageUtils.getMaterialName(itemID)) + ".");
        }
        // remove item that was added to NPC
        InventoryUtils.decreaseItemInHand(player);
        // add all items to the player's inventory AFTER in-hand item was
        // removed
        boolean drop = false;
        for (ItemStack i : toAdd) {
            // drop items that don't fit in a player's inventory
            for (ItemStack unadded : player.getInventory().addItem(i).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), unadded);
                drop = true;
            }
        }
        if (drop) {
            Messaging.sendError(player, "Some items couldn't fit in your inventory and were dropped at your location.");
        }
        player.updateInventory();

        addItems(npc, items);
        NPCManager.removeForRespawn(npc.getUID());
        NPCManager.register(npc.getUID(), npc.getOwner(), NPCCreateReason.RESPAWN);
    }

    public static int getSelected(Player player) {
        return selectedNPCs.get(player.getName());
    }

    // Get an npc's text.
    public static Deque<String> getText(int UID) {
        return NPCTexts.get(UID);
    }

    public static void handleEquipmentEditor(NPCRightClickEvent event) {
        Player player = event.getPlayer();
        HumanNPC npc = event.getNPC();
        if (equipmentEditors.containsKey(player) && equipmentEditors.get(player) == npc.getUID()) {
            equip(player, npc);
        }
    }

    public static void handlePathEditor(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (pathEditors.get(player) == null)
            return;
        PathEditingSession session = pathEditors.get(player);
        HumanNPC npc = session.getNPC();
        if (npc == null) {
            pathEditors.remove(player);
            player.sendMessage(ChatColor.GRAY + "Something went wrong (NPC is dead?).");
            return;
        }
        switch (event.getAction()) {
        case LEFT_CLICK_BLOCK:
            Location loc = event.getClickedBlock().getLocation();
            if (!npc.getWorld().equals(player.getWorld())) {
                player.sendMessage(ChatColor.GRAY + "Waypoints must be in the same world as the npc.");
                break;
            }
            if (npc.getWaypoints().size() > 0
                    && session.getCurrentLocation(npc.getWaypoints()).distance(loc) > Settings
                            .getDouble("PathfindingRange")) {
                player.sendMessage(ChatColor.GRAY + "Points can't be more than "
                        + StringUtils.wrap(Settings.getDouble("PathfindingRange"), ChatColor.GRAY)
                        + " blocks away from each other.");
                break;
            }
            session.insert(npc.getWaypoints(), new Waypoint(loc));
            event.getPlayer().sendMessage(
                    StringUtils.wrap("Added") + " waypoint at index " + StringUtils.wrap(session.getIndex()) + " ("
                            + StringUtils.wrap(loc.getBlockX()) + ", " + StringUtils.wrap(loc.getBlockY()) + ", "
                            + StringUtils.wrap(loc.getBlockZ()) + ") (" + StringUtils.wrap(npc.getWaypoints().size())
                            + " " + StringUtils.pluralise("waypoint", npc.getWaypoints().size()) + ")");
            break;
        case RIGHT_CLICK_BLOCK:
        case RIGHT_CLICK_AIR:
            if (npc.getWaypoints().size() > 0) {
                session.remove(npc.getWaypoints());
                event.getPlayer().sendMessage(
                        StringUtils.wrap("Undid") + " the last waypoint ("
                                + StringUtils.wrap(npc.getWaypoints().size()) + " remaining)");

            } else
                event.getPlayer().sendMessage(ChatColor.GRAY + "No more waypoints.");
            break;
        }
    }

    public static void handlePathRestart(NPCRightClickEvent event) {
        if (event == null || !pathEditors.containsKey(event.getPlayer())
                || pathEditors.get(event.getPlayer()).getUID() != event.getNPC().getUID())
            return;
        pathEditors.get(event.getPlayer()).restartAtIndex();
    }

    // Resets an NPC's text.
    public static void resetText(int UID) {
        setText(UID, new ArrayDeque<String>());
    }

    public static void selectNPC(Player player, HumanNPC npc) {
        selectedNPCs.put(player.getName(), npc.getUID());
    }

    // Sets an npc's text to the given texts.
    public static void setText(int UID, Deque<String> text) {
        text = StringUtils.colourise(text);
        NPCTexts.put(UID, text);
        NPCManager.get(UID).getNPCData().setTexts(text);
    }
}