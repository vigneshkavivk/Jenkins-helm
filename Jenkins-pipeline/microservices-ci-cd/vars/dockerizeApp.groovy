def call(String servicePath, String registry) {
    def serviceName = servicePath.tokenize('/')[-1]
    def image = "${registry}/${serviceName}:latest"

    echo "🐳 Building Docker image ${image}"
    sh """
        cd ${servicePath}
        docker build -t ${image} .
        docker push ${image}
    """
}
