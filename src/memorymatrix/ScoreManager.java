package memorymatrix;

import java.io.*;

public class ScoreManager {
    // mode: "classic", "easy", "medium", "hard"
    private static String fileName(String mode) {
        return "memorymatrix_" + mode + ".txt";
    }

    public static int loadBest(String mode) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName(mode)))) {
            return Integer.parseInt(br.readLine().trim());
        } catch (Exception e) { return 0; }
    }

    public static int saveBest(int score, String mode) {
        int best = loadBest(mode);
        if (score > best) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fileName(mode)))) {
                pw.println(score);
            } catch (Exception ignored) {}
            return score;
        }
        return best;
    }
}
