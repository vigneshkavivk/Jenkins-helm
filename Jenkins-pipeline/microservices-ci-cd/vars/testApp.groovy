def call(String servicePath) {
    echo "🧪 Testing ${servicePath}"
    sh "cd ${servicePath} && ./gradlew test"
}
