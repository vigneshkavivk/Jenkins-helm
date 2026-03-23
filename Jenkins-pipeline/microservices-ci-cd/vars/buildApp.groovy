def call(String servicePath) {
    echo "🔨 Building ${servicePath}"
    
    // Verify directory exists first
    sh """
        if [ ! -d "${servicePath}" ]; then
            echo "ERROR: Directory ${servicePath} does not exist!"
            echo "Current directory contents:"
            pwd && ls -la
            exit 1
        fi
    """
    
    // Execute build commands
    dir(servicePath) {
        sh 'pwd && ls -la'  // Debug current location
        sh './gradlew clean build'
    }
}
