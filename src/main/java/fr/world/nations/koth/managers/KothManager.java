package fr.world.nations.koth.managers;

/*
 *  * @Created on 09/05/2022
 *  * @Project AresiaKoth
 *  * @Author Jimmy  / SKAH#7513
 */

import com.google.common.collect.Lists;
import fr.world.nations.koth.WonKoth;
import fr.world.nations.koth.json.IDataSerialisable;
import fr.world.nations.koth.models.KothModel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
public class KothManager extends IDataSerialisable<KothModel> {
    private final List<KothModel> kothModelList = Lists.newArrayList();
    private final File kothsFolder;

    public KothManager(WonKoth plugin) {
        this.kothsFolder = new File(plugin.getConfigFolder(), "/koths/");
        if (!kothsFolder.exists()) kothsFolder.mkdirs();
    }

    public void loadKoths() {
        if (kothsFolder == null) return;
        File[] files = kothsFolder.listFiles(filter -> filter.getName().endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                loadKothFromFile(file.getName().replace(".json", ""));
            }
        }
    }

    //JSON FILE LOAD
    public boolean loadKothFromFile(String kothName) {
        if (kothsFolder == null) return false;
        final File kothFile = new File(kothsFolder, kothName + ".json");
        if (kothFile.exists()) {
            KothModel kothModel = load(kothFile, KothModel.class);
            kothModel.start();
            addKothCache(kothModel);
            return true;
        }
        return false;
    }

    //JSON FILE SAVE
    public void saveKoth(KothModel kothModel, boolean removeFromCache) {
        super.save(new File(kothsFolder, kothModel.getKothName() + ".json"), kothModel);
        if (removeFromCache) removeKothCache(kothModel);
    }

    public void removeKothFile(String kothName) {
        final File kothFile = new File(kothsFolder, kothName + ".json");
        if (kothFile.exists()) kothFile.delete();
    }

    //CACHE KOTH MODEL
    public void addKothCache(KothModel kothModel) {
        if (kothModel == null) return;
        this.kothModelList.add(kothModel);
    }

    //UNCACHE KOTH MODEL
    public boolean removeKothCache(KothModel kothModel) {
        if (kothModel == null) return false;
        return kothModelList.remove(kothModel);
    }

    //GET KOTH MODEL FROM CACHE
    public KothModel getKothFromCache(String kothName) {

        KothModel kothModel = new KothModel();
        kothModel.setKothName(kothName);

        return kothModelList.contains(kothModel) ? kothModelList.get(kothModelList.indexOf(kothModel)) : null;
    }

    public List<String> getKothNames() {
        File[] files = kothsFolder.listFiles();
        List<String> kothNames = Lists.newArrayList();

        if (files == null) return Lists.newArrayList("No koths found");
        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                kothNames.add(file.getName().replace(".json", ""));
            }
        }

        return kothNames;
    }


    public List<KothModel> getKothModelList() {
        return kothModelList;
    }
}
