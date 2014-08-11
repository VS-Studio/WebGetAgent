### 说明
====

WebGetAgent是爬虫系统与WEB网站之间的一个代理服务，获取爬虫系统的请求后代理服务会缓存页面数据并将请求插入到队列，后台服务会从队列中获取请求将爬取后的数据保存到数据库中，爬虫下次爬取的时可以获取更新后的数据。详细说明请移步：http://doc.ucweb.local/pages/viewpage.action?pageId=22686525


### 依赖库
====

目前只依赖2个jar包： mysql驱动和fastjson ， 将两个jar包放在lib目录下面
mysql: http://dev.mysql.com/downloads/connector/j/ 
fastjson: http://repo1.maven.org/maven2/com/alibaba/fastjson/


### 编译安装
====

# 编译脚本 build.sh
mkdir bin

javac -classpath ./lib/*:. -sourcepath src -d bin -encoding utf-8 -Xlint:unchecked src/network/WebGetAgent.java
javac -classpath ./lib/*:. -sourcepath src -d bin -encoding utf-8 -Xlint:unchecked src/mq/MQHttpServer.java



### 配置
====

#proxy 
port = 8989 代理端口
host = 10.1.85.198 代理地址
log_level = 0 错误日志级别 0：debug 1: info 2: warn 3: error
#mysql 10.1.72.154
dbhost = 10.1.72.154 数据库地址
dbport = 3306 数据库端口
dbuser = root 数据库账号
dbpass = root 数据库密码
dbase = liujf_db 数据库
#mq
mq_port = 8988 队列端口
mq_thread_num = 5 队列线程数
#webkit
#代理请求增加： webgetagent_with=ajax 参数
webkit_process_cmd = phantomjs ./proxy.js 


### 运行 run.sh
====

nohup java -classpath ./lib/*:./bin:. mq.MQHttpServer  >nohup.out 2>&1 &
nohup java -classpath ./lib/*:./bin:. network.WebGetAgent >nohup.out 2>&1 &



### Webkit内核渲染
====

下载安装 Phantomjs 配置到环境变量，将需要进行webkit渲染的请求增加参数： webgetagent_with=ajax，如： http://video.ucweb.com/?webgetagent_with=ajax




### 数据库表结构
====

CREATE TABLE `page_data` (
  `url_hash` char(50) NOT NULL,
  `url` text,
  `headers` blob,
  `content` longblob,
  `last_modified_time` char(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `last_visit_time` datetime DEFAULT NULL,
  `last_fetch_time` datetime DEFAULT NULL,
  `last_update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`url_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
