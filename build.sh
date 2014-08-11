mkdir bin
#javac -classpath mysql.jar;fastjson.jar;. -d bin -encoding utf-8 -Xlint:unchecked src\network\WebGetAgent.java
#javac -classpath mysql.jar;fastjson.jar;. -d bin -encoding utf-8 -Xlint:unchecked src/mq/MQHttpServer.java

## win
#javac -classpath ./lib/*;. -sourcepath src -d bin -encoding utf-8 -Xlint:unchecked src\network\WebGetAgent.java
#javac -classpath ./lib/*;. -sourcepath src -d bin -encoding utf-8 -Xlint:unchecked src\mq\MQHttpServer.java

## linux
javac -classpath ./lib/*:. -sourcepath src -d bin -encoding utf-8 -Xlint:unchecked src/network/WebGetAgent.java
javac -classpath ./lib/*:. -sourcepath src -d bin -encoding utf-8 -Xlint:unchecked src/mq/MQHttpServer.java