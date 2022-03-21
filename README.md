# Reference
https://khoabui7.atlassian.net/wiki/spaces/PAN/pages/118554625/Kubernetes

# Getting started
Create a new namespace
```
$ kubectl create namespace notification
```

# Basic deploy pod to k8s cluster
```
$ kubectl apply -f k8s-object
```

After this step we will have:
- 2 pods containing the services.
- 2 service serv as interface for the pods.

## Verify services exposed via NodePort
Verify deployments by command
```
$ curl <k8s-cluster-ip-address>:30333/api/checkNotification/refresh
```

## Expose services using ingress-nginx
- Expose services via Ingress (prefered over NodePort).
- Refer to this for Ingress: https://khoabui7.atlassian.net/wiki/spaces/PAN/pages/118554625/Kubernetes#Ingress
- Install Kubernetes Ingress Controller
```
$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.1.1/deploy/static/provider/cloud/deploy.yaml
```
- Pre-flight check
```
A few pods should start in the ingress-nginx namespace
$ kubectl get pods --namespace=ingress-nginx
```
> If your Kubernetes cluster is a "real" cluster that supports services of type LoadBalancer, it will have allocated an external IP address or FQDN to the ingress controller. You can see that IP address or FQDN with the following command
```
$ kubectl get service ingress-nginx-controller --namespace=ingress-nginx
```
> It will be the EXTERNAL-IP field. If that field shows `<pending>`, this means that your Kubernetes cluster wasn't able to provision the load balancer (generally, this is because it doesn't support services of type LoadBalancer). You can still able access by `<k8s-cluster-master-ip-address:ingress-nginx-controller-node-port>`
- Apply the ingress rule
```
$ kubectl apply -f k8s-ingress-nginx
```
Open the browser and enter ```k8s-ingress-nginx-ip/api/checkNotification/refresh``` to see result.

# [Kong Ingress Controller](https://docs.konghq.com/kubernetes-ingress-controller/2.2.x/guides/overview/)
## Install Kong
- Create `kong` namespace
```
$ kubectl create namespace kong
```

- Install `kong` api gateway with postgres database
```
$ kubectl apply -f kong/all-in-one-postgres.yaml
```

- Verify that `kong` is installed
```
$ kubectl -n kong get service kong-proxy
```

## Install Konga UI
```
$ kubectl apply -f kong/konga.yml
```

Create connection between Kong and Konga
- Open browser and navigate to `http://<KONGA_SERVICE-IP>:30330` then create the first admin user.
- Next, create Connection to Kong API Admin by enter `http://<KONG_PROXY-IP>:8001` to the Kong Admin URL section and click Create Connection button.

## Expose service through [Kong Gateway](https://docs.konghq.com/kubernetes-ingress-controller/2.2.x/guides/getting-started/#basic-proxy)
- Expose API via Kong gateway
```
$ kubectl apply -f kong/kong-ingress.yaml
```

- Verify API
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
- Create `redis` service
```
$ kubectl apply -n kong -f https://bit.ly/k8s-redis
```

- Apply global rate limit
```
$ kubectl apply -f kong/kong-rate-limit.yaml
```

