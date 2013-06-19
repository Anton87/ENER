from __future__ import print_function
from __future__ import division
from collections import defaultdict

import os
import sys
import urllib2
import argparse
import itertools


def evaluate(pred_file, gold_file, output_file):
    pred_file = open(pred_file)
    gold_file = open(gold_file)

    predictions = pred_file.readlines()
    labels = gold_file.readlines()
    
    classNames = predictions[0].strip().split('\t')

    gheaders = labels[0].strip().split('\t')
    pheaders = predictions[0].strip().split('\t')

    gid2class = dict([(i, header) for i, header in enumerate(gheaders)])
    pid2class = dict([(i, header) for i, header in enumerate(pheaders)])

    gclass2id = dict([(header, i) for i, header in enumerate(gheaders)])
    pclass2id = dict([(header, i) for i, header in enumerate(pheaders)])

        

    #print("g_headers: %s" % (gheaders, ))
    #print("p_headers: %s" % (pheaders, ))

    stats = defaultdict(lambda: defaultdict(int))

    for gline, pline in zip(labels[1:], predictions[1:]):
        gbits = gline.strip().split('\t')
        pbits = pline.strip().split('\t')

       
        for gid, label in gid2class.iteritems():
            pid = pclass2id[label]

            gid = int(gid)
            pid = int(pid)

            # print("gid: %s, pid: %s" % (gid, pid))
            
            inGold = int(gbits[gid])
            inPred  = int(pbits[pid])

            if inGold and inPred: stats[label]["TP"] += 1
            elif inGold  and not inPred: stats[label]["FN"] += 1
            elif not inGold and inPred: stats[label]["FP"] += 1
            else: stats[label]["TN"] += 1



        for gbit, pbit in zip(gbits, pbits):
            pass
            #print("%s/%s " % (gbit, pbit), end="")
        #print("")

    out = open(output_file, 'w')

    tp, fp, fn, tn = 0, 0, 0, 0
    macro_precision, macro_recall, macro_fScore = 0, 0, 0
    for className in classNames:
        #modelName = models + urllib2.quote(className) + ".txt"

        # Local measures
        stats[className]["correct"] = stats[className]["TP"] + stats[className]["TN"]
        print("correct(%s): %s" % (className, stats[className]["correct"]))
        stats[className]["incorrect"] = stats[className]["FP"] + stats[className]["FN"]
        print("incorrect(%s): %s" % (className,  stats[className]["incorrect"]))
        stats[className]["total"] = stats[className]["correct"] + stats[className]["incorrect"]
        stats[className]["accuracy"] = 100 * stats[className]["correct"] / stats[className]["total"]

        retrieved = stats[className]['TP'] + stats[className]['FP']

        positives  = stats[className]['TP'] + stats[className]['FN']

        stats[className]["precision"] = (100 * stats[className]["TP"]
                /retrieved) if retrieved else 0
        stats[className]["recall"] = (100 * stats[className]["TP"] / positives)  if positives else 0

        prec_plus_rec = stats[className]["precision"] + stats[className]["recall"]
        stats[className]["f-score"] = (2 * (stats[className]["precision"] *
            stats[className]["recall"]) / prec_plus_rec) if prec_plus_rec  else 0

        # Global measures
        tp += stats[className]["TP"]
        fp += stats[className]["FP"]
        fn += stats[className]["FN"]
        tn += stats[className]["TN"]
        macro_precision += stats[className]["precision"]
        macro_recall += stats[className]["recall"]
        macro_fScore += stats[className]["f-score"]
        
        print("className: %s" % (className, ))
        out.write("className: %s\n" % (className, ))
        #print("Model name: %s" % (modelName, ))
        #out.write("Model name: %s\n" % (modelName, ))
        print("TP: %(TP)s, FP: %(FP)s, FN: %(FN)s, TN: %(TN)s" %
                (stats[className]))
        out.write("TP: %(TP)s, FP: %(FP)s, FN: %(FN)s, TN:%(TN)s\n" % \
                (stats[className]))
        print("Accuracy on test set: %.2f%% (%s correct, %s uncorrect, %s \
                total)" % (stats[className]["accuracy"],
                    stats[className]["correct"], stats[className]["incorrect"],
                    stats[className]["total"]))
        out.write("Accuracy on test set: %.2f%% (%s correct, %s uncorrect, %s total)\n" % \
                (stats[className]["accuracy"], stats[className]["correct"], stats[className]["incorrect"], stats[className]["total"]))
        print("Precision/recall on test set: %.2f%%/%.2f%%" %
                (stats[className]["precision"], stats[className]["recall"]))
        out.write("Precision/recall on test set: %.2f%%/%.2f%%\n" % (stats[className]["precision"], stats[className]["recall"]))
        print("F1-measure on test set: %.2f%%" % (stats[className]["f-score"]))
        out.write("F1-measure on test set: %.2f%%\n" % \
            (stats[className]["f-score"]))
        print("")
        out.write('\n')

    macro_precision /= len(classNames)
    macro_recall /= len(classNames)
    macro_fScore /= len(classNames)
    print("Macro precision/macro recall on test set: %.2f%%/%.2f%%" %
            (macro_precision, macro_recall))
    out.write("Macro precision/macro recall on test set: %.2f%%/%.2f%%\n" % \
        (macro_precision, macro_recall))
    print("Macro F1-measure on test set: %.2f%%" % (macro_fScore))
    out.write("Macro F1-measure on test set: %.2f%%\n" % (macro_fScore))
    
    micro_precision = 100 * tp / (tp + fp)
    micro_recall = 100 * tp / (tp + fn)
    micro_fScore = 2 * (micro_precision * micro_recall) / (micro_precision + micro_recall)
    print("Micro precision/micro recall on test set: %.2f%%/%.2f%%" %
            (micro_precision, micro_recall))
    out.write("Micro precision/micro recall on test set: %.2f%%/%.2f%%\n" %
        (micro_precision, micro_recall))
    print("Micro F1-measure on test set: %.2f%%\n" % (micro_fScore))
    out.write("Micro F1-measure on test set: %.2f%%" % (micro_fScore))

    out.close()
"""            
     
def isTP(cls, actual, predicted):
    return (cls in actual) and (cls in predicted)
    
def isFP(cls, actual, predicted):
    return (cls not in actual) and (cls in predicted)

def isFN(cls, actual, predicted):
    return (cls in actual) and (cls not in predicted)

def isTN(cls, actual, predicted):
    return (cls not in actual) and (cls not in predicted)
    
 
def readlines(filepath):
    lines = []
    with open(filepath) as f: 
        lines = f.readlines()
    return lines

"""
if __name__ == "__main__":
    parser = argparse.ArgumentParser("evaluate the system performances.")

    parser.add_argument("pred_file", help="the prediction file")
    parser.add_argument("gold_file", help="the gold file")
    parser.add_argument("output_file", help="the ouput file")

    args = parser.parse_args()

    if not os.path.exists(args.pred_file):
        print("prediction file not found: \"%s\"" % (args.pred_file))
        sys.exit(1)

    if not os.path.exists(args.gold_file):
        print("gold file not found: \"%s\"" % (args.gold_file))
        sys.exit(1)

    evaluate(args.pred_file, args.gold_file, args.output_file)     
