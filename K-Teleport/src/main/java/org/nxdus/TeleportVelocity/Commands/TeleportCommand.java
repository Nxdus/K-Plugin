package org.nxdus.TeleportVelocity.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
import org.nxdus.TeleportVelocity.Listeners.WorldListListener;
import org.nxdus.TeleportVelocity.Subscribe.MessageSender;

import java.util.Collection;
import java.util.List;

public class TeleportCommand {

    public static BrigadierCommand createTeleportCommand(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> vTeleport = BrigadierCommand.literalArgumentBuilder("vteleport")
                .executes(commandContext -> {
                    CommandSource commandSource = commandContext.getSource();

                    commandSource.sendMessage(
                            Component.text("----------------", TextColor.color(0xFFC955))
                                    .append(Component.text("[", TextColor.color(0xFFC955)))
                                    .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                    .append(Component.text("]", TextColor.color(0xFFC955)))
                                    .append(Component.text("----------------", TextColor.color(0xFFC955)))
                    );

                    commandSource.sendMessage(
                            Component.text("-", TextColor.color(0xFFFFFF))
                                    .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Players] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                    .append(Component.text("for teleport to players", TextColor.color(0xFFE884)))
                    );

                    commandSource.sendMessage(
                            Component.text("-", TextColor.color(0xFFFFFF))
                                    .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Players] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Players/Servers] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                    .append(Component.text("for teleport player to another servers or players", TextColor.color(0xFFE884)))
                    );

