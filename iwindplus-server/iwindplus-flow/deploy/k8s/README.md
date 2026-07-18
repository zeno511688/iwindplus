firewall-cmd --permanent --zone=trusted --add-source=172.18.136.156
firewall-cmd --permanent --zone=trusted --add-source=172.18.136.157
firewall-cmd --permanent --zone=trusted --add-source=172.18.136.158
firewall-cmd --permanent --zone=trusted --add-source=172.18.136.159
firewall-cmd --permanent --zone=trusted --add-source=172.18.136.163
# 增加防火墙规则
firewall-cmd --permanent --direct --add-rule ipv4 filter INPUT 1 -j ACCEPT -m comment --comment "kube-proxy redirects"
firewall-cmd --permanent --direct --add-rule ipv4 filter FORWARD 1  -j ACCEPT -m comment --comment "docker subnet"
# 设置防火墙伪装ip, 打开NAT，默认是关闭状态
firewall-cmd --add-masquerade --permanent

# 所有k8s的NodePort端口添加到例外
firewall-cmd --permanent --zone=public --add-port=30000-32767/tcp

systemctl restart firewalld
firewall-cmd --permanent --zone=public --add-port=8777/tcp
firewall-cmd --permanent --zone=public --add-port=4443/tcp
firewall-cmd --permanent --zone=public --add-port=7848/tcp
firewall-cmd --permanent --zone=public --add-port=8848/tcp
firewall-cmd --permanent --zone=public --add-port=9848/tcp
firewall-cmd --permanent --zone=public --add-port=9849/tcp
firewall-cmd --permanent --zone=public --add-port=2376/tcp
firewall-cmd --permanent --zone=public --add-port=2379/tcp
firewall-cmd --permanent --zone=public --add-port=2380/tcp
firewall-cmd --permanent --zone=public --add-port=8472/tcp
firewall-cmd --permanent --zone=public --add-port=4789/tcp
firewall-cmd --permanent --zone=public --add-port=9099/tcp
firewall-cmd --permanent --zone=public --add-port=9796/tcp
firewall-cmd --permanent --zone=public --add-port=10250/tcp
firewall-cmd --permanent --zone=public --add-port=10254/tcp
firewall-cmd --permanent --zone=public --add-port=6443/tcp
firewall-cmd --permanent --zone=public --add-port=6783/tcp
firewall-cmd --permanent --zone=public --add-port=6784/tcp
firewall-cmd --permanent --zone=public --add-port=9443/tcp
firewall-cmd --permanent --zone=public --add-port=9091/tcp
firewall-cmd --permanent --zone=public --add-port=9100/tcp
firewall-cmd --permanent --zone=public --add-port=9101/tcp
firewall-cmd --permanent --zone=public --add-port=18080/tcp
firewall-cmd --permanent --zone=public --add-port=9093/tcp
firewall-cmd --permanent --zone=public --add-port=3000/tcp
firewall-cmd --permanent --zone=public --add-port=9201/tcp
firewall-cmd --permanent --zone=public --add-port=15010/tcp
firewall-cmd --permanent --zone=public --add-port=15012/tcp
firewall-cmd --permanent --zone=public --add-port=15014/tcp
firewall-cmd --permanent --zone=public --add-port=15017/tcp
firewall-cmd --permanent --zone=public --add-port=53/tcp
firewall-cmd --permanent --zone=public --add-port=15001/tcp
firewall-cmd --permanent --zone=public --add-port=15090/tcp
firewall-cmd --permanent --zone=public --add-port=15004/tcp
firewall-cmd --permanent --zone=public --add-port=15006/tcp
firewall-cmd --permanent --zone=public --add-port=15008/tcp
firewall-cmd --permanent --zone=public --add-port=15009/tcp
firewall-cmd --permanent --zone=public --add-port=15020/tcp
firewall-cmd --permanent --zone=public --add-port=15021/tcp
firewall-cmd --reload


docker run -d --name nacos --restart=always -p 8848:8848 \
-e MODE=standalone \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=172.18.136.156 \
-e MYSQL_SERVICE_PORT=3317 -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_USER=nacos \
-e MYSQL_SERVICE_PASSWORD=iwindplus123456@ \
-v /etc/localtime:/etc/localtime:ro \
-v /usr/local/nacos/data:/home/nacos/data \
-v /usr/local/nacos/logs:/home/nacos/logs \
nacos/nacos-server:1.4.2

---
## 一、自定义mysql主从
# 主库操作
grant replication slave on *.* to 'slave'@'%' identified by 'zaq12wsx';
flush privileges;

# 从库操作
change master to master_host='mysql-master-1-0.mysql-master-1.middle.svc.cluster.local',master_user='slave',master_password='zaq12wsx',master_port=3317,master_log_file='master-1-bin.000002',master_log_pos=975;
start slave;
----

## 二、radondb安装mysql

helm repo add radondb https://radondb.github.io/radondb-mysql-kubernetes/

helm search repo

helm install mysql-server radondb/mysql-operator

## redis安装
#进入容器创建集群命令
redis-cli --cluster create redis-cluster-0.redis-cluster.middle.svc.cluster.local:6379 redis-cluster-1.redis-cluster.middle.svc.cluster.local:6379 redis-cluster-2.redis-cluster.middle.svc.cluster.local:6379 redis-cluster-3.redis-cluster.middle.svc.cluster.local:6379 redis-cluster-4.redis-cluster.middle.svc.cluster.local:6379 redis-cluster-5.redis-cluster.middle.svc.cluster.local:6379 --cluster-replicas 1 -a zaq12wsx
nslookup redis-cluster-0.redis-cluster.middle.svc.cluster.local

#执行创建集群命令
kubectl exec -it redis-cluster-0 -n middle -- redis-cli -a zaq12wsx --cluster create --cluster-replicas 1 $(kubectl get pods -l app=redis-cluster -n middle -o jsonpath='{range.items[*]}{.status.podIP}:6379 {end}')

kubectl exec -it redis-cluster-0 -n middle -- redis-cli -a zaq12wsx cluster info
#可以进入任意一个节点pod中进行验证集群状态
kubectl exec -it redis-cluster-0 -n middle -- redis-cli -a zaq12wsx --cluster check  $(kubectl get pods -l app=redis-cluster -n middle -o jsonpath='{range.items[0]}{.status.podIP}:6379 {end}')

#进入容器
kubectl exec -it redis-cluster-0 -n middle bash