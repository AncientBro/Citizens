package net.citizensnpcs.questers.quests.types;

import net.citizensnpcs.questers.quests.QuestIncrementer;
import net.citizensnpcs.questers.quests.Objectives.ObjectiveCycler;
import net.citizensnpcs.resources.npclib.HumanNPC;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class CollectQuest extends QuestIncrementer {
	public CollectQuest(HumanNPC npc, Player player, String questName,
			ObjectiveCycler objectives) {
		super(npc, player, questName, objectives);
	}

	@Override
	public void updateProgress(Event event) {
		if (event instanceof PlayerPickupItemEvent) {
			PlayerPickupItemEvent ev = (PlayerPickupItemEvent) event;
			if (ev.getItem().getItemStack().getType() == this.objective
					.getMaterial()) {
				this.getProgress().incrementCompleted(
						ev.getItem().getItemStack().getAmount());
			}
		}
	}

	@Override
	public boolean isCompleted() {
		return this.getProgress().getAmount() >= this.objective.getAmount();
	}

	@Override
	public Type[] getEventTypes() {
		return new Type[] { Type.PLAYER_PICKUP_ITEM };
	}
}