package fr.world.nations.assault.explosion;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AssaultDebugCmd extends FCommand {

    public AssaultDebugCmd() {
        aliases.add("debug");
        requiredArgs.add("x");
        requiredArgs.add("y");
        requiredArgs.add("z");
    }

    @Override
    public void perform(CommandContext commandContext) {
        double x = commandContext.argAsDouble(0);
        double y = commandContext.argAsDouble(1);
        double z = commandContext.argAsDouble(2);

        Location targetLocation = new Location(commandContext.player.getWorld(), x, y, z);

        commandContext.msg(getDirectionArrow(commandContext.player, targetLocation));
        commandContext.msg(getDirectionArrow2(commandContext.player, targetLocation));
    }

    private String getDirectionArrow(Player player, Location targetLocation) {
        if (targetLocation == null || player == null) return "?";

        Location targetLoc = targetLocation.clone();
        Location pLoc = player.getLocation().clone();

        Vector pDir = new Vector(pLoc.getDirection().getX(), 0, pLoc.getDirection().getZ()).normalize();
        Vector toDir = targetLoc.clone().subtract(pLoc).toVector().normalize();
        toDir = new Vector(toDir.getX(), 0, toDir.getZ()).normalize();

        double angle = Math.acos(pDir.dot(toDir));

        Vector crossProduct = pDir.crossProduct(toDir);
        angle *= (crossProduct.getY() / (Math.abs(crossProduct.getY())));

        double yaw = Math.toDegrees(angle);

        System.out.println(yaw);

        if (-45 < yaw && yaw <= 45) return "↑";
        if (-135 < yaw && yaw <= -45) return "→";
        if (45 < yaw && yaw <= 135) return "←";
        return "↓";
    }

    private String getDirectionArrow2(Player player, Location targetLocation) {
        if (targetLocation == null || player == null) return "?";

        Location playerLocation = player.getLocation();

        // Calculate the relative angles
        double playerAngle = Math.toRadians(playerLocation.getYaw());
        double targetAngle = Math.atan2(targetLocation.getZ() - playerLocation.getZ(),
                targetLocation.getX() - playerLocation.getX());

        // Normalize the angles
        playerAngle = (playerAngle + 2 * Math.PI) % (2 * Math.PI);
        targetAngle = (targetAngle + 2 * Math.PI) % (2 * Math.PI);

        // Calculate the smallest difference between angles
        double angleDifference = targetAngle - playerAngle;
        if (angleDifference >= Math.PI) {
            angleDifference -= 2 * Math.PI;
        } else if (angleDifference < -Math.PI) {
            angleDifference += 2 * Math.PI;
        }

        System.out.println(Math.toDegrees(angleDifference));

        // Determine the direction based on the angle difference
        if (-Math.PI / 4 <= angleDifference && angleDifference < Math.PI / 4) {
            return "↑";
        } else if (Math.PI / 4 <= angleDifference && angleDifference < 3 * Math.PI / 4) {
            return "→";
        } else if (-3 * Math.PI / 4 <= angleDifference && angleDifference < -Math.PI / 4) {
            return "←";
        } else {
            return "↓";
        }
    }


    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
