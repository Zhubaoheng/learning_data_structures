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
    public static void main(String[] args) {
        if (args == null) {
            throw error("Please enter a command.");
        }

        // TODO: what if args is empty?
        String firstArg = args[0];
        Repository repo = new Repository();
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
                break;

            case "log":
                repo.log();
                break;

            case "global-log":
                repo.globalLog();
                break;

            case "find":
                repo.find(args[1]);
                break;

            case "rm-branch":
                repo.rmBranch(args[1]);
                break;

            case "branch":
                repo.branch(args[1]);
                break;

            case "status":
                repo.status();
                break;

            case "checkout":
                if (args.length == 2) {
                    repo.checkout3(args[1]);
                    break;
                }

                if (args.length == 3) {
                    repo.checkout1(args[2]);
                    break;
                }

                if (args.length == 4) {
                    repo.checkout2(args[1], args[3]);
                    break;
                }

            case "reset":
                repo.reset(args[1]);
                break;

            case "merge":
                repo.merge(args[1]);
                break;

        }
    }
}
