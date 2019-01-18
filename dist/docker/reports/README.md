# Docker container with Tomcat 7, MariaDB and JClic Reports

## Usage:

- Install [Docker](https://www.docker.com/) on your system. See detailed instructions for:
  - [Linux](https://docs.docker.com/install/linux/docker-ce/ubuntu/)
  - [Windows](https://docs.docker.com/docker-for-windows/install/)
  - [MacOS](https://docs.docker.com/docker-for-mac/install/)

- Create your docker container for projectestac/jclic-reports:
```bash
$ docker run -d --name reports -p 8080:8080 projectestac/jclic-reports
```
This command downloads the docker image, creates a container on your system (named "reports") and automatically starts it. JClic Reports will be then available at:
http://localhost:8080/reports

JClic Reports has no data on first launch. So, there are no groups, no users and no reports data at all, but you can load a set of sample data to see it in action. To do that, just launch:
```bash
$ docker exec reports loadSampleData
```

## Useful commands:

To use JClicReports in another language, go to:

http://localhost:8080/reports/main?lang=xx

... replacing `xx` by `ca` (Catalan), `es` (Spanish) or `en` (English)


To stop the container, preservig all collected data:
```bash
$ docker stop reports
```

To start the container again:
```bash
$ docker start reports
```
This command should be launched after each restart of your host system, unless you create your container in [automatic restart](https://docs.docker.com/config/containers/start-containers-automatically/) mode.


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

### Storing the MySQL data files on your local filesystem

Collected data is stored by default inside your container instance. So, when the container is removed (with `docker rm` or similar) all data will be lost. To avoid this, you can design a local folder on your filesystem to be used for storing the database files:

```bash
# Stop and remove the existing container instance, if any
$ docker stop reports
$ docker rm reports
# Replace "/my/data/directory" by a real directory on your file system:
$ cd /my/data/directory
# Create a specific directory for the JClic Reports database data:
$ mkdir mysql-jclic
# Create a new container instance using this directory.
# On Windows systems replace "$(pwd)" with "%cd%" (Windows cmd) or "${PWD}" (PowerShell):
$ docker run -d --name reports --mount type=bind,source=$(pwd)/mysql-jclic,target=/var/lib/mysql -p 8080:8080 projectestac/jclic-reports
```

For a more advanced use of Docker volumes see: https://docs.docker.com/storage/volumes/

### Exposing the MySQL port

The container can also expose a running instance of MySQL/MariaDB to your system. To activate this feature, just add `-p 3306:3306` (near the current `-p 8080:8080`) to your `docker run` command.

Of course, you can redirect the default ports 8080 and 3306 to any other port of your choice. For example, use `-p 8080:80` to redirect the service to the default HTTP port on your host, so JClic Reports will be available at: http://localhost/reports

### Customizing your Docker image

JClic is an open source system, so you can costumize the original docker image to fit specific needs (using another kind of database or another J2EE server, serving on different ports, changing database user and password, providing custom data...)

The components used to create the Docker image are available at:

https://github.com/projectestac/jclic/tree/master/dist/docker/reports


You can clone the repository, edit `Dockerfile` and other components, and build your Docker image with:
```
# Replace "mydockername" with your docker user id
docker build -t mydockername/jclic-reports .
```

Launch docker with:
```
docker run -d --name reports -p 8080:8080  mydockername/jclic-reports
```

List all current containers with:
```
docker ps -a
```

Init a shell session into the container with:
```
docker exec -it reports bash
```

Clear current container and image with:
```
docker stop reports
docker rm reports
docker rmi mydockername/jclic-reports
```
