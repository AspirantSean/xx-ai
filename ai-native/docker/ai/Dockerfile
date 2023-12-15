#基础镜像部分，按照运行时进行选择
FROM ailpha-registry:5000/ailpha-base:v2.0

#env 环境变量，主要定义需要人为控制的写入的环境变量
ENV TZ=Asia/Shanghai

#port 定义端口
EXPOSE 8902/tcp

#prepare 准备阶段，主要是定义工作目录

ADD ailpha-ext-ai-* /usr/hdp/20231103/app-ai/

ADD component/statsmodels.tar /usr/lib/python2.7/site-packages/
ADD component/cpulimit /opt/
RUN ln -s /opt/cpulimit /usr/bin/cpulimit

#runner 启动脚本部分呢，主要定义启动脚本相关参数
COPY docker/docker-entrypoint.sh /usr/local/bin/
RUN chmod u+x /usr/local/bin/docker-entrypoint.sh

WORKDIR /usr/hdp/20231103/app-ai

#用 bash /usr/local/bin/docker-entrypoint.sh 启动
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

CMD java $JAVA_OPTS com.dbapp.extension.ai.ExtApplication