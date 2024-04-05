package com.ortodontalio.smindless.command;

import com.ortodontalio.smindless.model.Smile;
import com.ortodontalio.smindless.service.PermissionService;
import com.ortodontalio.smindless.service.SmindlessService;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static com.ortodontalio.smindless.command.CommandType.*;

public class SmilesCommand implements CommandExecutor {

    private static final String MESSAGE_NO_PERMS = "messages.no-perms";
    private static final String MESSAGE_NO_SMILES = "messages.no-smiles";
    private static final String MESSAGE_NO_PLAYER = "messages.player-not-found";
    private static final String MESSAGE_NO_SMILE = "messages.smile-not-found";
    private static final String MESSAGE_RELOADED = "messages.reloaded";
    private static final String MESSAGE_SMILES_LIST = "messages.smiles-list";
    private static final String MESSAGE_PREMIUM_LIST = "messages.premium-list";
    private static final String MESSAGE_SMILES_PLAYER_LIST = "messages.smiles-player-list";
    private static final String MESSAGE_USER_BANNED = "messages.user-banned";
    private static final String MESSAGE_USER_UNBANNED = "messages.user-unbanned";
    private static final String MESSAGE_USER_GRANTED = "messages.user-granted";
    private static final String MESSAGE_USER_UNGRANTED = "messages.user-ungranted";
    private static final String MESSAGE_USER_ALREADY_BANNED = "messages.user-already-banned";
    private static final String MESSAGE_USE_GRANT = "messages.use-grant";
    private static final String MESSAGE_USER_ALREADY_UNBANNED = "messages.user-already-unbanned";
    private static final String MESSAGE_USER_ALREADY_GRANTED = "messages.user-already-granted";
    private static final String MESSAGE_USER_ALREADY_UNGRANTED = "messages.user-already-ungranted";
    private static final String MESSAGE_GRANT_NON_PREMIUM_FAILED = "messages.non-premium";
    private static final String MESSAGE_GRANT_NON_PREMIUM_UNGRANT = "messages.non-premium-ungrant";
    private static final String MESSAGE_BAN_FAILED = "messages.ban-failed";
    private static final String MESSAGE_UNBAN_FAILED = "messages.unban-failed";
    private static final String MESSAGE_GRANT_FAILED = "messages.grant-failed";
    private static final String MESSAGE_UNGRANT_FAILED = "messages.ungrant-failed";
    private final JavaPlugin plugin;
    private final PermissionService permissionService;

