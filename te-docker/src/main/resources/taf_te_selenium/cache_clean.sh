#!/usr/bin/env bash

totalMem=$(free -g | grep Mem: | awk '{print $2}')
freeMem=$(free -g | grep Mem: | awk '{print $4}')

freeMemPer=$(((freeMem*100)/totalMem))

if [ $freeMemPer -lt 25 ]
then
    echo "FreeMemPer : $freeMemPer : `date`" >> $HOME/cache_log
    echo 3 > /proc/sys/vm/drop_caches
fi
