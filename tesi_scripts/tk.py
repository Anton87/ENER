""" Tree Kernel (Tk) in python. """

from __future__ import print_function
from operator import mul
import argparse

def eval_tree(s):
    """Convert a tree parse string into a tree.

       Keyword arguments:
       s -- the tree parse string

    """
    print('str: \'%s\'' % (s, ))
    s = s[1:len(s)-1]
    if '(' not in s and ')' not in s:
        #print 'split(%s): %s' % (s, s.split())
        terminal, child = s.split()
        #print('leaf reached: Tree(%s, [%s])' % (terminal, child))
        return Tree(terminal, [Tree(child, [])])
    else:
        non_terminal = s[:s.index(' ')]
        #print('branch: Tree(%s, ...)' % (non_terminal, ))
        buff = ''

        substrs = []
        parents_num = 0
        for i in range(len(s)):
            ch = s[i]
            if ch == '(': parents_num += 1
            elif ch == ')': parents_num -= 1
            if parents_num > 0:
                buff += ch
            elif parents_num == 0 and len(buff) != 0:
                buff += ch
                substrs.append(buff)
                buff = ''
        children = map(eval_tree, substrs)
        return Tree(non_terminal, children)



class Tree:

    def __init__(self, elem, children):
        self.elem = elem
        self.children = children

    def __str__(self):
        buff = ''
        return self.elem if self.isLeaf() else ('(' + self.elem + ' ' +
    ' '.join(map(str, self.children)) + ')')

    def nodes(self):
        trees = []
        queue = [self]
        while queue:
            tree = queue.pop(len(queue) - 1)
            trees.append(tree)
            queue += tree.children
        return trees

    def isPreterminal(self):
        return len(self.children) == 1 and \
               self.children[0].isTerminal()
        #return self.isNonterminal() and self.children[0].isTerminal()

    def isNonterminal(self):
        return not self.isLeaf()


    def isTerminal(self):
        return self.isLeaf()

    def isLeaf(self):
        return len(self.children) == 0


def kernel(tree1, tree2, delta):
    score = .0
    for node1 in tree1.nodes():
        for node2 in tree2.nodes():
            score += delta(node1, node2)
    return score


def evaluate_kernel(tree1, tree2, delta):
    score = .0
    for node1 in tree1.nodes():
        for node2 in tree2.nodes():
            d_result = delta(node1, node2)
            print('kernel(%s, %s) = %.3f' % (node1, node2, d_result))
            score += d_result
    return score


def production(tree):
    return [tree.elem] + map(lambda t: t.elem, tree.children)


def delta_factory(sigma, remove_leaves=True):

    def delta(node1, node2):
        """
        if node1.isLeaf() and node2.isLeaf():
            if not remove_leaves:
                return 1 if node1.elem == node2.elem else 0
        """
        if node1.isLeaf() and node2.isLeaf():
            if node1.elem == node2.elem: 
                return 0 if remove_leaves else 1 

        if node1.isPreterminal() and node2.isPreterminal():            
            if production(node1) == production(node2): 
                return 1 if remove_leaves else 1 + delta(node1.children[0], node2.children[0])
               # return 1 if remove_leaves else  1 + delta(node1.children[0], node2.children[0])
            else: 
                return 0

        elif production(node1) == production(node2):
                return reduce(mul, [sigma + delta(node1.children[j],
                    node2.children[j]) for j in range(len(node1.children))], 1)
        return 0
            


    return delta


stk = delta_factory(0)

sstk = delta_factory(1)

stk_bow = delta_factory(0, False)

sstk_bow = delta_factory(1, False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser('tree kernel test')
    parser.add_argument('tree1', help='the first tree')
    parser.add_argument('tree2', help='the second tree')
    args = parser.parse_args()

    tree1 = eval_tree(args.tree1)
    tree2 = eval_tree(args.tree2)
    #print('tree1: %s' % (tree1, ))
    #print('tree2: %s' % (tree2, ))

    print('stk(tree1, tree2) = %s' % (kernel(tree1, tree2, stk), ))
    print('sstk(tree1, tree2) = %s' % (kernel(tree1, tree2, sstk), ))
    print('stk_bow(tree1, tree2) = %s' % (kernel(tree1, tree2, stk_bow), ))
    print('sstk_bow(tree1, tree2) = %s' % (kernel(tree1, tree2, sstk_bow), ))
    
    # print('stk(tree1, tree2) = %.3f' % (evaluate_kernel(tree1, tree2, stk))) 
    #print('sstk(tree1, tree2) = %.3f' % (evaluate_kernel(tree1, tree2, sstk)))

    #print('stk_bow(tree1, tree2) = %.3f' % (evaluate_kernel(tree1, tree2,
    #    stk_bow)))
    #print('sstk_bow(tree1, tree2) = %.3f' % (evaluate_kernel(tree1, tree2,
    #    sstk_bow)))
