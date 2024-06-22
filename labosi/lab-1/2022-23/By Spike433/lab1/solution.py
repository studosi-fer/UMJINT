import heapq
import sys
from heapq import heappop, heappush

def getValueFromIndex(string):
    index = args.index(string)
    return args[index + 1]
#astar ai pass passes, fail in infinite loop
#ucs fails at ai

args = ['.\\solution.py', '--alg', 'ucs', '--ss', 'ai.txt', '--h', '3x3_misplaced_heuristic.txt']
#args = ['.\\solution.py', '--check-consistent', '--ss', 'ai.txt', '--h', 'ai_pass.txt']
#args = sys.argv  #todo renable this when done

pathString = getValueFromIndex("--ss")

def readFromFile(path):
    file1 = open(path, 'r', encoding='utf-8')
    return file1.readlines()


Lines = readFromFile(pathString)

start = []
goals = []
states = {}
heuristicStates = {}
nameToId = {}
idToName = {}

def fillHeuristicStates():
    global val
    heuristicString = getValueFromIndex("--h")
    heuristics = sorted(readFromFile(heuristicString))
    i = 0
    for heurString in heuristics:  # Baderna : 25
        splited = heurString.split(":")
        left = splited[0].strip()
        val = splited[1].strip()
        heuristicStates[left] = float(val)
        nameToId[left] = i
        idToName[i] = left
        i = i + 1

class AStarStucture:
    def __init__(self, name, valueFromStart : float, previous, needHeuristic):
        self.name = name
        self.valueFromStart = valueFromStart
        self.previousState = previous
        self.needHeur = needHeuristic
        if needHeuristic == True:
            self.heur = float(heuristicStates.get(name))+float(valueFromStart)


    def __repr__(self):
        #return f'Struct ("{self.name}","{self.valueFromStart} "," {self.heur})'

        if self.needHeur:
            return self.name + " " + str(self.valueFromStart) + " " +self.heur

        return self.name + " " + str(self.valueFromStart)


    def __lt__(self, other):

        if self.needHeur:
            if self.heur < other.heur:
                return True
            elif self.heur == other.heur:
                if str(self.name) < str(other.name):
                    return True
        else:
            if self.valueFromStart < other.valueFromStart:
                return True
            elif self.valueFromStart == other.valueFromStart:
                if str(self.name) < str(other.name):
                    return True
        return False


def formatInput():
    global start, goals, states
    i = 0
    for line in Lines:
        line = line.strip()

        if line.startswith("#"):
            continue
        else:
            i = i + 1

            if i == 1:
                start = line
            elif i == 2:
                goals = line.split(" ")
            else:
                state = line.split(":")
                leftSide = state[0].strip()
                rightSide = state[1].strip()

                if rightSide == '':
                    states[leftSide] = '$'
                else:
                    listOfStates = rightSide.split(" ")
                    states[leftSide] = sorted(listOfStates)


def ucs():
    # UCS and Astar --> FOUND Solution and TotalCost

    global pathString
    # UCS and Astar --> FOUND Solution and TotalCost
    formatInput()
    open = []
    #open.append(AStarStucture(start, 0, "$", False))
    heappush(open, AStarStucture(start, 0, "$", False))
    closed = {}
    costPath = {}
    previousStates = {}

    while open != []:

        head = open[0]
        stateN = head.name
        headValue = head.valueFromStart

        closed[stateN] = True
        #open.pop(0)
        heappop(open)
        previousStates[stateN] = head.previousState

        if goals.__contains__(stateN):
            reversedPaths = applyBackTrack(previousStates, stateN)

            pathsFinal = ' => '.join(reversedPaths)

            parseOutputForBFSucsASTAR("UCS", "yes", pathsFinal, len(reversedPaths), len(previousStates),
                                      head.valueFromStart)
            return

        expandedState = states.get(stateN)

        for state in expandedState:
            if closed.__contains__(state.split(",")[0]) and closed[state.split(",")[0]]:  # istra example in infinite loop
                continue
            astar = AStarStucture(state.split(",")[0], float(state.split(",")[1]) + float(headValue), stateN, False)
            # it is okay to save to save lower value than in open, not greater
            if costPath.__contains__(astar.name) and not (costPath.get(astar.name) > astar.valueFromStart):
                continue
            #open.append(astar)
            heappush(open, astar)
            costPath[astar.name] = astar.valueFromStart

        #open = sorted(open, key=lambda x: (x.valueFromStart, x.name))

    parseOutputForBFSucsASTAR("UCS", "no", "", "", "", "")
    return



