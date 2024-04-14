package gitlet;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Baoheng Zhu
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     * 一个commit中要包括
     * 文件名到blob的映射 父commit的uid  对于合并来说 还应该保存另一个父commit的uid
     * metadata: timestamp  log message
     */

    /** The message of this Commit. */

    public static final File COMMITS = join(Repository.GITLET_DIR, "commits");
    private final String message;
    private final String parent;
    private final String mergeParent;
    private final Date timeStamp;
    private final String branch;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);

    private HashMap<String, String> blobMap;
    private String hash;
    public Commit() {
        this.mergeParent = null;
        this.parent = null;
        this.timeStamp = new Date(0);
        blobMap = new HashMap<>();
        hash = calcHash();
        message = "initial commit";
        branch = "master";
    }

    public Commit(String parent, String mergeParent, String message, String branch) {
        this.mergeParent = mergeParent;
        this.parent = parent;
        timeStamp = new Date();
        this.message = message;
        this.branch = branch;
        Commit parentCommit = load(parent);
        blobMap = new HashMap<>();
        assert parentCommit != null;
        blobMap = parentCommit.blobMap;
        hash = calcHash();
    }

    private String calcHash() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return Utils.sha1((Object) bos.toByteArray());
    }

    public String getHash() {
        return hash;
    }

    public void save() {
        File folder = join(COMMITS, hash.substring(0, 8));
        folder.mkdir();
        writeObject(join(folder, hash), this);
    }

    public HashMap<String, String> getBlobMap() {
        return blobMap;
    }

    public String getMessage() {
        return message;
    }

    public String getParent() {
        return parent;
    }

    public String getMergeParent() {
        return mergeParent;
    }
    public String getBranch() {
        return branch;
    }
    public static Commit load(String uid) {
        File folder = join(COMMITS, uid.substring(0, 8));
        if (folder.exists()) {
            File target = folder.listFiles()[0];
            return readObject(target, Commit.class);
        } else {
            return null;
        }
    }

    public void print() {
        System.out.println("===");
        System.out.println("commit" + " " + hash);
        if (mergeParent != null) {
            System.out.println("Merge:" + " " + parent.substring(0, 7)
                    + " " + mergeParent.substring(0, 7));
        }
        System.out.println("Date:" + " " + dateFormat.format(timeStamp));
        System.out.println(message);
        System.out.println();
    }

    public static List<String> loadCommitList() {
        List<String> commitList = new LinkedList<>();
        File[] commitFolder = COMMITS.listFiles();
        if (commitFolder == null) {
            return null;
        }
        for (File folder : commitFolder) {
            commitList.addAll(plainFilenamesIn(folder));
        }
        return commitList;
    }


}
