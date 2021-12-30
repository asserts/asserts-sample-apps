Karte is a demo app for Asserts.

Installing
==========
These instructions assume you are running a local kind cluster.
From this directory, run:

1. ``docker build -t 543343501704.dkr.ecr.us-west-2.amazonaws.com/ai.asserts.karte .``

2. ``kind load docker-image 543343501704.dkr.ecr.us-west-2.amazonaws.com/ai.asserts.karte``

3. ``brew install istioctl``

4. ``istioctl operator init``

5. ``kubectl create ns istio-system``

6. ``kubectl create ns prometheus``

7. ``kubectl create ns karte-dev``

8. ``kubectl label namespace default istio-injection=enabled --overwrite``

9. ``cd helm``

10. ``kubectl apply -f karte-umbrella/istio-operator.yaml`` (note that the Istio operator must be installed before the application so that the Envoy sidecar is injected into each application pod).

11. ``helm dep up karte-umbrella``

12. ``helm install karte-umbrella --generate-name``

PyCharm
=======
To use with PyCharm, install the Poetry plugin.
