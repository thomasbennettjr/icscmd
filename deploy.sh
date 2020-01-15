mvn validate
mvn test
mvn clean compile assembly:single

cp ./target/icscmd-1.0-jar-with-dependencies.jar ./icscmd.jar
cat stub.sh icscmd.jar > icscmd && chmod 777 icscmd
~/Downloads/launch4j/launch4j ./icscmd.xml

cp ./icscmd ~/packages/metaopsis-cli/Mac
cp ./icspasswd ~/packages/metaopsis-cli/Mac
cp ./icscmd ~/packages/metaopsis-cli/Linux
cp ./icspasswd ~/packages/metaopsis-cli/Linux
cp ./icspasswd.exe ~/packages/metaopsis-cli/Windows
cp ./icscmd.exe ~/packages/metaopsis-cli/Windows