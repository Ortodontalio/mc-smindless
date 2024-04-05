package com.ortodontalio.smindless.service;

import com.ortodontalio.smindless.command.CommandType;
import com.ortodontalio.smindless.model.Smile;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionService {
    private static final String SMILE_COMMAND_PERM = "smindless.command.%s";
    private final FileService fileService;

    public PermissionService(JavaPlugin plugin) {
        this.fileService = new FileService(plugin);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasCommandPermission(CommandSender player, CommandType command) {
        return player.hasPermission(String.format(SMILE_COMMAND_PERM, command.getName()));
    }

    public boolean hasSmilePermission(Player player, Smile smile) {
        if (smile.premium()) {
            return fileService.grantExistsForPlayer(player.getName(), smile.key());
        }
        return !fileService.restrictExistsForPlayer(player.getName(), smile.key());
    }

    public boolean banSmile(Player player, String key) {
        return fileService.writeToBanList(player.getName(), key);
    }

    public boolean unbanSmile(Player player, String key) {
        return fileService.deleteFromBanList(player.getName(), key);
    }

    public boolean grantSmile(Player player, Smile smile) {
        return fileService.writeToPremiumList(player.getName(), smile.key());
    }

    public boolean ungrantSmile(Player player, Smile smile) {
        return fileService.deleteFromPremiumList(player.getName(), smile.key());
    }
}
