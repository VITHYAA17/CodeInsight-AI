output "gke_cluster_name" {
  description = "Name of the provisioned GKE Cluster"
  value       = module.gke.cluster_name
}

output "gke_endpoint" {
  description = "Kubernetes API endpoint"
  value       = module.gke.endpoint
}

output "kubectl_configure_command" {
  description = "Execute this command to configure kubectl credentials for the cluster"
  value       = module.gke.get_credentials_command
}

output "artifact_registry_repo_url" {
  description = "Docker repository URL for pushing container images"
  value       = module.artifact_registry.repository_url
}

output "database_connection_name" {
  description = "Cloud SQL PostgreSQL instance connection string"
  value       = module.cloud_sql.connection_name
}

output "database_private_ip" {
  description = "Internal IP address for PostgreSQL in the VPC"
  value       = module.cloud_sql.private_ip
}

output "redis_host" {
  description = "Internal IP address of Redis instance"
  value       = module.redis.host
}

output "redis_port" {
  description = "Port number of Redis instance"
  value       = module.redis.port
}
