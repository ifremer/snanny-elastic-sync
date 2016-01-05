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
  	
## First launch

- Create an elasticSearch index and configure the mapping (see mapping.json)
- Install the owncloud api plugin
- execute command line
 	
## Usage 
java -jar elastic-sync.jar 

|option | detail|
| -h,--help              |  print this message|
| -r,--range <from> <to> |  Synchronize from owncloud to elasticsearch since a period|
| -s,--since <period>    |  Synchronize from owncloud to elasticsearch since a period|
| -f,--relaunch_failure  |  Synchronize from owncloud to elasticsearch the last failed synchronization|
