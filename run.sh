#kill -9  `ps -ef | grep 'mq.MQHttpServer' |grep java|awk '{print $2}' `
#kill -9  `ps -ef | grep 'network.WebGetAgent' |grep java|awk '{print $2}'`

#nohup java -classpath ./mysql.jar:fastjson.jar:. mq.MQHttpServer >MQHttpServer.out 2>&1 &
#nohup java -classpath ./mysql.jar:fastjson.jar:. network.WebGetAgent >WebGetAgent.out 2>&1 &

#win
#nohup java -classpath ./lib;./bin;. mq.MQHttpServer  >MQHttpServer.out 2>&1 &
#nohup java -classpath ./lib;./bin;. network.WebGetAgent >WebGetAgent.out 2>&1 &

#linux
if [ -a pid ]; then
        kill -9 `cat pid|tr '\n' ' '`
        rm -f pid
fi

nohup java -classpath ./lib/*:./bin:. mq.MQHttpServer  >nohup.out 2>&1 &
echo $! >> pid
nohup java -classpath ./lib/*:./bin:. network.WebGetAgent >nohup.out 2>&1 &
echo $! >> pid