def bfs():


    # UCS and Astar --> FOUND Solution and TotalCost
    formatInput()
    open = []
    open.append(AStarStucture(start, 0, "$", False))
    closed = {}
    costPath = {}
    previousStates = {}

    while open != []:

        head = open[0]
        stateN = head.name
        headValue = head.valueFromStart

        closed[stateN] = True
        open.pop(0)
        previousStates[stateN] = head.previousState

        if goals.__contains__(stateN):
            reversedPaths = applyBackTrack(previousStates, stateN)

            pathsFinal = ' => '.join(reversedPaths)

            parseOutputForBFSucsASTAR("BFS", "yes", pathsFinal, len(reversedPaths), len(previousStates),
                                      head.valueFromStart)
            return

        expandedState = states.get(stateN)

        for state in expandedState:
            if closed.__contains__(state.split(",")[0]) and closed[state.split(",")[0]]: # infinite loop in backtraing when using istra.txt
                continue
            astar = AStarStucture(state.split(",")[0], float(state.split(",")[1]) + float(headValue), stateN, False)
            if costPath.__contains__(astar.name) and not (costPath.get(astar.name) > astar.valueFromStart):
                continue
            open.append(astar)
            costPath[astar.name] = astar.valueFromStart

        # sort by value
        #open = sorted(open, key=lambda x: (x.valueFromStart, x.name))

    parseOutputForBFSucsASTAR("BFS", "no", "", "", "", "")
    return


def parseOutputForBFSucsASTAR(algType, foundSol, path, pathLength, statesVisited, totalCost):

    if foundSol == "yes":
        print("# "+algType+" "+pathString)
        print("[FOUND_SOLUTION]: " + foundSol.__str__())
        print("[STATES_VISITED]: " + statesVisited.__str__())
        print("[PATH_LENGTH]: " + pathLength.__str__())
        print("[TOTAL_COST]: " + totalCost.__str__())
        print("[PATH]: " + path.__str__())
    else:
        print("# "+algType+" "+pathString)
        print("[FOUND_SOLUTION]: no")


def aastar():
    global pathString
    # UCS and Astar --> FOUND Solution and TotalCost
    formatInput()
    open = []
    #open.append(AStarStucture(start, 0, "$", True))
    heapq.heappush(open, AStarStucture(start, 0, "$", True))
    closed = {}
    costPath = {}
    previousStates = {}

    while open != []:

        head = open[0]
        stateN = head.name
        headValue = head.valueFromStart

        closed[stateN] = True
        #open.pop(0)
        heappop(open)

        previousStates[stateN] = head.previousState

        if goals.__contains__(stateN):
            pathString = getValueFromIndex("--h")  # change output from ordinary file to heuristic.txt

            reversedPaths = applyBackTrack(previousStates, stateN)

            pathsFinal = ' => '.join(reversedPaths)

            parseOutputForBFSucsASTAR("A-STAR", "yes", pathsFinal, len(reversedPaths), len(previousStates), head.valueFromStart)
            return

        expandedState = states.get(stateN)

        for state in expandedState:
            if closed.__contains__(state.split(",")[0]) and closed[state.split(",")[0]]:
                continue
            astar = AStarStucture(state.split(",")[0], float(state.split(",")[1])+float(headValue), stateN, True)
            if costPath.__contains__(astar.name) and not (costPath.get(astar.name) > astar.valueFromStart):
                continue
            #open.append(astar)
            heappush(open, astar)
            costPath[astar.name] = astar.valueFromStart

        #open = sorted(open, key=lambda x: (x.heur, x.name))


    parseOutputForBFSucsASTAR("A-STAR", "no", "", "", "", "")
    return


def applyBackTrack(previousStates, stateN):
    unReversedPaths = []
    while stateN != "$":
        unReversedPaths.append(stateN)
        stateN = previousStates.get(stateN)
    reversedPaths = []
    for path in reversed(unReversedPaths):
        reversedPaths.append(path)
    return reversedPaths

