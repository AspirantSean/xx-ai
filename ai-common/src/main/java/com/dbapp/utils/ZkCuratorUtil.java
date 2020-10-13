package com.dbapp.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author delin
 * @date 2018/9/6
 */
public class ZkCuratorUtil {

    private final Logger logger = LoggerFactory.getLogger(ZkCuratorUtil.class);

    private final static Integer DEFAULT_SESSION_TIMEOUT = 300000;
    private final static Integer DEFAULT_CONNECT_TIMEOUT = 150000;
    private final static Integer DEFAULT_BASE_SLEEP_TIME = 1000;
    private final static Integer DEFAULT_MAX_RETRY = 5;
    private final static Charset charset = Charsets.UTF_8;

    private String zkHosts;
    private String basePath;
    private Integer connectTimeout;
    private Integer sessionTimeout;
    private Integer maxRetry;
    private CuratorFramework client;
    private final Map<String, TreeCache> treeCaches = Maps.newHashMap();

    public ZkCuratorUtil(String zkHosts, String basePath) {
        this(zkHosts, basePath, DEFAULT_CONNECT_TIMEOUT);
    }

    public ZkCuratorUtil(String zkHosts, String basePath, Integer connectTimeout) {
        this(zkHosts, basePath, connectTimeout, DEFAULT_SESSION_TIMEOUT);
    }

    public ZkCuratorUtil(String zkHosts, String basePath, Integer connectTimeout, Integer sessionTimeout) {
        this(zkHosts, basePath, connectTimeout, sessionTimeout, DEFAULT_MAX_RETRY);
    }

    public ZkCuratorUtil(String zkHosts, String basePath, Integer connectTimeout, Integer sessionTimeout, Integer maxRetry) {
        this.zkHosts = zkHosts;
        this.basePath = basePath;
        this.connectTimeout = connectTimeout;
        this.sessionTimeout = sessionTimeout;
        this.maxRetry = maxRetry;
    }

    private CuratorFramework connect() {
        client = CuratorFrameworkFactory.builder()
                .connectString(zkHosts)
                .retryPolicy(new ExponentialBackoffRetry(DEFAULT_BASE_SLEEP_TIME, this.maxRetry))
                .sessionTimeoutMs(this.sessionTimeout)
                .connectionTimeoutMs(this.connectTimeout)
                .namespace(basePath)
                .build();
        return client;
    }

    public CuratorFramework getClient() {
        return client == null ? connect() : client;
    }

    public void addListener(String node, TreeCacheListener listener) throws Exception {
        if(treeCaches.containsKey(node)) {
            return;
        }

        node = fixed(node);
        TreeCache treeCache = new TreeCache(this.client, node);
        treeCache.getListenable().addListener(listener);
        treeCache.start();
        treeCaches.put(node, treeCache);
    }

    public CuratorFramework start() {
        if (client == null) {
            client = connect();
        }

        /**
         * 判断CuratorFramework是否启动
         */
        if (!client.getState().equals(CuratorFrameworkState.STARTED)) {
            client.start();
        }
        return client;
    }

    public Stat checkExists(String nodePath) throws Exception {
        String path=fixed(nodePath);
        return client.checkExists().forPath(path);
    }

    public String send(String nodePath, String content) throws Exception {
        return send(nodePath, Strings.nullToEmpty(content).getBytes(charset));
    }

    public String send(String nodePath, byte[] bytes) throws Exception {
        nodePath = fixed(nodePath);
        Stat stat = checkExists(nodePath);
        if (stat == null) {
            client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(nodePath, bytes);
        } else {
            client.setData().forPath(nodePath, bytes);
        }
        return nodePath;
    }

    public void create(String nodePath)throws Exception{
        nodePath = fixed(nodePath);
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(nodePath);
    }

    public String get(String nodePath) throws Exception {
        nodePath = fixed(nodePath);
        Stat stat = checkExists(nodePath);
        if (stat != null) {
            byte[] bytes = client.getData().forPath(nodePath);
            if (bytes != null) {
                return new String(bytes, charset);
            }
        }
        return null;
    }

    public String delete(String nodePath) throws Exception {
        nodePath = fixed(nodePath);

        Stat stat = checkExists(nodePath);
        if (stat != null) {
            client.delete().deletingChildrenIfNeeded().forPath(nodePath);
        }

        return nodePath;
    }

    public void close() throws Exception {
        client.close();
        client = null;
    }

    private static String fixed(String path) {
        path = path.startsWith("/") ? path : String.format("/%s", path);
        return path.replaceAll("[/]{2,}", "/");
    }

    public String buildPath(String... zNodeParts) {
        StringBuilder builder = new StringBuilder(256);

        String path;
        if (zNodeParts != null && zNodeParts.length != 0) {
            builder.append("/");

            path = Joiner.on("/").appendTo(builder, zNodeParts).toString();
        } else {
            path = builder.toString();
        }

        builder = null;
        return fixed(path);
    }

    public List<String> getChildren(String nodePath) throws Exception {
        nodePath = fixed(nodePath);
        Stat stat = checkExists(nodePath);
        if (stat != null) {
            return client.getChildren().forPath(nodePath);
        }
        return null;
    }

    public static void main(String[] args){
        ZkCuratorUtil zkCuratorUtil =  new ZkCuratorUtil("172.16.100.39:2181","test");
        zkCuratorUtil.start();

        // 添加监听
        new Thread(() -> {
            try {
                TreeCacheListener listener = (curatorFramework, event) -> {
                    switch (event.getType()) {
                        case NODE_ADDED:
                            System.out.println("add,path:" + event.getData().getPath() + ",data:" + new String(event.getData().getData()));
                            break;
                        case NODE_UPDATED:
                            System.out.println("update,path:" + event.getData().getPath() + ",data:" + new String(event.getData().getData()));
                            break;
                        case NODE_REMOVED:
                            System.out.println("remove,path:" + event.getData().getPath() + ",data:" + new String(event.getData().getData()));
                            break;
                        default:
                            break;
                    }
                };

                zkCuratorUtil.addListener("/test/a",listener);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

        // zk发送数据
        try {
            zkCuratorUtil.send("/test/a", "a".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
