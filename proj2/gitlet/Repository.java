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
            exitWithMessage("A Gitlet version-control system "
                   + "already exists in the current directory.");
        }
        setupRepoFile();

        Commit initCommit = new Commit();
        initCommit.save();
        CommitBranchMap map = new CommitBranchMap();
        map.updateBranch(initCommit.getHash(), "master");
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
        checkGitletDir();
        String itemHash;
        File fileForAdd = join(CWD, fileName);
        if (!fileForAdd.exists()) {
            exitWithMessage("File does not exist.");
        }

        Blob newBlob = new Blob(readContents(fileForAdd));
        itemHash = newBlob.getHash();

        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        assert curCommit != null;
        HashMap<String, String> blobMap = curCommit.getBlobMap();
        StagingArea stageArea = StagingArea.load();
        if (!stageArea.getAddition().isEmpty()) {
            stageArea.getAddition().remove(fileName);
        }
        if (!stageArea.getRemoval().isEmpty()) {
            stageArea.getRemoval().remove(fileName);
        }

        if (blobMap.get(fileName) != null && blobMap.get(fileName).equals(itemHash)) {
            stageArea.save();
            return;
        }
        stageArea.getAddition().put(fileName, itemHash);
        stageArea.save();
        newBlob.save();
    }

    public void commit(String message) {
        checkGitletDir();
        StagingArea stageArea = StagingArea.load();
        CommitBranchMap map = CommitBranchMap.load();
        if (stageArea.getRemoval().isEmpty() && stageArea.getAddition().isEmpty()) {
            exitWithMessage("No changes added to the commit.");
        }
        if (message.isEmpty()) {
            exitWithMessage("Please enter a commit message.");
        }
        // if no merge
        Commit newCommit = new Commit(Branch.getBranches(HEAD.getHead()),
                null, message);
        // 检查Staged for addition 和 Staged for removal
        map.updateBranch(newCommit.getHash(), HEAD.getHead());
        newCommit.getBlobMap().putAll(stageArea.getAddition());
        for (String key : stageArea.getRemoval()) {
            newCommit.getBlobMap().remove(key);
        }
        stageArea.clear();
        newCommit.save();
        stageArea.save();
        Branch.setBranches(HEAD.getHead(), newCommit.getHash());
    }


    /**如果暂存区中存在这个文件，把它从暂存区中移除*/
    public void rm(String fileName) {
        checkGitletDir();
        StagingArea stageArea = StagingArea.load();
        boolean changed = false;
        if (stageArea.getAddition().containsKey(fileName)) {
            stageArea.getAddition().remove(fileName);
            changed = true;
        }

        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        assert curCommit != null;
        if (curCommit.getBlobMap().containsKey(fileName)) {
            join(CWD, fileName).delete();
            stageArea.getRemoval().add(fileName);
            changed = true;
        }
        stageArea.save();
        if (!changed) {
            exitWithMessage("No reason to remove the file.");
        }
    }

    public void log() {
        checkGitletDir();
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
        checkGitletDir();
        //CommitBranchMap map = CommitBranchMap.load();
        //System.out.println(map.getCBMap());
        List<String> commitList = Commit.loadCommitList();
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
        checkGitletDir();
        List<String> commitList = Commit.loadCommitList();
        boolean changed = false;
        if (commitList == null) {
            return;
        }
        for (String hash : commitList) {
            Commit commit = Commit.load(hash);
            if (commit.getMessage().equals(message)) {
                System.out.println(commit.getHash());
                changed = true;
            }
        }
        if (!changed) {
            exitWithMessage("Found no commit with that message.");
        }
    }


    public void branch(String branchName) {
        checkGitletDir();
        File newBranch = join(Branch.BRANCHES, branchName);
        if (newBranch.exists()) {
            exitWithMessage("A branch with that name already exists.");
        }
        Branch.setBranches(branchName, Branch.getBranches(HEAD.getHead()));
        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        CommitBranchMap map = CommitBranchMap.load();
        map.updateAll(branchName);
        curCommit.save();
    }

    public void rmBranch(String branchName) {
        checkGitletDir();
        if (HEAD.getHead().equals(branchName)) {
            exitWithMessage("Cannot remove the current branch.");
        }
        List<String> branches = plainFilenamesIn(Branch.BRANCHES);
        if (!branches.contains(branchName)) {
            exitWithMessage("A branch with that name does not exist. ");
        }
        join(Branch.BRANCHES, branchName).delete();
    }

    public void status() {
        checkGitletDir();
        StagingArea stageArea = StagingArea.load();
        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        List<String> cwd = plainFilenamesIn(CWD);
        System.out.println("===" + " " + "Branches" + " " + "===");
        printBranches();

        System.out.println("===" + " " + "Staged Files" + " " + "===");
        printStagedFiles(stageArea);

        System.out.println("===" + " " + "Removed Files" + " " + "===");
        printRemovedFiles(stageArea);

        System.out.println("===" + " " + "Modifications Not Staged For Commit" + " " + "===");

        printModifyNotStage(stageArea, curCommit, cwd);


        System.out.println("===" + " " + "Untracked Files" + " " + "===");
        printUntrackedFiles(findUntrackedFiles(stageArea, curCommit, cwd));
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

    private void printStagedFiles(StagingArea stageArea) {
        HashMap<String, String> addition = stageArea.getAddition();
        TreeSet<String> additionTree = new TreeSet<>(addition.keySet());
        for (String s : additionTree) {
            System.out.println(s);
        }
        System.out.println();
    }

    private void printRemovedFiles(StagingArea stageArea) {
        HashSet<String> removal = stageArea.getRemoval();
        TreeSet<String> removalTree = new TreeSet<>(removal);
        for (String s : removalTree) {
            System.out.println(s);
        }
        System.out.println();
    }

    private void printModifyNotStage(StagingArea stageArea, Commit curCommit, List<String> cwd) {
        HashMap<String, String> blobMap = curCommit.getBlobMap();
        TreeSet<String> modified = new TreeSet<>();
        TreeSet<String> deleted = new TreeSet<>();
        HashSet<String> removal = stageArea.getRemoval();
        HashMap<String, String> addition = stageArea.getAddition();
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

    private TreeSet<String> findUntrackedFiles(StagingArea stageArea,
                                               Commit curCommit, List<String> cwd) {
        HashMap<String, String> blobMap = curCommit.getBlobMap();
        HashSet<String> removal = stageArea.getRemoval();
        HashMap<String, String> addition = stageArea.getAddition();
        TreeSet<String> untrackedFiles = new TreeSet<>();
        if (cwd == null) {
            return untrackedFiles;
        }
        for (String fileName : cwd) {
            if (!removal.contains(fileName) && !addition.containsKey(fileName)
                    && !blobMap.containsKey(fileName)) {
                untrackedFiles.add(fileName);
            }
        }
        return untrackedFiles;
    }

    private void printUntrackedFiles(TreeSet<String> untrackedFiles) {
        for (String s : untrackedFiles) {
            System.out.println(s);
        }
        System.out.println();
    }

    public void checkout1(String fileName) {
        checkout2(Branch.getBranches(HEAD.getHead()), fileName);
    }

    public void checkout2(String commitId, String fileName) {
        checkGitletDir();
        Commit commit = Commit.load(commitId);
        if (commit != null) {
            HashMap<String, String> commitMap = commit.getBlobMap();
            if (!commitMap.containsKey(fileName)) {
                exitWithMessage("File does not exist in that commit.");
            }
            String fileHash = commitMap.get(fileName);
            File file = join(Blob.BLOBS, fileHash);
            writeContents(join(CWD, fileName), (Object) readContents(file));
        } else {
            exitWithMessage("No commit with that id exists.");
        }
    }
    public void checkout3(String branchName) {
        checkGitletDir();
        List<String> branches = plainFilenamesIn(Branch.BRANCHES);
        if (!branches.contains(branchName)) {
            exitWithMessage("No such branch exists.");
        }
        if (branchName.equals(HEAD.getHead())) {
            exitWithMessage("No need to checkout the current branch. ");
        }
        resetFile(Branch.getBranches(branchName));
        HEAD.setHead(branchName);
    }

    private void resetFile(String uid) {
        Commit curCommit = Commit.load(Branch.getBranches(HEAD.getHead()));
        Commit commit = Commit.load(uid);
        HashMap<String, String> commitMap = commit.getBlobMap();
        HashMap<String, String> curCommitMap = curCommit.getBlobMap();
        List<String> cwd = plainFilenamesIn(CWD);
        // 以前的commit包括一个文件，现在的commit不包括，且它还在工作目录中，输出一个异常
        //遍历以前commit的文件
        for (Map.Entry<String, String> entry : commitMap.entrySet()) {
            //现在commit不包括这个文件
            if (!curCommitMap.containsKey(entry.getKey())) {
                if (cwd.contains(entry.getKey())) { //如果它在工作目录中，抛出错误信息
                    exitWithMessage("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                }
                writeContents(join(CWD, entry.getKey()),
                        (Object) readContents(join(Blob.BLOBS,
                                entry.getValue())));
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

        //遍历cmd中的文件
        for (String file : cwd) {
            if (!curCommitMap.containsKey(file) && !commitMap.containsKey(file)) {
                join(CWD, file).delete();
            }
        }
    }

    public void reset(String uid) {
        checkGitletDir();
        Commit commit = Commit.load(uid);
        CommitBranchMap map = CommitBranchMap.load();
        if (commit == null) {
            exitWithMessage("No commit with that id exists.");
        }
        boolean flag = true;
        Set<String> curSet = map.getCBMap().get(Branch.getBranches(HEAD.getHead()));
        Set<String> uidSet = map.getCBMap().get(commit.getHash());
        for (String uidBranch : uidSet) {
            if (curSet.contains(uidBranch)) {
                flag = false;
            }
        }
        if (flag) {
            exitWithMessage("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
        StagingArea stageArea = StagingArea.load();
        stageArea.clear();
        stageArea.save();
        resetFile(uid);
        Branch.setBranches(HEAD.getHead(), uid);
    }
    private void checkMergeError(String branchName) {
        List<String> branches = plainFilenamesIn(Branch.BRANCHES);
        StagingArea stageArea = StagingArea.load();
        Commit curBranch = Commit.load(Branch.getBranches(HEAD.getHead()));
        List<String> cwd = plainFilenamesIn(CWD);
        TreeSet<String> untrackedFile = findUntrackedFiles(stageArea, curBranch, cwd);
        if (!branches.contains(branchName)) {
            exitWithMessage("A branch with that name does not exist. ");
        }
        if (HEAD.getHead().equals(branchName)) {
            exitWithMessage("Cannot merge a branch with itself.");
        }
        if (!stageArea.getAddition().isEmpty() || !stageArea.getRemoval().isEmpty()) {
            exitWithMessage("You have uncommitted changes.");
        }
        if (!untrackedFile.isEmpty()) {
            exitWithMessage("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
        }
        // find the split point

        Commit spPoint = findSplitPoint(HEAD.getHead(), branchName);
        Commit givenBranch = Commit.load(Branch.getBranches(branchName));
        CommitBranchMap map = CommitBranchMap.load();
        if (spPoint.getHash().equals(curBranch.getHash())) {
            checkout3(branchName);
            exitWithMessage("Current branch fast-forwarded.");
        }

        if (map.getCBMap().get(givenBranch.getHash()).
                containsAll(map.getCBMap().get(spPoint.getHash()))) {
            exitWithMessage("Given branch is an ancestor of the current branch.");
        }
    }

    public void merge(String branchName) {
        checkGitletDir();
        checkMergeError(branchName);
        Commit curBranch = Commit.load(Branch.getBranches(HEAD.getHead()));
        Set<String> fileNames = new TreeSet<>();
        // find the split point
        Commit spPoint = findSplitPoint(HEAD.getHead(), branchName);
        Commit givenBranch = Commit.load(Branch.getBranches(branchName));
/**
        if (branchName.equals("master")) {
            System.out.println(spPoint.getMessage());
            System.out.println(curBranch.getMessage());
            System.out.println(givenBranch.getMessage());
        }*/
        Commit mergeCommit = new Commit(Branch.getBranches(HEAD.getHead()),
                givenBranch.getHash(), "Merged " + branchName + " into "
                + HEAD.getHead() + ".");
        CommitBranchMap map = CommitBranchMap.load();
        map.updateBranch(mergeCommit.getHash(), branchName);
        for (String branch : map.getCBMap().get(curBranch.getHash())) {
            map.updateBranch(mergeCommit.getHash(), branch);
        }
        HashMap<String, String> spMap = spPoint.getBlobMap();
        HashMap<String, String> curMap = curBranch.getBlobMap();
        HashMap<String, String> givenMap = givenBranch.getBlobMap();
        HashMap<String, String> mergeMap = mergeCommit.getBlobMap();
        fileNames.addAll(spMap.keySet());
        fileNames.addAll(curMap.keySet());
        fileNames.addAll(givenMap.keySet());
        mergeCore(fileNames, spMap, curMap, givenMap, mergeMap, givenBranch);
        mergeCommit.save();
        Branch.setBranches(HEAD.getHead(), mergeCommit.getHash());
    }

    private void mergeCore(Set<String> fileNames, HashMap<String, String> spMap,
                           HashMap<String, String> curMap, HashMap<String, String> givenMap,
                           HashMap<String, String> mergeMap, Commit givenBranch) {
        for (String f : fileNames) {
            // case 6
            if (spMap.containsKey(f) && curMap.containsKey(f) && !givenMap.containsKey(f)) {
                // conflict 2b
                if (!spMap.get(f).equals(curMap.get(f))) {
                    handleConflict(curMap, givenMap, mergeMap, f);
                    continue;
                } else {
                    join(CWD, f).delete();
                    mergeMap.remove(f);
                    continue;
                }
            }
            // case 7
            if (spMap.containsKey(f) && !curMap.containsKey(f) && givenMap.containsKey(f)) {
                // conflict 2a
                if (!spMap.get(f).equals(givenMap.get(f))) {
                    handleConflict(curMap, givenMap, mergeMap, f);
                    continue;
                } else {
                    continue;
                }
            }
            // case 3b
            if (spMap.containsKey(f) && !curMap.containsKey(f) && !givenMap.containsKey(f)) {
                continue;
            }

            // case 4
            if (!spMap.containsKey(f) && curMap.containsKey(f) && !givenMap.containsKey(f)) {
                mergeMap.put(f, curMap.get(f));
                continue;
            }
            // case 5
            if (!spMap.containsKey(f) && !curMap.containsKey(f) && givenMap.containsKey(f)) {
                checkout2(givenBranch.getHash(), f);
                mergeMap.put(f, givenMap.get(f));
                continue;
            }
            // conflict 3
            if  (!spMap.containsKey(f) && curMap.containsKey(f) && givenMap.containsKey(f)) {
                if (!givenMap.get(f).equals(curMap.get(f))) {
                    handleConflict(curMap, givenMap, mergeMap, f);
                    continue;
                } else {
                    mergeMap.put(f, curMap.get(f));
                    continue;
                }
            }
            // case 1
            if (!spMap.get(f).equals(givenMap.get(f)) && spMap.get(f).equals(curMap.get(f))) {
                checkout2(givenBranch.getHash(), f);
                mergeMap.put(f, curMap.get(f));
                continue;
            }
            // case 2
            if (spMap.get(f).equals(givenMap.get(f)) && !spMap.get(f).equals(curMap.get(f))) {
                mergeMap.put(f, curMap.get(f));
                continue;
            }
            // case 3a
            if (!spMap.get(f).equals(givenMap.get(f)) && !spMap.get(f).equals(curMap.get(f))
                    && givenMap.get(f).equals(curMap.get(f))) {
                mergeMap.put(f, curMap.get(f));
                continue;
            }
            // conflict 1
            if (!spMap.get(f).equals(givenMap.get(f)) && !spMap.get(f).equals(curMap.get(f))
                    && !givenMap.get(f).equals(curMap.get(f))) {
                handleConflict(curMap, givenMap, mergeMap, f);
                continue;
            }
        }
    }


    private Commit findSplitPoint(String curBranch, String givenBranch) {
        CommitBranchMap map = CommitBranchMap.load();
        HashMap<String, Set<String>> cbMap = map.getCBMap();
        Commit curCommit = Commit.load(Branch.getBranches(curBranch));
        Commit givenCommit = Commit.load(Branch.getBranches(givenBranch));
        while (givenCommit.getParent() != null) {
            givenCommit = Commit.load(givenCommit.getParent());
            Set<String> givenSet = cbMap.get(givenCommit.getHash());
            Set<String> curSet = cbMap.get(curCommit.getHash());
            for (String branch : curSet) {
                if (givenSet.contains(branch)) {
                    return givenCommit;
                }
            }

        }
        if (givenCommit.getParent() == null) {
            givenCommit = Commit.load(givenCommit.getParent());
            Set<String> givenSet = cbMap.get(givenCommit.getHash());
            Set<String> curSet = cbMap.get(curCommit.getHash());
            for (String branch : curSet) {
                if (givenSet.contains(branch)) {
                    return givenCommit;
                }
            }
        }
        return null;
    }

    private void handleConflict(HashMap<String, String> curMap, HashMap<String, String> givenMap,
                                HashMap<String, String> mergeMap, String f) {
        System.out.println("Encountered a merge conflict.");
        String curFile;
        String givenFile;
        if (curMap.get(f) != null && join(Blob.BLOBS, curMap.get(f)).exists()) {
            curFile = readContentsAsString(join(Blob.BLOBS, curMap.get(f)));
        } else {
            curFile = "";
        }
        if (givenMap.get(f) != null && join(Blob.BLOBS, givenMap.get(f)).exists()) {
            givenFile = readContentsAsString(join(Blob.BLOBS, givenMap.get(f)));
        } else {
            givenFile = "";
        }
        String content = "<<<<<<< HEAD\n" + curFile + "=======\n" + givenFile +  ">>>>>>>\n";
        writeContents(join(CWD, f), content);
        Blob newBlob = new Blob(readContents(join(CWD, f)));
        mergeMap.put(f, newBlob.getHash());
        newBlob.save();
    }

    private void exitWithMessage(String message) {
        System.out.println(message);
        exit(0);
    }

    private void checkGitletDir() {
        if (!GITLET_DIR.exists()) {
            exitWithMessage("Not in an initialized Gitlet directory.");
        }
    }

    public static void checkOperands(String[] args,
                                     int expectedNum) {
        if (args.length - 1 > expectedNum) {
            System.out.println("Not in an initialized Gitlet directory.");
            exit(0);
        }

        for (String arg : args) {
            if (!(arg instanceof String)) {
                System.out.println("Not in an initialized Gitlet directory.");
                exit(0);
            }
        }
    }
}
