package ui;

import java.util.*;

public class DecisionTree {

    private Node root;
    private int maxDepth;

    public DecisionTree(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void fit(List<HashMap<String, String>> data, List<String> featureNames, String targetFeatureName) {
        List<String> featureNamesCopy = new ArrayList<>(featureNames);
        this.root = id3(data, featureNamesCopy, targetFeatureName, 0);
    }

    private Node id3(List<HashMap<String, String>> data, List<String> featureNames, String targetFeatureName, int depth) {
        // Check for base cases: if all data are of the same class, return a leaf node with the class label
        String targetValue = data.get(0).get(targetFeatureName).toLowerCase();
        boolean targetFormatIsYesNoMaybe = targetValue.equals("yes") || targetValue.equals("no") || targetValue.equals("maybe");
        if (allPositive(data, targetFeatureName)) {
            if (targetFormatIsYesNoMaybe) {
                return new Node("yes", true);
            } else {
                return new Node("True", true);
            }
        }
        if (allNegative(data, targetFeatureName)) {
            if (targetFormatIsYesNoMaybe) {
                return new Node("no", true);
            } else {
                return new Node("False", true);
            }
        }
        if (allMaybe(data, targetFeatureName)) {
            if (targetFormatIsYesNoMaybe) {
                return new Node("maybe", true);
            } else {
                return new Node("Unknown", true);
            }
        }

        // Check for the case where there are no more features to split on or the maximum depth has been reached
        // If so, return a leaf node with the most common target value
        if (featureNames.isEmpty() || depth == maxDepth) {
            return new Node(mostCommonTargetValue(data, targetFeatureName), true);
        }

        // Select the feature that results in the maximum information gain
        String bestFeature = Solution.getColumnWithMaxIG(data, featureNames);

        // Create a new internal node for this feature
        Node node = new Node(bestFeature);
        // Remove the feature from the list of available features
        featureNames.remove(bestFeature);

        // For each possible value of the best feature, add a new subtree
        Set<String> values = uniqueValues(data, bestFeature);
        for (String value : values) {
            // Split the data on the current value of the feature
            List<HashMap<String, String>> subset = dataSubset(data, bestFeature, value);
            if (subset.isEmpty()) {
                // If there are no examples left, add a leaf node with the most common target value
                node.addChild(value, new Node(mostCommonTargetValue(data, targetFeatureName), true));
            } else {
                // Recursively call the ID3 algorithm on the remaining data
                node.addChild(value, id3(subset, new ArrayList<>(featureNames), targetFeatureName, depth + 1));
            }
        }

        return node;
    }


    private boolean allPositive(List<HashMap<String, String>> data, String target) {
        for (HashMap<String, String> row : data) {
            if (!row.get(target).equalsIgnoreCase("yes")
                    && !row.get(target).equalsIgnoreCase("True")) {
                return false;
            }
        }
        return true;
    }

    private boolean allNegative(List<HashMap<String, String>> data, String target) {
        for (HashMap<String, String> row : data) {
            if (!row.get(target).equalsIgnoreCase("no")
                    && !row.get(target).equalsIgnoreCase("False")) {
                return false;
            }
        }
        return true;
    }

    private boolean allMaybe(List<HashMap<String, String>> data, String target) {
        for (HashMap<String, String> row : data) {
            if (!row.get(target).equalsIgnoreCase("maybe")
                    && !row.get(target).equalsIgnoreCase("Unknown")) {
                return false;
            }
        }
        return true;
    }

    private String mostCommonTargetValue(List<HashMap<String, String>> data, String target) {
        HashMap<String, Integer> targetCounts = new HashMap<>();
        for (HashMap<String, String> row : data) {
            String value = row.get(target);
            targetCounts.put(value, targetCounts.getOrDefault(value, 0) + 1);
        }

        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(targetCounts.entrySet());
        entryList.sort((entry1, entry2) -> {
            // If counts are equal, compare keys alphabetically else, compare based on counts
            if (entry1.getValue().equals(entry2.getValue())) {
                return entry1.getKey().compareTo(entry2.getKey());
            } else {
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        return entryList.get(0).getKey();
    }

    private Set<String> uniqueValues(List<HashMap<String, String>> data, String feature) {
        Set<String> values = new HashSet<>();
        // iterate over data column and add all values for desired feature (column)
        for (HashMap<String, String> row : data) {
            values.add(row.get(feature));
        }
        return values;
    }

    private List<HashMap<String, String>> dataSubset(List<HashMap<String, String>> data, String feature, String value) {
        List<HashMap<String, String>> subset = new ArrayList<>();

        // Iterate over all rows in the input data
        for (HashMap<String, String> row : data) {
            // Get the value for the specified feature in the current row
            String featureValue = row.get(feature);

            if (featureValue != null && featureValue.equals(value)) {
                HashMap<String, String> rowCopy = new HashMap<>(row);
                rowCopy.remove(feature);

                // Add the modified row copy to the subset
                subset.add(rowCopy);
            }
        }
        return subset;
    }

    public void printBranches() {
        System.out.println("[BRANCHES]:");
        printBranches(this.root, "", 1);
    }

    private void printBranches(Node node, String path, int level) {
        if (node.isLeaf()) {
            System.out.println(path + node.getLabel());
        } else {
            for (Map.Entry<String, Node> child : node.getChildren().entrySet()) {
                printBranches(child.getValue(), path + level + ":" + node.getLabel() + "=" + child.getKey() + " ", level + 1);
            }
        }
    }

    public List<String> predict(List<HashMap<String, String>> testData) {
        List<String> predictions = new ArrayList<>();
        for (HashMap<String, String> instance : testData) {
            predictions.add(predictInstance(this.root, instance));
        }
        return predictions;
    }

    private String predictInstance(Node node, HashMap<String, String> instance) {
        if (node.isLeaf()) {
            return node.getLabel();
        }
//        System.out.println(node);
//        System.out.println(instance);
        String featureValue = instance.get(node.getLabel());
//        System.out.println(featureValue);
        if (node.getChildren().containsKey(featureValue)) {
            return predictInstance(node.getChildren().get(featureValue), instance);
        } else {
            // Handle unseen feature value
            return "maybe";
        }
    }

    public double accuracy(List<String> actual, List<String> predicted) {
        int correct = 0;
        for (int i = 0; i < actual.size(); i++) {
            if (actual.get(i).equals(predicted.get(i))) {
                correct++;
            }
        }
        return (double) correct / actual.size();
    }


    public int[][] confusionMatrix(List<String> actual, List<String> predicted) {
        // Get all unique labels
        Set<String> labels = new HashSet<>(actual);
        labels.addAll(predicted);

        // Sort labels alphabetically
        List<String> sortedLabels = new ArrayList<>(labels);
        Collections.sort(sortedLabels);

        // Create label index mapping for easy access
        Map<String, Integer> labelIndices = new HashMap<>();
        for (int i = 0; i < sortedLabels.size(); i++) {
            labelIndices.put(sortedLabels.get(i), i);
        }

        // Create confusion matrix
        int[][] matrix = new int[sortedLabels.size()][sortedLabels.size()];
        for (int i = 0; i < actual.size(); i++) {
            int actualIndex = labelIndices.get(actual.get(i));
            int predictedIndex = labelIndices.get(predicted.get(i));
            matrix[actualIndex][predictedIndex]++;
        }

        return matrix;
    }

}
