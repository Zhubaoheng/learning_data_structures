package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import static java.lang.System.exit;

import java.io.Serializable;
import java.util.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Baoheng Zhu
 */
public class Repository implements Serializable {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    //创建 /.gitlet下的文件结构

    private static void setupRepoFile() {
        GITLET_DIR.mkdir();
        Blob.BLOBS.mkdir();
        Commit.COMMITS.mkdir();
        Branch.BRANCHES.mkdir();
    }

    /** 应该创建一个文件 保存所有的branch和HEAD
     /** 初始化仓库 创建一个.gitlet文件夹 如果已经存在 就打印错误语句
     *  创建一个initial commit 创建一个master指针指向这个commit
     * 创建时间戳 00:00:00 UTC, Thursday, 1 January 1970
     * 这个uid可以在不同仓库中分享 因为是在任何仓库中init是完全一样的
     */

    public void init() {
        if (GITLET_DIR.exists()) {
            throw error("Please enter a command.");
        }
        setupRepoFile();

        Commit initCommit = new Commit();
        initCommit.save();
        Branch.setBranches("master", initCommit.getHash());
        HEAD.setHead("master");
        StagingArea initStagingArea = new StagingArea();
        initStagingArea.save();
    }

    /** 把更改的文件复制去staging area
     * 如果这个文件已经在staging area中存在 就覆盖掉原来的文件
     * 在.gitlet中需要创建一个文件夹作为staging area
     * 如果在工作目录 中的文件与 current commit 相同 那就不要给它添加到staging area
     * 也就是说在add的时候要在HEAD和add区和remove区查询.contains
     * 如果staging area 已经存在这个文件 那么就给它删除
     * 如果这个文件是待删除，则把它拖出来
     *
     */
    public void add(String fileName) {
        String itemHash;
        File fileForAdd = join(CWD, fileName);
        if (!fileForAdd.exists()) {
            throw error("File does not exist.");
        }

        Blob newBlob = new Blob(readContents(fileForAdd));
        itemHash = newBlob.getHash();

        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        assert curCommit != null;
        HashMap<String, String> blobMap = curCommit.getBlobMap();
        StagingArea stage_area = StagingArea.load();
        if (blobMap.get(fileName) != null && blobMap.get(fileName).equals(itemHash)) {
            stage_area.save();
            return;
        }
        if (stage_area.getAddition() != null) {
            stage_area.getAddition().remove(fileName);
        }
        if (stage_area.getRemoval() != null) {
            stage_area.getRemoval().remove(fileName);
        }
        stage_area.getAddition().put(fileName, itemHash);
        stage_area.save();
        newBlob.save();
    }

    public void commit(String message) {
        StagingArea stage_area = StagingArea.load();
        if (stage_area.getRemoval().isEmpty() && stage_area.getAddition().isEmpty()) {
            throw error("No changes added to the commit.");
        }
        if (message == null) {
            throw error("Please enter a commit message.");
        }
        // if no merge
        Commit newCommit = new Commit(Branch.getBranches(HEAD.getHead()), null, message);
        // 检查Staged for addition 和 Staged for removal

        newCommit.getBlobMap().putAll(stage_area.getAddition());
        for (String key : stage_area.getRemoval()) {
            newCommit.getBlobMap().remove(key);
        }
        stage_area.clear();
        newCommit.save();
        stage_area.save();
        Branch.setBranches(HEAD.getHead(), newCommit.getHash());
        //TODO:有merge情况未考虑
    }


    /**如果暂存区中存在这个文件，把它从暂存区中移除*/
    public void rm(String fileName) {
        StagingArea stage_area = StagingArea.load();
        boolean changed = false;
        if (stage_area.getAddition().containsKey(fileName)) {
            stage_area.getAddition().remove(fileName);
            changed = true;
        }

        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        assert curCommit != null;
        if (curCommit.getBlobMap().containsKey(fileName)) {
            join(CWD, fileName).delete();
            stage_area.getRemoval().add(fileName);
        }
        stage_area.save();
        if (!changed) {
            throw error("No reason to remove the file.");
        }
    }

