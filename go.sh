rm -rf $JENKINS_HOME/plugins/tasks*

mvn install || { echo "Build failed"; exit 1; }
 
cp -f target/*.hpi $JENKINS_HOME/plugins/

cd $JENKINS_HOME
./go.sh
