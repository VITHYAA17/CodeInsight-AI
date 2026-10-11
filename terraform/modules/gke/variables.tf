variable "project_id" {
  description = "GCP Project ID"
  type        = string
}

variable "region" {
  description = "GCP Region"
  type        = string
}

variable "zone" {
  description = "GCP Zone"
  type        = string
}

variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "cluster_name" {
  description = "Name of the GKE Cluster"
  type        = string
}

variable "network_id" {
  description = "VPC Network ID"
  type        = string
}

variable "subnet_id" {
  description = "Subnet ID for GKE"
  type        = string
}

variable "pods_range_name" {
  description = "Secondary range name for Pods"
  type        = string
}

variable "services_range_name" {
  description = "Secondary range name for Services"
  type        = string
}

variable "node_count_min" {
  description = "Minimum nodes in autoscaling node pool"
  type        = number
  default     = 1
}

variable "node_count_max" {
  description = "Maximum nodes in autoscaling node pool"
  type        = number
  default     = 4
}

variable "machine_type" {
  description = "Compute Engine machine type for nodes"
  type        = string
  default     = "e2-standard-2"
}
