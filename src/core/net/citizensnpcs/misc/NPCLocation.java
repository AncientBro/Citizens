package net.citizensnpcs.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NPCLocation {
	private final int chunkX;
	private final int chunkZ;
	private final String owner;
	private final float pitch;
	private final int UID;
	private final String world;
	private final int x;
	private final int y;
	private final float yaw;
	private final int z;

	/**
	 * Used for respawning npcs.
	 * 
	 * @param plugin
	 * @param loc
	 * @param UID
	 * @param owner
	 */
	public NPCLocation(Location loc, int UID, String owner) {
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		this.pitch = loc.getPitch();
		this.yaw = loc.getYaw();
		this.world = loc.getWorld().getName();
		this.UID = UID;
		this.owner = owner;
		this.chunkX = this.x >> 4;
		this.chunkZ = this.z >> 4;
	}

	public int getChunkX() {
		return this.chunkX;
	}

	public int getChunkZ() {
		return this.chunkZ;
	}

	public Location getLocation() {
		return new Location(Bukkit.getServer().getWorld(this.world), x, y, z,
				pitch, yaw);
	}

	public String getOwner() {
		return this.owner;
	}

	public int getUID() {
		return this.UID;
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}
}