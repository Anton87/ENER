"""This module splits a set of files into pos and neg exmaples."""

from __future__ import print_function

import os
import sys
import shutil
import urllib2
import argparse


def fetch_all_negatives(data):
    """Featch the negative examples for all categories.

       Keyword arguments:
       data -- the data dir

    """
    for dirname in os.listdir(data):
        className = urllib2.unquote(dirname)
        dest_dirpath = os.path.join(data, dirname, 'train')
        print('fetching negative examples for class: %s... ' % (className), end="")
        fetch_negatives_for_class(className, data, dest_dirpath)
        print('done')


def fetch_negatives_for_class(className, src, dest):
    """Fetch the negative examples for the sepecified category.
    
       Keyword arguments:
       className -- the category className
       src -- the src dir
       dest -- the dest dir

    """
    dest_dat_file = open(os.path.join(dest, 'negative.dat'), 'w')
    dest_tsv_file = open(os.path.join(dest, 'negative.tsv'), 'w')


    # Holds positive train .dat files by className
    class2dat = {} 
    # Holds positive train .tsv files by className 
    class2tsv = {}

    fileset = set()
    
    for dirname in os.listdir(src):
        klass = urllib2.unquote(dirname)
        if klass != className:
            class2dat[klass] = open(os.path.join(src, dirname, 'train', dirname +
                '.dat'))
            class2tsv[klass] = open(os.path.join(src, dirname, 'train', dirname +
                '.tsv'))

    while len(fileset) != len(class2dat):
        for klass in class2dat:
            dat_file = class2dat[klass] 
            tsv_file = class2tsv[klass]

            example = dat_file.readline()
            types = tsv_file.readline()

            if not example: 
                fileset.add(klass)
                continue

            elif className not in types:
                dest_dat_file.write('-1' + example[2:])
                dest_tsv_file.write(types)
                continue
                
    for klass in class2dat:
        class2dat[klass].close()
        class2tsv[klass].close()

    dest_dat_file.close()
    dest_tsv_file.close()
   

def write_test_files(src, dest, gold_file):
    """Write the test files for the classes in src.

       Keyword arguments:
       src -- the src dir
       dest -- the dest dir
       golf_file -- the gold file name

    """    
    testSet = os.path.basename(dest)

    if os.path.isfile(gold_file):
        print('removing file: %s... ' % (gold_file), end='')
        os.remove(gold_file)
    print('done')

    g = open(gold_file, 'w')

    # Remove the test dir
    if os.path.isdir(dest):
        print('removing dir: %s... ' % (dest), end='')
        shutil.rmtree(dest)
        print('done')
    print('creating dir: %s... ' % (dest), end='')
    os.makedirs(dest)
    print('done')


    out_files = {}
            
    
    dirnames = sorted([dirname for dirname in os.listdir(src)])

    print("dirnames: %s" % (dirnames))
    klasses  = [urllib2.unquote(dirname) for dirname in dirnames]
    g.write('\t'.join(klasses) + '\n')
    for dirname in dirnames:
        klass = urllib2.unquote(dirname)
        dirpath = os.path.join(src, dirname)
        
        out = open(os.path.join(dest, dirname + '.dat'), 'w')
        out_files[klass] = out

    for klass in klasses:
        dirname = urllib2.quote(klass, '')
        e = open(os.path.join(src, dirname, testSet, dirname + '.dat'))
        t = open(os.path.join(src, dirname, testSet, dirname + '.tsv'))

        
        
        for example, types in zip(e, t):
            bits = []
            for otherKlass in klasses:
                bit = ['0', '1'][otherKlass in types]
                bits.append(bit)

                label = '+1' if otherKlass in types else '-1'
                example = label + example[2:]
                out_files[otherKlass].write(example)
            g.write('\t'.join(bits) + '\n')

        e.close()
        t.close()
    g.close()
    for out in out_files.values(): out.close()


