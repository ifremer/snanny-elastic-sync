# snanny-elastic-sync
batch to index observations from owncloud to elasticSearch

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
<pre><code>
java -jar elastic-sync.jar 

 -h,--help                print this message
 -r,--range &lt;from&gt; &lt;to&gt;   Synchronize from owncloud to elasticsearch from date to date
 -s,--since &lt;period&gt;      Synchronize from owncloud to elasticsearch since a period
 -f,--relaunch_failure    Synchronize from owncloud to elasticsearch the last failed synchronization
</code></pre>
 
