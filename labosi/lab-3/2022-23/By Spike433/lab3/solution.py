import math
import sys

args = ['.\\solution.py', 'v.csv', 'vt.csv', '2']
#args = sys.argv

def getSecondArg():
    return args[1]


def getThirdArg():
    return args[2]


def getOptionalArg():
    try:
        optional = args[3]
        return optional
    except:
        return None


class Info:
    def __init__(self, name: str, value: float, col: int, next: str, mostCommon : str):
        self.name = name
        self.value = value
        self.next = next
        self.col = col
        self.mostCommon = mostCommon

    def __str__(self):
        return self.name + " " + self.next

    def __repr__(self):
        return self.name + " " + self.next


class ResultInfo:
    def __init__(self, name: str, result: str):
        self.combined = name + "|" + result

    def getResult(self):
        second = self.combined.split("|")[1]
        return second

    def __str__(self):
        return self.combined

    def __repr__(self):
        return self.combined

class CommonValue:
    def __init__(self, name : str, value : int):
        self.name = name
        self.value = value

    def __eq__(self, other):
        if self.name == other:
            return self

    def __lt__(self, other):

        if self.value < other.value:
            return True
        elif self.value == other.value:
            if str(self.name) < str(other.name):
                return True

        return False

def readFromFile(path):
    file1 = open(path, 'r', encoding='utf-8')
    linesTemp = file1.readlines()
    file1.close()
    return linesTemp


confustionResultSet = set()
confusionResultDict = {}
confusionResultDictBack = {}
CommonValueDict = {}

def createMatrix(arg):
    global confustionResultSet, CommonValueDict

    linesMat = readFromFile(arg)

    mat = []

    first: bool = True

    line: str
    for line in linesMat:
        splited = line.strip().split(',')
        if splited != ['']:

            if first:
                first = False
            else:
                confustionResultSet.add(splited[-1])

                lastItemName : str = splited[-1]
                if CommonValueDict.__contains__(lastItemName):
                    CommonValueDict[lastItemName] += 1
                else:
                    CommonValueDict[lastItemName] = 1

            mat.append(splited)

    return mat

result = {}
totalRows: int = 0

firstTime: bool = True

mostSignificant : CommonValue

def getEntropy(mat):
    global result, totalRows, firstTime, mostSignificant

    result = {}
    rows: int = len(mat) - 1  # remove first row
    sumH: float = 0

    if firstTime:
        firstTime = False
        totalRows = rows

    for line in mat[1:]:
        if not (result.__contains__(line[-1])):
            result[line[-1]] = 1
        else:
            result[line[-1]] += 1

    first = True
    for r in result:
        if first:
            first = False
            mostSignificant = CommonValue(r, result[r])
        elif mostSignificant.value < result[r]:
            mostSignificant = CommonValue(r, result[r])

    for resultRow in result:
        if resultRow == 0:
            sumH += 0
        else:
            sumH += -(result[resultRow] / rows) * math.log2((result[resultRow] / rows))

    return sumH


tree = {}


def highestGainCalculator(mat, branch, depth):
    h = getEntropy(mat)

    subtree = {}

    maxInfo: Info = Info("$", 0, 0, "$", "$")
    col: int
    for col in range(0, len(mat[0]) - 1):

        if tree.__contains__(mat[0][col]):
            continue

        groups = {}
        row: int
        length: int = len(mat)
        for row in range(1, length):
            value: str = mat[row][col]
            lastInRow: str = mat[row][-1]

            resultInfo: ResultInfo = ResultInfo(value, lastInRow)

            if groups.__contains__(resultInfo.__str__()):
                groups[resultInfo.__str__()] = groups[resultInfo.__str__()] + 1
            else:
                groups[resultInfo.__str__()] = 1

        sum: float = 0
        branches = []
        subtree[mat[0][col]+"#"+depth.__str__()] = branches
        closedBranches = {}

        for group in groups:
            name = group.split("|")[0]

            if not (closedBranches.__contains__(name)):
                closedBranches[name] = 0
            else:
                continue

            sumResults: int = 0  # collect sumations of yes/no
            for resultRow in result:
                if groups.__contains__(name + "|" + resultRow):
                    sumResults += groups[name + "|" + resultRow]

            hSum: float = 0

            info: Info = Info(name+"#"+depth.__str__(), 0, col, "$", "$")

            first = True
            mostCom : CommonValue
            for resultRow in result:
                if groups.__contains__(name + "|" + resultRow):
                    division: float = groups[name + "|" + resultRow] / sumResults

                    if first:
                        first = False
                        mostCom = CommonValue(resultRow, groups[name + "|" + resultRow])
                    elif mostCom.value < groups[name + "|" + resultRow] or (mostCom.value == groups[name + "|" + resultRow] and mostCom.name > resultRow):
                        mostCom.name = resultRow
                        mostCom.value = groups[name + "|" + resultRow]

                    if division == 1:
                        info.next = resultRow
                    elif division != 0:
                        hSum += -1 * ((division) * math.log2(division))

            info.mostCommon = mostCom.name
            branches.append(info)

            hCat = (sumResults / (length - 1)) * (hSum)

            sum = sum + hCat

        gain = h - sum

        if maxInfo.value == gain and maxInfo.name >= mat[0][col]:
            maxInfo.name = mat[0][col]+"#"+depth.__str__()
            maxInfo.value = gain

        elif maxInfo.value < gain:  # todo not sorted alphabecitcly
            maxInfo.name = mat[0][col]+"#"+depth.__str__()
            maxInfo.value = gain

    branch: Info

    if cutOff != None and cutOff == '0':
        tempInfo = []
        tempInfo.append(Info("%#%", maxInfo.value,col, mostSignificant.name))
        tree[maxInfo.name] = tempInfo
        return

    if maxInfo.name == "$":
        return

    tree[maxInfo.name] = subtree[maxInfo.name]

    if branch != "$":
        tree[branch] = maxInfo.name

    for branch in subtree[maxInfo.name]:
        if branch.next == "$":
            newMatrice = []
            newMatrice.append(mat[0])
            iCounter: int
            for iCounter in range(1, len(mat)):
                if mat[iCounter][branch.col] == branch.name.split("#")[0]:
                    newMatrice.append(mat[iCounter])

            if newMatrice == []:
                return
            depth += 1
            highestGainCalculator(newMatrice, branch.name, depth)


