package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

public class CommitBranchMap implements Serializable {
    public static final File MAP = join(Repository.GITLET_DIR, "commitBranchMap");
    private HashMap<String, Set<String>> map;
    public CommitBranchMap() {
        map = new HashMap<>();
    }

    public HashMap<String, Set<String>> getCBMap() {
        return map;
    }

    public void updateAll(String branch) {
        Commit commit = Commit.load(Branch.getBranches(HEAD.getHead()));
        while (commit.getParent() != null) {
            updateBranch(commit.getHash(), branch);
            commit = Commit.load(commit.getParent());
        }
        if (commit.getParent() == null) {
            updateBranch(commit.getHash(), branch);
        }
    }

    public void updateBranch(String commitHash, String branch) {
        Set<String> branchList = map.get(commitHash);
        if (branchList == null) {
            branchList = new TreeSet<>();
            branchList.add(branch);
        } else {
            branchList.add(branch);
        }
        map.put(commitHash, branchList);
        writeObject(MAP, this);
    }

    public static CommitBranchMap load() {
        return readObject(MAP, CommitBranchMap.class);
    }
}
