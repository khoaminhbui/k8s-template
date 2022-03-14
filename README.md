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