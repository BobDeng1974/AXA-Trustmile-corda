ps -ef | grep corda | grep -v ".webserver.Starter" | grep java | grep -v grep | grep -v IntelliJ | awk '{print "kill -9 " $2 ";"  }'
