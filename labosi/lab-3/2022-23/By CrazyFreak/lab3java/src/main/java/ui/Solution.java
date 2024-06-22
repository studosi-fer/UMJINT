package ui;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Locale;

public class Solution {
    public static DecimalFormat df = new DecimalFormat("#.####");

    public static void main(String ... args) {
        Locale.setDefault(Locale.US);

        // Check the number of command line arguments
        if (args.length < 2){
            System.out.println("ERROR, not enough args!");
            return;
        }

        // Read command line arguments
        String trainFilePath = args[0];
        String testFilePath = args[1];
        int maxDepth = (args.length > 2) ? Integer.parseInt(args[2]) : -1;

        // Initialize lists for feature names and data
        List<String> featureNames = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();

        // Read training data from file
        try (BufferedReader reader = new BufferedReader(new FileReader(trainFilePath))) {
            // Read feature names
            featureNames.addAll(List.of(reader.readLine().split(",")));

            // Read data
            String line;
            while ((line = reader.readLine()) != null) {
                data.add(new ArrayList<>(List.of(line.split(","))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert data to list of maps
        List<HashMap<String, String>> dataMapList = new ArrayList<>();
        for (List<String> row : data) {
            HashMap<String, String> dataMap = new HashMap<>();
            for (int i = 0; i < featureNames.size(); i++) {
                dataMap.put(featureNames.get(i), row.get(i));
            }
            dataMapList.add(dataMap);
        }

        // Train model
        System.out.println("Treniranje modela...");
        String targetFeatureName = featureNames.get(featureNames.size() - 1);
        DecisionTree decisionTree = new DecisionTree(maxDepth);
        decisionTree.fit(dataMapList, featureNames, targetFeatureName);

        // Print tree
        System.out.println("\nStablo izgrađeno...");
        decisionTree.printBranches();

        // Read test data from file
        List<List<String>> testData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(testFilePath))) {
            reader.readLine(); // skip feature names
            String line;
            while ((line = reader.readLine()) != null) {
                testData.add(new ArrayList<>(List.of(line.split(","))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert test data to list of maps
        List<HashMap<String, String>> testMapList = new ArrayList<>();
        for (List<String> row : testData) {
            HashMap<String, String> dataMap = new HashMap<>();
            for (int i = 0; i < featureNames.size(); i++) {
                dataMap.put(featureNames.get(i), row.get(i));
            }
            testMapList.add(dataMap);
        }

        // Make predictions
        List<String> predictions = decisionTree.predict(testMapList);
        System.out.print("\n[PREDICTIONS]: ");
        predictions.forEach(prediction -> System.out.print(prediction + " "));
        System.out.println();

        // Calculate accuracy
        List<String> actual = new ArrayList<>();
        for (HashMap<String, String> instance : testMapList) {
            actual.add(instance.get(targetFeatureName));
        }
        double accuracy = decisionTree.accuracy(actual, predictions);
        System.out.printf("\n[ACCURACY]: %.5f\n", accuracy);

        // Print confusion matrix
        int[][] confusionMatrix = decisionTree.confusionMatrix(actual, predictions);
        System.out.println("\n[CONFUSION_MATRIX]:");
        for (int[] row : confusionMatrix) {
            for (int i = 0; i < row.length; i++) {
                System.out.print(row[i]);
                if (i != row.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }


    public static List<int[]> getCountsByColumn(List<HashMap<String, String>> data, String column, String targetFeatureName) {
        // HashSet to store unique values from the column
        Set<String> uniqueValues = new HashSet<>();

        // Find unique values in the column
        for (HashMap<String, String> row : data) {
            uniqueValues.add(row.get(column));
        }

        List<int[]> counts = new ArrayList<>();

        // Iterate over each unique value
        for (String value : uniqueValues) {
            int yesCount = 0;
            int noCount = 0;
            int maybeCount = 0;

            // Iterate over all rows in the data
            for (HashMap<String, String> hashMap : data) {
                // If the value matches the provided value
                if (hashMap.get(column).equals(value)) {
                    String targetValue = hashMap.get(targetFeatureName).toLowerCase();
                    // Update the corresponding count based on the target value
                    if (targetValue.equals("yes") || targetValue.equals("true")) {
                        yesCount++;
                    } else if (targetValue.equals("maybe") || targetValue.equals("unknown")) {
                        maybeCount++;
                    } else {
                        noCount++;
                    }
                }
            }
            // Add the counts for this value to the list
            counts.add(new int[]{yesCount, noCount, maybeCount});
        }

        return counts;
    }

    static double computeEntropy(int yesNum, int noNum, int maybeNum) {
        int[] counts = {yesNum, noNum, maybeNum};
        double totalNum = yesNum + noNum + maybeNum;
        double entropy = 0.0;

        // Iterate over each type of response
        for (int count : counts) {
            // If the count of a response type is not 0, compute its contribution to entropy
            if (count != 0) {
                double probability = count / totalNum;
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }

        return entropy;
    }

    public static double computeInformationGain(List<int[]> counts) {
        int totalYesNum = 0;
        int totalNoNum = 0;
        int totalMaybeNum = 0;

        // Calculate total yes, no and maybe numbers
        for (int[] triplet : counts) {
            totalYesNum += triplet[0];
            totalNoNum += triplet[1];
            totalMaybeNum += triplet[2];
        }

        double totalDatasetSize = totalYesNum + totalNoNum + totalMaybeNum;
        double IG = computeEntropy(totalYesNum, totalNoNum, totalMaybeNum); // entropy of the total dataset

        for (int[] triplet : counts) {
            double subsetSize = triplet[0] + triplet[1] + triplet[2];
            IG -= (subsetSize / totalDatasetSize) * computeEntropy(triplet[0], triplet[1], triplet[2]);
        }

        return IG;
    }

    public static String getColumnWithMaxIG(List<HashMap<String, String>> data, List<String> featureNames) {
        String maxIGColumn = null;
        double maxIG = Double.NEGATIVE_INFINITY;

        // Loop over all columns except the last one
        for (int i = 0; i < featureNames.size() - 1; i++) {
            List<int[]> counts = getCountsByColumn(data, featureNames.get(i), featureNames.get(featureNames.size() - 1));
            double IG = computeInformationGain(counts);

            System.out.print("IG(" + featureNames.get(i) + ")=" + df.format(IG) + " ");

            // If this column's IG is higher than the current max, update max and max column
            if (IG > maxIG) {
                maxIG = IG;
                maxIGColumn = featureNames.get(i);
            }
        }

        System.out.println();
        return maxIGColumn;
    }
}
