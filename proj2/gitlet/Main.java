package gitlet;

import java.io.File;

import static gitlet.Utils.*;
import static java.lang.System.exit;

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
            System.out.println("Please enter a command.");
            exit(0);
        }

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            exit(0);
        }

        String firstArg = args[0];
        Repository repo = new Repository();
        switch(firstArg) {
            case "init":
                repo.init();
                exit(0);

            case "add":
                Repository.checkOperands(args, 1);
                repo.add(args[1]);
                exit(0);

            case "commit":
                Repository.checkOperands(args, 1);
                repo.commit(args[1]);
                exit(0);

            case "rm":
                Repository.checkOperands(args, 1);
                repo.rm(args[1]);
                exit(0);

            case "log":
                repo.log();
                exit(0);

            case "global-log":
                repo.globalLog();
                exit(0);

            case "find":
                Repository.checkOperands(args, 1);
                repo.find(args[1]);
                exit(0);

            case "rm-branch":
                Repository.checkOperands(args, 1);
                repo.rmBranch(args[1]);
                exit(0);

            case "branch":
                Repository.checkOperands(args, 1);
                repo.branch(args[1]);
                exit(0);

            case "status":
                repo.status();
                exit(0);

            case "checkout":
                Repository.checkOperands(args, 3);
                if (args.length == 2) {
                    repo.checkout3(args[1]);
                    exit(0);
                }

                if (args.length == 3) {
                    repo.checkout1(args[2]);
                    exit(0);
                }

                if (args.length == 4) {
                    repo.checkout2(args[1], args[3]);
                    exit(0);
                }

            case "reset":
                Repository.checkOperands(args, 1);
                repo.reset(args[1]);
                exit(0);

            case "merge":
                Repository.checkOperands(args, 1);
                repo.merge(args[1]);
                exit(0);
        }
        System.out.println("No command with that name exists.");
        exit(0);
    }
}
