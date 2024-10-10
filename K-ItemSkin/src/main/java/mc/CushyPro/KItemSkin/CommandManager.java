package mc.CushyPro.KItemSkin;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import mc.CushyPro.KItemSkin.Menus.MenuSetSkin;
import mc.CushyPro.KItemSkin.Menus.MenuSetting;
import mc.CushyPro.KItemSkin.Used.CommandModule;
import mc.CushyPro.KItemSkin.Used.RegData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CommandManager extends CommandModule implements BasicCommand {

    public CommandManager() {
        super();
        setLabel("/k-itemskin");
    }

    @RegData("reload")
    public class reload extends CommandModule {

        @Override
        public void onRun(CommandSender sender, String[] args) {
            KItemSkinMain.getInstance().getStoreConfig().loadConfig();
            sender.sendMessage("reload Config");
        }

    }

    @RegData("save")
    public class save extends CommandModule {

        @Override
        public void onRun(CommandSender sender, String[] args) {
            KItemSkinMain.getInstance().getStoreConfig().saveConfig();
            sender.sendMessage("save Config");
        }

    }

    @RegData("model")
    public class model extends CommandModule {

        @Override
        public void onRun(CommandSender sender, String[] args) {
            if (sender instanceof Player player) {
                ItemStack stack = player.getInventory().getItemInMainHand();
                stack.editMeta(meta -> meta.setCustomModelData(Integer.parseInt(args[0])));
            }
        }

    }

    @RegData("setting")
    public class setting extends CommandModule {

        @Override
        public void onRun(CommandSender sender, String[] args) {
            if (sender instanceof Player player) {
                new MenuSetting(player).open();
            } else {
                sender.sendMessage("command on player only");
            }
        }

    }

    @RegData("menu")
    public class menu extends CommandModule {

        @Override
        public void onRun(CommandSender sender, String[] args) {
            Player player;
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getLabel() + " <player>");
                    return;
                }
                player = (Player) sender;
            } else {
                player = Bukkit.getPlayerExact(args[0]);
            }
            if (player == null) {
                sender.sendMessage("player not online: " + args[0]);
                return;
            }
            new MenuSetSkin(player).open();
        }

    }

    @Override
    public void execute(@NotNull CommandSourceStack c, @NotNull String[] args) {
        onCmdRun(c.getSender(), args);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack cmd, @NotNull String[] args) {
        return onCmdTabs(cmd.getSender(), args);
    }
}
