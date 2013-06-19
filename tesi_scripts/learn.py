from __future__ import print_function
from subprocess import call


import os
import shutil
import argparse


svm_learn = "SVM-Light-1.5-rer/svm_learn"

def learn(train, models, params):
    global svm_learn
    if os.path.isdir(models):
        print("removing dir: %s... " % (models, ), end="")
        try:
            shutil.rmtree(models)
            print("done")
        except OSError:
            print("fail")

    print("creating dir: %s... " % (models, ), end="")
    try:
        os.makedirs(models)
    except OSError as e:
        print("fail")
        print("OS error({0}): {1}".format(e.errno, e.strerror))
        sys.exit(-1)

    for filename in os.listdir(train):
        train_file = os.path.join(train, filename)
        model_file = os.path.join(models, filename)
        cmd = "%s %s %s %s" % (svm_learn, params, train_file, model_file)
        print(cmd)
        call(cmd, shell=True)
             

if __name__ == "__main__":
    parser = argparse.ArgumentParser("learn models for different classes")
    parser.add_argument("train", help="the train set")
    parser.add_argument("models", help="the models dir")
    parser.add_argument("params", help="the learn parameters")
    args = parser.parse_args()

    if os.path.isdir(args.train):
        learn(args.train, args.models, args.params)
    else:
        print("dir not found: %s" % (args.train))
