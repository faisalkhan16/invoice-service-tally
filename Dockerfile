FROM openjdk:11
COPY target/*.jar einvoicing-tally.jar
ENTRYPOINT ["java","-jar","/einvoicing-tally.jar","-web -webAllowOthers -tcp -tcpAllowOthers -browser"]