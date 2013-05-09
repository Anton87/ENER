from __future__ import print_function

import sys
import os

def main(argv=None):
    if argv is None:
        argv = sys.argv
    if (len(argv) < 5):
        usage()
        sys.exit(0)
    prog, dataSet, trainSet, testSet, n = argv
    
    print("dataSet: \"" + dataSet + "\".")
    print("trainSet: \"" + trainSet + "\".")
    print("testSet: \"" + testSet + "\".")

    if not os.path.exists(trainSet): os.makedirs(trainSet)
    if not os.path.exists(testSet): os.makedirs(testSet)
    
    for filename in os.listdir(dataSet):
        dataFile = os.path.join(dataSet, filename) 
        trainFile = os.path.join(trainSet, filename)
        testFile = os.path.join(testSet, filename)

        copyFirstNLines(dataFile, trainFile, int(n))
        copyLinesFromLineN(dataFile, testFile, int(n))
        
def copyFirstNLines(srcFile, destFile, n):
    print("Copying first %s lines from \"%s,\" to \"%s\"... " % (n, srcFile, destFile), end="")
    with open(srcFile) as fin:
        fout = open(destFile, 'w')
        lines = fin.readlines()
        for line in lines[0:n]:
            fout.write(line)
    fout.close()
    print("Done.")
       
def copyLinesFromLineN(srcFile, destFile, n):
    print("Copying from line n.%s from \"%s\" to \"%s\"... " % (n, srcFile, destFile), end="")
    with open(srcFile) as fin:
        fout = open(destFile, 'w')
        lines = fin.readlines()
    for line in lines[n:]:
        fout.write(line)
    fout.close()    
    print("Done.")

def usage():
    print("Usage: python split-daset.py dataSet trainSet testSet linesNum")
        
if __name__ == "__main__":
    sys.exit(main())


