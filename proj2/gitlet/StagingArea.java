package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

public class StagingArea implements Serializable {
    public static final File STAGE_FILE = join(Repository.GITLET_DIR, "stageFile");
    private HashMap<String, String> addition;
    private HashSet<String> removal;
    public StagingArea() {
        addition = new HashMap<>();
        removal = new HashSet<>();
    }

    public HashMap<String, String> getAddition() {
        return addition;
    }

    public HashSet<String> getRemoval() {
        return removal;
    }

    public void save() {
        writeObject(STAGE_FILE, this);
    }

    public static StagingArea load() {
        return readObject(STAGE_FILE, StagingArea.class);
    }


    public  void clear() {
        addition.clear();
        removal.clear();
    }
}