def write_train_files2(src, dest, g):
    """Write training files mixing positive and negative
       examples according to the g ratio.

       Positive examples will be draw not only from the pool of positives
       examples retrieved for the specified class, but also from positive
       examples appearin gin the train set of the other classes.

       Keyword arguments:
       src -- the src dir
       dest -- the dest dir
       g  -- the pos/neg ratio

    """
    g = float(g)

    # Remove the train files
    if os.path.isdir(dest):
        print('removing dir: %s... ' % (dest), end='')
        shutil.rmtree(dest)
        print('done')

    # Create the train set
    print('creating dir: %s... ' % (dest), end='')
    os.makedirs('done')
    print('done')

    for dirname in os.listdir(src):
        dirpath = os.path.join(src, dirname)

        # The name of the positive train file.
        dest_file = os.path.join(dest, dirname + '.dat')
        out = open(dest_file, 'w')

        pos_examples_file = os.path.join(dirpath, 'train', dirname + '.dat')
        # Count the number of positive examples available for the class
        with open(pos_examples_file) as f:
            pos_examples_num = count([line for line in f])

        class2dat = {}
        class2tsv = {}

        fileset = set()

        for dirname2 in os.listdir(src):
            klass = urllib2.unquote(dirname2)
            class2dat[klass] = open(os.path.join(src, dirname2, 'train',
                dirname2 + '.dat'))
            class2tsv[klass] = open(os.path.join(src, dirname2, 'train',
                dirname2 + 'tsv'))
            
            num_examples = 0
            while num_examples < pos_examples_num and len(fileset) != len(class2dat):
                for klass in class2dat:
                    dat_file = class2dat[klass]
                    tsv_file = class2tsv[klass]

                    example = dat_file.readline()
                    types = tsv_file.readline()

                    if not example:
                        fileset.add(klass)
                        continue

                    elif className in types:
                        out.write('+1' + example[2:])
                        continue
            for klass in class2dat:
                class2dat[klass].close()
                class2tsv[klass].close()


            neg_examples_file = os.path.join(dirpath, 'train', 'negative.dat')
            f = open(neg_examples_file)
            for i, line in enumerate(f):
                if i >= linesNum: break
                out.write(line)
            f.close()

            out.close()


           
          

    
def write_train_files(src, dest, g):
    """Write training files mixing positive and negative
       examples according to the g ratio.

       src -- the src dir
       dest -- the dest dir
       g -- the pos/neg ratio

    """
    g = float(g)
    # Remove the train files
    if os.path.isdir(dest):
        print('removing dir: %s... ' % (dest), end='')
        shutil.rmtree(dest)
        print('done')
    # Create the train set
    print('creating dir: %s... ' % (dest), end='')
    os.makedirs(dest)
    print('done')

    

    for dirname in os.listdir(src):
        dirpath = os.path.join(src, dirname)

        dest_file = os.path.join(dest, dirname + '.dat')
        out = open(dest_file, 'w')

        pos_examples_file = os.path.join(dirpath, 'train', dirname + '.dat')
        f = open(pos_examples_file)

        for i, line in enumerate(f):
            out.write(line)
        f.close()

        linesNum = int((i + 1) / g)
        neg_examples_file = os.path.join(dirpath, 'train', 'negative.dat')

        f = open(neg_examples_file)
        for i, line in enumerate(f):
            if i >= linesNum: break
            out.write(line)
        f.close()
        
        out.close()


