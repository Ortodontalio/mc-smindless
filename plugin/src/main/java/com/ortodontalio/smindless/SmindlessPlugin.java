package com.ortodontalio.smindless;

import com.ortodontalio.smindless.command.SmilesCommand;
import com.ortodontalio.smindless.listener.SmindlessListener;
import com.ortodontalio.smindless.service.SmindlessService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class SmindlessPlugin extends JavaPlugin {
    public static final String SMILES_COMMAND = "smiles";
    private final SmindlessService service = SmindlessService.getInstance();

    @Override
    public void onEnable() {
        service.initSmiles(getConfig());
        saveDefaultConfig();
        afterStarting();
        getServer().getPluginManager().registerEvents(new SmindlessListener(this), this);
        Objects.requireNonNull(getServer().getPluginCommand(SMILES_COMMAND)).setExecutor(new SmilesCommand(this));
    }

    private void afterStarting() {
        getLogger().info(" .d8888b.  888b     d888 8888888 888b    888 8888888b.  888      8888888888 .d8888b.   .d8888b.  ");
        getLogger().info("d88P  Y88b 8888b   d8888   888   8888b   888 888  \"Y88b 888      888       d88P  Y88b d88P  Y88b ");
        getLogger().info("Y88b.      88888b.d88888   888   88888b  888 888    888 888      888       Y88b.      Y88b.      ");
        getLogger().info(" \"Y888b.   888Y88888P888   888   888Y88b 888 888    888 888      8888888    \"Y888b.    \"Y888b.   ");
        getLogger().info("    \"Y88b. 888 Y888P 888   888   888 Y88b888 888    888 888      888           \"Y88b.     \"Y88b. ");
        getLogger().info("      \"888 888  Y8P  888   888   888  Y88888 888    888 888      888             \"888       \"888 ");
        getLogger().info("Y88b  d88P 888   \"   888   888   888   Y8888 888  .d88P 888      888       Y88b  d88P Y88b  d88P ");
        getLogger().info(" \"Y8888P\"  888       888 8888888 888    Y888 8888888P\"  88888888 8888888888 \"Y8888P\"   \"Y8888P\"");
    }
}
