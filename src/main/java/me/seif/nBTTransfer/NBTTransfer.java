package me.seif.nBTTransfer;

import me.seif.nBTTransfer.commands.FetchPlayerDataCommand;
import me.seif.nBTTransfer.commands.TransferDataCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class NBTTransfer extends JavaPlugin {

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        getCommand("transferdata").setExecutor(new TransferDataCommand(this));
        getCommand("fetchplayerdata").setExecutor(new FetchPlayerDataCommand(this));
    }
}
