package fr.world.nations.koth.handlers;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import fr.world.nations.koth.WonKoth;
import fr.world.nations.koth.models.KothModel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KothHandler {

    public static void createKothFromRegion(String name, Player player) {

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        try {
            RegionSelector selector;

            final LocalSession session = worldEdit.getWorldEdit().getSessionManager().findByName(player.getName());

            if (session != null && session.getSelectionWorld() != null && (selector = session.getRegionSelector(session.getSelectionWorld())).isDefined()) {

                if (!(selector.getRegion() instanceof CuboidRegion)) {
                    player.sendMessage("§cla région doit être un cuboid");
                    return;
                }

                final KothModel kothModel = new KothModel();
                kothModel.setKothName(name);
                kothModel.setKothCuboid(((CuboidRegion) selector.getRegion()).clone());

                WonKoth plugin = WonKoth.getInstance();
                if (plugin.getKothManager().getKothFromCache(kothModel.getKothName()) != null) {
                    player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.koth-already-exists").replace("%area_name%", name));
                    return;
                }
                plugin.getKothManager().saveKoth(kothModel, false);
                plugin.getKothManager().addKothCache(kothModel);
                kothModel.start();
                player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.koth-created").replace("%area_name%", name));
            }

        } catch (final IncompleteRegionException e) {
            player.sendMessage("§cLa région n'est pas complète");
        }
    }

    public static void deleteKothFromName(String kothName, CommandSender sender) {
        WonKoth plugin = WonKoth.getInstance();
        KothModel koth = new KothModel();
        koth.setKothName(kothName);
        plugin.getKothManager().removeKothFile(kothName);
        sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.koth-delete").replace("%area_name%", kothName));
    }

}
