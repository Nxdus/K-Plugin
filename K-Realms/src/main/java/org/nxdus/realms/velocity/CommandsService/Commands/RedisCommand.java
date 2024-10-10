package org.nxdus.realms.velocity.CommandsService.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.nxdus.core.velocity.KCoreVelocity;

public class RedisCommand {

    public static BrigadierCommand createRedisCommand(ProxyServer server) {

        LiteralCommandNode<CommandSource> RedisCommand = BrigadierCommand.literalArgumentBuilder("vredis")
                .then(BrigadierCommand.requiredArgumentBuilder("channel", StringArgumentType.string())
                        .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.string())
                                .executes(commandContext -> {

                                    CommandSource commandSource = commandContext.getSource();

                                    String channel = StringArgumentType.getString(commandContext, "channel");
                                    String message = StringArgumentType.getString(commandContext, "message");

                                    if(channel == null || channel.isEmpty() || message == null || message.isEmpty()) {
                                        commandSource.sendMessage(
                                                Component.text("Argument [1,2] is required", TextColor.color(255, 0, 0))
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    // Start Pub
                                    KCoreVelocity.redisManager.publish(channel, message);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
        return new BrigadierCommand(RedisCommand);
    }

}
