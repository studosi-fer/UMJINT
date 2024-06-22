package ui;

import java.util.*;

public class NeuralNet {
    private final String neuralNetworkStructure;
    private final int inputVectorSize;
    private int[] hiddenLayers; // [hiddenlayer]
    private double[][][] weights; // [layer][neuron][weight]
    private double[][] biases; // [layer][neuron]

    public NeuralNet(String neuralNetworkStructure, int inputVectorSize) {
        this.inputVectorSize = inputVectorSize;
        this.neuralNetworkStructure = neuralNetworkStructure;

        setupNeuralNetwork();
    }

    private void setupNeuralNetwork() {
        Random rand = new Random(); // needed for normal distribution

        String[] layers = neuralNetworkStructure.split("s");

        hiddenLayers = new int[layers.length];

        weights = new double[hiddenLayers.length + 1][][]; // +1 for output layer
        biases = new double[hiddenLayers.length + 1][]; // +1 for output layer


        int prevLayerSize = inputVectorSize;
        // for each hidden layer
        for (int layerCount = 0; layerCount < layers.length; layerCount++) {
            // set number of neurons for that layer (example: 5s = 5 neurons)
            hiddenLayers[layerCount] = Integer.parseInt(layers[layerCount]);

            // initialize arrays for weights
            weights[layerCount] = new double[prevLayerSize][hiddenLayers[layerCount]];
            biases[layerCount] = new double[hiddenLayers[layerCount]];

            // set initial weights (from normal distribution with standard deviation 0.01)
            setStartingWeights(weights[layerCount], biases[layerCount], rand);

            prevLayerSize = hiddenLayers[layerCount];
        }

        // repeat once more for output layer
        weights[hiddenLayers.length] = new double[prevLayerSize][1]; // only 1 neuron in output layer
        biases[hiddenLayers.length] = new double[1]; // only 1 neuron in output layer
        setStartingWeights(weights[hiddenLayers.length], biases[hiddenLayers.length], rand);
    }

    private void setStartingWeights(double[][] weights, double[] biases, Random rand) {
        // for each neuron in layer
        for (int neuronCount = 0; neuronCount < weights.length; neuronCount++) {

            // for each weight
            for (int weightCount = 0; weightCount < weights[neuronCount].length; weightCount++) {

                // set weights (w1...wX) from normal distribution with standard deviation 0.01
                // https://www.javamex.com/tutorials/random_numbers/gaussian_distribution_2.shtml
                weights[neuronCount][weightCount] = rand.nextGaussian() * 0.01;
            }
        }

        // for each neuron
        for (int neuronCount = 0; neuronCount < biases.length; neuronCount++) {

            // set biases (w0) from normal distribution with standard deviation 0.01
            biases[neuronCount] = rand.nextGaussian() * 0.01;
        }
    }

    public double calcError(List<List<Double>> inputData) {
        double mse = 0.0;

        // for each sample Xi in testData
        for (List<Double> sample : inputData) {
            // get actual output Yi
            double target = sample.get(sample.size() - 1);

            // get NN output NN(Xi)
            double output = forwardPass(sample.subList(0, sample.size() - 1));

            mse += Math.pow(target - output, 2); // (Yi - NN(Xi))^2
        }

        return (1.0 / inputData.size()) * mse; // (1 / N ) * mse
    }

    double forwardPass(List<Double> sampleInputVector) {
        // copy input vector
        double[] currentLayerOutput = new double[sampleInputVector.size()];
        for (int i = 0; i < sampleInputVector.size(); i++) {
            currentLayerOutput[i] = sampleInputVector.get(i);
        }

        // for each hidden layer
        for (int hiddenLayerCount = 0; hiddenLayerCount < hiddenLayers.length; hiddenLayerCount++) {
            double[] nextLayerOutput = new double[hiddenLayers[hiddenLayerCount]];

            // for each neuron in next layer
            for (int neuronInNextLayerCount = 0; neuronInNextLayerCount < hiddenLayers[hiddenLayerCount]; neuronInNextLayerCount++) {

                // add bias
                nextLayerOutput[neuronInNextLayerCount] = biases[hiddenLayerCount][neuronInNextLayerCount];

                // for each neuron in current layer
                for (int neuronInCurrentLayerCount = 0; neuronInCurrentLayerCount < currentLayerOutput.length; neuronInCurrentLayerCount++) {

                    // add weighted sum of inputs to bias
                    nextLayerOutput[neuronInNextLayerCount] += currentLayerOutput[neuronInCurrentLayerCount]
                            * weights[hiddenLayerCount][neuronInCurrentLayerCount][neuronInNextLayerCount];
                }

                // apply logistic sigmoid transfer function (maps value to 0...1)
                nextLayerOutput[neuronInNextLayerCount] = logisticSigmoidTransferFunction(nextLayerOutput[neuronInNextLayerCount]);
            }

            // update current layer's output for next layer
            currentLayerOutput = nextLayerOutput;
        }

        // repeat once more for output layer
        double output = biases[hiddenLayers.length][0];
        for (int neuronInOutputLayerCount = 0; neuronInOutputLayerCount < currentLayerOutput.length; neuronInOutputLayerCount++) {
            output += currentLayerOutput[neuronInOutputLayerCount] * weights[hiddenLayers.length][neuronInOutputLayerCount][0];
        }

        return output;
    }

    private double logisticSigmoidTransferFunction(double x) {
        return 1.0 / (1.0 + Math.exp(-x)); // 1 / (1 + e^-x)
    }

    public String getNeuralNetworkStructure() {
        return neuralNetworkStructure;
    }

    public int getInputVectorSize() {
        return inputVectorSize;
    }

    public double[][][] getWeights() {
        return weights;
    }

    public double[][] getBiases() {
        return biases;
    }
}


