package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        int M = 10000;
        printTimingTable(getNsList(), getTimesList(getNsList(), M), getOpsList(getNsList(), M));
    }

    private static AList<Integer> getNsList() {
        AList<Integer> Ns = new AList<>();
        Ns.addLast(1000);
        Ns.addLast(2000);
        Ns.addLast(4000);
        Ns.addLast(8000);
        Ns.addLast(16000);
        Ns.addLast(32000);
        Ns.addLast(64000);
        Ns.addLast(128000);
        return Ns;
    }

    private static AList<Double> getTimesList(AList<Integer> Ns, int M) {
        AList<Double> times = new AList<>();
        SLList<Integer> target = new SLList<>();

        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            for (int j = 0; j < N; j++) {
                target.addLast(1);
            }

            Stopwatch sw = new Stopwatch();
            for (int k = 0; k < M; k++){
                target.getLast();
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
        }

        return times;
    }


        private static AList<Integer> getOpsList(AList<Integer> Ns, int M) {
            AList<Integer> Ops = new AList<>();

            for (int i = 0; i < Ns.size(); i += 1) {
                Ops.addLast(M);
            }
            return Ops;
        }


}
