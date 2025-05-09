package fr.world.nations.country.listener;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.shade.net.kyori.adventure.text.Component;
import com.massivecraft.factions.shade.net.kyori.adventure.text.format.NamedTextColor;
import fr.world.nations.country.Country;
import fr.world.nations.country.CountryManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionListener implements Listener {
    private final CountryManager countryManager;

    public FactionListener(CountryManager countryManager) {
        this.countryManager = countryManager;
    }

    @EventHandler
    public void onFactionCreate(FactionCreateEvent event) {
        if (event.getFPlayer().isAdminBypassing()) return;
        Country country = countryManager.getCountry(event.getFactionTag());
        if (country == null) {
            event.getFPlayer().sendComponent(Component.text("Ce pays n'existe pas").color(NamedTextColor.RED));
            event.setCancelled(true);
        } else if (!country.isAvailable()) {
            event.getFPlayer().sendComponent(Component.text("Ce pays n'est pas disponible").color(NamedTextColor.RED));
            event.setCancelled(true);
        } else if (country.getSpawn() == null) {
            event.getFPlayer().sendComponent(Component.text("Veuillez d'abord préciser le home de ce pays " +
                    "avec la commande \"/f country pos " + country.getName()
                    + " quand vous vous trouvez sur le bloc choisi !\"").color(NamedTextColor.RED));
            event.setCancelled(true);
        } else {
            country.setAvailable(false);
        }
    }

    @EventHandler
    public void onFactionJoin(FPlayerJoinEvent event) {
        if (event.getReason().equals(FPlayerJoinEvent.PlayerJoinReason.CREATE)) {
            Country country = countryManager.getCountry(event.getFaction().getTag());
            if (country != null) {
                Board.getInstance().setFactionAt(event.getFaction(), FLocation.wrap(country.getSpawn()));
                event.getFaction().setHome(country.getSpawn());
            }
        }
    }

    @EventHandler
    public void onFactionDisband(FactionDisbandEvent event) {
        Country country = countryManager.getCountry(event.getFaction().getTag());
        if (country != null) {
            country.setAvailable(true);
        }
    }
}
