output "cluster_id" {
  description = "The ID of the GKE cluster"
  value       = google_container_cluster.primary.id
}

output "cluster_name" {
  description = "The name of the GKE cluster"
  value       = google_container_cluster.primary.name
}

output "endpoint" {
  description = "The IP address of the Kubernetes master endpoint"
  value       = google_container_cluster.primary.endpoint
}

output "ca_certificate" {
  description = "The base64 encoded public CA certificate of the cluster"
  value       = google_container_cluster.primary.master_auth[0].cluster_ca_certificate
  sensitive   = true
}

output "get_credentials_command" {
  description = "gcloud command to configure kubectl"
  value       = "gcloud container clusters get-credentials ${google_container_cluster.primary.name} --region ${var.region} --project ${var.project_id}"
}
