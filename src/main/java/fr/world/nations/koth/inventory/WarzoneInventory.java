package fr.world.nations.koth.inventory;

import fr.world.nations.koth.WonKoth;
import fr.world.nations.koth.fastinv.FastInv;
import fr.world.nations.koth.managers.KothManager;
import fr.world.nations.koth.models.KothModel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarzoneInventory extends FastInv {


    public WarzoneInventory() {
        super(9, ChatColor.RED + "Warzone");
        final KothManager kothManager = WonKoth.getInstance().getKothManager();


        for (KothModel koth : kothManager.getKothModelList()) {
            ItemStack it = new ItemStack(Material.PAPER);
            ItemMeta itemMeta = it.getItemMeta();
            itemMeta.setDisplayName(koth.getKothName());
            it.setItemMeta(itemMeta);
            addItem(it, inventoryClickEvent -> teleport(inventoryClickEvent, koth));
        }

    }

    private void teleport(InventoryClickEvent event, KothModel koth) {
        HumanEntity player = event.getWhoClicked();
        boolean hasArmor = false;
        ItemStack[] armorContent = player.getInventory().getArmorContents();
        for (ItemStack item : armorContent) {
            if (item == null) continue;
            if (item.getType() != Material.AIR) hasArmor = true;
        }
        if (hasArmor) {
            player.teleport(koth.getTeleportPoint());
        } else {
            for (String msg : WonKoth.getInstance().getConfig().getStringList("messages.players.tp-warzone-noarmor")) {
                player.sendMessage(msg);
            }
        }
    }
}
