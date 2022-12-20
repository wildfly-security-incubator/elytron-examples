# Self service quickstart

## Prerequisites

Install [mvn](https://maven.apache.org/)

### Run and configure wildfly:

Run wildfly server and then move to source folder of this example. To configure elytron:
```
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=configure.cli
```

## Production Deployment
```
mvn clean package install wildfly:deploy
```
#### Now you can access the the quickstart at [http://localhost:8080/self-service](http://localhost:8080/self-service) 

To login: **jane** with password **passwordJane**

### To restore wildfly to its initial configuration

Undeploy the package, then you can use restore.cli file:

```
mvn wildfly:undeploy
{path_to_wildfly}/bin/jboss-cli.sh --connect --file=restore.cli (this will restore previous changes to server)
```

## Development Guide

Install [yarn](https://yarnpkg.com/en/)
 
### Go to source folder of this example and run: 
```
mvn clean install wildfly:deploy
```
### Then to run webpack and develop front end:
```
cd src/main/frontend
yarn install
yarn start 
```
#### Now you can access the page in your browser at [http://localhost:3000/](http://localhost:3000/) 
