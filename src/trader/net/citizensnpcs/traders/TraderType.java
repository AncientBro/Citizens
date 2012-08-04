package net.citizensnpcs.traders;

import net.citizensnpcs.commands.CommandHandler;
import net.citizensnpcs.npctypes.CitizensNPC;
import net.citizensnpcs.npctypes.CitizensNPCType;
import net.citizensnpcs.npctypes.NPCTypeManager;
import net.citizensnpcs.properties.Properties;

public class TraderType extends CitizensNPCType {
    @Override
    public CommandHandler getCommands() {
        return TraderCommands.INSTANCE;
    }

    @Override
    public CitizensNPC getInstance() {
        return new Trader();
    }

    @Override
    public String getName() {
        return "trader";
    }

    @Override
    public Properties getProperties() {
        return TraderProperties.INSTANCE;
    }

    @Override
    public void registerEvents() {
        NPCTypeManager.registerEvents(new CitizensListen());
    }
}