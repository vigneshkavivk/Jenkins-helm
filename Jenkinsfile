pipeline {
    agent any

    environment {
        CLUSTER_NAME = "your-eks-cluster"
        REGION = "ap-south-1"
        NODE_NAME = "ip-10-0-1-23.ec2.internal"   // NotReady node
        ASG_NAME = "your-asg-name"
        DESIRED_CAPACITY = "4"
    }

    stages {

        stage('Update Kubeconfig') {
            steps {
                sh """
                aws eks update-kubeconfig --region $REGION --name $CLUSTER_NAME
                """
            }
        }

        //  Step 1: Check cluster capacity
        stage('Check Cluster Capacity') {
            steps {
                script {
                    def highUsage = sh(
                        script: """
                        kubectl top nodes | awk 'NR>1 {if($3+0 > 80 || $5+0 > 80) print}' | wc -l
                        """,
                        returnStdout: true
                    ).trim()

                    if (highUsage.toInteger() > 0) {
                        env.SCALE_REQUIRED = "true"
                    } else {
                        env.SCALE_REQUIRED = "false"
                    }

                    echo "Scale Required: ${env.SCALE_REQUIRED}"
                }
            }
        }

        //  Step 2: Scale only if needed
        stage('Pre-Scale Nodes (Conditional)') {
            when {
                expression { env.SCALE_REQUIRED == "true" }
            }
            steps {
                sh """
                aws autoscaling update-auto-scaling-group \
                  --auto-scaling-group-name $ASG_NAME \
                  --desired-capacity $DESIRED_CAPACITY
                """
            }
        }

        //  Step 3: Wait for nodes (only if scaled)
        stage('Wait for New Nodes Ready') {
            when {
                expression { env.SCALE_REQUIRED == "true" }
            }
            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        waitUntil {
                            def readyNodes = sh(
                                script: "kubectl get nodes --no-headers | grep -c ' Ready'",
                                returnStdout: true
                            ).trim()

                            echo "Ready Nodes: ${readyNodes}"
                            return readyNodes.toInteger() >= DESIRED_CAPACITY.toInteger()
                        }
                    }
                }
            }
        }

        //  Step 4: Cordon bad node
        stage('Cordon Node') {
            steps {
                sh """
                kubectl cordon $NODE_NAME
                """
            }
        }

        //  Step 5: Drain safely
        stage('Drain Node Safely') {
            steps {
                sh """
                kubectl drain $NODE_NAME \
                  --ignore-daemonsets \
                  --delete-emptydir-data
                """
            }
        }

        //  Step 6: Verify rescheduling
        stage('Verify Pods') {
            steps {
                sh """
                kubectl get pods -o wide
                """
            }
        }

        //  Step 7: Health check
        stage('Check Pod Health') {
            steps {
                script {
                    def notRunning = sh(
                        script: "kubectl get pods --all-namespaces --field-selector=status.phase!=Running | wc -l",
                        returnStdout: true
                    ).trim()

                    if (notRunning.toInteger() > 1) {
                        error "Some pods are not running properly!"
                    } else {
                        echo "All pods healthy "
                    }
                }
            }
        }

        //  Step 8: Optional scale down
        stage('Scale Down (Optional)') {
            steps {
                input message: "Scale down extra nodes?", ok: "Yes"

                sh """
                aws autoscaling update-auto-scaling-group \
                  --auto-scaling-group-name $ASG_NAME \
                  --desired-capacity 3
                """
            }
        }
    }
}
