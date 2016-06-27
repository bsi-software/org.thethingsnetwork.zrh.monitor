FROM tomcat:jre8
MAINTAINER BSI-Business-Systems-Integration-AG

# option 1: install the application as /monitor
ADD org.thethingsnetwork.zrh.monitor.ui.html.app.dev-1.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/ttn-monitor.war

# option 2: install the application as root application
# RUN rm -r /usr/local/tomcat/webapps/ROOT
# ADD org.thethingsnetwork.zrh.monitor.ui.html.app.dev-1.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]