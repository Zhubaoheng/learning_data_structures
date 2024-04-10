package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    public static final File BLOBS = join(Repository.GITLET_DIR, "blobs");
    private final byte[] content;
    private final String hash;
    public Blob(byte[] content) {
        this.content = content;
        hash = calcHash();
    }

    private String calcHash() {
        return sha1((Object) this.content);
    }

    public String getHash() {
        return hash;
    }

    public void save() {
        writeContents(join(BLOBS, hash), (Object) content);
    }

}
