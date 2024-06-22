import math
import random

import numpy
import numpy as np
import sys

args = ['.\\solution.py','--train', 'rastrigin_train.txt', '--test', 'rastrigin_test.txt', '--nn', '5s', '--popsize','10' , '--elitism', '1', '--p', '0.3', '--K', '0.5', '--iter', '2000']
#args = sys.argv

class Population:
    def __init__(self, inputLen, nn):
        dangling = []
        dangling.append(inputLen)
        dangling.extend(nn)
        dangling.append(1)

        weightsNpArr = []
        b = []

        for i in range(1, len(dangling)):  # 1 5 1
            connections = np.random.normal(0, 0.01, size=(int(dangling[i]), int(dangling[i - 1])))
            weightsNpArr.append(connections)
            b.append(np.random.normal(0, 0.01, size=(int(dangling[i]))))

        self.weights = weightsNpArr
        self.b = b
        self.diffSquared = float('inf')

    def __str__(self):
        return self.weights

    def __repr__(self):
        return self.weights


def readFromFile(path):
    file1 = open(path, 'r', encoding='utf-8')
    linesTemp = file1.readlines()
    file1.close()
    return linesTemp

def createMatrix(fileName):

    linesMat = readFromFile(fileName)

    mat = []

    line: str
    for line in linesMat:
        splited = line.strip().split(',')
        if splited != ['']:

            mat.append(splited)
    return mat

def sigCalc(x):
    return 1/(1 + np.exp(-x))

# check if x or x1,x2
# --> len
def propagate(data, populations):

    pop: Population
    for pop in populations:

        difSquared: float = 0
        for i in range(1, len(data)):
            row = trainData[i]
            y = float(row[-1])
            x = np.asarray(list(np.float_(row[:-1])), dtype=np.float64)

            for j, layer in enumerate(pop.weights):
                result = np.add(np.dot(layer, x), pop.b[j])

                if j != len(pop.weights) - 1:
                    result = sigCalc(result)

                x = result

            difSquared += math.pow(y - result, 2)

        difSquared /= (len(trainData) - 1)
        pop.diffSquared = difSquared

trainInputTxt : str
testInputTxt : str
nn = []
popsize : int
elitism : int
k : float
iter : int
probabiltyForChromosomeMutation : float

for count, arg in enumerate(args, 0):
    if arg == "--train":
        trainInputTxt = args[count+1]
    elif arg == "--test":
        testInputTxt = args[count+1]
    elif arg == "--nn":
        n = args[count+1]

        if n == "5s":
            nn.append(5)
        elif n == "20s":
            nn.append(20)
        elif n == "5s5s":
            nn.append(5)
            nn.append(5)

    elif arg == "--popsize":
        popsize = int(args[count+1])
    elif arg == "--elitism":
        elitism = int(args[count+1])
    elif arg == "--p":
        probabiltyForChromosomeMutation = float(args[count+1])
    elif arg == "--K":
        k =  float(args[count+1])
    elif arg == "--iter":
        iter = int(args[count+1])

trainData = createMatrix(trainInputTxt)
testData = createMatrix(testInputTxt)

inputLen = len(trainData[0])-1
populations = []

for i in range(0, popsize):
    populations.append(Population(inputLen, nn))

for c in range(iter):

    propagate(trainData, populations)

    #elitism , selection
    sortedPopulation = sorted(populations, key=lambda x: x.diffSquared)
    sortedPopulation = sortedPopulation[:elitism]

    if c % 2000 == 1999:
        print("[Train error @"+str(c+1)+")]: "+sortedPopulation[0].diffSquared.__str__())

    #crossing
    newPopulation : list[Population] = []

    while len(newPopulation) < popsize:
        neuralNetwork1 = random.sample(sortedPopulation, 1)[0]
        neuralNetwork2 = random.sample(sortedPopulation, 1)[0] if len(newPopulation) == 0 else random.sample(newPopulation, 1)[0]

        newWeights : list[np.ndarray] = []
        for n1, n2 in zip(neuralNetwork1.weights, neuralNetwork2.weights):
            newWeight = (n1+n2)/2
            newWeights.append(newWeight)

        newB: list[np.ndarray] = []
        for n1, n2 in zip(neuralNetwork1.b, neuralNetwork2.b):
            currentB = (n1 + n2) / 2
            newB.append(currentB)

        newPopulation.append(Population(inputLen, nn))
        newPopulation[-1].weights = newWeights.copy()
        newPopulation[-1].b = newB.copy()

    #mutation

    for currentPop in newPopulation:
        #currentPop.weights += np.random.normal(0, 0.01, size=(int(dangling[i]), int(dangling[i - 1])))
        for w in currentPop.weights:
            w += np.random.normal(0, k, size= w.shape) * (np.random.random() < probabiltyForChromosomeMutation)

        for b in currentPop.b:
            b += np.random.normal(0, k, size= b.shape) * (np.random.random() < probabiltyForChromosomeMutation)

    populations = newPopulation.copy()

getFirst = sorted(populations, key=lambda x: x.diffSquared)[0]
tempArr = []
tempArr.append(getFirst)
propagate(testData, tempArr)
print("[Test error]: "+str(getFirst.diffSquared))
