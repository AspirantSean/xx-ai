package com.dbapp.extension.ai.ha;

import cn.hutool.core.io.FileUtil;

import java.io.File;

public class ZookeeperPropsWithWindows extends ZookeeperPropsWithLinux {

	@Override
	protected File getFile() {
		return FileUtil.file("C:\\Windows\\System32\\drivers\\etc\\hosts");
	}

	@Override
	public String getUrl() {
		return "192.168.30.195:12181";
	}
}
