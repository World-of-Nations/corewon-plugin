package fr.world.nations.koth.tasks;

/*
 *  * @Created on 19/08/2022
 *  * @Project KOTH-WON
 *  * @Author Jimmy  / SKAH#7513
 */

import fr.world.nations.koth.managers.PowerManager;

public class UpdatePowerTask implements Runnable {

    @Override
    public void run() {
        PowerManager.getInstance().updatePower();
        PowerManager.getInstance().savePower();
    }
}