# Service mesh [Istio](https://istio.io/latest/docs/setup/getting-started/)
## Install Istio
- Download [Istio](https://github.com/istio/istio/releases) and add `istioctl` client to the path.
- Install `istio` on kubernetes cluster by command
```
$ istioctl install
```
## Inject Envoy sidecar
- Inject envoy sidecar to `notification` namespace
```
$ kubectl label namespace notification istio-injection=enabled --overwrite
```

- Recreate pod
```
$ kubectl delete -f k8s-object
$ kubectl apply -f k8s-object
```
## Open the application to outside traffic
- Associate this application with the Istio gateway
```
$ kubectl apply -f istio/istio-gateway.yaml
```

- Apply destination rule
```
$ kubectl apply -f istio/istio-destination-rule.yaml
```

- Apply virtual service
```
$ kubectl apply -f istio/check-notification-istio-virtual-service.yaml
$ kubectl apply -f istio/notification-istio-virtual-service.yaml
```

- Determining the Istio Gateway IP
```
$ kubectl get svc istio-ingressgateway -n istio-system
```

- Verify the traffic has been opened to the outside world
```
$ curl <ISTIO_GATEWAY-IP>/api/checkNotification/refresh
```

## Traffic Management
### [Destination Rule](https://istio.io/latest/docs/reference/config/networking/destination-rule/)
The Istio DestinationRule resource provides a way to configure traffic once it has been routed by a VirtualService resource. It’s can be used to configure load balancing, security and connection details like timeouts and maximum numbers of connections.

#### [Subset](https://istio.io/latest/docs/reference/config/networking/destination-rule/#Subset)
```
  subsets:
    - name: v1
      labels:
        version: v1
    - name: v2
      labels:
        version: v2
```

#### [Load balancer](https://istio.io/latest/docs/reference/config/networking/destination-rule/#LoadBalancerSettings)
```
     loadBalancer:
       simple: ROUND_ROBIN
```

### [Virtual Service](https://istio.io/latest/docs/reference/config/networking/virtual-service/)
Virtual Service acts in much the same capacity as a traditional Kubernetes Ingress resource, in that a VirtualService resource matches traffic and directs it to a Service resource.

#### [Route Destination](https://istio.io/latest/docs/reference/config/networking/virtual-service/#HTTPRouteDestination)
Apply the rule to send traffic to multiple version of Service
```
  http:
    - route:
      - destination:
          host: notification-service
          subset: v1
        weight: 40
      - destination:
          host: notification-service
          subset: v2
        weight: 60
```

#### [Network Fault](https://istio.io/latest/docs/reference/config/networking/virtual-service/#HTTPFaultInjection) 
A great way to test how applications respond to failed requests
```
      fault:
        delay:
          percentage:
            value: 30
          fixedDelay: 5s
```

#### [HTTPRoute](https://istio.io/latest/docs/reference/config/networking/virtual-service/#HTTPRoute)
Describes match conditions and actions for routing traffic, including rewrite url, cors policy, fault injection, timeout,…
```
      match:
        - uri:
            prefix: "/api/checkNotification/showUnreadNotification"
      rewrite:
        uri: "/api/checkNotification/show"
```
```
      corsPolicy:
        allowOrigins:
          - exact: http://<your-origin-ip>
        allowMethods:
          - POST
        allowHeaders:
          - "Content-Type"
```
```
      fault:
        delay:
          percentage:
            value: 30
          fixedDelay: 3s
        abort:
          percentage:
            value: 10
          httpStatus: 502
```

## Telemetry with Kiali
> Istio integrates with [several](https://istio.io/latest/docs/ops/integrations/) different telemetry applications. These can help you gain an understanding of the structure of your service mesh, display the topology of the mesh, and analyze the health of your mesh.

- Install Kiali and the other addons and wait for them to be deployed.
```
$ kubectl apply -f addons
$ kubectl rollout status deployment/kiali -n istio-system
Waiting for deployment "kiali" rollout to finish: 0 of 1 updated replicas are available...
deployment "kiali" successfully rolled out
```

- Access the Kiali dashboard.
```
$ istioctl dashboard kiali
```
> To see trace data, you must send requests to your service. The number of requests depends on Istio’s sampling rate. You set this rate when you install Istio. The default sampling rate is 1%. You need to send at least 100 requests before the first trace is visible. To send a 100 requests to the `checkNotification` service, use the following command
```
$ for i in $(seq 1 100); do curl -s -o /dev/null "http://<ISTIO_GATEWAY-IP>/api/checkNotification/refresh"; done
```

## Integrating Kong
- Enable the namespace for the Istio mesh
```
$ kubectl label namespace kong istio-injection=enabled
```
- Reapply all `kong` kubernetes object
```
$ kubectl delete -f kong/all-in-one-postgres.yaml
$ kubectl apply -f kong/all-in-one-postgres.yaml
```
- Recreate pod and service
```
$ kubectl delete -f k8s-object
$ kubectl apply -f k8s-object
$ kubectl apply -f kong-istio
```
- Verify traffic
```
$ curl <KONG_PROXY-IP>/api/checkNotification/refresh
```
