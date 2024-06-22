package ui;

import java.util.*;

public class GeneticAlg {

    private final int populationSize;
    private final int elitismNum;
    private final double mutationsStanDev;
    private final double mutationProbability;

    public GeneticAlg(int populationSize, int elitismNum, double mutationsStanDev, double mutationProbability) {
        this.populationSize = populationSize;
        this.elitismNum = elitismNum;
        this.mutationsStanDev = mutationsStanDev;
        this.mutationProbability = mutationProbability;
    }

    public List<NeuralNet> evolve(List<NeuralNet> population, List<List<Double>> data) {
        // elitism
        List<NeuralNet> bestNNs = elitism(population, elitismNum, data);
        List<NeuralNet> newPopulation = new ArrayList<>(bestNNs);

        while (newPopulation.size() < populationSize) {
            // selection
            List<NeuralNet> parents = selectParents(population, data); // fitness proportional selection

            // crossover
            List<NeuralNet> children = crossover(parents); // crossover operator should be the arithmetic mean

            // mutation
            mutate(children);

            newPopulation.addAll(children);
        }

        return newPopulation;
    }

    public static List<NeuralNet> elitism(List<NeuralNet> population, int howMany, List<List<Double>> inputData) {
        population.sort(Comparator.comparingDouble(nn -> nn.calcError(inputData)));

        List<NeuralNet> bestNNs = new ArrayList<>(population.subList(0, howMany));

        return bestNNs;
    }

    private List<NeuralNet> selectParents(List<NeuralNet> population, List<List<Double>> data) {
        double totalFitness = population.stream()
                .mapToDouble(nn -> calcFitness(nn, data)) // Convert error to fitness score
                .sum();

        List<NeuralNet> parents = new ArrayList<>();
        Random rand = new Random();

        // Perform roulette wheel selection to choose parents
        while (parents.size() < 2) {
            // choose a random number within total fitness range
            double r = rand.nextDouble() * totalFitness;

            double sum = 0;
            // for each nn
            for (NeuralNet nn : population) {
                double nnFitness = calcFitness(nn, data);

                sum += nnFitness;

                // if sum (accumulated fitness) exceeds the random value
                if (sum >= r) {
                    parents.add(nn);

                    break; // for each nn
                }
            }
        }

        return parents;
    }

    private double calcFitness(NeuralNet nn, List<List<Double>> data) {
        // https://www.gepsoft.com/gepsoft/APS3KB/Chapter08/Section2/SS05.htm#:~:text=The%20mean%20squared%20error%20E,j%20and%20Ei%20%3D%200.
        return 1000 / (1.0 + nn.calcError(data));
    }

    private List<NeuralNet> crossover(List<NeuralNet> parents) {
        String nnStructure = parents.get(0).getNeuralNetworkStructure();
        int inputVectorSize = parents.get(0).getInputVectorSize();

        List<NeuralNet> children = new ArrayList<>();
        int numberOfChildrenToProduce = 2;
        for (int childrenCount = 0; childrenCount < numberOfChildrenToProduce; childrenCount++) {
            NeuralNet child = new NeuralNet(nnStructure, inputVectorSize);

            // for each weight (w1...wX) in child nn
            for (int i = 0; i < child.getWeights().length; i++) {
                for (int j = 0; j < child.getWeights()[i].length; j++) {
                    for (int k = 0; k < child.getWeights()[i][j].length; k++) {

                        // arithmetic mean crossover for weights
                        double avgWeight = (parents.get(0).getWeights()[i][j][k] + parents.get(1).getWeights()[i][j][k]) / 2.0;

                        child.getWeights()[i][j][k] = avgWeight;
                    }
                }
            }

            // for each bias (w0) in child nn
            for (int i = 0; i < child.getBiases().length; i++) {
                for (int j = 0; j < child.getBiases()[i].length; j++) {
                    double avgBias = (parents.get(0).getBiases()[i][j] + parents.get(1).getBiases()[i][j]) / 2.0;
                    child.getBiases()[i][j] = avgBias;
                }
            }

            children.add(child);
        }

        return children;
    }

    private void mutate(List<NeuralNet> children) {
        Random rand = new Random();

        // for each child
        for (NeuralNet child : children) {
            double[][][] weights = child.getWeights();

            // for every weight (w1...wX) in child nn
            for (int i = 0; i < weights.length; i++) {
                for (int j = 0; j < weights[i].length; j++) {
                    for (int k = 0; k < weights[i][j].length; k++) {

                        // if random num less than probability of mutation
                        if (rand.nextDouble() < mutationProbability) {

                            // mutate
                            weights[i][j][k] += rand.nextGaussian() * mutationsStanDev;
                        }
                    }
                }
            }

            double[][] biases = child.getBiases();
            // for every bias (w0) in child nn
            for (int i = 0; i < biases.length; i++) {
                for (int j = 0; j < biases[i].length; j++) {

                    // if random num less than probability of mutation
                    if (rand.nextDouble() < mutationProbability) {

                        // mutate
                        biases[i][j] += rand.nextGaussian() * mutationsStanDev;
                    }
                }
            }
        }
    }
}