def rekTree(start : str, depth, output: str):
    subTree = tree[start]

    if cutOff != None and cutOff == '0':
        output+= mostSignificant.name
        print(output)
        return
    output += depth.__str__() + ":" + start.split("#")[0] + "="

    node: Info
    for node in subTree:

        if cutOff != None and int(cutOff) == depth:
            output += node.name.split("#")[0] + " " + node.mostCommon
            print(output)

            if node.next == "$":
                node.next = node.mostCommon
            output = output[:output.rfind("=") + 1]
            continue

        if node.next == "$":
            connector = tree[node.name]
            output += node.name.split("#")[0] + " "
            rekTree(connector, depth + 1, output)
            output = output[:output.rfind("=") + 1]
        else:

            endValue: str = node.next

            print(output + node.name.split("#")[0] + " " + endValue)
    return


testPredictions = []


def rekTest(start, selectedCollumn, treeResult, currentRowInTest):
    global testPredictions, collumnMapper

    sub: Info
    for sub in treeResult:
        if sub.name.split("#")[0] == start and sub.next == "$":
            subTree = tree[start +"#" + sub.name.split("#")[1]]
            reselectCollumn = collumnMapper[subTree.split("#")[0]]
            changeStart = currentRowInTest[reselectCollumn]
            next = tree[subTree]
            rekTest(changeStart, reselectCollumn, next, currentRowInTest)
            return
        elif currentRowInTest.__contains__(sub.name.split("#")[0]):

            if currentRowInTest[selectedCollumn] == sub.name.split("#")[0]:
                testPredictions.append(sub.next)
                return

    testPredictions.append(mostCommon.name)
    return


trainOutput = []
collumnMapper = {}

def firstCollumnMapper(mat):
    global collumnMapper
    j: int
    for j in range(0, len(mat[0])):
        collumnMapper[mat[0][j]] = j


def predictions(mat, root : str):
    global trainOutput

    firstCollumnMapper(mat)

    selectedCollumn: int = collumnMapper[root.split("#")[0]]

    for row in mat[1:]:
        trainOutput.append(row[-1])
        rowSubClass = tree[firstElement]
        rekTest(row[selectedCollumn], selectedCollumn, rowSubClass, row)


def getAccuracyAndFillConfusionMatrix(confMat, testRows):
    i: int
    correct: int = 0
    for i in range(0, len(testPredictions)):
        if testPredictions[i] == trainOutput[i]:
            correct += 1
            point = confusionResultDict[testPredictions[i]]
            confMat[point][point] += 1
        else:
            rowActual = confusionResultDict[trainOutput[i]]
            colActual = confusionResultDict[testPredictions[i]]
            confMat[rowActual][colActual] += 1

    print("\n[ACCURACY]:", end=" ")
    print("{:.5f}".format(correct / testRows))
    print("\n[CONFUSION_MATRIX]:")

    r : list
    for r in confMat:
        print(" ".join(map(str, r)))


def matrixMapper():
    global confusionResultDict, confusionResultDictBack, confustionResultSet

    c: int = 0
    for res in sorted(confustionResultSet):
        confusionResultDict[res] = c
        confusionResultDictBack[c] = res
        c += 1


def confustionMatrix():
    mat = []
    i: int
    for i in range(0, len(confusionResultDict)):
        j: int
        row = []
        for j in range(0, len(confusionResultDict)):
            row.append(0)
        mat.append(row)

    return mat


matTrain = createMatrix(getSecondArg())
depth = 0
cutOff = getOptionalArg()

# get most common
tempCommon = []
for common in CommonValueDict:
    tempCommon.append(CommonValue(common, CommonValueDict[common]))

mostCommon : CommonValue
mostCommon = sorted(tempCommon)[0]
####
highestGainCalculator(matTrain, "$", depth)
print(tree)

firstElement = next(iter(tree))

print("[BRANCHES]:")
output = ""
rekTree(firstElement, 1, output)

matrixMapper()

print("\n[PREDICTIONS]:", end=" ")
matTest = createMatrix(getThirdArg())
predictions(matTest, firstElement)
print(" ".join(testPredictions))
testRows = len(matTest) - 1
totalRows += testRows
conf = confustionMatrix()
getAccuracyAndFillConfusionMatrix(conf, testRows)




