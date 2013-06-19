#NE='ORG'
#F='2'
#t='5'

NE=$1
t=$2
F=$3

EXPECTED_ARGS=3
E_BADARGS=65

#g='0.4'

# invoke usage
# call usage() function if args not supplied
if [ $# -ne $EXPECTED_ARGS ]
then
	echo "Usage: $0 NE t F"
	exit $E_BADARGS


fi

echo "-NE: $NE, -g: $g, gStr: $gStr, -t: $t, -F: $F"



if [ -d "tree/${NE}/models_t${t}F${F}" ]; then
    rm -rf "tree/${NE}/models_t${t}F${F}"
fi
mkdir -p "tree/${NE}/models_t${t}F${F}"

if [ -d "tree/${NE}/pred_devTest_t${t}F${F}" ]; then
   rm -rf "tree/${NE}/pred_devTest_t${t}F${F}"
fi
mkdir -p "tree/${NE}/pred_devTest_t${t}F${F}"

if [ -d "tree/${NE}/eval_devTest_t${t}F${F}" ]; then
   rm -rf "tree/${NE}/eval_devTest_t${t}F${F}"
fi
mkdir -p "tree/${NE}/eval_devTest_t${t}F${F}"



if [ ! -d "tree/${NE}/split" ]; then
	python split_files.py tree/$NE/data tree/$NE/split
	echo ""
fi 



declare -a g_values=('0.3' '0.4' '0.5' '0.6' '0.7' '0.8'  '0.9' '1.0')
for g in "${g_values[@]}"
do
	gStr=`echo $g| sed -e 's/\.//g'`
	echo "$gStr"

	if [ ! -d "tree/${NE}/train_g${gStr}" ]; then
		python split_dataset.py tree/${NE}/split .7/.15/.15 $g tree/${NE}
		mv tree/${NE}/train tree/${NE}/train_g${gStr}
		
		
	fi
	
	#echo "python split_dataset.py tree/${NE}/split .7/.15/.15 $g tree/${NE}"
	python learn.py tree/${NE}/train_g${gStr} tree/${NE}/models_g${gStr}t${t}F${F} "-t $t -F $F"
	python classify.py tree/${NE}/models_g${gStr}t${t}F${F} tree/${NE}/devTest tree/${NE}/predictions tree/${NE}/pred.devTest_g${gStr}t${t}F${F}.tsv
	mv tree/${NE}/pred.devTest.tsv tree/${NE}/pred.devTest_g${gStr}t${t}F${F}.tsv
	python eval.py tree/${NE}/pred.devTest_g${gStr}t${t}F${F}.tsv tree/${NE}/gold.devTest.tsv tree/${NE}/eval.devTest_g${gStr}t${t}F${F}.txt
	echo ""

COMMENT

	mv tree/${NE}/models_g${gStr}t${t}F${F} tree/${NE}/models_t${t}F${F} &&
	mv tree/${NE}/pred.devTest_g${gStr}t${t}F${F}.tsv tree/${NE}/pred_devTest_t${t}F${F}/pred.devTest_g${gStr}t${t}F${F}.tsv &&
	mv tree/${NE}/eval.devTest_g${gStr}t${t}F${F}.txt tree/${NE}/eval_devTest_t${t}F${F}/eval.devTest_g${gStr}t${t}F${F}.txt 




done





