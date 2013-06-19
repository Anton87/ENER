"""Split the wikipedia fetched data in train and test set."""

from __future__ import print_function

import os
import sys
import shutil
import argparse


def split_fetched_data(fetched_data, dest, test_lines):
    """Split the wikipedia train and test files.

       Keyword arguments:
       fetched_data -- the wikipedia fetched data dir
       dest -- the dest dir
       n -- the test file lines number

    """
    train = os.path.join(dest, 'train')
    test  = os.path.join(dest, 'test')

    # Remove the train dir
    if os.path.isdir(train):
        print('removing dir: %s... ', end='')
        #shutil.rmtree(train)
        print('done')

    # Create the train dir
    if not os.path.isdir(train):
        print('creating dir: %s... ', end='')
        os.makedirs(train)
        print('done')

    # Remove the test dir
    if os.path.isdir(test): 
        print('removing dir: %s... ', end='')
        shutil.rmtree(test)
        print('done')

    # Create the test dir
    if not os.path.isdir(test):
        print('creating dir: %s... ', end='')
        os.makedirs(test)
        print('done')
    
    # Iterate over data_fetched files
    for filename in os.listdir(fetched_data):
        if filename.endswith('.tsv'):
            filepath = os.path.join(fetched_date, dirname)

            # Split the data lines
            print('splitting lines in dir: %s... ', end='')
            with open(filepath) as source_file:
                lines = [line for line in source_file]
                train_filepath = os.path.join(train, filename)
                test_filepath  = os.path.join(test,  filename)

                train_file = open(train_filepath, 'w')
                test_file  = open(test_filepath, 'w')

                # Compute the number of lines in train set
                train_lines = len(lines) - test_lines 

                for lineNum, line in enumerate(source_file):
                    if lineNum < train_lines: 
                        train_file.write(line)
                    else:
                        test_file.write(line)
            train.close()
            test.close()
            print('done')
    

if __name__ == '__main__':
    parser = argparse.ArgumentParser('split the wikipedia fetched dataset \
    in train and test set.')
    parser.add_argument('src', help='')
    parser.add_argument('fetched_data', help='the wikipedia fetched data')
    parser.add_argument('dest', help='the split dest')
    parser.add_argument('test_lines' help='the test files lines number')
    args = parser.parse_args()

    # split the fetched dataset
    split_fetched_data(args.fetched_data, args.dest, args.test_lines)
