from collections import defaultdict

def has_duplicates(filepath):
    """Test whether a file has some duplicate lines.

       Keyword arguments:
       filepath -- The file to search for duplicates
       
    """
    line_counts = defaultdict(int)
    with open(filepath) as f:
        for line in f:
            line_counts[line.strip()] += 1

    return any([count > 2 for count in  line_counts.values()])
    
    
