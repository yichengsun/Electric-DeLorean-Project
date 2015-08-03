dict = dict()

with open('ev_charger_pts') as file:
    while True:
        line = file.readline()
        if not line:
            break
        tagsPosition = line.find("tags")
        tagStart = tagsPosition + 9
        tagEnd = 0
        tagChars = 0
        tag = ""

        for char in line[tagStart:]:
            tagChars += 1
            if char is "'":
                tagEnd = tagStart + tagChars
                break
            else:
                tag += char
        tag = tag.replace(' ', '_')
        tag = tag.replace('(', '')
        tag = tag.replace(')', '')

        positionStart = tagEnd + 16
        xEnd = positionStart
        xCoord = ""
        yCoord = ""
        xCoordChars = 0
        for char in line[positionStart:]:
            xCoordChars += 1
            if char is ',':
                xEnd = positionStart + xCoordChars
                break
            else:
                xCoord += char

        for char in line[xEnd+1:]:
            if char is "'":
                break
            else:
                yCoord += char

        # print(''.join([tag, " ", xCoord, ", ", yCoord]))

        if tag in dict:
            dict[tag].append((xCoord, yCoord))
        else:
            dict[tag] = []
            dict[tag].append((xCoord, yCoord))

for tag in dict:
    size = len(dict[tag])
    count = 0
    print(''.join(['double[][] ', tag, ' = new double[][] {']))
    for item in dict[tag]:
        count += 1
        if count == size:
            print(''.join(['{ ', item[0], ', ', item[1], ' }']))
        else:
            print(''.join(['{ ', item[0], ', ', item[1], ' },']))
    print('};\n')