def createMatrix(n):

    mat = []
    while len(mat) < n:
        mat.append([])
        while len(mat[-1]) < n:
            mat[-1].append(float('inf'))

    for i in range(n):
        mat[i][i] = 0.0

    return mat

def checkOptimistic(path):
    formatInput()
    vertices = len(heuristicStates)
    matrix = createMatrix(vertices)

    matrix = floydMatrixGetter(matrix, vertices)

    Optimistic = True

    matrixGoals = []
    for goal in goals:
        matrixGoals.append(nameToId.get(goal))

    outputForSort = []
    for i in range(0, vertices):

        min = float('inf')
        for j in matrixGoals:
            if min > matrix[i][j]:
                min = matrix[i][j]

        name = idToName.get(i)
        heurCost = heuristicStates.get(name)

        err = False
        if not(heurCost <= min):
            Optimistic = False
            err = True

        if not err:
            message = "[CONDITION]: [OK] h({}) <= h*: {:.1f} <= {:.1f}".format(name, heurCost, min)
        else:
            message = "[CONDITION]: [ERR] h({}) <= h*: {:.1f} <= {:.1f}".format(name, heurCost, min)
        outputForSort.append(message)

    printSortedArr(outputForSort)

    if Optimistic:
        print("[CONCLUSION]: Heuristic is optimistic.")
    else:
        print("[CONCLUSION]: Heuristic is not optimistic.")


def printSortedArr(outputForSort):
    print("\n".join(sorted(outputForSort)))


def floydMatrixGetter(matrix, vertices):

    # fill matrix with known tranzitions
    # state is row i , tranz column j
    for state in states:
        tranzitions = states.get(state)

        # we could have goal or empty state which is labeled $
        if tranzitions != "$":
            i = nameToId.get(state)

            for tranz in tranzitions:
                # data is a,4 --> get only a
                leftFromComma = str(tranz).split(",")[0]
                j = nameToId.get(leftFromComma)
                rightValue = str(tranz).split(",")[1]
                matrix[i][j] = float(rightValue)

    # complexity O(vertices^3)
    for k in range(0, vertices):
        for i in range(0, vertices):
            for j in range(0, vertices):
                if matrix[i][j] > matrix[i][k] + matrix[k][j]:
                    matrix[i][j] = matrix[i][k] + matrix[k][j]

    return matrix


def checkConsistent(path):
    formatInput()

    Consistent = True

    outputArrForSort = []

    for state in heuristicStates:
        onlyName = str(state).split(",")[0]
        expandedState = states.get(onlyName)

        if expandedState != "$":
            h = heuristicStates.get(onlyName)
            for tranzient in expandedState:
                name = str(tranzient).split(",")[0]
                c = float(str(tranzient).split(",")[1])
                hRight = heuristicStates.get(name)

                if  h <= hRight + c :
                    currentOutput = "[CONDITION]: [OK] h({}) <= h({}) + c: {:.1f} <= {:.1f} + {:.1f}".format(onlyName, name, h, hRight, c)
                else:
                    currentOutput = "[CONDITION]: [ERR] h({}) <= h({}) + c: {:.1f} <= {:.1f} + {:.1f}".format(onlyName, name, h, hRight, c)
                    Consistent = False
                outputArrForSort.append(currentOutput)

    printSortedArr(outputArrForSort)
    if Consistent:
        print("[CONCLUSION]: Heuristic is consistent.")
    else:
        print("[CONCLUSION]: Heuristic is not consistent.")


if args.__contains__("--alg"):
    algorithmForSearch = getValueFromIndex("--alg")

    if algorithmForSearch == "bfs":
        bfs()
    elif algorithmForSearch == "ucs":
        ucs()
    else:
        fillHeuristicStates()
        aastar()

elif args.__contains__("--check-optimistic"):
    heuristic = getValueFromIndex("--h")
    fillHeuristicStates()
    print("# HEURISTIC-OPTIMISTIC "+heuristic)
    checkOptimistic(pathString)

elif args.__contains__("--check-consistent"):
    heuristic = getValueFromIndex("--h")
    fillHeuristicStates()
    print("# HEURISTIC-CONSISTENT "+heuristic)
    checkConsistent(pathString)
