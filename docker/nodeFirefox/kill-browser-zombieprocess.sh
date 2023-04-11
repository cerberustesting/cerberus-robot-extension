#!/bin/bash

# This is script is to kill old processes of firefox or chrome that sometimes, selenium does not manage to kill.
# This is to avoid overloading the server hosting the selenium server by pilling the firefox process.

# Log File.
export MYLOG=/tmp/killbrowser.`date +%y%m`.log

if [ "$1" == "" ]; then
    echo Missing parameter !!
    echo Usage : $0 processName [debug]
    echo Example : $0 firefox debug
    exit 1;
fi

export browser=$1
export debug=$2

# getting the list of old processes that will be killed nicely later and others even older that will be killed in a more brutal way.
export proclistnofilter=`ps -eo pid,fname,etime | grep $browser | awk '{print $1}'`
export proclist=`ps -eo pid,fname,etime | grep $browser | awk '$3 >= "10:00" {print $1}'`
export proclistold=`ps -eo pid,fname,etime | grep $browser | awk '$3 >= "30:00" {print $1}'`

if [ "$debug" != "" ]
  then
    echo `date +%y%m%d-%H%M%S`" - trying to clean process : $browser (debug mode)"
    echo $proclistnofilter
    ps -eo pid,fname,etime | grep $browser
fi

# logging the nb of processes and correspondig list.
echo `date +%y%m%d-%H%M%S` nbfull : `echo $proclistnofilter | wc -w` old : `echo $proclist | wc -w` very old : `echo $proclistold | wc -w` - `echo $proclist` - `echo $proclistold`

# Killing the old processes
if [ "$proclist" != "" ]
  then
    echo "kill " $proclist
    kill $proclist
fi

# Killing in a brutal way the processes that could not be killed in a normal way before.
if [ "$proclistold" != "" ]
  then
    echo "kill -9 " $proclistold
    kill -9 $proclistold
fi
