rm -rf $JENKINS_HOME/plugins/tasks*

mvn clean install
cp -f target/tasks.hpi $JENKINS_HOME/plugins/

cd $JENKINS_HOME
./go.sh
