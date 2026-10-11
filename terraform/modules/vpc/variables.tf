variable "project_id" {
  description = "GCP Project ID"
  type        = string
}

variable "region" {
  description = "GCP Region"
  type        = string
}

variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "subnet_cidr" {
  description = "Primary CIDR block for GKE nodes"
  type        = string
  default     = "10.10.0.0/20"
}

variable "pods_cidr" {
  description = "Secondary CIDR block for GKE Pods"
  type        = string
  default     = "10.20.0.0/16"
}

variable "services_cidr" {
  description = "Secondary CIDR block for GKE Services"
  type        = string
  default     = "10.30.0.0/20"
}
