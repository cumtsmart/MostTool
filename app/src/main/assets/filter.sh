#!/system/bin/sh

MOST_RESULT=$1
FILTER_RESULT=$2
KEY_WORD=$3

rm -f $FILTER_RESULT

##### get all related pid
app_pids=`pgrep $KEY_WORD`

#[[:blank:]]{2}D[[:blank:]]+[[:print:]]*ppid:3386[[:alnum:]]*]
pattern="([[:blank:]]{2}""D""[[:blank:]]+[[:print:]]*""ppid:"

for pid in $app_pids
do
    pattern=${pattern}${pid}"[[:alnum:]]*]|"
done

pattern=${pattern%'|'}')'

echo $pattern

##### filter result, only D type
egrep "${pattern}" $MOST_RESULT | tee -a $FILTER_RESULT
