package com.adam.medipathbackend.config;

public class Utils {

    static final int MINIMUM_ACCEPTABLE_SIMILARITY_THRESHOLD = 5;

    static int compute_Levenshtein_distance(String str1, String str2) {
        int[][] distanceMatrix = new int[str1.length() + 1][str2.length() + 1];
        int substitutionCost = 0;

        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {

                if (i == 0) {
                    distanceMatrix[i][j] = j;

                } else if (j == 0) {
                    distanceMatrix[i][j] = i;

                } else {
                    substitutionCost = str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1;

                    distanceMatrix[i][j] = min(distanceMatrix[i - 1][j - 1] + substitutionCost,
                            distanceMatrix[i - 1][j] + 1,
                            distanceMatrix[i][j - 1] + 1);
                }
            }
        }

        return distanceMatrix[str1.length()][str2.length()];
    }

    static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    public static boolean isSimilar(String str1, String str2) {
        return compute_Levenshtein_distance(str1, str2) < MINIMUM_ACCEPTABLE_SIMILARITY_THRESHOLD;
    }
    public static boolean isValidMongoOID(String oid) {
        if(oid == null || oid.length() != 24) return false;
        char c;
        for(int i = 0; i < 24; i++) {
            c = oid.charAt(i);
            if((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
                continue;
            }
            return false;
        }
        return true;
    }
}
