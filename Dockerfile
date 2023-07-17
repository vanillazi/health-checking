FROM library/openjdk
ADD target/health-checking-0.0.1-SNAPSHOT.jar /app.jar
ADD checking.sh /checking.sh
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s CMD /checking.sh
ENTRYPOINT ["java","-jar","/app.jar"]
