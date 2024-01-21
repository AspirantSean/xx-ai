#基础镜像部分，按照运行时进行选择
FROM ailpha-registry:5000/ailpha-base:${tag}

#env 环境变量，主要定义需要人为控制的写入的环境变量
ENV TZ=Asia/Shanghai

#port 定义端口
EXPOSE 8902/tcp

#prepare 准备阶段，主要是定义工作目录

ADD ${package-name} /usr/hdp/20231103/flex-ai/

ADD component/statsmodels.tar /usr/lib/python2.7/site-packages/
ADD component/cpulimit /opt/
COPY config_update /usr/local/config_update
COPY docker/docker-entrypoint.sh /usr/local/bin/
COPY docker/nacos.sh /usr/local/bin/


RUN ln -s /opt/cpulimit /usr/bin/cpulimit \
    && chmod u+x /usr/local/bin/docker-entrypoint.sh \
    && chmod u+x /usr/local/bin/nacos.sh


WORKDIR /usr/hdp/20231103/flex-ai

#用 bash /usr/local/bin/docker-entrypoint.sh 启动
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

CMD java $JAVA_OPTS com.dbapp.extension.ai.ExtApplication