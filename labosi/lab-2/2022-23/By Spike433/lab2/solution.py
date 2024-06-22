import sys

#args = ['.\\solution.py', 'resolution', 'new_example_6.txt']


#args = ['.\\solution.py', 'cooking', 'cooking_coffee.txt','cooking_coffee_input.txt']
args = sys.argv


def readFromFile(path):
    file1 = open(path, 'r', encoding='utf-8')
    lines = file1.readlines()
    file1.close()
    return lines


def getSecondArg():
    return args[2]


def getThirdArg():
    return args[3]


class Mark:
    def __init__(self, name: str, position: int):
        self.name = name
        self.position = position

    def invert(self):
        if self.name.__contains__("~"):
            self.name = self.name.replace("~", "")
        else:
            self.name = "~" + self.name

    def __str__(self):
        return self.name

    def __repr__(self):
        return self.name + " " + self.position.__str__()

    def __eq__(self, other):
        return self.name == other.name


result: str = "[CONCLUSION]: "
claussesInput = readFromFile(getSecondArg())
claussseFormated = []
lineCounter: int = 0
unModifiedList: list = []
thresold: int = 0
lastItem: Mark


def formatResolutionInput():
    global claussesList, lineCounter, thresold

    thresold = len(claussesInput)

    for line in claussesInput:
        if line.startswith("#"):
            continue
        else:
            lineCounter = lineCounter + 1
            stripAndLower = line.strip().lower()

            markParser(stripAndLower)


def markParser(stripAndLower):
    global claussseFormated, lineCounter

    marksTempList = []
    if stripAndLower.__contains__(" v "):
        marks = stripAndLower.split(" v ")

        for mark in marks:
            markClass = Mark(mark.strip(), lineCounter)
            marksTempList.append((markClass))
        claussseFormated.append(marksTempList)
        unModifiedList.append(marksTempList)
    else:
        markClass = Mark(stripAndLower, lineCounter)
        marksTempList.append(markClass)
        claussseFormated.append(marksTempList)
        unModifiedList.append(marksTempList)


def resolution():
    global result, claussseFormated, lineCounter

    lastItemList: list = []
    lastItemList.extend(claussseFormated[-1])
    claussseFormated.pop()

    collectNames = []
    for name in lastItemList:
        collectNames.append(name.name)
    result = result + " v ".join(collectNames)

    mark: Mark
    for mark in lastItemList:
        inverted = Mark(mark.name, lineCounter)
        inverted.invert()
        tempList = []
        tempList.append(inverted)
        claussseFormated.append(tempList)
        lineCounter = lineCounter + 1

    print(claussseFormated)
    length: int = len(claussseFormated)
    i: int = 0

    try:
        while i < length:
            currentList: list = claussseFormated[i]

            item: Mark
            for item in currentList:

                if item.name.__contains__("$"):
                    continue

                j = 0
                while j < i:

                    searchList: list = []
                    searchList.extend(claussseFormated[j])

                    inverted = Mark(item.name, item.position)
                    inverted.invert()

                    if searchList.__contains__(inverted):
                        searchList.remove(inverted)
                        extendetList: list = []
                        extendetList.extend(currentList)
                        extendetList.remove(item)
                        if extendetList == []:
                            pos: str = str(j + 1) + " " + str(i + 1)
                            searchList.append(Mark("$", pos))
                        elif searchList == []:
                            searchList.extend(extendetList)
                            pos: str = str(j + 1) + " " + str(i + 1)
                            searchList.append(Mark("$", pos))
                        else:
                            pos: str = str(j + 1) + " " + str(i + 1)
                            extendetList.append(Mark("$", pos))
                            searchList.extend(extendetList)

                        iteminSearch: Mark

                        # remove duplicates --> ~a v b ~a --> ~a v b
                        iSearch: int = 0
                        while iSearch < len(searchList):
                            iSecond = iSearch + 1
                            while iSecond < len(searchList):
                                if searchList[iSearch].name == searchList[iSecond].name:
                                    searchList.pop(iSecond)
                                    break
                                iSecond = iSecond + 1
                            iSearch = iSearch + 1

                        found: bool = True
                        for test in searchList:
                            if not (test.name in ("$")):
                                found = False
                                break
                        if found:
                            formatOutput(searchList)
                            return True

                        if checkTaut(searchList):
                            j = j + 1
                            continue

                        if checkRedundancy(searchList):
                            j = j + 1
                            continue

                        claussseFormated.append(searchList)

                        length = length + 1

                    j = j + 1
            i = i + 1

    except:
        return False


