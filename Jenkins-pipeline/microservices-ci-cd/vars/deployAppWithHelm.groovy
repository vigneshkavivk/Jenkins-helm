def call(Map config) {
    echo "🚀 Deploying ${config.releaseName} using Helm"

    sh """
        helm upgrade --install ${config.releaseName} ${config.chartPath} \
          --namespace ${config.namespace} \
          --set image.repository=${config.imageRepo} \
          --set image.tag=${config.imageTag} \
          --create-namespace
    """
}