    public SmilesCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.permissionService = new PermissionService(plugin);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label,
                             String[] args) {
        if (args.length > 0) {
            CommandType smiCommand = CommandType.getByNameOrAlias(args[0]);
            return switch (smiCommand) {
                case LIST -> listCommand(sender);
                case PLAYER_LIST -> playerListCommand(sender, args);
                case RELOAD -> reloadCommand(sender);
                case BAN -> banCommand(sender, args);
                case UNBAN -> unbanCommand(sender, args);
                case GRANT -> grantCommand(sender, args);
                case UNGRANT -> ungrantCommand(sender, args);
            };
        }
        return false;
    }

    /**
     * Method, describing /smi r command. Used for reloading all smiles.
     *
     * @param sender is a command sender (player).
     * @return true, if command has worked successfully.
     */
    private boolean reloadCommand(CommandSender sender) {
        if (checkCommandPermission(sender, RELOAD)) {
            plugin.reloadConfig();
            int reloadedCount = SmindlessService.getInstance().initSmiles(plugin.getConfig());
            sender.sendMessage(SmindlessService.getInstance().colored(Objects.requireNonNull(
                            plugin.getConfig().getString(MESSAGE_RELOADED))
                    .replace("%count%", String.valueOf(reloadedCount))));
            return true;
        }
        return false;
    }

    /**
     * Method, describing /smi l command. Used for listing all smiles.
     *
     * @param sender is a command sender (player).
     * @return true, if command has worked successfully.
     */
    private boolean listCommand(CommandSender sender) {
        if (checkCommandPermission(sender, LIST)) {
            List<Smile> allSmiles = SmindlessService.getInstance().getSmiles();
            if (allSmiles.isEmpty()) {
                sendServerMessage(sender, MESSAGE_NO_SMILES);
                return true;
            }
            if (sender instanceof Player player) {
                TextComponent smilesList = new TextComponent(SmindlessService.getInstance().colored(plugin.getConfig()
                        .getString(MESSAGE_SMILES_LIST)));
                printSmiles(smilesList, allSmiles.stream().filter(smile -> !smile.premium()).toList(), player, false);
                smilesList.addExtra("\n");
                smilesList.addExtra(new TextComponent(SmindlessService.getInstance().colored(plugin.getConfig()
                        .getString(MESSAGE_PREMIUM_LIST))));
                printSmiles(smilesList, allSmiles.stream().filter(Smile::premium).toList(), player, false);
                player.spigot().sendMessage(smilesList);
            }
            return true;
        }
        return false;
    }

    /**
     * Secondary method for printing smiles
     * @param smilesComponent base component, accumulates all text for smiles list;
     * @param smilesList smiles list to print;
     * @param player player, which runs the command (or for which list should be printed);
     * @param playerList if smiles list is printed for another player.
     */
    private void printSmiles(TextComponent smilesComponent, List<Smile> smilesList, Player player, boolean playerList) {
        for (Smile smile : smilesList) {
            String smileOutput = smile.output();
            boolean hasSmilePerm = permissionService.hasSmilePermission(player, smile);
            if (!hasSmilePerm) {
                smileOutput = "&8" + smileOutput;
            }
            TextComponent smileView = new TextComponent(SmindlessService.getInstance().colored(smileOutput));
            smileView.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(SmindlessService.getInstance().colored(determineHoverText(smile)))));
            if (playerList) {
                smileView.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        determineCommandForPlayerList(player.getName(), smile, hasSmilePerm)));
            } else {
                smileView.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                        determineCopyCommand(smile, hasSmilePerm)));
            }
            smilesComponent.addExtra(smileView);
        }
    }

    /**
     * Secondary method to determine hover text for smile.
     * @param smile smile with hover text.
     * @return hover text.
     */
    private String determineHoverText(Smile smile) {
        String joiningInputs = String.join(",", smile.inputs());
        if (smile.premium()) {
            return String.format("&6Premium: %s", joiningInputs);
        }
        return joiningInputs;
    }

    /**
     * Secondary method to determine copied text when click on the smile.
     * @param smile clickable smile.
     * @param hasSmilePerm if the player has permissions for this smile.
     * @return smile's output, if the player has permissions, otherwise - first input placeholder.
     */
    private String determineCopyCommand(Smile smile, boolean hasSmilePerm) {
        if (hasSmilePerm) {
            return smile.output();
        }
        return smile.inputs().get(0);
    }

    /**
     * Method, describing /smi p command. Used for listing all smiles for the specific player
     *
     * @param sender is a command sender (player).
     * @return true, if command has worked successfully.
     */
    private boolean playerListCommand(CommandSender sender, String[] args) {
        if (checkCommandPermission(sender, PLAYER_LIST) && (args.length == 2)) {
            String playerName = args[1];
            Player playerToList = Bukkit.getPlayerExact(playerName);
            if (playerToList == null) {
                sendServerMessage(sender, MESSAGE_NO_PLAYER);
                return true;
            }
            List<Smile> allSmiles = SmindlessService.getInstance().getSmiles();
            if (allSmiles.isEmpty()) {
                sendServerMessage(sender, MESSAGE_NO_SMILES);
                return true;
            }
            if (sender instanceof Player player) {
                TextComponent smilesList = new TextComponent(SmindlessService.getInstance().colored(
                        Objects.requireNonNull(plugin.getConfig().getString(MESSAGE_SMILES_PLAYER_LIST))
                                .replace("%user%", playerName)));
                printSmiles(smilesList, allSmiles.stream().filter(smile -> !smile.premium()).toList(), player, true);
                smilesList.addExtra("\n");
                smilesList.addExtra(new TextComponent(SmindlessService.getInstance().colored(plugin.getConfig()
                        .getString(MESSAGE_PREMIUM_LIST))));
                printSmiles(smilesList, allSmiles.stream().filter(Smile::premium).toList(), player, true);
                player.spigot().sendMessage(smilesList);
            }
            return true;
        }
        return false;
    }

    private String determineCommandForPlayerList(String playerName, Smile smile, boolean hasPermForSmile) {
        if (!hasPermForSmile) {
            if (smile.premium()) {
                return String.format("/smi g %s %s", playerName, smile.inputs().get(0));
            }
            return String.format("/smi ub %s %s", playerName, smile.inputs().get(0));
        }
        if (smile.premium()) {
            return String.format("/smi ug %s %s", playerName, smile.inputs().get(0));
        }
        return String.format("/smi b %s %s", playerName, smile.inputs().get(0));
    }

    /**
     * Method, describing /smi b command. Used to prohibit a player from writing a certain smile.
     *
     * @param sender is a command sender (player).
     * @return true, if command has worked successfully.
     */
    private boolean banCommand(CommandSender sender, String[] args) {
        if (checkCommandPermission(sender, BAN) && (args.length == 3)) {
            String playerName = args[1];
            String smileKey = args[2];
            Player playerToBan = Bukkit.getPlayerExact(playerName);
            Smile smileByKey = SmindlessService.getInstance().getSmilesSortedByInput().get(smileKey);
            if (checkUserAndSmile(sender, playerToBan, smileByKey)) {
                if (smileByKey.premium()) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_USE_GRANT, smileByKey.output(), playerName);
                    return true;
                }
                if (!permissionService.hasSmilePermission(playerToBan, smileByKey)) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_USER_ALREADY_BANNED, smileByKey.output(), playerName);
                    return true;
                }
                //noinspection ConstantConditions
                if (!permissionService.banSmile(playerToBan, smileByKey.key())) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_BAN_FAILED, smileByKey.output(), playerName);
                    return true;
                }
                sendServerMessageWithSmileUser(sender, MESSAGE_USER_BANNED, smileByKey.output(), playerName);
            }
            return true;
        }
        return false;
    }

    /**
     * Method, describing /smi g command. Used to give the player the opportunity to use the premium smile.
     *
     * @param sender is a command sender (player).
     * @return true, if command has worked successfully.
     */
    private boolean grantCommand(CommandSender sender, String[] args) {
        if (checkCommandPermission(sender, GRANT) && (args.length == 3)) {
            String playerName = args[1];
            String smileKey = args[2];
            Player playerToGrant = Bukkit.getPlayerExact(playerName);
            Smile smileByKey = SmindlessService.getInstance().getSmilesSortedByInput().get(smileKey);
            if (checkUserAndSmile(sender, playerToGrant, smileByKey)) {
                if (!smileByKey.premium()) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_GRANT_NON_PREMIUM_FAILED, smileByKey.output(), playerName);
                    return true;
                }
                if (permissionService.hasSmilePermission(playerToGrant, smileByKey)) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_USER_ALREADY_GRANTED, smileByKey.output(), playerName);
                    return true;
                }
                //noinspection ConstantConditions
                if (!permissionService.grantSmile(playerToGrant, smileByKey)) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_GRANT_FAILED, smileByKey.output(), playerName);
                    return true;
                }
                sendServerMessageWithSmileUser(sender, MESSAGE_USER_GRANTED, smileByKey.output(), playerName);
            }
            return true;
        }
        return false;
    }

    /**
     * Method, describing /smi u command. Used for allowing a player to write a certain smile.
     *
     * @param sender is a command sender (player).
     * @return true, if command has worked successfully.
     */
    private boolean unbanCommand(CommandSender sender, String[] args) {
        if (checkCommandPermission(sender, UNBAN) && (args.length == 3)) {
            String playerName = args[1];
            String smileKey = args[2];
            Player playerToUnban = Bukkit.getPlayerExact(playerName);
            Smile smileByKey = SmindlessService.getInstance().getSmilesSortedByInput().get(smileKey);
            if (checkUserAndSmile(sender, playerToUnban, smileByKey)) {
                if (smileByKey.premium()) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_USE_GRANT, smileByKey.output(), playerName);
                    return true;
                }
                if (permissionService.hasSmilePermission(playerToUnban, smileByKey)) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_USER_ALREADY_UNBANNED, smileByKey.output(), playerName);
                    return true;
                }
                //noinspection ConstantConditions
                if (!permissionService.unbanSmile(playerToUnban, smileByKey.key())) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_UNBAN_FAILED, smileByKey.output(), playerName);
                    return true;
                }
                sendServerMessageWithSmileUser(sender, MESSAGE_USER_UNBANNED, smileByKey.output(), playerName);
            }
            return true;
        }
        return false;
    }

    /**
     * Method, describing /smi ug command. Used to prohibit a player from writing a certain premium smile.
     *
     * @param sender is a command sender (player).
     * @return true, if command has worked successfully.
     */
    private boolean ungrantCommand(CommandSender sender, String[] args) {
        if (checkCommandPermission(sender, UNGRANT) && (args.length == 3)) {
            String playerName = args[1];
            String smileKey = args[2];
            Player playerToBan = Bukkit.getPlayerExact(playerName);
            Smile smileByKey = SmindlessService.getInstance().getSmilesSortedByInput().get(smileKey);
            if (checkUserAndSmile(sender, playerToBan, smileByKey)) {
                if (!smileByKey.premium()) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_GRANT_NON_PREMIUM_UNGRANT, smileByKey.output(), playerName);
                    return true;
                }
                if (!permissionService.hasSmilePermission(playerToBan, smileByKey)) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_USER_ALREADY_UNGRANTED, smileByKey.output(), playerName);
                    return true;
                }
                //noinspection ConstantConditions
                if (!permissionService.ungrantSmile(playerToBan, smileByKey)) {
                    sendServerMessageWithSmileUser(sender, MESSAGE_UNGRANT_FAILED, smileByKey.output(), playerName);
                    return true;
                }
                sendServerMessageWithSmileUser(sender, MESSAGE_USER_UNGRANTED, smileByKey.output(), playerName);
            }
            return true;
        }
        return false;
    }

    /**
     * Secondary method for check, if player has a permission to run the command.
     *
     * @param sender  player, which runs a command.
     * @param command command, need to be checked.
     * @return true, if player has a permission, otherwise - false.
     */
    private boolean checkCommandPermission(CommandSender sender, CommandType command) {
        if (!permissionService.hasCommandPermission(sender, command)) {
            sendServerMessage(sender, MESSAGE_NO_PERMS);
            return false;
        }
        return true;
    }


    /**
     * Secondary method to check, if the user and the smile is not null.
     * @param sender command sender.
     * @param player player, determined by some command.
     * @param smile smile, determined by some command.
     * @return true is both of the player and the smile are not null.
     */
    private boolean checkUserAndSmile(CommandSender sender, @Nullable Player player, @Nullable Smile smile) {
        if (player == null) {
            sendServerMessage(sender, MESSAGE_NO_PLAYER);
            return false;
        }
        if (smile == null) {
            sendServerMessage(sender, MESSAGE_NO_SMILE);
            return false;
        }
        return true;
    }

    /**
     * Secondary method to send a message with %smile% and %user% placeholders.
     * @param sender command sender;
     * @param message message to send to chat;
     * @param smileKey smile's key, replace %smile%;
     * @param playerName player's name, replace %user%.
     */
    private void sendServerMessageWithSmileUser(CommandSender sender, String message, String smileKey, String playerName) {
        sender.sendMessage(SmindlessService.getInstance().colored(Objects.requireNonNull(plugin.getConfig()
                        .getString(message))
                .replace("%smile%", smileKey)
                .replace("%user%", playerName)));
    }

    /**
     * Secondary method to send a colored message to chat.
     * @param sender command sender;
     * @param messageKey message key from the config.
     */
    private void sendServerMessage(CommandSender sender, String messageKey) {
        sender.sendMessage(SmindlessService.getInstance().colored(plugin.getConfig().getString(messageKey)));
    }
}
