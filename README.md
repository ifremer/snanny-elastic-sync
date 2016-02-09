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

<pre><code>mvn clean install -DskipTests</code></pre>

## Configuration
Configuration files are :
  - application.properties  
  	Configure elasticsearch entry point and owncloud entrypoint.
  	The properties can be externalized by coping the properties files in the same folder of the jar
  	
## First launch

- Create an elasticSearch index and configure the mapping (see mapping.json)
- Install the owncloud api plugin
- execute command line

## Parsers 

Requirements :
The elastic synchronisation allow to extend datatype used in parse mode with externals parser,
Parsers are loaded at runtime, using path describes in the application.properties

<code>
sync.parsers.folder=/path/to/parserLibs
</code>

Each parser can load properties by using the package name of the parser followed by the property name

Availables properties : 
- modulo


<code>
fr.ifremer.sensornanny.observation.parser.momar.MomarObservationParser.modulo=50  
fr.ifremer.sensornanny.observation.parser.netcdf.NetCdfObservationParser.modulo=20
</code>


If property is missing, the parser will not be included, and an error message will be displayed in log

<code>
GRAVE: Property named fr.ifremer.sensornanny.observation.parser.netcdf.NetCdfObservationParser.modulo not found in 'application.properties'
</code>

## Usage 

You need to add folder wich contains application.properties to the execution classpath

<pre><code>java -cp .;elastic-sync.jar fr.ifremer.sensornanny.sync.Main

 -h,--help                print this message
 -r,--range &lt;from&gt; &lt;to&gt;   Synchronize from owncloud to elasticsearch from date to date
 -s,--since &lt;period&gt;      Synchronize from owncloud to elasticsearch since a period
 -f,--relaunch_failure    Synchronize from owncloud to elasticsearch the last failed synchronization</code></pre>