def split_files(src, dest, ratio):
    """Split training files in each category in train, devTest and test dataset.

       Keyword arguments:
       src -- the src dir
       dest -- the dest dir
       ratio -- the split ratio

    """
    ratio = map(float, ratio.split('/'))

    if os.path.isdir(dest):
        # Remove the dest dir
        print("removing dir: %s... " % (dest), end="")
        shutil.rmtree(dest)
        print("done")
    # Create the dest dir
    print("creating dir: %s... " % (dest), end="")
    os.makedirs(dest)
    print("done")    
    
    # Create train, devTest, test dataset
    for dirname in os.listdir(src):
        # Count number of lines in the src file
        linesNum = len([line for line in open(os.path.join(src, dirname,
            dirname + '.dat')).readlines()])
        #print("linesNum: %s" % (linesNum, ))
        start = 0
        for i, name in enumerate(['train', 'devTest', 'test']):
            # Compute the number of lines to copy
            fileLinesNum = int(ratio[i] * linesNum)
            dirpath = os.path.join(dest, dirname, name)
            print("creating dir: %s... " % (dirpath), end="")
            os.makedirs(dirpath)
            print('done')
            # Split .dat files
            src_file = os.path.join(src, dirname, dirname + '.dat')
            dest_file = os.path.join(dest, dirname, name, dirname + '.dat')
            print('copying lines [%s:%s] from: %s to %s... ' % (start, start +
                fileLinesNum, src_file, dest_file), end="")
            copy_lines(src_file, dest_file, start, start + fileLinesNum)
            print('done')
            # Split .tsv files
            src_file = os.path.join(src, dirname, dirname + '.tsv')
            dest_file = os.path.join(dest, dirname, name, dirname + '.tsv')
            print('copying lines [%s:%s] from: %s to %s... ' % (start, start +
                fileLinesNum, src_file, dest_file), end="")
            copy_lines(src_file, dest_file, start, start + fileLinesNum)
            print("done")
            start += fileLinesNum

               
def copy_lines(src_file, dest_file, line_start, line_end):
    """Copy the specified range of lines form src_file to dest_file.

       Keyword arguments:
       src_file -- the src file
       dest_file -- the dest file
       line_start -- the start copy line num
       line_end -- the end copy line num

    """
    with open(src_file) as f:
        with open(dest_file, 'w') as out:
            for line_num, line in enumerate(f):
                if line_start <= line_num < line_end:
                    out.write(line) 


    """
        src_dirpath = os.path.join(src, dirname)
        dest_dirpath = os.path.join(dest, dirname)
        # Create pos directory
        pos_dirpath = os.path.join(dest_dirpath, "pos")
        neg_dirpath = os.path.join(dest_dirpath, "neg")
        print("creating dir: %s... " % (pos_dirpath), end="")
        os.makedirs(pos_dirpath)
        print("done")
         # Create neg directory
        print("creating dir: %s... " % (neg_dirpath), end="")
        os.makedirs(neg_dirpath)
        print("done")
        # Copy file from src to dest dir as positives
        for filename in os.listdir(src_dirpath):
            src_file = os.path.join(src_dirpath, filename)
            dest_file = os.path.join(pos_dirpath, filename)
            print("copying file: %s to %s... " % (src_file, dest_file), end="")
            shutil.copyfile(src_file, dest_file)
            print("done")
    """

if __name__ == '__main__':
    parser = argparse.ArgumentParser('split a set of files in pos and neg \
    examples')
    parser.add_argument('src', help='the src dir')
    parser.add_argument('dest', help='the dest dir')
    
    parser.add_argument('ratio', help='the split ratio (e.g .7/.15/.15)')
    parser.add_argument('g', help='the pos/neg ratio')

    args = parser.parse_args()

    split_dirpath = os.path.join(args.dest, 'split')
    split_files(args.src, split_dirpath, args.ratio)


    fetch_all_negatives(split_dirpath)

    train = os.path.join(args.dest, 'train')

    
    devTest = os.path.join(args.dest, 'devTest')

    test = os.path.join(args.dest, 'test')
   
    write_test_files(split_dirpath, devTest, os.path.join(args.dest,
    'gold.devTest.tsv'))
    print('test files written in dir: %s' % (devTest))
    write_test_files(split_dirpath, test, os.path.join(args.dest,
    'gold.test.tsv'))
    print('test files written in dir: %s' % (test))


    write_train_files(split_dirpath, train, args.g)
    write_train_files(split_dirpath, train + '2', args.g)


