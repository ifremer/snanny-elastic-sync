# snanny-elastic-sync
batch to index observations from owncloud to elasticSearch

## Development 
Two librairies are not stored on nexus, before develop, you will have ton install them into your local repository using this commands lines 
<pre>
<code>mvn install:install-file -Dfile=lib/skosapi.jar -DgroupId=org.semanticweb -DartifactId=skos -Dversion=1.0.0 -DgeneratePom=true -Dpackaging=jar

mvn install:install-file -Dfile=lib/owlapi.jar -DgroupId=org.semanticweb -DartifactId=owlapi -Dversion=2.2.0 -DgeneratePom=true -Dpackaging=jar
</code>
</pre>
## Build and deploy
tested with java 1.8 and maven 3.0.4 or above

git clone https://github.com/ifremer/snanny-elastic-sync.git

Install the non installed librairies in your local repository (have to be done the first time only)
<pre><code>mvn install:install-file -Dfile=lib/skosapi.jar -DgroupId=org.semanticweb -DartifactId=skos -Dversion=1.0.0 -DgeneratePom=true -Dpackaging=jar

mvn install:install-file -Dfile=lib/owlapi.jar -DgroupId=org.semanticweb -DartifactId=owlapi -Dversion=2.2.0 -DgeneratePom=true -Dpackaging=jar</code></pre>
Then : 

<pre><code>mvn install -DskipTests</code></pre>

## Configuration
Configuration files are :
  - application.properties
  	Configure elasticsearch entry point and owncloud entrypoint
  	
## First launch

- Create an elasticSearch index and configure the mapping (see mapping.json)
- Install the owncloud api plugin
- execute command line
 	
## Usage 
<pre><code>java -jar elastic-sync.jar 

 -h,--help                print this message
 -r,--range &lt;from&gt; &lt;to&gt;   Synchronize from owncloud to elasticsearch from date to date
 -s,--since &lt;period&gt;      Synchronize from owncloud to elasticsearch since a period
 -f,--relaunch_failure    Synchronize from owncloud to elasticsearch the last failed synchronization</code></pre>
 
