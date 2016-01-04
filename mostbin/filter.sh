#!/system/bin/sh

MOST_RESULT=$1
FILTER_RESULT=$2
KEY_WORD=$3

rm -f $FILTER_RESULT
tmp_tids="/sdcard/tid.list"
rm -f $tmp_tids
#app related   :::log order change
tmp_result="/sdcard/tmp.txt"
rm -f $tmp_result
#mmcqd related :::log order not change
tmp_filter="/sdcard/filter.txt"
rm -f $tmp_filter
##### get all related pid
app_pids=`pgrep $KEY_WORD`


##### generate tmp_tids
for pid in $app_pids
do
    ps -t $pid > $tmp_tids
done


##### get all related tids. uniq will only handle interfacing line
app_tids=(`cat $tmp_tids | /data/busybox awk -F' ' '{print $2}' | uniq`)


##### filter result
tid_num=${#app_tids[@]}

echo 'first:'${app_tids[1]}
# beacuse the first is PID, so we start from index 1
egrep '[[:blank:]]+'${app_tids[1]}'[[:blank:]]{2}''I' $MOST_RESULT | tee -a $tmp_result

tid_max=`expr $tid_num - 1`
echo $tid_max

# index 1 was added before, so we just search from index 2
for tid in `seq ${app_tids[2]} ${app_tids[$tid_max]}`
do
    echo $tid
    egrep '[[:blank:]]+'${tid}'[[:blank:]]{2}''I' $MOST_RESULT | tee -a $tmp_result
done


##### find all mmcqd related D log
mmc_pid=`pgrep mmcqd/0$`
egrep '[[:blank:]]+'${mmc_pid}'[[:blank:]]{2}''D' $MOST_RESULT | tee -a $tmp_filter


##### use tmp_result to filter mmcqd log
regrex='('
blocks=(`cat $tmp_result | /data/busybox awk -F' ' '{print $8}' | uniq`)
for block in ${blocks[@]}
do
    echo $block
    block_option='[[:blank:]]+'${block}'[[:blank:]]{1}''\+'
    regrex=$regrex''$block_option'|'
    # egrep '[[:blank:]]+'${block}'[[:blank:]]{1}''\+' $tmp_filter | tee -a $FILTER_RESULT
done

regrex=${regrex%'|'}')'
echo ${regrex}
# order not change
egrep ${regrex} $tmp_filter | tee -a $FILTER_RESULT


##### delete unused files
rm -f $tmp_tids
rm -f $tmp_result
rm -f $tmp_filter
