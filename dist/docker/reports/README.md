# Docker container with Tomcat 7, MariaDB and JClic Reports

## Usage:

- Install [Docker](https://www.docker.com/) CE or EE on your system. See detailed instructions for:
  - [Linux](https://docs.docker.com/install/linux/docker-ce/ubuntu/)
  - [Windows](https://docs.docker.com/docker-for-windows/install/)
  - [MacOS](https://docs.docker.com/docker-for-mac/install/)

- Create and start the docker container:
```bash
$ docker run -d --name reports -p 8080:8080 projectestac/jclic-reports
```
This will download the docker image and start the container for first time on your system. An empty instance of JClic Reports will be available at:
http://localhost:8080/reports

If you want to load some sample data to see JClic Reports in action, just launch:
```bash
$ docker exec reports loadSampleData
```

## Useful commands:

To stop the container, preservig all collected data:
```bash
$ docker stop reports
```

To start the container again:
```bash
$ docker start reports
```

To delete the container, loosing all data:
```bash
# Stop the container instance if already running
$ docker stop reports
# Remove the container instance
$ docker rm reports
# Optional: remove also the downloaded base image
$ docker rmi projectestac/jclic-reports
```

## Advanced usage

### Store MySQL data on your local filesystem

All the collected data is stored by default inside the container instance. So, when the container is removed (with `docker rm` or similar) all data will be lost. To avoid this, you can use a local folder on your filesystem to store the database files:

```bash
# Stop and remove the existing container instance, if any (see above)
# Enter the base directory where the database data will be stored:
$ cd /my/data/storage/unit
# Create a specific directory for MySQL data:
$ mkdir mysql-jclic
# Create a container instance using this directory:
$ docker run -d --name reports --mount type=bind,source="$(pwd)"/mysql,target=/var/lib/mysql -p 8080:8080 projectestac/jclic-reports
```

For a more advanced use of Docker volumes see: https://docs.docker.com/storage/volumes/

### Expose the MySQL port

The container can also expose a running instance of MySQL/MariaDB to your system. To activate this feature, just add `-p 3306:3306` (near the current `-p 8080:8080`) to your `docker run` command.

Of course, you can redirect the default ports 8080 and 3306 to any other port of your choice. For example, use `-p 8080:80` to redirect the service to the default HTTP port of your host, so JClic Reports will be available on http://localhost/reports

### Customize your image


## Advanced usage

Build the Docker image with:
```
docker build -t projectestac/jclicreports .
```

Launch docker with:
```
docker run -d --name reports -p 8080:8080  projectestac/jclicreports
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

