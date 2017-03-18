pipeline {
    agent {
        dockerfile {
            dir 'build'
        }
    }
    stages {
        stage('Verify') {
            steps {
                sh 'mvn -B verify -Prun-its'
            }
        }
    }
}

