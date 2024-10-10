package org.nxdus.realms.paper.RealmMenu;

import org.bukkit.event.Listener;
import org.nxdus.realms.paper.MainPaper;

public class RealmBannedMenu implements Listener {

    public RealmBannedMenu() {
        MainPaper.plugin.getServer().getPluginManager().registerEvents(this, MainPaper.plugin);

    }

}
