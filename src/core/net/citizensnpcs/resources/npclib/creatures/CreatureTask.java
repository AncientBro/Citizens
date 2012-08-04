package net.citizensnpcs.resources.npclib.creatures;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import net.citizensnpcs.api.event.NPCRemoveEvent.NPCRemoveReason;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.resources.npclib.NPCSpawner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.google.common.collect.MapMaker;

public class CreatureTask implements Runnable {
    private Player[] online;
    private void onSpawn(CreatureNPC creatureNPC) {
        creatureNPC.onSpawn();
    }
    @Override
    public void run() {
        if (dirty) {
            online = Bukkit.getServer().getOnlinePlayers();
            dirty = false;
        }
        if (online != null && online.length > 0) {
            Player player = online[random.nextInt(online.length)];
            CreatureNPCType type = CreatureNPCType.getRandomType(random);
            if (type == null)
                return;
            // TODO: favour certain biomes perhaps?
            spawnCreature(type, player.getLocation());
        }
    }
    private void spawnCreature(CreatureNPCType type, Location location) {
        if (spawned.get(type) == null) {
            spawned.put(type, 0);
        } else if (type.canSpawn(spawned.get(type))) {
            HumanNPC npc = type.spawn(location);
            if (npc != null) {
                spawned.put(type, spawned.get(type) + 1);
                creatureNPCs.put(npc.getPlayer().getEntityId(), (CreatureNPC) npc.getHandle());
                onSpawn(creatureNPCs.get(npc.getPlayer().getEntityId()));
            }
        }
    }
    public static class CreatureTick implements Runnable {
        @Override
        public void run() {
            for (CreatureNPC npc : creatureNPCs.values()) {
                npc.doTick();
            }
        }
    }

    public final static Map<Integer, CreatureNPC> creatureNPCs = new MapMaker().makeMap();

    private static boolean dirty = true;

    private static Random random = new Random(System.currentTimeMillis());

    private final static EnumMap<CreatureNPCType, Integer> spawned = new EnumMap<CreatureNPCType, Integer>(
            CreatureNPCType.class);

    public static void despawn(CreatureNPC npc, NPCRemoveReason reason) {
        removeFromMaps(npc);
        NPCSpawner.despawnNPC(npc, reason);
    }

    public static void despawnAll(NPCRemoveReason reason) {
        for (CreatureNPC creature : creatureNPCs.values()) {
            NPCSpawner.despawnNPC(creature, reason);
        }
    }

    public static CreatureNPC getCreature(Entity entity) {
        return creatureNPCs.get(entity.getEntityId());
    }

    public static void onDamage(Entity entity, EntityDamageEvent event) {
        if (getCreature(entity) != null) {
            creatureNPCs.get(entity.getEntityId()).onDamage(event);
        }
    }

    public static void onEntityDeath(Entity entity) {
        if (getCreature(entity) != null) {
            CreatureNPC creatureNPC = creatureNPCs.get(entity.getEntityId());
            creatureNPC.onDeath();
            removeFromMaps(creatureNPC);
            NPCSpawner.despawnNPC(creatureNPC, NPCRemoveReason.DEATH);
        }
    }

    public static void removeFromMaps(CreatureNPC npc) {
        creatureNPCs.remove(npc.getBukkitEntity().getEntityId());
        spawned.put(npc.getType(), spawned.get(npc.getType()) - 1);
    }

    public static void setDirty() {
        dirty = true;
    }
}