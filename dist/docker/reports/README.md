# Docker container with Tomcat 7, MariaDB and JClic Reports

### Usage:

- Install [Docker](https://www.docker.com/) CE or EE on your system. See detailed instructions for:
  - [Linux](https://docs.docker.com/install/linux/docker-ce/ubuntu/)
  - [Windows](https://docs.docker.com/docker-for-windows/install/)
  - [MacOS](https://docs.docker.com/docker-for-mac/install/)

- Create the docker container:
```
docker run -d --name reports -p 8080:8080 -p 3306:3306 projectestac/jclicreports
```

This will download the docker image and start the container for first time on your system. An empty instance of JClic Reports will be available at:
http://localhost:8080

If you want to see JClic Reports in action with sample data, just launch:
```
docker exec reports loadSampleData
```

You will have also an instance of MySQL/MariaDB running on your system, using the standard port 3306. You can change this port number (and also the 8080 service port) to avoid conflicts with other instances of same o similar services already running.





## Advanced usage

Build the Docker image with:
```
docker build -t projectestac/jclicreports .
```

Launch docker with:
```
docker run -d --name reports -p 8080:8080 -p 3306:3306 projectestac/jclicreports
```

List containers with:
```
docker ps -a
```

Enter container with:
```
docker exec -it reports bash
```

Clear current container and image with:
```
docker stop reports
docker rm reports
docker rmi projectestac/jclicreports
```

Test JClic Reports Server in: http://localhost:8080/reports

