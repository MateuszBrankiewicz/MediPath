package com.adam.medipathbackend.config;

public class Utils {

    static final int MINIMUM_ACCEPTABLE_SIMILARITY_THRESHOLD = 5;

    static int compute_Levenshtein_distance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        int subCost = 0;
        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    subCost = str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = min(dp[i - 1][j - 1] + subCost,
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[str1.length()][str2.length()];
    }
    static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    public static boolean isSimilar(String str1, String str2) {
        return compute_Levenshtein_distance(str1, str2) < MINIMUM_ACCEPTABLE_SIMILARITY_THRESHOLD;
    }
}
