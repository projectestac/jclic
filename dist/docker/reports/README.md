

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

