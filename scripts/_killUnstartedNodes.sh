#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

stopped=
checkPorts(){
	port=$1
	name=$2
	nof=$(netstat -an | grep $port.*LISTEN | wc -l)
	if [ $nof -eq 0 ];  then
		echo "kill Java Nodes for $name"
		cd $BASEDIR
		echo "$(eval `./_killNode.sh $name`)"
		stopped="${stopped}${name}"
	fi
}

checkPorts 10003 $NodeName0
checkPorts 10006 $NodeName1
checkPorts 10009 $NodeName2
checkPorts 10012 $NodeName3
checkPorts 10015 $NodeName4

retval=${stopped}

