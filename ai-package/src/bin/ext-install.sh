#!/bin/bash
install_log_file=/data/var/log/install-ext.log
mkdir -p /data/var/log
log() {
    echo "[`date +%Y-%m-%d' '%H:%M:%S`] $1" >> $install_log_file
}

log_command_result() {
    OLD_IFS=$IFS
    IFS=$'\n'

    for line in `eval $1`
    do
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] $line" >> $install_log_file
    done

    IFS=$OLD_IFS
}

#------------------------------------------------当前目录------------------------------------------------
current_path=$(cd `dirname $0`/..; pwd)

#------------------------------------------------安装目录------------------------------------------------
ext_home=`cat $current_path/conf/meta_info.data |jq -r '.deployPath'`

#---------------------------------------------安装、升级判断----------------------------------------------
action=install
if [ -d $ext_home ]; then
    echo "开始升级程序，详情查看：$install_log_file"
    log "检测当前程序状态："
    log_command_result "$ext_home/bin/ext-cli.sh status"

    #停止程序运行
    $ext_home/bin/ext-cli.sh stop

    log "关闭程序完成"
    log "开始升级程序"
    action=update
else
    echo "开始安装程序，详情查看：$install_log_file"
    mkdir -p $ext_home
    log "开始安装程序"
fi

#---------------------------------------------如果升级则备份----------------------------------------------
if [[ $action == "update" ]]; then
    log "开始备份"

    #创建备份目录
    ext_name=`$ext_home/bin/ext-cli.sh status | sed -n '3p' | awk '{print $1}'`
    version=`$ext_home/bin/ext-cli.sh -v`
    date=`date +%Y%m%d`
    backup_dir=/data/backup/$ext_name/$version/$date
    if [ ! -d $backup_dir ]; then
        mkdir -p $backup_dir
        log "创建备份目录：$backup_dir"
    fi

    log "备份程序文件..."

    #备份
    \cp -rf $ext_home $backup_dir

    log "完成备份"
fi

#--------------------------------------------进入安装、升级任务-------------------------------------------

#------------------------------------------------配置处理------------------------------------------------
if [[ $action == "update" ]]; then
    #配置文件升级处理
    if [ -f $current_path/bin/properties.update ]; then
        log "配置升级处理"
        separator='='
        while read line
        do
            #首行为表头剔除
            if [[ $line == "file-----type-----key-----value-----description" || -z $line ]]; then
                continue
            fi
            #解析每行操作
            content=(${line//-----/ })
            properties_file=$ext_home/conf/${content[0]}
            operation_type=${content[1]}
            properties_key=${content[2]}
            properties_value=${content[3]}
            properties_description=${content[4]}

            #判断是否需要操作
            if [ ! -f $properties_file ]; then
                continue
            fi
            if [[ $operation_type != "insert" && $operation_type != "delete" && $operation_type != "update" ]]; then
                continue
            fi

            #删除配置
            if [[ $operation_type == "delete" ]]; then
                log "移除配置项：$properties_key"
                sed -i s#^$properties_key.*##g $properties_file
                continue
            fi

            #配置内容拼装
            property_content=""
            if [[ -z $properties_description ]]; then
                property_content="$properties_key=$properties_value"
            else
                property_content="$properties_description\n$properties_key=$properties_value"
            fi
            #如果不存在配置则为新增操作
            if [[ -z `cat $properties_file | grep $properties_key` ]]; then
                operation_type=insert
            fi
            if [[ $operation_type == "insert" ]]; then
                #新增配置
                log "新增配置项：$properties_key=$properties_value 至 $properties_file"
                log "新增配置项描述：$properties_description"
                echo -e "\n$property_content" >> $properties_file
            elif [[ $operation_type == "update" ]]; then
                #修改配置
                log "修改配置项：$properties_key=$properties_value 至 $properties_file"
                log "修改配置项描述：$properties_description"
                quotation="'"
                property_content=${property_content//#/\\#}
                cmd="sed -i s#^$properties_key.*#$quotation$property_content$quotation#g $properties_file"
                eval $cmd
            fi
        done < $current_path/bin/properties.update
        log "配置升级处理完成"
    fi
fi

#--------------------------------------------执行安装、升级任务-------------------------------------------
log "开始复制/bin目录"
rm -rf $ext_home/bin
\cp -rpf $current_path/bin $ext_home
log "复制/bin目录完成"

log "开始复制/lib目录"
rm -rf $ext_home/lib
\cp -rpf $current_path/lib $ext_home
log "复制/lib目录完成"

log "开始复制/conf目录"
if [[ $action == "install" ]]; then
    \cp -rpf $current_path/conf $ext_home
else
    log "升级程序需排除一下文件："
    log_command_result "cat $current_path/bin/exclude_conf.list"
    rsync -avP --exclude-from=$current_path/bin/exclude_conf.list $current_path/conf/ $ext_home/conf >> $install_log_file
fi
log "复制/conf目录完成"

log "开始复制/web目录"
rm -rf $ext_home/web
if [ -d $current_path/web ]; then
    \cp -rpf $current_path/web $ext_home
fi
log "复制/web目录完成"

if [ -f $current_path/bin/custom_install.sh ]; then
    log "执行ext定制安装脚本"
    sh $current_path/bin/custom_install.sh
    log "执行ext定制安装脚本完成"
fi

#------------------------------------------------启动程序------------------------------------------------
if [[ $action == "install" ]]; then
    log "安装完成，开始启动程序"
elif [[ $action == "update" ]]; then
    log "升级完成，开始启动程序"
fi
$ext_home/bin/ext-cli.sh restart
