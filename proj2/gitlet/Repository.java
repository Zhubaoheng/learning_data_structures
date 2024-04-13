package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import static java.lang.System.exit;

import java.io.Serializable;
import java.util.*;

/** Represents a gitlet repository.
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
            changed = true;
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
            if (commit.getMessage().equals(message)) {
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

    public void rmBranch(String branchName) {
        if (HEAD.getHead().equals(branchName)) {
            throw error("Cannot remove the current branch.");
        }
        List<String> branches = plainFilenamesIn(Branch.BRANCHES);
        if (!branches.contains(branchName)) {
            throw error("A branch with that name does not exist. ");
        }
        restrictedDelete(join(Branch.BRANCHES, branchName));
    }

    public void status() {
        StagingArea stage_area = StagingArea.load();
        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        List<String> cwd = plainFilenamesIn(CWD);
        System.out.println("===" + " " + "Branches" + " " + "===");
        printBranches();

        System.out.println("===" + " " + "Staged Files" + " " + "===");
        printStagedFiles(stage_area);

        System.out.println("===" + " " + "Removed Files" + " " + "===");
        printRemovedFiles(stage_area);

        System.out.println("===" + " " + "Modifications Not Staged For Commit" + " " + "===");

        printModifyNotStage(stage_area, curCommit, cwd);


        System.out.println("===" + " " + "Untracked Files" + " " + "===");
        printUntrackedFiles(stage_area, curCommit, cwd);
    }
    private void printBranches() {
        List<String> branches = plainFilenamesIn(Branch.BRANCHES);
        System.out.println("*" + HEAD.getHead());
        Collections.sort(branches);
        for (String branch : branches) {
            if (branch.equals(HEAD.getHead())) {
                continue;
            }
            System.out.println(branch);
        }
        System.out.println();
    }

    private void printStagedFiles(StagingArea stage_area) {
        HashMap<String, String> addition = stage_area.getAddition();
        TreeSet<String> additionTree = new TreeSet<>(addition.keySet());
        for (String s : additionTree) {
            System.out.println(s);
        }
        System.out.println();
    }

    private void printRemovedFiles(StagingArea stage_area) {
        HashSet<String> removal = stage_area.getRemoval();
        TreeSet<String> removalTree = new TreeSet<>(removal);
        for (String s : removalTree) {
            System.out.println(s);
        }
        System.out.println();
    }

    private void printModifyNotStage(StagingArea stage_area, Commit curCommit, List<String> cwd) {
        HashMap<String, String> blobMap = curCommit.getBlobMap();
        TreeSet<String> modified = new TreeSet<>();
        TreeSet<String> deleted = new TreeSet<>();
        HashSet<String> removal = stage_area.getRemoval();
        HashMap<String, String> addition = stage_area.getAddition();

        for (String fileName : addition.keySet()) {
            if (!cwd.contains(fileName)) {
                deleted.add(fileName);
            }
        }

        for (String fileName : blobMap.keySet()) {
            if (!removal.contains(fileName) && !cwd.contains(fileName)) {
                deleted.add(fileName);
            }
        }

        for (String s : deleted) {
            System.out.println(s + " " + "(deleted)");
        }

        if (cwd == null) {
            return;
        }

        for (String fileName : cwd) {
            String cwdFileHash = sha1((Object) readContents(join(CWD, fileName)));
            String curCommitHash = blobMap.get(fileName);
            String additionHash = addition.get(fileName);
            if (curCommitHash != null) {
                //commit和cwd不一样
                if (!curCommitHash.equals(cwdFileHash)) {
                    modified.add(fileName);
                }
            }

            if (additionHash != null) {
                //addition和cwd不一样
                if (!additionHash.equals(cwdFileHash)) {
                    modified.add(fileName);
                }
            }
        }

        for (String s : modified) {
            System.out.println(s + " " +  "(modified)");
        }
        System.out.println();
    }

    private void printUntrackedFiles(StagingArea stage_area, Commit curCommit, List<String> cwd) {
        if (cwd == null) {
            return;
        }
        HashMap<String, String> blobMap = curCommit.getBlobMap();
        HashSet<String> removal = stage_area.getRemoval();
        HashMap<String, String> addition = stage_area.getAddition();
        TreeSet<String> untrackedFiles = new TreeSet<>();

        for (String fileName : cwd) {
            if (!removal.contains(fileName) && !addition.containsKey(fileName)
                    && !blobMap.containsKey(fileName)) {
                untrackedFiles.add(fileName);
            }
        }

        for (String s : untrackedFiles) {
            System.out.println(s);
        }
        System.out.println();
    }

    public void checkout1(String fileName) {
        checkout2(Branch.getBranches(HEAD.getHead()), fileName);
    }

    public void checkout2(String commitId, String fileName) {
        //TODO:缩写要能处理
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
        resetFile(Branch.getBranches(branchName));
        HEAD.setHead(branchName);
    }

    private void resetFile(String Uid) {
        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        Commit commit = Commit.load(Uid);
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
    }

    public void reset(String Uid) {
        Commit commit = Commit.load(Uid);
        if (commit == null) {
            throw error("No commit with that id exists.");
        }
        if (!inCurBranch(Uid)) {
            throw error("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
        }
        StagingArea stage_area = StagingArea.load();
        stage_area.clear();
        stage_area.save();
        resetFile(Uid);
        Branch.setBranches(HEAD.getHead(), Uid);
    }

    private boolean inCurBranch(String Uid) {
        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        while (curCommit.getParent() != null) {
            if (curCommit.getHash().equals(Uid)) {
                return true;
            } else {
                curCommit = Commit.load(curCommit.getParent());
            }
        }
        return false;
    }

    public void merge(String branchName) {}

}
