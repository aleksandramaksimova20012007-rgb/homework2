import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        long[] ns = {1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L};
        int[] ms = {1, 2, 4, 8, 16, 32, 64, 128};

        String outputFile = "pi_monte_carlo_parallel_results.csv";
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));

        // Заголовок CSV (M по горизонталі)
        writer.print("N/M");
        for (int m : ms) writer.print("," + m);
        writer.println();

        for (long n : ns) {
            writer.print(n);
            for (int m : ms) {
                System.out.println("Running N=" + n + ", M=" + m);
                double time = runExperiment(n, m);
                writer.print("," + String.format("%.3f", time));
                writer.flush();
            }
            writer.println();
        }
        writer.close();
        System.out.println("Done! Results saved to " + outputFile);
    }

    private static double runExperiment(long n, int m) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(m);
        long pointsPerThread = n / m;
        long startTime = System.nanoTime();

        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            futures.add(executor.submit(() -> {
                long count = 0;
                ThreadLocalRandom random = ThreadLocalRandom.current();
                for (long j = 0; j < pointsPerThread; j++) {
                    double x = random.nextDouble();
                    double y = random.nextDouble();
                    if (x * x + y * y <= 1) count++;
                }
                return count;
            }));
        }

        long totalInCircle = 0;
        for (Future<Long> f : futures) totalInCircle += f.get();

        long endTime = System.nanoTime();
        executor.shutdown();

        return (endTime - startTime) / 1_000_000_000.0; // секунди
    }
}