from __future__ import print_function

from random import shuffle

import os
import sys
import shutil
import urllib2
import argparse


def split_dataset(data, s, j, dest):
    s = map(float, s.split('/'))
    j = float(j)

    print("splits: %s" % (s, ))

    for name in ["train", "devTest", "test"]:
        dataset = os.path.join(dest, name)

        if os.path.isdir(dataset):
            print("removing dir: %s... " % (dataset, ), end="")
            filepath = os.path.join(dataset, "test.tsv")
            if os.path.isfile(filepath):
                print("removing file: %s... " % (filepath, ), end="")
            try:
                shutil.rmtree(dataset)
                print("done")
            except OSError:
                print("fail")
        print("creating dir: %s... " % (dataset, ), end="")
        try:
            os.makedirs(dataset)
            print("done")
        except OSError:
            print("fail")
            sys.exit(-1)

    train = os.path.join(dest, "train")
    devTest = os.path.join(dest, "devTest")
    test = os.path.join(dest, "test")

    testExamples = []
    devTestExamples = []

    dirnames = [filename for filename in os.listdir(data) if "%2F" in filename]

    for dirname in dirnames:
        dirpath = os.path.join(data, dirname)

        className = urllib2.unquote(os.path.splitext(dirname)[0])

        dirpath = os.path.join(data, dirname)
        posExNum = count_files(os.path.join(dirpath, "pos", "dat"))


        """ Generate training files """
        trainPosExNum = int(s[0] * posExNum)        

        print("%s pos examples: %s" % (className, posExNum))
        print("%s train examples: %s" % (className, trainPosExNum))
            
        trainNegExNum = int(trainPosExNum / j)
        print("%s train neg examples: %s" % (className, trainNegExNum))

        #@TODO: code to generate train file (.txt) for Naive-Bayes classifier
        pos = os.path.join(dirpath, "pos", "dat")
        neg = os.path.join(dirpath, "neg", "dat")

        print("pos: %s" % (pos, ))
        print("neg: %s" % (neg, ))

        posExamples = sorted([os.path.join(pos, strip_ext(filename)) for
            filename in os.listdir(pos)])
        negExamples = [os.path.join(neg, strip_ext(filename)) for 
            filename in os.listdir(neg)]
        shuffle(negExamples)

        #print("posExamples: %s" % (posExamples, ))
        #print("negExamples: %s" % (negExamples, ))
        
        examples = posExamples[:trainPosExNum] + negExamples[:trainNegExNum]
        examples = map(lambda e: e + ".dat", examples)

        train_file = os.path.join(train, dirname + ".dat")
        write_file(examples, train_file, False)

        """ Generate devTest and test file """

       
        devTestExamples = []
        testExamples = []
        for dirname2 in dirnames:
            klass = urllib2.unquote(dirname2)
            
            klass_pos = os.path.join(data, dirname2, "pos", "dat")
            print("%s pos: %s" % (klass, klass_pos))

            posExamplesPerKlass = sorted([os.path.join(klass_pos, strip_ext(filename))
                for filename in os.listdir(klass_pos)])
            posExamplesPerKlassNum = len(posExamplesPerKlass)
            
            devTestExNum = int(s[1] * posExamplesPerKlassNum)
            
            end = trainPosExNum + devTestExNum
            devTestExamples.extend(posExamplesPerKlass[trainPosExNum:end])
            testExamples.extend(posExamplesPerKlass[end:])
     

        devTest_file = os.path.join(devTest, dirname + ".dat")
        test_file = os.path.join(test, dirname +  ".dat")

        write_file(map(lambda e: e + ".dat",  devTestExamples), devTest_file,
                test=True)

        write_file(map(lambda e: e + ".dat", testExamples), test_file,
                test=True)

    # devTest_file = os.path.join(devTest, dirname + ".tsv")
    test_file = os.path.join(test, "test.tsv")
    devTest_file = os.path.join(devTest, "devTest.tsv")
    
    classes_files = []
    devTest_file = os.path.join(dest, "test.devTest.tsv")
    for filepath in devTestExamples:
        filename, ext = os.path.splitext(filepath)
        fn =  os.path.basename(filename)

        parts = filepath.split(os.sep)
        classes_file = os.sep.join(parts[:-3]) + os.sep + "pos" + os.sep + \
                       "tsv" + os.sep + fn + ".tsv"
        classes_files.append(classes_file)
    write_file(classes_files, devTest_file)


    classes_files = []
    test_file = os.path.join(dest, "test.test.tsv")
    for filepath in testExamples:
        filename, ext = os.path.splitext(filepath)
        fn = os.path.basename(filename)

        parts = filepath.split(os.sep)

        classes_file = os.sep.join(parts[:-3]) + os.sep + "pos" + os.sep + \
                           "tsv" + os.sep + fn + ".tsv"
        classes_files.append(classes_file)
    write_file(classes_files, test_file)
    
    classNames = [urllib2.unquote(os.path.splitext(filename)[0]) for filename in
            os.listdir(data) if "%2F" in filename]
    print("classNames: %s" % (classNames, ))

    for name in ["devTest", "test"]:
        gold_file = os.path.join(dest, "gold" + "." + name + ".tsv")
        test_file = os.path.join(dest, "test" + "." + name + ".tsv")
        if os.path.isfile(gold_file):
            print("removing file: %s... " % (gold_file, ), end="")
            try:
                os.remove(gold_file)
                print("done")
            except OSError:
                print("fail")
                sys.exit(-1)

        print("creating file: %s..." % (test_file, ), end="")
        try:
            write_gold_file(classNames, test_file, gold_file)
            print("done")
        except OSError:
            print("fail")
            sys.exit(-1)

    
        
    
    
    

