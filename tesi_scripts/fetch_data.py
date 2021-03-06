from bs4 import BeautifulSoup, element
from subprocess import call
from MySQLdb import cursors

import ConfigParser
import MySQLdb
import urllib2
import argparse
import codecs
import string
import json
import nltk
import sys
import re
import os


# Number of backlinks to retrieve each time 
bllimit = 20
# Number of examples retrieved each time from freebase
epp = 20 

def retrieve_examples(notable_type, examples_num, db, epp, bllimit, output_file):
    print 'examples_num: %s' % (examples_num, )
    dirpath = os.path.dirname(output_file)
    
    if not os.path.exists(dirpath): os.makedirs(dirpath)

    out = codecs.open(output_file, 'w', 'utf-8')
    out.write('paragraph\tsentence\tmid\ttitle\tstart\tend\tnotable_types\n')


    entities = get_entities_by_notable_for(db, apikey, notable_type, 0, epp)
    print 'retrieved %s entities' % (len(entities), )

    offset = len(entities)

    retrieved_examples = 0 

    title2blcontinue = {}
    visited = []
    while (entities or visited) and retrieved_examples < examples_num:
        entity = entities.pop(0) if entities else visited.pop(0)
        mid, uri, notable_types, notable_for = entity


        if uri:
            title = uri[uri.rindex('/') + 1:].replace(' ', '_')

            print 'title: ' + title
            if title in title2blcontinue:
                blcontinue = title2blcontinue[title]
            else:
                blcontinue = None
            backlinks, blcontinue = get_backlinks(title, bllimit, blcontinue)

           # Store the blcontinue for later reuse
            if blcontinue: 
                a, b, c = blcontinue.split('|')
    	        blcontinue = '|'.join([a, title, c])
                visited.append(entity)
                title2blcontinue[title] = blcontinue


            # Search for examples in backlink containing link to the main topic page
            for backlink in backlinks:
                print 'backlink: %s' % (backlink, )
                try:
                    html_doc = get_wiki(backlink)
                except urllib2.HTTPError:
                    html_doc = ''

                if not html_doc: continue

                examples = process_wiki_test(title, html_doc, backlink)
                #print 'examples: %s' % (examples, )
                for example in examples:
                    #print 'got new example: %s' % (example, )
                    paragraph, sent, start, end, = example

                    s = paragraph 
                    s += '\t' + sent
                    s += '\t' + mid
                    s += '\t' + title
                    s += '\t' + str(start)
                    s += '\t' + str(end)
                    s += '\t' + ','.join(notable_types)  
                    out.write(s + '\n' )
    
                    retrieved_examples += 1
                    print 'retrieved_examples: %s, examples_num: %s, offset: %s' %\
                    (retrieved_examples, examples_num, offset)
                    if retrieved_examples >= examples_num:
                        out.close()
                        sys.exit(0)
                  
        if not entities:
            entities = get_entities_by_notable_for(db, apikey, notable_type,
            offset, epp)
            offset += len(entities)
    out.close()
        

def get_backlinks(bltitle, bllimit, blcontinue=None):
    """Retrieve a list of backlinks with the specified title

       Keyword arguments:
       bltitle -- the backlink title
       bllimit -- the number of backlinks per page
       blcontinue -- continue the previous request

    """
    req = 'http://en.wikipedia.org/w/api.php'
    req += '?action=query'
    req += '&list=backlinks'
    req += '&bltitle=' + bltitle
    req += '&bllimit=' + str(bllimit)
    req += '&blfilterredir=nonredirects'
    req += '&blnamespace=0'

    if blcontinue: req += '&blcontinue=' + blcontinue
    req += '&format=json'
    
    data = json.load(urllib2.urlopen(req))

    # Get the blcontinue value, if exists
    blcontinue = data.get('query-continue', {})
    blcontinue = blcontinue.get('backlinks', {})
    blcontinue = blcontinue.get('blcontinue', '')

    # Get the backlinks 
    backlinks = data.get('query', {})
    backlinks = backlinks.get('backlinks', [])
    # backlinks = [bl['title'] for bl in backlinks]
    backlinks = [bl['title'].replace(' ', '_') for bl in backlinks]

    return (backlinks, blcontinue)


def get_entities_by_notable_for(db, apikey, notable_for, offset, limit):
    """Retrieve a set of entities by their notable_for type"""
    entities = []
    for mid in get_mids_by_notable_for(db, notable_for, offset, limit):
        notable_types = get_notable_types_by_mid(db, mid)
        uri = get_uri_by_mid(apikey, mid)

        entity = (mid, uri, notable_types, notable_for)
        entities.append(entity)
    return entities


