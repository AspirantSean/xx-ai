package com.dbapp.extension.ai.ha;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.LineHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ZookeeperPropsWithLinux implements ZookeeperProps {

	private static final String HOSTS_FILE = "/etc/hosts";
	private static final String ZOOKEEPER_SERVER_DEFAULT = "1.zookeeper1:2181,1.zookeeper2:2181,1.zookeeper3:2181";

	protected File getFile() {
		return FileUtil.file(HOSTS_FILE);
	}

	@Override
	public String getUrl() {
		List<String> zookeepers = new ArrayList<>();
		FileUtil.readLines(getFile(), Charset.defaultCharset(), (LineHandler) line -> {
			if (!line.startsWith("#") && line.contains("zookeeper")) {
				String zookeeperHost = line.split("\\s")[1].trim();
				log.debug("从 hosts 中获取到的 zookeeper 域名 {}", zookeeperHost);
				zookeepers.add(zookeeperHost + ":2181");
			}
		});
		return zookeepers.size() == 0 ? ZOOKEEPER_SERVER_DEFAULT : CollectionUtil.join(zookeepers, ",");
	}
}
