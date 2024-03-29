#!/bin/bash

# 定义脚本的用法函数
usage() {
  echo "必须参数: "
  echo "--wechat.webhook.url=<URL> 企业微信的webhook"
  echo "--wechat.webhook.template=<x.tmpl> 模板文件，使用markdown格式"
  echo "--server.port=<server_port> 服务器端口"
  echo "其他参数说明："
  echo "--customLabel=<key1,key2> 自定义label，可选"
  echo "--customAnnotations=<key1,key2> 自定义annotation，可选"
  exit 1
}

# 定义空的变量存放参数信息
url=''
template=''
serverPort=''
customLab=''
customAnnotations=''

# 解析命令行参数
for arg in "$@"; do
  case $arg in
    -h|--help)
      usage
      ;;
    --wechat.webhook.url=*)
      url=${arg#*=}
      shift
      ;;
    --wechat.webhook.template=*)
      template=${arg#*=}
      shift
      ;;
    --server.port=*)
      serverPort=${arg#*=}
      shift
      ;;
    --customLabel=*)
      customLabel=${arg#*=}
      shift
      ;;
    --customAnnotations=*)
      customAnnotations=${arg#*=}
      shift
      ;;
  esac
done

# 如果url、template和serverPort这三个关键参数缺失的话，就显示使用方法
if [ -z "$url" ] || [ -z "$template" ] || [ -z "$serverPort" ]; then
  echo "错误：需要url、模板参数和服务器端口参数"
  usage
fi

# 启动 Prometheus
java -jar prometheus-1.0-SNAPSHOT.jar --wechat.webhook.url=$url --wechat.webhook.template=$template --server.port=$serverPort --customLabel=$customLabel --customAnnotations=$customAnnotations
