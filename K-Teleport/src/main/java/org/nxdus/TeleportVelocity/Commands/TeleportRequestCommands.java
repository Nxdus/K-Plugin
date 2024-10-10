package org.nxdus.TeleportVelocity.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.nxdus.TeleportVelocity.KTeleport;
import org.nxdus.TeleportVelocity.Subscribe.MessageSender;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TeleportRequestCommands {

    private static final HashMap<String, String> RequestTeleport = new HashMap<>();
    private static final HashMap<String, Boolean> PlayerDelaySuccess = new HashMap<>();

    private static ProxyServer proxyServer;
    private static KTeleport instance;

    public TeleportRequestCommands(ProxyServer proxyServer, KTeleport instance) {
        TeleportRequestCommands.proxyServer = proxyServer;
        TeleportRequestCommands.instance = instance;
    }

    public static BrigadierCommand createTpaCommand() {
        LiteralCommandNode<CommandSource> TpaCommand = BrigadierCommand.literalArgumentBuilder("tpa")
                .executes(commandContext -> {

                    CommandSource source = commandContext.getSource();

                    source.sendMessage(Component.text("tpa"));

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("players", StringArgumentType.word())
                        .suggests((commandContext, suggestionsBuilder) -> {

                            Collection<Player> searchPlayer = proxyServer.getAllPlayers();
                            Player sourcePlayer = (Player) commandContext.getSource();

                            try {
                                String playerArgument = commandContext.getArgument("players", String.class);

                                searchPlayer.forEach(player -> {
                                    if (!playerArgument.isEmpty() && player.getUsername().toLowerCase().startsWith(playerArgument.toLowerCase())) {
                                        if (!(player.getUsername().equalsIgnoreCase(sourcePlayer.getUsername()))) {
                                            suggestionsBuilder.suggest(player.getUsername(), VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername())));
                                        }
                                    }
                                });
                            } catch (IllegalArgumentException ignored) {
                                searchPlayer.forEach(player -> {
                                    if (!(player.getUsername().equalsIgnoreCase(sourcePlayer.getUsername()))) suggestionsBuilder.suggest(player.getUsername(), VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername())));
                                });
                            }

                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(commandContext -> {

                            CommandSource source = commandContext.getSource();

                            if (!(source instanceof Player)) return Command.SINGLE_SUCCESS;

                            Optional<Player> sourcePlayer = proxyServer.getPlayer(((Player) source).getUsername());
                            Optional<Player> targetPlayer = proxyServer.getPlayer(commandContext.getArgument("players", String.class));

                            if (targetPlayer.isEmpty() || sourcePlayer.isEmpty()) return Command.SINGLE_SUCCESS;

                            if (sourcePlayer.get().getUsername().equalsIgnoreCase(targetPlayer.get().getUsername())) {
                                source.sendMessage(
                                        Component.text("[", TextColor.color(0xFFC955))
                                                .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                .append(Component.text("You can't teleport to yourself !", TextColor.color(255, 68, 67)))
                                );
                                return Command.SINGLE_SUCCESS;
                            }

                            if (RequestTeleport.get(targetPlayer.get().getUsername()) != null) {
                                sourcePlayer.get().sendMessage(Component.text("Already has request !"));
                                return Command.SINGLE_SUCCESS;
                            }

                            RegisteredServer targetServer = targetPlayer.get().getCurrentServer().orElseThrow().getServer();

                            if (targetServer == null) return Command.SINGLE_SUCCESS;

                            RequestTeleport.put(targetPlayer.get().getUsername(), sourcePlayer.get().getUsername());
                            targetPlayer.get().sendMessage(Component.text("You have request !"));
                            MessageSender.sendPlayerTeleportAcceptMenu(targetServer, targetPlayer.get().getUsername());

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();

        return new BrigadierCommand(TpaCommand);
    }

    public static BrigadierCommand createTpahereCommand() {
        LiteralCommandNode<CommandSource> TpaHereCommand = BrigadierCommand.literalArgumentBuilder("tpahere")
                .executes(commandContext -> {

                    CommandSource source = commandContext.getSource();

                    source.sendMessage(Component.text("tpahere"));

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("players", StringArgumentType.word())
                        .suggests((commandContext, suggestionsBuilder) -> {

                            Collection<Player> searchPlayer = proxyServer.getAllPlayers();
                            Player sourcePlayer = (Player) commandContext.getSource();

                            try {
                                String playerArgument = commandContext.getArgument("players", String.class);

                                searchPlayer.forEach(player -> {
                                    if (!playerArgument.isEmpty() && player.getUsername().toLowerCase().startsWith(playerArgument.toLowerCase())) {
                                        if (!(player.getUsername().equalsIgnoreCase(sourcePlayer.getUsername()))) {
                                            suggestionsBuilder.suggest(player.getUsername(), VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername())));
                                        }
                                    }
                                });
                            } catch (IllegalArgumentException ignored) {
                                searchPlayer.forEach(player -> {
                                    if (!(player.getUsername().equalsIgnoreCase(sourcePlayer.getUsername()))) suggestionsBuilder.suggest(player.getUsername(), VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername())));
                                });
                            }

                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(commandContext -> {

                            CommandSource source = commandContext.getSource();

                            if (!(source instanceof Player)) return Command.SINGLE_SUCCESS;

                            Optional<Player> sourcePlayer = proxyServer.getPlayer(((Player) source).getUsername());
                            Optional<Player> targetPlayer = proxyServer.getPlayer(commandContext.getArgument("players", String.class));

                            if (targetPlayer.isEmpty() || sourcePlayer.isEmpty()) return Command.SINGLE_SUCCESS;

                            if (sourcePlayer.get().getUsername().equalsIgnoreCase(targetPlayer.get().getUsername())) {
                                source.sendMessage(
                                        Component.text("[", TextColor.color(0xFFC955))
                                                .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                .append(Component.text("You can't teleport to yourself !", TextColor.color(255, 68, 67)))
                                );
                                return Command.SINGLE_SUCCESS;
                            }

                            if (RequestTeleport.get(targetPlayer.get().getUsername()) != null) {
                                sourcePlayer.get().sendMessage(Component.text("Already has request !"));
                                return Command.SINGLE_SUCCESS;
                            }

                            RegisteredServer sourceServer = sourcePlayer.get().getCurrentServer().orElseThrow().getServer();

                            if (sourceServer == null) return Command.SINGLE_SUCCESS;

                            RequestTeleport.put(sourcePlayer.get().getUsername(), targetPlayer.get().getUsername());
                            sourcePlayer.get().sendMessage(Component.text("You have request !"));
                            MessageSender.sendPlayerTeleportAcceptMenu(sourceServer, sourcePlayer.get().getUsername());

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();

        return new BrigadierCommand(TpaHereCommand);
    }

    public static BrigadierCommand createTpacceptCommand() {
        LiteralCommandNode<CommandSource> TpacceptCommand = BrigadierCommand.literalArgumentBuilder("tpaccept")
                .executes(commandContext -> {

                    CommandSource source = commandContext.getSource();

                    if (!(source instanceof Player)) return Command.SINGLE_SUCCESS;

                    TeleportAccept(((Player) source).getUsername());

                    return Command.SINGLE_SUCCESS;
                })
                .build();
        return new BrigadierCommand(TpacceptCommand);
    }

    public static void PlayerDelaySuccess(String playerName) {
        PlayerDelaySuccess.put(playerName, true);
    }

    public static void TeleportAccept(String playerName) {
        Optional<Player> sourcePlayer = proxyServer.getPlayer(playerName);

        if (sourcePlayer.isEmpty()) return;

        String requester = RequestTeleport.get(sourcePlayer.get().getUsername());

        if (requester == null) {
            sourcePlayer.get().sendMessage(Component.text("You don't have any requests"));
            return;
        }

        Optional<Player> requesterPlayer = proxyServer.getPlayer(requester);

        if (requesterPlayer.isEmpty()) return;

        RegisteredServer sourceServer = sourcePlayer.get().getCurrentServer().orElseThrow().getServer();
        RegisteredServer targetServer = requesterPlayer.get().getCurrentServer().orElseThrow().getServer();

        if (sourceServer == null || targetServer == null) return;

        MessageSender.sendPlayerTeleportDelay(targetServer, requesterPlayer.get().getUsername());

        proxyServer.getScheduler().buildTask(
                        instance, () -> {

                            if (PlayerDelaySuccess.containsKey(requesterPlayer.get().getUsername()) && PlayerDelaySuccess.get(requesterPlayer.get().getUsername())) {
                                requesterPlayer.get().sendMessage(Component.text("Success"));
                                if (sourceServer.getServerInfo().getName().equalsIgnoreCase(targetServer.getServerInfo().getName())) {
                                    MessageSender.teleportToPlayers(sourceServer,requesterPlayer.get().getUsername(),sourcePlayer.get().getUsername(),true);
                                } else if (requesterPlayer.get().createConnectionRequest(sourceServer).connectWithIndication().completeExceptionally(new Throwable())) {
                                    MessageSender.teleportToPlayers(sourceServer,requesterPlayer.get().getUsername(),sourcePlayer.get().getUsername(),false);
                                }
                            }

                            RequestTeleport.remove(sourcePlayer.get().getUsername());
                            PlayerDelaySuccess.remove(requesterPlayer.get().getUsername());
                        })
                .delay(6L, TimeUnit.SECONDS)
                .schedule();
    }

}
