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
  description = "VPC Network ID for Private Service Access"
  type        = string
}

variable "private_vpc_connection" {
  description = "Private VPC connection dependency"
  type        = any
}

variable "tier" {
  description = "Machine tier for Cloud SQL"
  type        = string
  default     = "db-custom-2-7680"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "codeinsight"
}

variable "db_username" {
  description = "PostgreSQL username"
  type        = string
  default     = "codeinsight_user"
}

variable "db_password" {
  description = "PostgreSQL password"
  type        = string
  sensitive   = true
}
