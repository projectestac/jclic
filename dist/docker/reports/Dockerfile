FROM mdelapenya/tomcat-mysql

COPY ./entrypoint.sh /root/entrypoint.sh
COPY ./createDatabase.sql /root/createDatabase.sql
COPY ./testData.sql /root/testData.sql
COPY ./jclicReports.properties /root/jclicReports.properties

# Retrieve last version of WAR file from clic.xtec.cat
ADD  https://clic.xtec.cat/dist/reports/jclicreports.war /opt/apache-tomcat-7.0.77/webapps/reports.war


# Install libmysql-java and create a symlink in Tomcat lib
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && \
  apt-get -y install libmysql-java && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
  cd /opt/apache-tomcat-7.0.77/lib && \
  ln -s /usr/share/java/mysql-connector-java.jar .

ENTRYPOINT [ "/root/entrypoint.sh" ]
