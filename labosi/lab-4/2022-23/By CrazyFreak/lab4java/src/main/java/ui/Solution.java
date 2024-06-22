package ui;

import java.io.*;
import java.util.*;

import static ui.GeneticAlg.elitism;



public class Solution {
    public static void main(String ... args) {
        Map<String, String> argMap = new HashMap<>();

        // Parse command-line arguments
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                String key = args[i];
                String value = args[i + 1];
                argMap.put(key, value);
            }
        }

        String trainFilePath = argMap.get("--train");
        String testFilePath = argMap.get("--test");
        String nnStructure = argMap.get("--nn");
        int populationSize = Integer.parseInt(argMap.get("--popsize"));
        int elitismNum = Integer.parseInt(argMap.get("--elitism"));
        double mutationProbability = Double.parseDouble(argMap.get("--p"));
        double mutationsStanDev = Double.parseDouble(argMap.get("--K"));
        int iterations = Integer.parseInt(argMap.get("--iter"));

//        String trainFilePath = "D:\\Data\\Docs\\Education\\FER - Fakultet elektrotehnike i računarstva\\FER 6. semestar\\UUUI - Uvod u umjetnu inteligenciju\\Laboratorijske vježbe\\autograder\\data\\lab4\\files\\"
//            + "rosenbrock_train.txt"; // rastrigin_train OR rosenbrock_train OR sine_train
//        String testFilePath = "D:\\Data\\Docs\\Education\\FER - Fakultet elektrotehnike i računarstva\\FER 6. semestar\\UUUI - Uvod u umjetnu inteligenciju\\Laboratorijske vježbe\\autograder\\data\\lab4\\files\\"
//            + "rosenbrock_test.txt"; // rastrigin_test OR rosenbrock_test OR sine_test
//        String nnStructure = "4s3s2s";
//        int populationSize = 12;
//        int elitismNum = 2;
//        double mutationProbability = 0.1;
//        double mutationsStanDev = 0.1;
//        int iterations = 10000;

        final int generationsPerOutput = 1000;


        // Initialize lists for data and axis
        List<List<Double>> trainData = new ArrayList<>();
        List<List<Double>> testData = new ArrayList<>();
        List<String> givenAxis = new ArrayList<>();
        String targetAxis;

        // Read training data from file
        try (BufferedReader reader = new BufferedReader(new FileReader(trainFilePath))) {
            // Read feature names
            givenAxis.addAll(List.of(reader.readLine().split(",")));

            // Read data
            String line;
            while ((line = reader.readLine()) != null) {
                List<Double> row = new ArrayList<>();
                for (String value : line.split(",")) {
                    row.add(Double.parseDouble(value));
                }
                trainData.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        targetAxis = givenAxis.remove(givenAxis.size() - 1);

        // Read test data from file
        try (BufferedReader reader = new BufferedReader(new FileReader(testFilePath))) {
            reader.readLine(); // skip feature names

            // Read data
            String line;
            while ((line = reader.readLine()) != null) {
                List<Double> row = new ArrayList<>();
                for (String value : line.split(",")) {
                    row.add(Double.parseDouble(value));
                }
                testData.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Gives axis: " + givenAxis);
        System.out.println("Target axis: " + targetAxis);
        System.out.println("Train data: " + trainData);
        System.out.println("Test data: " + testData);


        // create population
        System.out.println("Creating population...");
        List<NeuralNet> nnPopulation = new ArrayList<>();
        for (int nnCount = 0; nnCount < populationSize; nnCount++) {
            nnPopulation.add(new NeuralNet(nnStructure, givenAxis.size()));
        }

        // train population
        System.out.println("Training population...");
        GeneticAlg geneticAlg = new GeneticAlg(populationSize, elitismNum, mutationsStanDev, mutationProbability);

        NeuralNet bestNN = elitism(nnPopulation, 1, trainData).get(0);
        double bestNNScore = bestNN.calcError(trainData);
        System.out.println("[Train error @0]: " + bestNNScore);

        for (int i = 0; i < iterations / generationsPerOutput; i++) {
            for (int j = 0; j < iterations / (iterations / generationsPerOutput); j++) {
                // evolve population
                nnPopulation = geneticAlg.evolve(nnPopulation, trainData);

                // get best neural network in population
                bestNN = elitism(nnPopulation, 1, trainData).get(0);
            }

            bestNNScore = bestNN.calcError(trainData);
            System.out.println("[Train error @" + ((i + 1) * generationsPerOutput) + "]: " + bestNNScore);
        }

        bestNNScore = bestNN.calcError(testData);
        System.out.println("[Test error]: " + bestNNScore);
    }
}
