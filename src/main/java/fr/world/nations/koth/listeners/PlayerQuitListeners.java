package fr.world.nations.koth.listeners;

/*
 *  * @Created on 12/08/2022
 *  * @Project KOTH-WON
 *  * @Author Jimmy  / SKAH#7513
 */

import com.sk89q.worldedit.math.Vector3;
import fr.world.nations.koth.WonKoth;
import fr.world.nations.koth.models.KothModel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListeners implements Listener {


    @EventHandler
    private void onPlayerDisconnect(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Location playerLocation = player.getLocation();

        for (KothModel kothModel : WonKoth.getInstance().getKothManager().getKothModelList()) {
            if (kothModel.getKothCuboid().contains(Vector3.toBlockPoint(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()))) {
                player.damage(player.getHealth() + 20);
            }
        }

    }
}
