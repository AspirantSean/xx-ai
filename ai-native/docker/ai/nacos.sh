#!/bin/bash

# 从环境变量获取Nacos配置信息
NACOS_SERVER_ADDRESS="${NACOS_SERVER_ADDRESS}"
NACOS_SERVER_PORT="${NACOS_SERVER_PORT}"
NACOS_CONF_NAMESPACE_ID="${NACOS_CONF_NAMESPACE_ID}"
NACOS_CONF_GROUP="${NACOS_CONF_GROUP}"
NACOS_USERNAME="${NACOS_AUTH_IDENTITY_KEY}"
NACOS_PASSWORD="${NACOS_AUTH_IDENTITY_VALUE}"

# Nacos配置信息
NACOS_CONFIG_ENDPOINT="http://${NACOS_SERVER_ADDRESS}:${NACOS_SERVER_PORT}"
NACOS_CONFIG_NAMESPACE="${NACOS_CONF_NAMESPACE_ID}"
NACOS_CONFIG_GROUP="${NACOS_CONF_GROUP}"

# 登录获取accessToken
LOGIN_RESULT=$(curl -s -X POST "${NACOS_CONFIG_ENDPOINT}/nacos/v1/auth/login" -d "username=${NACOS_USERNAME}&password=${NACOS_PASSWORD}")
echo "$LOGIN_RESULT"
ACCESS_TOKEN=$(echo "$LOGIN_RESULT" | awk -F'"' '/accessToken/{print $4}')

# 检查登录结果
if [ -z "$ACCESS_TOKEN" ]; then
    echo "Nacos登录失败，请检查用户名和密码。"
    exit 1
fi

# 获取配置
NACOS_GET_COMMAND="curl -s -X GET '${NACOS_CONFIG_ENDPOINT}/nacos/v2/cs/config?dataId=flex-ai.yaml&group=${NACOS_CONFIG_GROUP}&namespaceId=${NACOS_CONFIG_NAMESPACE}&accessToken=${ACCESS_TOKEN}'"
GET_CONFIG_RESULT=$(eval "$NACOS_GET_COMMAND")

# 解析返回结果
CODE=$(echo "$GET_CONFIG_RESULT" | grep -o '"code":[^,]*' | awk -F':' '{print $2}' | tr -d '"')
MESSAGE=$(echo "$GET_CONFIG_RESULT" | grep -o '"message":[^,]*' | awk -F':' '{print $2}' | tr -d '"')


# 检查是否成功获取配置
if [ "$CODE" == "0" ] && [ "$MESSAGE" == "success" ]; then
    echo "存在flex-ai.yaml配置"
elif [ "$CODE" == "20004" ] ; then
    echo "nacos 无存在flex-ai.yaml配置，推送配置"
    # 本地配置文件路径
    LOCAL_CONFIG_FILE="/usr/local/config_update/application.yml"

    # 读取本地配置文件内容
    CONFIG_CONTENT=$(cat "$LOCAL_CONFIG_FILE")

    # 发布配置
    NACOS_POST_COMMAND="curl -s -d 'type=yaml' -d 'dataId=flex-ai.yaml' -d 'group=${NACOS_CONFIG_GROUP}' -d 'namespaceId=${NACOS_CONFIG_NAMESPACE}' --data-urlencode 'content=${CONFIG_CONTENT}' -X POST '${NACOS_CONFIG_ENDPOINT}/nacos/v2/cs/config?accessToken=${ACCESS_TOKEN}'"
    POST_CONFIG_RESULT=$(eval "$NACOS_POST_COMMAND")
    echo "$POST_CONFIG_RESULT"
else
    # 其他情况，输出返回结果并退出
    echo "从Nacos获取配置时发生错误:"
    echo "$GET_CONFIG_RESULT"
    exit 1
fi
