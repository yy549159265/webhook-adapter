文件说明：  
```bash
code：源码
  ├── src/
  │   ├── main/
  │   └── test/
  ├── pom.xml
  ├── README.md
setup：可直接安装文件，包含启动脚本，dockerfile，jar包
```

从头使用说明：
1. 使用以下命令用maven构建项目：
    ```bash
    mvn clean package
    ```
2. 将生成的jar文件、Dockerfile文件以及prometheus-up.sh脚本放入同一目录，并在该目录下执行以下命令构建Docker镜像：
    ```bash
    docker build -t webhook-adapter:v1.0 .
    ```
3. K8S部署  
3.1 创建ntp.tmpl和新的configmap：
   ```bash
    下面这些变量是默认的，不用自己定义，持续时间是开始时间-结束时间
    # 告警信息
    **告警名称：** ${alertname}
    **名字空间：** ${namespace}
    **严重程度：** ${severity}
    **状态：** ${status}
    **开始时间：** ${startTime}
    **结束时间：** ${endTime}

    # 描述信息
    **动作建议：** ${action}
    **问题描述：** ${description}
    **问题详情：** ${message}
    **问题总结：** ${summary}

    # 时间信息
    **当前时间：** ${currentTime}
    **持续时间：** ${duringTime}
    ```
    ```bash
    kubectl delete -n monitoring cm ntp-configmap
    kubectl create configmap ntp-configmap --from-file=/root/ntp.tmpl -n monitoring
    ```
    3.2 部署adapter和svc
    部署你自己的adapter和svc，可以参考以下命令和yaml文件：
    ```bash
    cat > ntp-webhook-wechat.yaml <<'EOF'
    apiVersion: apps/v1
    kind: Deployment
    metadata:
    name: webhook-wechat
    namespace: monitoring
    labels:
        app: webhook-wechat
    spec:
    replicas: 1
    selector:
        matchLabels:
        app: webhook-wechat
    template:
        metadata:
        labels:
            app: webhook-wechat
        spec:
        # 每次都部署到120上方便测试了
        nodeSelector:
            disktype: k8s120
        volumes:
        - name: config-volume
            configMap:
            name: ntp-configmap
        containers:
        - name: webhook-wechat
            image: webhook-adapter:v1.0
            imagePullPolicy: IfNotPresent
            args:  
            - --wechat.webhook.url=https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=27c8fca6-b8fe-4e93-ab21-078bd999d20d
            - --wechat.webhook.template=ntp.tmpl
            - --server.port=8081
            ports:
            - containerPort: 8081
            name: wechat
            volumeMounts:
            - name: config-volume
            mountPath: /usr/app/template
    EOF
    kubectl apply -f ntp-webhook-wechat.yaml

    cat > ntp-webhook-wechat-service.yaml <<'EOF'
    apiVersion: v1
    kind: Service
    metadata:
    name: webhook-wechat
    namespace: monitoring
    spec:
    selector:
        app: webhook-wechat
    ports:
    - name: wechat
        port: 9080
        targetPort: wechat
    EOF

    kubectl apply -f ntp-webhook-wechat-service.yaml
    ```
    3.3最后配置接收器和路由(注意替换配置文件中的10.107.92.68为实际的webhook-wechat的svc的IP)
    ```bash
    kubectl get svc,pod -n monitoring |grep webhook

    cat > ntp-alertmanager-config.yaml <<'EOF'
    apiVersion: monitoring.coreos.com/v1alpha1
    kind: AlertmanagerConfig
    metadata:
    name: ntp
    namespace: monitoring
    labels:
        alertmanagerConfig: example
    spec:
    receivers:
    - name: webhook
        webhookConfigs:
        # webhook-wechat的svc的ip
        - url: 'http://10.107.92.68:9080/alert' 
        sendResolved: true
        maxAlerts: 1
    route:
        groupBy:
        - alertname
        receiver: webhook
        groupWait: 1m
        groupInterval: 2m
        repeatInterval: 5m
        matchers:
        - name: service
        value: ntp
        matchType: "="
        continue: false
    EOF
    kubectl apply -f ntp-alertmanager-config.yaml
    ```
镜像webhook-adapter:v1.0参数说明：  
    必须参数：  
    --wechat.webhook.url=<URL>: 这是企业微信的webhook URL，不能省略。  
    --wechat.webhook.template=<x.tmpl>: 这是模板文件的名称，它使用的是markdown格式，不能省略。  
    --server.port=<prot>: 这是springboot启动监听端口，不能省略。
    可选参数：  
    --customLabel=<key1,key2>: 如果您有需要增加的自定义label，可以用这个参数，它是可选的。  
    --customAnnotations=<key1,key2>: 如果您有需要增加的自定义annotation，可以用这个参数，它是可选的。  
