FROM tomcat:7-jre8-alpine

LABEL maintainer="Francesc Busquets <francesc@gmail.com>"

EXPOSE 8080 3306
VOLUME ["/etc/mysql", "/var/lib/mysql"]

ENV DEBIAN_FRONTEND noninteractive
ENV MYSQL_DATABASE JClicReports
ENV MYSQL_USER jclic_user
ENV MYSQL_PASSWORD jclic_pwd
# ENV MYSQL_SAMPLE_DATA /root/testData.sql

# Retrieve last version of WAR file from clic.xtec.cat
ADD  https://clic.xtec.cat/dist/reports/jclicreports.war /usr/local/tomcat/webapps/reports.war

# Retrieve mysql connector and store it in Tomcat/lib
ADD https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.45/mysql-connector-java-5.1.45.jar /usr/local/tomcat/lib/mysql-connector-java.jar

# Install mySQL
RUN apk add --update mysql mysql-client ttf-dejavu && rm -f /var/cache/apk/*

COPY ./my.cnf /etc/mysql/my.cnf
COPY ./start.sh /root/start.sh
COPY ./loadSampleData.sh /root/loadSampleData.sh
RUN ln -s /root/loadSampleData.sh /usr/bin/loadSampleData
COPY ./testData.sql /root/testData.sql
COPY ./jclicReports.properties /root/jclicReports.properties

CMD [ "/root/start.sh" ]