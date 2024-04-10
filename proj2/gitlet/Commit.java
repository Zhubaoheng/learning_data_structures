package gitlet;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;

// TODO: any imports you need here

import java.util.Date; // TODO: You'll likely use this in this class
import java.util.List;
import java.util.Locale;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Baoheng Zhu
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     *
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
    }

    public Commit(String parent, String mergeParent, String message) {
        this.mergeParent = mergeParent;
        this.parent = parent;
        timeStamp = new Date();
        this.message = message;

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
        writeObject(join(COMMITS, hash), this);
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

    public static Commit load(String Uid) {
        File target = join(COMMITS, Uid);
        if (target.exists()) {
            return readObject(target, Commit.class);
        }else {
            return null;
        }
    }

    public void print() {
        System.out.println("===");
        System.out.println("commit" + " " + hash);
        if (mergeParent != null) {
            System.out.println("Merge:"
                    + " "
                    + parent.substring(0, 7)
                    + " "
                    + mergeParent.substring(0, 7));
        }
        System.out.println("Date:" + " " + dateFormat.format(timeStamp));
        System.out.println(message);
        System.out.println();
    }

}
