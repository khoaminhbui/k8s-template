# k8s-template
# Getting started
Create a new namespace
```
$ kubectl create namespace notification
```
# Deploy containers to k8s cluster and expose to the outside world with NodePort
```
$ kubectl apply -f k8s-object
```
Verify deployments by command
```
$ curl <k8s-cluster-ip-address>:30333/api/checkNotification/refresh
```
# Expose using ingress-nginx
```
$ kubectl apply -f k8s-ingress-nginx
```
Open the browser and enter ```k8s-ingress-nginx-ip/api/checkNotification/refresh``` to see result.
# [Kong Ingress Controller](https://docs.konghq.com/kubernetes-ingress-controller/2.2.x/guides/overview/)
## Install Kong
Create `kong` namespace
```
$ kubectl create namespace kong
```
Install `kong` api gateway with postgres database
```
$ kubectl apply -f kong/all-in-one-postgres.yaml
```
Verify that `kong` is installed
```
$ kubectl -n kong get service kong-proxy
```
## Install Konga UI
```
$ kubectl apply -f kong/konga.yml
```
Create connection between Kong and Konga
- Open browser and navigate to http://<KONGA_SERVICE-IP>:30330 then create the first admin user.
- Next, create Connection to Kong API Admin by enter http://<KONG_PROXY-IP>:8001 to the Kong Admin URL section and click Create Connection button.
## Expose service through [Kong Gateway](https://docs.konghq.com/kubernetes-ingress-controller/2.2.x/guides/getting-started/#basic-proxy)
```
$ kubectl apply -f k8s-object
```
```
$ kubectl apply -f kong/kong-ingress.yaml
```
```
$ curl <KONG_PROXY-IP>/api/checkNotification/refresh
```
## Using [Kong Plugin](https://docs.konghq.com/kubernetes-ingress-controller/2.2.x/guides/using-kongplugin-resource/)
### CORS configuration
>Note: Change `config.origin` in the `kong-cors.yaml` to your cluster ip address
```
$ kubectl apply -f kong/kong-cors.yaml
```
### Rate limiting with [Redis](https://docs.konghq.com/kubernetes-ingress-controller/2.2.x/guides/redis-rate-limiting/)
Create `redis` service
```
$ kubectl apply -n kong -f https://bit.ly/k8s-redis
```
Apply global rate limit
```
$ kubectl apply -f kong/kong-rate-limit.yaml
```
