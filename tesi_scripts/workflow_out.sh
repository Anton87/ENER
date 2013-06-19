-g: 0.4, gStr: 04, -F: 2
python split_dataset.py tree/ORG/data .7/.15/.15 0.4 tree/ORG
mv tree/ORG/train tree/ORG/train_g04
python learn.py tree/ORG/train_g04 tree/ORG/models_g04t5F2 "-t 5 F 2"
python classify.py tree/ORG/models_g04t2 tree/ORG/devTest tree/ORG/predictions tree/ORG/pred.devTest_g04t2.tsv
python eval.py tree/ORG/pred.devTest_g04t4tF2.tsv tree/ORG/gold.devTest.tsv tree/ORG/eval.devTest_g004t2.txt
