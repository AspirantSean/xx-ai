#!/bin/bash

#到工程的总目录下
path=$(cd `dirname $0`/../../..; pwd)
#参数赋值
for x in $@
do
    if [[ $x =~ ^tag.* ]]; then
        tag=${x#*tag=}
    elif [[ $x =~ ^release_version.* ]]; then
        release_version=${x#*release_version=}
    elif [[ $x =~ ^zip_password.* ]]; then
        zip_password=${x#*zip_password=}
    fi
done
#打包
mvn package -Dmaven.test.skip=true -Drat.skip=true -Dtag=${tag} -Drelease.version=${release_version} -f pom.xml
#生成zip压缩包文件
#删除历史程序包
if [ -d package ];then
	rm -rf package
fi
mkdir package

#将程序tar包移到package目录下
tar_name=`ls -l *-package/target | grep -E "ailpha-app.*.tar.gz$" | awk 'NR==1{print $9}'`
mv *-package/target/$tar_name package
cd package
fileMd5=`md5sum $tar_name | cut -d ' ' -f1`
echo $fileMd5 > md5.txt
#打成zip包
zip_name="${tar_name%-bin.tar.gz*}.zip"
if [[ -n $zip_password ]]; then
    zip -P $zip_password -r $zip_name $tar_name md5.txt
else
    zip -P "dbapp_ailpha@2020" -r $zip_name $tar_name md5.txt
fi
cd ..
mv package/$zip_name ./
rm -rf package
echo "===============package finished==============="
chmod -R 755 $zip_name