    public void log() {
        getLog(Branch.getBranches(HEAD.getHead()));
    }

    private void getLog(String headHash) {
        Commit curCommit = Commit.load(headHash);
        assert curCommit != null;
        if (curCommit.getParent() == null) {
            curCommit.print();
            exit(0);
        }
        curCommit.print();
        getLog(curCommit.getParent());
    }

    public void globalLog() {
        List<String> commitList = plainFilenamesIn(Commit.COMMITS);
        if (commitList == null) {
            return;
        }
        for (String hash : commitList) {
            Commit commit = Commit.load(hash);
            assert commit != null;
            commit.print();
        }
    }

    public void find(String message) {
        List<String> commitList = plainFilenamesIn(Commit.COMMITS);
        boolean changed = false;
        if (commitList == null) {
            return;
        }
        for (String hash : commitList) {
            Commit commit = readObject(join(Commit.COMMITS, hash), Commit.class);
            while (commit.getMessage().equals(message)) {
                System.out.println(hash);
                changed = true;
            }
        }
        if (!changed) {
            throw error("Found no commit with that message.");
        }
    }


    public void branch(String branchName) {
        File newBranch = join(Branch.BRANCHES, branchName);
        if (newBranch.exists()) {
            throw error("A branch with that name already exists.");
        }
        Branch.setBranches(branchName, Branch.getBranches(HEAD.getHead()));
    }

    public void checkout1(String fileName) {
        checkout2(Branch.getBranches(HEAD.getHead()), fileName);
    }

    public void checkout2(String commitId, String fileName) {
        File commit = join(Commit.COMMITS, commitId);
        if (commit.exists()) {
            Commit thisCommit = readObject(commit, Commit.class);
            HashMap<String, String> commitMap = thisCommit.getBlobMap();

            if (!commitMap.containsKey("wug.txt")) {
                throw error("File does not exist in that commit.");
            }
            String fileHash = commitMap.get(fileName);
            File file = join(Blob.BLOBS, fileHash);
            writeContents(join(CWD, fileName), (Object) readContents(file));
        } else {
            throw error("No commit with that id exists.");
        }
    }
    public void checkout3(String branchName) {
        List<String> branches = plainFilenamesIn(Branch.BRANCHES);
        if (!branches.contains(branchName)) {
            throw error("No such branch exists.");
        }
        if (branchName.equals(HEAD.getHead())) {
            throw error("No need to checkout the current branch. ");
        }

        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        Commit commit = Commit.load(Branch.getBranches(branchName));
        HashMap<String, String> commitMap = commit.getBlobMap();
        HashMap<String, String> curCommitMap = curCommit.getBlobMap();
        List<String> cwd = plainFilenamesIn(CWD);
        // 以前的commit包括一个文件，现在的commit不包括，且它还在工作目录中，输出一个异常
        //遍历以前commit的文件
        for (Map.Entry<String, String> entry : commitMap.entrySet()) {
            //现在commit不包括这个文件
            if (!curCommitMap.keySet().contains(entry.getKey())) {
                if (cwd.contains(entry.getKey())) { //如果它在工作目录中，抛出错误信息
                    error("There is an untracked file in the way;" +
                            " delete it, or add and commit it first.");
                }
            } else { //现在依然有这个文件
                //覆写现在的文件
                writeContents(join(CWD, entry.getKey()),
                        (Object) readContents(join(Blob.BLOBS,
                                entry.getValue())));
            }
        }
        //遍历现在commit的文件
        for (Map.Entry<String, String> entry : curCommitMap.entrySet()) {
            //如果在以前的commit中
            if (!commitMap.keySet().contains(entry.getKey())) {
                if (cwd.contains(entry.getKey())) { //如果它在工作目录中，删掉它
                    join(CWD, entry.getKey()).delete();
                }
            }
        }
        HEAD.setHead(branchName);
    }





    /* TODO: fill in the rest of this class. */
}
