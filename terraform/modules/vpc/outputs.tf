output "network_id" {
  description = "The ID of the VPC network"
  value       = google_compute_network.vpc.id
}

output "network_name" {
  description = "The name of the VPC network"
  value       = google_compute_network.vpc.name
}

output "subnet_id" {
  description = "The ID of the GKE subnetwork"
  value       = google_compute_subnetwork.subnet.id
}

output "subnet_name" {
  description = "The name of the GKE subnetwork"
  value       = google_compute_subnetwork.subnet.name
}

output "pods_range_name" {
  description = "The secondary range name for Pods"
  value       = "k8s-pods"
}

output "services_range_name" {
  description = "The secondary range name for Services"
  value       = "k8s-services"
}

output "private_vpc_connection" {
  description = "Private VPC connection resource dependency for Cloud SQL"
  value       = google_service_networking_connection.private_vpc_connection.id
}
