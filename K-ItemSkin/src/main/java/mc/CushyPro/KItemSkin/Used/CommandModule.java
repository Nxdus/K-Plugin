package mc.CushyPro.KItemSkin.Used;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandModule {

    protected String label = "";

    public String getLabel() {
        return label;
    }

    public CommandModule() {
        registerCommand();
    }

    public void setLabel(String args) {
        this.label = args;
    }

    private final Map<String, CommandModule> maps = new HashMap<>();

    public void onRun(CommandSender sender, String[] args) {
        if (args.length == 0) {
            DefaultCommand(sender);
        }
    }

    public void DefaultCommand(CommandSender sender) {
        if (maps.isEmpty()) {
            return;
        }
        for (String s : maps.keySet()) {
            sender.sendMessage(label + " " + s);
        }
    }

    public void onCmdRun(CommandSender sender, String[] args) {
        onRun(sender, args);
        if (args.length == 0) {
            return;
        }
        if (maps.isEmpty()) {
            return;
        }
        String de = args[0];
        runSubCom(de, sender, removeargs(args));
    }

    public void runSubCom(String cmd, CommandSender sender, String[] args) {
        if (maps.containsKey(cmd)) {
            maps.get(cmd).onCmdRun(sender, args);
        } else {
            DefaultCommand(sender);
        }
    }

    public List<String> onCmdTabs(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        String cmd;
        if (args.length == 0) {
            cmd = "";
        } else {
            cmd = args[0];
        }
        if (maps.isEmpty()) {
            for (Player ps : Bukkit.getOnlinePlayers()) {
                tab.add(ps.getName());
            }
        } else {
            tab.addAll(maps.keySet());
        }
        if (args.length >= 1) {
            if (maps.containsKey(cmd)) {
                return maps.get(cmd).onCmdTabs(sender, removeargs(args));
            }
        }
        tab.removeIf(key -> !key.toUpperCase().startsWith(cmd.toUpperCase()));
        return tab;
    }

    private String[] removeargs(String[] args) {
        List<String> list = new ArrayList<>(List.of(args));
        list.removeFirst();
        return list.toArray(new String[]{});
    }

    public void registerCommand() {
        try {
            for (Class<?> cls : getClass().getClasses()) {
                RegData cmd = cls.getAnnotation(RegData.class);
                if (cmd != null) {
                    Object obj;
                    if (Modifier.isStatic(cls.getModifiers())) {
                        obj = cls.getDeclaredConstructor().newInstance();
                    } else {
                        obj = cls.getDeclaredConstructor(getClass()).newInstance(this);
                    }
                    String command = cmd.value();
                    if (command.isEmpty()) {
                        command = obj.getClass().getName();
                    }
                    if (obj instanceof CommandModule module) {
                        module.setLabel(getLabel() + " " + command);
                        register(command, module);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void register(String cmd, CommandModule obj) {
        maps.put(cmd, obj);
    }

}
