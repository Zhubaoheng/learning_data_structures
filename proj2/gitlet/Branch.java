package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Branch implements Serializable {
    public static final File BRANCHES = join(Repository.GITLET_DIR, "branches");

    public static void setBranches(String branchName, String branchHash) {
        writeContents(join(BRANCHES, branchName), branchHash);
    }

    public static String getBranches(String branchName) {
        File branchFile = join(BRANCHES, branchName);
        if (branchFile.exists()) {
            return readContentsAsString(branchFile);
        } else {
            return null;
        }
    }

}
