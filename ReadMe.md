# 基于Spring Boot Web的容器应用健康检查
这是一个用来演示容器中基于Spring Boot Web应用的健康检查示例程序,分别从镜像构建,容器创建,服务编排三个方向实现容器应用的健康检查.
## 为我们的Spring Boot Web应用添加健康检查支持
得益于Spring的良好设计,我们只需要简单添加以下依赖,即可为我们的Web应用实现健康检查支持.
```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency> 
```
启动程序后,我们通过curl命令查看Web应用启动状态
```shell
curl http://localhost:8080/actuator/health
{"status":"UP"}
```
我们编制了一个需要消耗较长时间的初始化任务来试验健康检查,参考[LongTimeInitializerTask.java](./src/main/java/cn/vanillazi/learn/health/checking/task/LongTimeInitializerTask.java)
## 编制健康检查脚本
Dockerfile Reference HealthCheck章节约定采用健康检查脚本进程退出状态码来表示应用的健康状态,约定如下
- 0:成功,表示容器是健康并且可用.
- 1:不健康,表示容器没有正常工作.
- 2:保留退出状态码,不要使用这个退出状态码.

我们根据这个约定编制健康检查脚本如下:
```shell /checking
#!/usr/bin/env bash

if [ -n "$(curl -s http://localhost:8080/actuator/health | grep 'UP')" ];then
  exit 0
else
  exit 1
fi
```
## 配置容器健康检查
### Dockerfile中配置
我们可以在Dockerfile中指定容器默认的健康检查配置,配置格式为
```shell
HEALTHCHECK [选项] CMD 检查命令
```
#### 选项
- --interval=DURATION,容器启动后检查间隔,默认为30s
- --timeout=DURATION,单次检查超时时间,默认为30s
- --start-period=DURATION,容器启动时间,默认为0s
- --start-interval=DURATION,容器启动期间,检查间隔时间,默认为5s
- --retries=N,报告不健康状态需要连续失败次数,默认为3

容器启动后,第一次健康检查将会在interval秒后发生,以后每距离上一次检查执行完interval秒再执行一次.

单次检查任务超过timeout秒后,即认为检查失败.

如果连续retries次健康检查任务结果为失败后,即认为容器处于不健康状态.

start-period指定了容器的启动耗时,在此期间失败检查不计入最大重试次数.如果健康检查在启动期间检查结果为成功,那么容器即被标为已启动,接下来的所有连续失败检查次数将被计入累计失败次数.

start-interval指定了容器启动期间,健康检查的间隔时间.

#### 示例Dockerfile
```dockerfile
FROM library/openjdk
ADD target/health-checking-0.0.1-SNAPSHOT.jar /app.jar
ADD checking.sh /checking.sh
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s CMD /checking.sh
ENTRYPOINT ["java","-jar","/app.jar"]
```
#### 构建镜像
```shell
mvn package -DskipTests
docker build -t health-checking .
```
#### 启动
```dockerfile
docker run --rm -p 8080:8080/tcp health-checking
```
#### 健康状态展示
```shell
vanila@vanila:/mnt/d/workspaces/test/health-checking$ docker ps
CONTAINER ID   IMAGE               COMMAND                  CREATED          STATUS                    PORTS                                       NAMES
0833b92a8772   health-checking     "java -jar /app.jar"     47 minutes ago   Up 47 minutes (healthy)   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp   pedantic_bose
```
### 创建容器时配置
我们可以在创建容器时指定健康检查配置,参考docker create/run命令如下选项
- --health-cmd 健康检查命令
- --health-interval 每次检查间隔,默认值为0s
- --health-retries 报告不健康状态需要连续失败次数
- --health-start-period 容器启动时间,默认为0s
- --health-timeout 单次检查超时时间,默认为0s
#### 示例
1. 构建没有默认健康检查的镜像
   ```dockerfile
   FROM library/openjdk
   ADD target/health-checking-0.0.1-SNAPSHOT.jar /app.jar
   ADD checking.sh /checking.sh
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```
2. 构建并启动
   ```shell
   docker build -f Dockerfile-without-health-checking -t no-health-checking .
   docker run --rm --health-cmd=/checking.sh --health-interval=3s --health-retries=3 --health-start-period=30s --health-timeout=30s --rm no-health-checking
   ```
3. 查看运行状态
   ```shell
   vanila@vanila:/mnt/d/workspaces/test/health-checking$ docker ps
   CONTAINER ID   IMAGE                COMMAND                  CREATED             STATUS                       PORTS                                   
       NAMES
   61bbe4c5b933   no-health-checking   "java -jar /app.jar"     17 seconds ago      Up 16 seconds (healthy)                                              
   ```
### Docker Compose服务编排配置
我们也可以在Docker Compose配置文件中指定健康检查配置,参考配置如下
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost"]
  interval: 1m30s
  timeout: 10s
  retries: 3
  start_period: 40s
```
#### 示例
```yaml
version: '3.7'

services:
  no-health-checking:
    image: no-health-checking
    healthcheck:
      test: ["CMD", "/checking.sh"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```
启动并查看状态
```shell
vanila@vanila:/mnt/d/workspaces/test/health-checking$ docker compose up -d
vanila@vanila:/mnt/d/workspaces/test/health-checking$ docker compose ps
NAME                                   IMAGE                COMMAND                SERVICE              CREATED             STATUS                    PORTS
health-checking-no-health-checking-1   no-health-checking   "java -jar /app.jar"   no-health-checking   33 seconds ago      Up 33 seconds (healthy)

```
## 实践
容器应用的健康检查在微服务治理中扮演着重要的角色,健康检查可以为我们在服务实例监测,服务实例启动顺序提供基础支持.
## 参考
1. [Dockerfile Reference HealthCheck](https://docs.docker.com/engine/reference/builder/#healthcheck)
2. [Docker Create HealthCheck][https://docs.docker.com/engine/reference/commandline/create/]
3. [Docker Compose Service HealthCheck][https://docs.docker.com/compose/compose-file/05-services/#healthcheck]
4. [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)