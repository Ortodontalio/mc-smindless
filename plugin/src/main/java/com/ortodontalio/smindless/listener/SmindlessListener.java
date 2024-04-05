package com.ortodontalio.smindless.listener;

import com.ortodontalio.smindless.service.PermissionService;
import com.ortodontalio.smindless.service.SmindlessService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class SmindlessListener implements Listener {

    private final SmindlessService service = SmindlessService.getInstance();
    private final PermissionService permissionService;

    public SmindlessListener(JavaPlugin plugin) {
        this.permissionService = new PermissionService(plugin);
    }

    @EventHandler
    public void onPlayerMessageEvent(AsyncPlayerChatEvent event) {
        Optional.ofNullable(service.getSmilesSortedByInput())
                .ifPresent(smiles -> smiles.entrySet().stream()
                        .filter(entry -> event.getMessage().contains(entry.getKey()))
                        .filter(entry -> permissionService.hasSmilePermission(event.getPlayer(), entry.getValue()))
                        .forEach(entry -> event.setMessage(event.getMessage().replace(entry.getKey(),
                                SmindlessService.getInstance().colored(entry.getValue().output())))));
    }

    @EventHandler
    public void onServerMessageEvent(ServerCommandEvent event) {
        Optional.ofNullable(service.getSmilesSortedByInput())
                .ifPresent(smiles -> smiles.entrySet().stream()
                        .filter(entry -> event.getCommand().contains(entry.getKey()))
                        .forEach(entry -> event.setCommand(event.getCommand().replace(entry.getKey(),
                                SmindlessService.getInstance().colored(entry.getValue().output())))));
    }

    @EventHandler
    public void onBroadcastMessageEvent(BroadcastMessageEvent event) {
        Optional.ofNullable(service.getSmilesSortedByInput())
                .ifPresent(smiles -> smiles.entrySet().stream()
                        .filter(entry -> event.getMessage().contains(entry.getKey()))
                        .forEach(entry -> event.setMessage(event.getMessage().replace(entry.getKey(),
                                SmindlessService.getInstance().colored(entry.getValue().output())))));
    }
}
