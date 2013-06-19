""" classify a set of files and ouput a prediction file. """
from __future__ import print_function
from subprocess import call


import os
import sys
import shutil
import urllib2
import argparse

svm_classify = "SVM-Light-1.5-rer/svm_classify"

def get_threshold(model_file):
    threshold = .0
    with open(model_file) as f:
        for line in f.readlines():
            if "# threshold b" in line:
                threshold = float(line[:line.index(' ')])
                break            
    return threshold


def output_prediction(svm_predictions, pred_file, thresholdByClass):
    classNames = sorted([urllib2.unquote(os.path.splitext(filename)[0]) for
        filename in os.listdir(svm_predictions)])

    print("classNames: %s" % (classNames, ))
    files = {}
    for filename in os.listdir(svm_predictions):
        filepath = os.path.join(svm_predictions, filename)
        className = urllib2.unquote(os.path.splitext(filename)[0])
        f = open(filepath)
        files[className] = f

    f = files.values()[0]
    linesNum = len(f.readlines())
    f.seek(0)

    with open(pred_file, 'w') as out:
        out.write('\t'.join(classNames) + '\n')

        for i in range(linesNum):
            bits = []
            for className in classNames:
                score = float(files[className].readline().strip())
                
                #if score > thresholdByClass[className]:
                if score > 0:
                    bits.append(str(1))
                else:
                    bits.append(str(0))
            out.write('\t'.join(bits) + '\n')


def classify(models, test, predictions): 
    global svm_classify

    if os.path.isdir(predictions):
        print("removing dir: %s... " % (predictions, ), end="")
        shutil.rmtree(predictions)
        print("done")

    print("creating dir: %s... " % (predictions, ), end="")
    try:
        os.makedirs(predictions)
        print("done")
    except OSError as e:
        print("fail")
        print("I/O error({0}): {1}".format(e.errno, e.strerror))
        sys.exit(-1) 
   
    for filename in os.listdir(models):
        model_file = os.path.join(models, filename)
        example_file = os.path.join(test, filename)
        output_file = os.path.join(predictions, filename) 

        print("className: " + urllib2.unquote(os.path.splitext(filename)[0]))
        call(svm_classify + " " + example_file + " " + model_file + " " + \
                output_file,  shell=True)
        print("") 
    

if __name__ == "__main__":

    parser = argparse.ArgumentParser("classify all test files and output the \
                                      prediction file")
    parser.add_argument("models", help="the models")
    parser.add_argument("test", help="the test files")
    parser.add_argument("svm_predictions", help="the svm predictions files")
    parser.add_argument("pred_file", help="the prediction file")
    args = parser.parse_args()

    if not os.path.isdir(args.models):
        print("dir not found: %s" % (args.models, ))
        sys.exit(0)

    if not os.path.exists(args.test):
        print("dir not found: %s" % (args.test, ))
        sys.exit(0)

    classify(args.models, args.test, args.svm_predictions)

    thresholdByClass = {}
    for filename in os.listdir(args.models):
        fn, ext = os.path.splitext(filename)
        className = urllib2.unquote(fn) 
        model_file = os.path.join(args.models, filename)
        threshold = get_threshold(model_file) 
        thresholdByClass[className] = threshold

        print("class: %s, threshold: %s" % (className, threshold))
    output_prediction(args.svm_predictions, args.pred_file, thresholdByClass)
    
