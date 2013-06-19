"""Clean the dataset by removing duplicates and filtering out long examples."""
from __future__ import print_function
from collections import defaultdict

import argparse
import shutil
import os

MAX_EXAMPLE_SIZE = 8006

def clean(src, dest):
    """Write a cleaned copy of the dataset.

       Keyword arguments:
       src -- the src dir
       dest -- the dest dir
    """
    # Remove the dest directory
    if os.path.isdir(dest):
        print('removing dir: %s... ' % (dest, ), end='')
        shutil.rmtree(dest)
        print('done')

    # Create the dest directory
    print('creating dir: %s... ' % (dest, ), end='')
    os.makedirs(dest)
    print('done')

    # Iterate over directories in src
    for dirname in os.listdir(src):
        dirpath = os.path.join(src, dirname)

        # Check that the .dat file exists
        example_file = os.path.join(dirpath, dirname + '.dat')
        if os.path.isfile(example_file):
            klasses_file = os.path.join(dirpath, dirname + '.tsv')
            
            dest_dirpath = os.path.join(dest, dirname)
            # Create dest dir
            print('creating dir: %s... ' % (dest_dirpath, ), end='')
            os.makedirs(dest_dirpath)
            print('done')

            input_example = open(example_file)
            input_klasses = open(klasses_file)
        
    
            out_example_file = os.path.join(dest, dirname, dirname + '.dat')
            out_klasses_file = os.path.join(dest, dirname, dirname + '.tsv')
    
            dest_dirpath = os.path.join(dest, dirname)

            out_example = open(out_example_file, 'w')
            out_klasses = open(out_klasses_file, 'w')

            example_occ = defaultdict(int)
            for line in input_example:
                # Read an example and its classes
                example = line.strip()
                klasses = input_klasses.readline().strip()
                
                # Filter out duplicate lines
                if example not in example_occ and \
                   len(example) <= MAX_EXAMPLE_SIZE:
                    out_example.write(example + '\n')
                    out_klasses.write(klasses + '\n')
                    example_occ[example] += 1
        
            # close input streams
            input_klasses.close()
            input_example.close()

            # close output streams
            out_example.close()
            out_klasses.close()

if __name__ == '__main__':
    parser = argparse.ArgumentParser('clean the dataset')
    parser.add_argument('src', help='the src dir')
    parser.add_argument('dest', help='the dest dir')
    args = parser.parse_args()

    clean(args.src, args.dest)