def get_notable_types_by_mid(db, mid):
    """Retrive the notable types of the entity with the specified mid."""
    q = """select type_id from notable_types where id = '%s'""" % (mid, )
    c = db.cursor(cursors.SSCursor)
    c.execute(q)

    notable_types = []
    row = c.fetchone()
    while row is not None:
        notable_type = row[0]
        notable_types.append(notable_type)
        row = c.fetchone()
    return notable_types


def get_uri_by_mid(apikey, mid):
    """Returns the uri of a page by its mid."""
    url = 'https://www.googleapis.com/freebase/v1/topic' + mid +\
    '?filter=/common/topic/description&key=' + apikey
    response = urllib2.urlopen(url)
    data = json.load(response)
    uri = get_wiki_uri(data)
    return uri


def get_wiki_uri(data):
    data = data.get('property', {})
    data = data.get('/common/topic/description', {})
    data = data.get('values', [])
    for value in data:
        if value.get('lang', '') == 'en':
            data = value.get('citation', {})
            uri = data.get('uri', '')
            if uri: return uri
            
    return ''


        


def get_mids_by_notable_for(db, notable_for, offset, limit):
    """Retrieve a set of mids for the specified notable_for type."""
    q = """select id from notable_for where type_id = '%s' limit %s, %s""" % (notable_for, offset, limit)
    c = db.cursor(cursors.SSCursor)
    c.execute(q)

    mids = []
    row = c.fetchone()
    while row is not None:
        mid = row[0]
        mids.append(mid)
        row = c.fetchone()
    return mids




def get_wiki(wiki_title):
    """Retrieve the wiki page.

       Keyword arguments:
       wiki_title --the wiki page title

    """
    #url = 'http://en.wikipedia.org/wiki/' + wiki_page
    wiki_uri = 'http://en.wikipedia.org/w/index.php?title=' 
    wiki_uri += wiki_title.encode('utf-8')
    print 'fetching page: ' + wiki_uri
    opener = urllib2.build_opener()
    # Change user-agent to  Mozialla/5.0
    opener.addheaders = [('User-agent', 'Mozilla/5.0')]
    sock = opener.open(wiki_uri)
    data = sock.read()
    return data


def process_wiki_test(wiki_title, html_doc, backlink):
    examples = []

    backlink = backlink.replace(' ', '_')

    """Process wiki test."""
    # The id of the div containint the body document
    mdiawiki_contet_text = 'mw-content-text'
    soup = BeautifulSoup(html_doc, "lxml")
    

    print 'searching for link href="/wiki/'  + wiki_title + '" in page ' +\
          'en.wikipedia.org/w/index.php?title=' + backlink

    p_tags = soup.find_all('p')

    for p_tag in p_tags:
        a_tags = p_tag.find_all('a', href='/wiki/' + wiki_title)

        if not a_tags: continue
        text = re.sub('\[[^\]]+\](,\[[^\]]+)*', '', p_tag.text).strip()

        

        for a_tag in a_tags:
            for sent in  nltk.sent_tokenize(text):
                if a_tag.text in sent:
                    # print 'got a tag: %s' % (a_tag.text, )
                    start = sent.index(a_tag.text)
                    end = start + len(a_tag.text)

                    while end < len(sent) and not (sent[end].isspace() or ispunct(sent[end])):
                        end += 1

                     # If the mention matches with the entity anchor text, keep it
                    mention = sent[start:end]

                    # print 'a[href]: %s, mention: %s' % (a_tag.text, mention)

                    if mention == a_tag.text and '\n' not in sent:
                        example = (text, sent, start, end)
                        examples.append(example)
    return examples


def ispunct(ch):
    return ch in string.punctuation
        

if __name__ == '__main__':
    parser = argparse.ArgumentParser("fetch examples")
    parser.add_argument("cfg", help="the db cfg file")
    parser.add_argument("notable_type", help="a freebase type")
    parser.add_argument("examples_num", help="the number of examples to download")
    parser.add_argument("output_file", help="the output file")
    args = parser.parse_args()

    config = ConfigParser.ConfigParser()
    config.read(args.cfg)

    host = config.get("mysql", "host")
    user = config.get("mysql", "user")
    psswd = config.get("mysql", "psswd")
    apikey = config.get("freebase", "apikey")

    db = MySQLdb.connect(host, user, psswd, "freebase")

    retrieve_examples(args.notable_type, int(args.examples_num), db, epp,
            bllimit, args.output_file)
