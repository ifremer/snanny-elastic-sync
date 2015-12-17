# snanny-elastic-sync
restful api to index observations from owncloud to elasticSearch

## Build and deploy
tested with java 1.8 and maven 3.0.4 or above

git clone https://github.com/ifremer/snanny-elastic-sync.git

mvn install -DskipTests

## Configuration
Configuration files are :
  - application.properties
  	Configure elasticsearch entry point and owncloud entrypoint
  	
## Usage 
java snanny-elastic-sync.jar sync 
