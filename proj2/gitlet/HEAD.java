package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class HEAD implements Serializable {
    public static final File HEAD = join(Repository.GITLET_DIR, "HEAD");
    public static void setHead(String branchName) {
        writeContents(HEAD, branchName);
    }

    public static String getHead() {
        return readContentsAsString(HEAD);
    }
}
