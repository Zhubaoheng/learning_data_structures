package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Baoheng Zhu
 */


public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File REPO = join(CWD, ".gitlet", "REPO");

    public static void main(String[] args) {
        if (args == null) {
            throw error("Please enter a command.");
        }

        // TODO: what if args is empty?
        String firstArg = args[0];
        Repository repo;

        if (!REPO.exists()) {
            repo = new Repository();
        }else {
            repo = readObject(REPO, Repository.class);
        }
        switch(firstArg) {
            case "init":
                repo.init();
                break;

            case "add":
                repo.add(args[1]);
                break;

            case "commit":
                repo.commit(args[1]);
                break;

            case "rm":
                repo.rm(args[1]);

            case "log":
                repo.log();

            case "global-log":
                repo.globalLog();

            case "find":
                repo.find(args[1]);

            case "branch":
                repo.branch(args[1]);

            case "checkout":
                if (args.length == 2) {
                    repo.checkout3(args[1]);
                }

                if (args.length == 3) {
                    repo.checkout1(args[2]);
                }

                if (args.length == 4) {
                    repo.checkout2(args[1], args[3]);
                }

        }
        writeObject(REPO, repo);
    }
}
