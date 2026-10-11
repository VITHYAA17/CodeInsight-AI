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

variable "network_id" {
  description = "VPC Network ID"
  type        = string
}

variable "memory_size_gb" {
  description = "Redis memory capacity in GB"
  type        = number
  default     = 1
}