                    commandSource.sendMessage(
                            Component.text("-", TextColor.color(0xFFFFFF))
                                    .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Players] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Servers] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Worlds] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[X] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Y] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("[Z] ", TextColor.color(0xCCCCCC)))
                                    .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                    .append(Component.text("for teleport player to another servers and select worlds " +
                                            "if direct server not have player online you can go only main world !", TextColor.color(0xFFE884)))
                    );

                    commandSource.sendMessage(Component.text("-------------------------------------------------", TextColor.color(0xFFC955)));

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("players", StringArgumentType.word())
                        .suggests((commandContext, suggestionsBuilder) -> {

                            Collection<Player> searchPlayer = proxyServer.getAllPlayers();

                            searchPlayer.forEach(player -> {
                                try {
                                    String playerArgument = commandContext.getArgument("players", String.class);

                                    if (!playerArgument.isEmpty() && player.getUsername().toLowerCase().startsWith(playerArgument.toLowerCase())) {
                                        suggestionsBuilder.suggest(
                                                player.getUsername(),
                                                VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername()))
                                        );
                                    }
                                } catch (IllegalArgumentException ignored) {
                                    suggestionsBuilder.suggest(
                                            player.getUsername(),
                                            VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername()))
                                    );
                                }
                            });

                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(commandContext -> {

                            CommandSource commandSource = commandContext.getSource();

                            if (!(commandSource instanceof Player)) return Command.SINGLE_SUCCESS;

                            Player sourcePlayer = proxyServer.getPlayer(((Player) commandSource).getUsername()).orElseThrow();
                            RegisteredServer sourceServer = sourcePlayer.getCurrentServer().orElseThrow().getServer();

                            Player targetPlayer = proxyServer.getPlayer(commandContext.getArgument("players", String.class)).orElseThrow();
                            RegisteredServer targetServer = targetPlayer.getCurrentServer().orElseThrow().getServer();

                            if (sourcePlayer.getUsername().equalsIgnoreCase(targetPlayer.getUsername())) {
                                commandSource.sendMessage(
                                        Component.text("[", TextColor.color(0xFFC955))
                                                .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                .append(Component.text("You can't teleport to yourself !", TextColor.color(255, 68, 67)))
                                );
                                return Command.SINGLE_SUCCESS;
                            }

                            if (sourceServer.getServerInfo().getName().equalsIgnoreCase(targetServer.getServerInfo().getName())) {
                                MessageSender.teleportToPlayers(sourceServer,sourcePlayer.getUsername(),targetPlayer.getUsername(),true);
                                return Command.SINGLE_SUCCESS;
                            } else if (sourcePlayer.createConnectionRequest(targetServer).connectWithIndication().completeExceptionally(new Throwable())) {
                                MessageSender.teleportToPlayers(targetServer,sourcePlayer.getUsername(),targetPlayer.getUsername(),false);
                                return Command.SINGLE_SUCCESS;
                            }

                            commandSource.sendMessage(
                                    Component.text("[", TextColor.color(0xFFC955))
                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                            .append(Component.text("teleport to " + targetPlayer.getUsername(), TextColor.color(0xB8FF92)))
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("targets", StringArgumentType.string())
                                .suggests((commandContext, suggestionsBuilder) -> {

                                    Collection<Player> searchPlayer = proxyServer.getAllPlayers();
                                    Collection<RegisteredServer> searchServer = proxyServer.getAllServers();

                                    try {
                                        String targetsArgument = commandContext.getArgument("targets", String.class);

                                        searchPlayer.forEach(player -> {
                                            if (!targetsArgument.isEmpty() && player.getUsername().toLowerCase().contains(targetsArgument.toLowerCase())) {
                                                suggestionsBuilder.suggest(
                                                        player.getUsername(),
                                                        VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername()))
                                                );
                                            }
                                        });

                                        searchServer.forEach(server -> {
                                            if (!targetsArgument.isEmpty() && server.getServerInfo().getName().toLowerCase().contains(targetsArgument.toLowerCase())) {
                                                suggestionsBuilder.suggest(
                                                        "\"#" + server.getServerInfo().getName() + "\"",
                                                        VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(server.getServerInfo().getName()))
                                                );
                                            }
                                        });

                                    } catch (IllegalArgumentException ignored) {
                                        searchPlayer.forEach(player ->
                                            suggestionsBuilder.suggest(
                                                    player.getUsername(),
                                                    VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername()))
                                            )
                                        );

                                        searchServer.forEach(server ->
                                            suggestionsBuilder.suggest(
                                                    "\"#" + server.getServerInfo().getName() + "\"",
                                                    VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(server.getServerInfo().getName()))
                                            )
                                        );
                                    }

                                    return suggestionsBuilder.buildFuture();
                                })
                                .executes(commandContext -> {

                                    Player fromPlayer = proxyServer.getPlayer(commandContext.getArgument("players", String.class)).orElseThrow();
                                    RegisteredServer fromServer = fromPlayer.getCurrentServer().orElseThrow().getServer();

                                    String targetsArgument = commandContext.getArgument("targets", String.class);

                                    if (targetsArgument.contains("#")) {
                                        RegisteredServer targetServer = proxyServer.getServer(targetsArgument.replace("#", "").replace("\"", "")).orElseThrow();
                                        if (!targetServer.getServerInfo().getName().equalsIgnoreCase(fromServer.getServerInfo().getName())) {
                                            if (fromPlayer.createConnectionRequest(targetServer).connectWithIndication().completeExceptionally(new Throwable())) {
                                                commandContext.getSource().sendMessage(
                                                        Component.text("[", TextColor.color(0xFFC955))
                                                                .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                .append(Component.text("teleport " + fromPlayer.getUsername() + " to " + targetServer.getServerInfo().getName(), TextColor.color(0xB8FF92)))
                                                );
                                                return Command.SINGLE_SUCCESS;
                                            }
                                        } else {
                                            commandContext.getSource().sendMessage(
                                                    Component.text("[", TextColor.color(0xFFC955))
                                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                            .append(Component.text(fromPlayer.getUsername() + " already on this server !", TextColor.color(255, 68, 67)))
                                            );
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    } else {
                                        Player targetPlayer = proxyServer.getPlayer(commandContext.getArgument("targets", String.class)).orElseThrow();
                                        RegisteredServer targetPlayerServer = targetPlayer.getCurrentServer().orElseThrow().getServer();

                                        if (fromServer.getServerInfo().getName().equalsIgnoreCase(targetPlayerServer.getServerInfo().getName())) {
                                            if (fromPlayer.getUsername().equalsIgnoreCase(targetPlayer.getUsername())) {
                                                commandContext.getSource().sendMessage(
                                                        Component.text("[", TextColor.color(0xFFC955))
                                                                .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                .append(Component.text("You can't teleport to yourself !", TextColor.color(255, 68, 67)))
                                                );
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            MessageSender.teleportToPlayers(fromServer,fromPlayer.getUsername(),targetPlayer.getUsername(),true);

                                            commandContext.getSource().sendMessage(
                                                    Component.text("[", TextColor.color(0xFFC955))
                                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                            .append(Component.text("teleport " + fromPlayer.getUsername() + " to " + targetPlayer.getUsername(), TextColor.color(0xB8FF92)))
                                            );
                                            return Command.SINGLE_SUCCESS;
                                        } else if (fromPlayer.createConnectionRequest(targetPlayerServer).connectWithIndication().completeExceptionally(new Throwable())) {

                                            MessageSender.teleportToPlayers(targetPlayerServer,fromPlayer.getUsername(),targetPlayer.getUsername(),false);

                                            commandContext.getSource().sendMessage(
                                                    Component.text("[", TextColor.color(0xFFC955))
                                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                            .append(Component.text("teleport " + fromPlayer.getUsername() + " to " + targetPlayer.getUsername(), TextColor.color(0xB8FF92)))
                                            );
                                            return Command.SINGLE_SUCCESS;
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(BrigadierCommand.requiredArgumentBuilder("worlds", StringArgumentType.string())
                                        .suggests((commandContext, suggestionsBuilder) -> {
                                            CommandSource commandSource = commandContext.getSource();
                                            String targetsArgument = commandContext.getArgument("targets", String.class);

                                            if (targetsArgument.contains("#")) {
                                                RegisteredServer targetServer = proxyServer.getServer(targetsArgument.replace("#", "").replace("\"", "")).orElseThrow();
                                                List<String> worldList = WorldListListener.worldList.get(targetServer.getServerInfo().getName());

                                                if (worldList != null) {
                                                    try {
                                                        String worldsArgument = commandContext.getArgument("worlds", String.class);

                                                        worldList.forEach(world -> {
                                                            if (!worldsArgument.isEmpty() && world.toLowerCase().contains(worldsArgument.toLowerCase())) {
                                                                suggestionsBuilder.suggest(
                                                                        world,
                                                                        VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(world))
                                                                );
                                                            }
                                                        });

                                                    } catch (IllegalArgumentException ignored) {
                                                        worldList.forEach(world ->
                                                                suggestionsBuilder.suggest(
                                                                        world,
                                                                        VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(world))
                                                                )
                                                        );
                                                    }
                                                } else {
                                                    commandSource.sendMessage(
                                                            Component.text("[", TextColor.color(0xFFC955))
                                                                    .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                    .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                    .append(Component.text("Please connect server for searching world first !", TextColor.color(204, 204, 206)))
                                                    );
                                                }

                                            } else {
                                                commandSource.sendMessage(
                                                        Component.text("[", TextColor.color(0xFFC955))
                                                                .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                .append(Component.text("You can't find the world's name by player's name !", TextColor.color(255, 68, 67)))
                                                );
                                            }

                                            return suggestionsBuilder.buildFuture();
                                        })
                                        .executes(commandContext -> {
                                            commandContext.getSource().sendMessage(
                                                    Component.text("[", TextColor.color(0xFFC955))
                                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                            .append(Component.text("-", TextColor.color(0xFFFFFF)))
                                                            .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text(commandContext.getArgument("players", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text(commandContext.getArgument("targets", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text(commandContext.getArgument("worlds", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text("[X] ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text("[Y] ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text("[Z] ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text("[Yaw] ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text("[Pitch] ", TextColor.color(0xCCCCCC)))
                                                            .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                                            .append(Component.text("Please type coordinates successfully", TextColor.color(0xFFE884)))
                                            );
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(BrigadierCommand.requiredArgumentBuilder("posX", DoubleArgumentType.doubleArg())
                                                .suggests((commandContext, suggestionsBuilder) -> {
                                                    suggestionsBuilder.suggest("0.0");
                                                    return suggestionsBuilder.buildFuture();
                                                })
                                                .executes(commandContext -> {
                                                    Double posX = commandContext.getArgument("posX", Double.class);
                                                    commandContext.getSource().sendMessage(
                                                            Component.text("[", TextColor.color(0xFFC955))
                                                                    .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                    .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                    .append(Component.text("- ", TextColor.color(0xFFFFFF)))
                                                                    .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text(commandContext.getArgument("players", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text(commandContext.getArgument("targets", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text(commandContext.getArgument("worlds", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text(posX.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text("[Y] ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text("[Z] ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text("[Yaw] ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text("[Pitch] ", TextColor.color(0xCCCCCC)))
                                                                    .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                                                    .append(Component.text("Please type coordinates successfully", TextColor.color(0xFFE884)))
                                                    );
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(BrigadierCommand.requiredArgumentBuilder("posY", DoubleArgumentType.doubleArg())
                                                        .suggests((commandContext, suggestionsBuilder) -> {
                                                            suggestionsBuilder.suggest("0.0");
                                                            return suggestionsBuilder.buildFuture();
                                                        })
                                                        .executes(commandContext -> {

                                                            Double posX = commandContext.getArgument("posX", Double.class);
                                                            Double posY = commandContext.getArgument("posY", Double.class);

                                                            commandContext.getSource().sendMessage(
                                                                    Component.text("[", TextColor.color(0xFFC955))
                                                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                            .append(Component.text("- ", TextColor.color(0xFFFFFF)))
                                                                            .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text(commandContext.getArgument("players", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text(commandContext.getArgument("targets", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text(commandContext.getArgument("worlds", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text(posX.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text(posY.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text("[Z] ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text("[Yaw] ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text("[Pitch] ", TextColor.color(0xCCCCCC)))
                                                                            .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                                                            .append(Component.text("Please type coordinates successfully", TextColor.color(0xFFE884)))
                                                            );

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                        .then(BrigadierCommand.requiredArgumentBuilder("posZ", DoubleArgumentType.doubleArg())
                                                                .suggests((commandContext, suggestionsBuilder) -> {
                                                                    suggestionsBuilder.suggest("0.0");
                                                                    return suggestionsBuilder.buildFuture();
                                                                })
                                                                .executes(commandContext -> {
                                                                    Double posX = commandContext.getArgument("posX", Double.class);
                                                                    Double posY = commandContext.getArgument("posY", Double.class);
                                                                    Double posZ = commandContext.getArgument("posZ", Double.class);

                                                                    commandContext.getSource().sendMessage(
                                                                            Component.text("[", TextColor.color(0xFFC955))
                                                                                    .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                                    .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                                    .append(Component.text("- ", TextColor.color(0xFFFFFF)))
                                                                                    .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text(commandContext.getArgument("players", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text(commandContext.getArgument("targets", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text(commandContext.getArgument("worlds", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text(posX.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text(posY.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text(posZ.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text("[Yaw] ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text("[Pitch] ", TextColor.color(0xCCCCCC)))
                                                                                    .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                                                                    .append(Component.text("Please type coordinates successfully", TextColor.color(0xFFE884)))
                                                                    );

                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                                .then(BrigadierCommand.requiredArgumentBuilder("yaw", FloatArgumentType.floatArg())
                                                                        .suggests((commandContext, suggestionsBuilder) -> {
                                                                            suggestionsBuilder.suggest("0.0");
                                                                            return suggestionsBuilder.buildFuture();
                                                                        })
                                                                        .executes(commandContext -> {

                                                                            Double posX = commandContext.getArgument("posX", Double.class);
                                                                            Double posY = commandContext.getArgument("posY", Double.class);
                                                                            Double posZ = commandContext.getArgument("posZ", Double.class);
                                                                            Float yaw = commandContext.getArgument("yaw", Float.class);

                                                                            commandContext.getSource().sendMessage(
                                                                                    Component.text("[", TextColor.color(0xFFC955))
                                                                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                                            .append(Component.text("- ", TextColor.color(0xFFFFFF)))
                                                                                            .append(Component.text("/tp ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text(commandContext.getArgument("players", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text(commandContext.getArgument("targets", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text(commandContext.getArgument("worlds", String.class) + " ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text(posX.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text(posY.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text(posZ.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text(yaw.toString() + " ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text("[Pitch] ", TextColor.color(0xCCCCCC)))
                                                                                            .append(Component.text("- ", TextColor.color(0xA0A0A0)))
                                                                                            .append(Component.text("Please type coordinates successfully", TextColor.color(0xFFE884)))
                                                                            );

                                                                            return Command.SINGLE_SUCCESS;
                                                                        })
                                                                        .then(BrigadierCommand.requiredArgumentBuilder("pitch", FloatArgumentType.floatArg())
                                                                                .suggests((commandContext, suggestionsBuilder) -> {
                                                                                    suggestionsBuilder.suggest("0.0");
                                                                                    return suggestionsBuilder.buildFuture();
                                                                                })
                                                                                .executes(commandContext -> {
                                                                                    Player fromPlayer = proxyServer.getPlayer(commandContext.getArgument("players", String.class)).orElseThrow();
                                                                                    RegisteredServer fromServer = fromPlayer.getCurrentServer().orElseThrow().getServer();

                                                                                    String targetsArgument = commandContext.getArgument("targets", String.class);
                                                                                    String targetsWorld = commandContext.getArgument("worlds", String.class);

                                                                                    Double posX = commandContext.getArgument("posX", Double.class);
                                                                                    Double posY = commandContext.getArgument("posY", Double.class);
                                                                                    Double posZ = commandContext.getArgument("posZ", Double.class);
                                                                                    Float yaw = commandContext.getArgument("yaw", Float.class);
                                                                                    Float pitch = commandContext.getArgument("pitch", Float.class);

                                                                                    if (targetsArgument.contains("#")) {
                                                                                        RegisteredServer targetServer = proxyServer.getServer(targetsArgument.replace("#", "").replace("\"", "")).orElseThrow();

                                                                                        if (!targetServer.getServerInfo().getName().equalsIgnoreCase(fromServer.getServerInfo().getName())) {
                                                                                            if (fromPlayer.createConnectionRequest(targetServer).connectWithIndication().completeExceptionally(new Throwable())) {
                                                                                                MessageSender.teleportToWorld(targetServer,fromPlayer.getUsername(),targetsWorld,posX,posY,posZ,yaw,pitch,false);
                                                                                                commandContext.getSource().sendMessage(
                                                                                                        Component.text("[", TextColor.color(0xFFC955))
                                                                                                                .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                                                                .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                                                                .append(Component.text("teleport " + fromPlayer.getUsername() + " to " + targetsWorld + " in " + targetServer.getServerInfo().getName(), TextColor.color(0xB8FF92)))
                                                                                                );
                                                                                                return Command.SINGLE_SUCCESS;
                                                                                            }
                                                                                        } else {
                                                                                            MessageSender.teleportToWorld(targetServer,fromPlayer.getUsername(),targetsWorld,posX,posY,posZ,yaw,pitch,true);
                                                                                            commandContext.getSource().sendMessage(
                                                                                                    Component.text("[", TextColor.color(0xFFC955))
                                                                                                            .append(Component.text(" K-Realms Teleport ", TextColor.color(0xFFFFFF)))
                                                                                                            .append(Component.text("] ", TextColor.color(0xFFC955)))
                                                                                                            .append(Component.text("teleport " + fromPlayer.getUsername() + " to " + targetsWorld + " in " + targetServer.getServerInfo().getName(), TextColor.color(0xB8FF92)))
                                                                                            );
                                                                                            return Command.SINGLE_SUCCESS;
                                                                                        }
                                                                                    }
                                                                                    return Command.SINGLE_SUCCESS;
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build();

         return new BrigadierCommand(vTeleport);
    }

}
