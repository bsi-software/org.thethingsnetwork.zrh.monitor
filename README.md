# org.thethingsnetwork.zrh.monitor
A TheThingsNetwork monitor implemented with Eclipse Scout

## Screenshots
<img src="https://github.com/BSI-Business-Systems-Integration-AG/org.thethingsnetwork.zrh.monitor/blob/master/screenshots/ttn_monitor_home.png" width="400" margin="20">
<img src="https://github.com/BSI-Business-Systems-Integration-AG/org.thethingsnetwork.zrh.monitor/blob/master/screenshots/ttn_monitor_livemap.png" width="400" margin="20">

<img src="https://github.com/BSI-Business-Systems-Integration-AG/org.thethingsnetwork.zrh.monitor/blob/master/screenshots/ttn_monitor_gateways.png" width="400">
<img src="https://github.com/BSI-Business-Systems-Integration-AG/org.thethingsnetwork.zrh.monitor/blob/master/screenshots/ttn_monitor_messages.png" width="400">

## Build, Test and Upload the Docker Image

Disclaimer: The description below has been tested on a Windows box only.

### How to get docker up and running in console?
start docker using the quickstart terminal
open a shell and configure it for docker (using bash shell for the examples below)

```
eval $(docker-machine env default --shell bash)
```

### How to build the docker image?
```
cd <path to your local git repo>/ttn_monitor/org.thethingsnetwork.zrh.monitor.ui.html.app.dev
mvn clean package docker:build
```

for more infos see the [spotify docs](https://github.com/spotify/docker-maven-plugin).

### How check that the docker image has been built?
```
docker images
```

### How to run the ttn container?
```
docker run --name ttn -p 8085:8080 -d eclipsescout/org.thethingsnetwork.zrh.monitor:latest
```
**--name**: assignes a name (can later be used to stop/restart/... this container) <br>
**-p**: maps the container internal port 8080 to the externally visible port 8085 <br>
**-d**: runs the container in demon mode

### How to get the IP of the running container?
```
docker-machine ls
```
produces an output similar to the one below
```
NAME      ACTIVE   URL          STATE     URL                               SWARM   DOCKER   ERRORS
default   *        virtualbox   Running   tcp://<container-ip-address>:2376         v1.9.1
```

### How to connect to the application in the container?
```
<container-ip-address>:8085/ttn-monitor
```
Enter the above address in your browser, you should then see the application

### How to check logs in the running container?
```
docker exec -it ttn bash
```
This will open a shell inside the ttn container. In this shell you can then look around the container
```
ls -lart logs/
exit
```

### How to push the image to dockerhub?
```
docker login --username=eclipsescout --email=scout@bsi-software.com https://index.docker.io/v1/
docker push eclipsescout/org.thethingsnetwork.zrh.monitor
```
Then verify, the image is available on Dockerhub via the link below

[https://hub.docker.com/r/eclipsescout/org.thethingsnetwork.zrh.monitor/](https://hub.docker.com/r/eclipsescout/org.thethingsnetwork.zrh.monitor/)

### How to cleanup?
```
docker logout
docker stop ttn
docker rm ttn
docker rmi eclipsescout/org.thethingsnetwork.zrh.monitor:latest
```

## Troubleshooting

### Docker Login
In case of docker login issues there might be a problem with the default index registry, see the hints provided on [Stackoverflow](http://stackoverflow.com/questions/33748919/why-does-docker-login-fail-in-docker-quickstart-terminal-but-work-from-within)

### Building the image with the Spotify Docker plugin
At times changes of the app don't make it into the docker image. In this case, there might be some old jars cached.
Things to try:
-	Use mvn clean on the top level pom and rebuild from there
-	Delete the content in the local .m2 repo:  C:\Users\mzi\.m2\repository\org\thethingsnetwork
-	Rebuild from top level pom
-	Rebuild the html.app.dev to rebuild the docker image

