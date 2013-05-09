from __future__ import print_function
from itertools import islice

import urllib
import random
import os

def main(argv=None):
    if argv is None:
        argv = sys.argv


def buildModel(typeId, trainSet, destDir, posNum, negNum):
    posFile = os.path.join(trainSet, urllib.quote_plus(typeId) + ".txt")
    print("Reading positive examples from file: \"" + posFile + "\"... ", end="")
    posExamples = readlines(posFile, posNum)
    print("Done.")

    negExamples = []
    for filename in os.listdir(trainSet):
        print("NotableTypeId: " + urllib.unquote(filename)[:-4])
        if not filename.endswith("-types.txt") and \
           urllib.unquote(filename)[:-4] != typeId:
            filepath = os.path.join(trainSet, filename)
            print("Reading file: \"" +  filepath + "... ", end="")

            # Read negative examples for each class
            examples = []
            with open(filepath) as fin:
                examples += ["-1" + example[2:] for example in fin.readlines()]
            print("Done.")
            typesFile = os.path.join(trainSet, filename[:-4] + "-types.txt")
            print("Reading types file: \"" + typesFile + "... ", end="")
            typesPerExample = []
            with open(typesFile) as fin:
                typesPerExample += fin.readlines()
            print("Done.")
            negExamplesPerClass = [examples[i] for i, example in enumerate(examples) if typeId not in typesPerExample[i].split('\t')]
            negExamples += negExamplesPerClass
    print("Read %s negative examples." % (len(negExamples)))
    random.shuffle(negExamples)

    # Write model file
    if not os.path.exists(destDir):
        print("Creating directory: \"" + destDir + "\"... ", end="")
        os.makedirs(destDir)
        print("Done.")
    modelFile = os.path.join(destDir, urllib.quote_plus(typeId) + ".txt")
    with open(modelFile, 'w') as fin:
        for example in posExamples[:posNum]: fin.write(example)
        for example in negExamples[:negNum]: fin.write(example)

def readlines(filepath, n):
    with open(filepath) as fin:
        lines= list(islice(fin, n))
    return lines


        
            

    