def write_gold_file(classNames, test_file, output_file):
    classNames = sorted(classNames)
    with open(output_file, 'w') as out:
        out.write('\t'.join(classNames) + '\n')

        with open(test_file) as f:
            for line in f:
                bits = []
                tclasses = line.strip().split('\t')

                for className in classNames:
                    if className in tclasses:
                        bits.append(str(1))
                    else:
                        bits.append(str(0))
                out.write('\t'.join(bits) + '\n')
                 

    """
    files = []
    filenames = sorted(os.listdir(data))
    classNames= urllib2.unquote(os.path.splitext(filename)[0] for filename in
            filenames]
    
    filepaths = [os.path.join(data, filename) for filename in filenames]
    files = [open(filepath) for filepath in filepaths]

    linesNum = len([line for line in files[0]])
    files[0].seek()
    with open(output_file, 'w') as out:
        out.write('\t'.join(filenames)) 
    
        lines = []
        for n in range(linesNum):
            for f in files:
                f.readline()
    """



            



    """ 
       for filepath in testExamples:
            filename, ext = os.path.splitext(filepath)
            fn = os.path.basename(filename)

            parts = filepath.split(os.sep)

            classes_file = os.sep.join(parts[:-3]) + os.sep + "pos" + os.sep + \
                            "tsv" + os.sep + fn + ".tsv"
            classes_files.append(classes_file)
        write_file(classes_files, test_file)
    """                     
        
def write_file(examples, out_file, test=False):
    if test:
        merge_files2(examples, out_file)
    else:
        merge_files(examples, out_file)


def merge_files(filepaths, out_file):
    with open(out_file, 'w') as out:
        for filepath in filepaths:
            out.write(read(filepath))


def merge_files2(filepaths, out_file):
    filename, ext = os.path.splitext(out_file)
    fn = os.path.basename(filename)

    className = urllib2.unquote(fn)
    #print("className: %s" % (className, ))

    with open(out_file, 'w') as out:

        for filepath in filepaths:
            #print("filepath: %s" % (filepath, ))
            example = read(filepath)

            parts = filepath.split(os.sep) 
            #print("parts: %s" % (parts, ))
       
            fn, ext = os.path.splitext(parts[-1])
            #print("fn, ext = %s, %s" % (fn, ext))

            clss_file = os.sep.join(parts[:-2]) + os.sep + "tsv" + os.sep + fn + ".tsv"
            if className not in read(clss_file):
                example = "-1" + example[2:]
            else:
                example + "+1" + example[2:]

            #print("types file: %s" % (types))

            out.write(example)
        """ 
            className = urllib2.unquote(os.path.split(out_file)[-2])
            
            fp, ext = os.path.splitext(filepath)
            print("fp: %s" % (fp, ))
            fn = os.path.basename(fp)
            parts = filepath.split(os.sep)
            class_file = os.sep.join(parts[:4]) + os.sep + \
            urllib2.quote(className, "") + "pos" + os.sep + "tsv" + os.sep + fn + ".tsv"

            print("className: %s, class_file: %s" % (className, class_file))
        
            if className not in read(class_file):
                example = "-1" + example[2:]
            out.write(example)
         """


def read(filepath):
    txt = ""
    with open(filepath) as f:
        txt = f.read()
    return txt
        

def strip_ext(filename):
    return os.path.splitext(filename)[0]
        



        



def count_files(dirpath):
    return len([filename for filename in os.listdir(dirpath)])



if __name__ == "__main__":
    parser = argparse.ArgumentParser("""split a dataste into train, devTest and 
    test sets.""")
    parser.add_argument("data", help="the data to split")
    parser.add_argument("s", help="""the
    train/devTest/test ratio""")
    parser.add_argument("j", help="the pos/neg ratio")
    parser.add_argument("dest", help="the generated datasets save path")
    args = parser.parse_args()
    print(args, )

    split_dataset(args.data, args.s, args.j, args.dest)

    
    
    