def checkRedundancy(searchList):
    for list in claussseFormated:
        reduce: bool = True

        element: Mark
        for element in list:
            if element.name.__contains__("$"):
                continue

            if not (searchList.__contains__(element)):
                reduce = False
                break

        if reduce:
            return True

    return False


def checkTaut(searchList):
    for iFirst in range(0, len(searchList)):
        invertMark: Mark = Mark(searchList[iFirst].name, searchList[iFirst].position)
        invertMark.invert()
        for iSecond in range(iFirst + 1, len(searchList)):
            secondMark: Mark = searchList[iSecond]
            if invertMark == secondMark:
                return True
        invertMark.invert()
    return False


def formatOutput(searchList):
    print("==================")

    mark: Mark

    print(searchList)

    for mark in searchList:
        print("current mark: "+mark.__repr__())
        rek(mark)


def rek(mark):
    if mark.name == "$":
        first: int = int(mark.position.split(" ")[0])
        second: int = int(mark.position.split(" ")[1])

        if first > thresold:
            i : Mark
            for i in claussseFormated[first-1]:
                if i.name != "$":
                    continue
                print("current mark in rek(): " + i.__repr__())
                rek(i)
                if mark.name == "$":
                    second: int = int(mark.position.split(" ")[1])
                    if second > thresold:
                        pass
                    else:
                        return
        if second > thresold:
            j: Mark
            for j in claussseFormated[second - 1]:
                if j.name != "$":
                    continue
                print("current mark in rek(): " + j.__repr__())
                rek(j)
                return

        print("values below threshold")
        print(claussseFormated[first - 1])
        print(claussseFormated[second - 1])
        print("*******************")
        return


def getResolutionResult():
    global result
    if resolution():
        print(result + " is true")
    else:
        print(result + " is unknown")


if args.__contains__("resolution"):
    formatResolutionInput()
    getResolutionResult()


def removeIfExists():
    global claussseFormated

    marksTempList = []
    if command.__contains__(" v "):
        marks = command.split(" v ")

        for mark in marks:
            markClass = Mark(mark.strip(), 0)
            marksTempList.append((markClass))

    singleList: list

    removalIndex: int = 0

    if marksTempList == []:
        for singleList in claussseFormated:
            item: Mark
            for item in singleList:
                if item.name == command:
                    if len(singleList) == 1:
                        claussseFormated.pop(removalIndex)
                    else:
                        singleList.remove(item)
                    return True
            removalIndex = removalIndex + 1
    else:
        marksStr: list = []

        for m in marksTempList:
            marksStr.append(m.name)

        endMark: str = " v ".join(marksStr)

        indexJ: int = 0
        for singleList in claussseFormated:
            t: list = []
            for s in singleList:
                t.append(s.name)
            tStr: str = " v ".join(t)

            if endMark == tStr:
                claussseFormated.pop(indexJ)
                return True
            indexJ = indexJ + 1

    return False


if args.__contains__("cooking"):

    formatResolutionInput()
    userCommands = readFromFile(getThirdArg())

    command: str
    for command in userCommands:

        if command.__contains__("?"):
            command = command.replace("?", "").strip().lower()

            tempList: list = []
            tempList.extend(claussseFormated)

            lineCounter = lineCounter + 1
            thresold = thresold + 1
            result: str = "[CONCLUSION]: "

            markParser(command)

            getResolutionResult()

            claussseFormated.clear()
            claussseFormated.extend(tempList)
        elif command.__contains__("+"):
            command = command.replace("+", "").strip().lower()
            lineCounter = lineCounter + 1
            thresold = thresold + 1
            markParser(command)
        elif command.__contains__("-"):
            command = command.replace("-", "").strip().lower()
            if removeIfExists():
                thresold = thresold - 1
