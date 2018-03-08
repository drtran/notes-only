node {
   def mvnHome
   stage('Preparation') { 
      git 'https://github.com/drtran/forked-spring-petclinic.git'
      mvnHome = tool 'M3'
   }
   stage('Scan with SonarQube') {
       echo "Running SonarQube scan ..."
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' clean test verify sonar:sonar"
      } else {
         bat(/"${mvnHome}\bin\mvn" clean test verify sonar:sonar/)
      }

   }
   stage('Local Build') {
      // Run the maven build
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' package"
      } else {
         bat(/"${mvnHome}\bin\mvn" package/)
      }
   }

   stage('OpenShift Build') {
      if (isUnix()) {
         sh "'/home/kiet/minishift/oc' login --username=dev --password=dev"
         sh "'/home/kiet/minishift/oc' start-build pet-clinic -n pet-clinic"
      } else {
         bat(/"${mvnHome}\bin\mvn" package/)
      }  
   }
   stage('Deploy') {
       echo "deploying ..."
       if (isUnix()) {
           sh "cp target/petclinic.war /home/kiet/csd-work/bin/apache-tomcat-8.5.28/webapps/."
       } else {
           bat "copy target\\petclinic.war c:\\dev/bin\\apache-tomcat-8.5.28\\webapps\\."
       }
   }
   stage('Results') {
      echo 'Archiving ...'
      publishHTML(target: [
        allowMissing: true, 
        alwaysLinkToLastBuild: false, 
        keepAll: true, 
        reportDir: 'target/site/jacoco/', 
        reportFiles: 'index.html', 
        reportName: 'Code Coverage Report', 
        reportTitles: ''])
      junit '**/target/surefire-reports/TEST-*.xml'
      archive 'target/*.war'
   }
}
