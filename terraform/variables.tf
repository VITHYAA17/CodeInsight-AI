variable "project_id" {
  description = "The Google Cloud Platform (GCP) Project ID"
  type        = string
}

variable "region" {
  description = "Default GCP Region for resources"
  type        = string
  default     = "us-central1"
}

variable "zone" {
  description = "Default GCP Zone for compute/GKE resources"
  type        = string
  default     = "us-central1-a"
}

variable "environment" {
  description = "Deployment environment (e.g. dev, staging, prod)"
  type        = string
  default     = "prod"
}

variable "cluster_name" {
  description = "Name of the Google Kubernetes Engine (GKE) Cluster"
  type        = string
  default     = "codeinsight-gke-cluster"
}

variable "gke_node_count_min" {
  description = "Minimum nodes in GKE autoscaling node pool"
  type        = number
  default     = 1
}

variable "gke_node_count_max" {
  description = "Maximum nodes in GKE autoscaling node pool"
  type        = number
  default     = 4
}

variable "gke_machine_type" {
  description = "Machine type for GKE node pool"
  type        = string
  default     = "e2-standard-2"
}

variable "db_instance_tier" {
  description = "Machine tier for Cloud SQL PostgreSQL instance"
  type        = string
  default     = "db-custom-2-7680" # 2 vCPU, 7.5GB RAM
}

variable "db_name" {
  description = "Name of PostgreSQL database"
  type        = string
  default     = "codeinsight"
}

variable "db_username" {
  description = "PostgreSQL application user"
  type        = string
  default     = "codeinsight_user"
}

variable "db_password" {
  description = "PostgreSQL user password (managed securely via secret)"
  type        = string
  sensitive   = true
}

variable "redis_memory_size_gb" {
  description = "Cloud Memorystore Redis cache capacity in GB"
  type        = number
  default     = 1
}
