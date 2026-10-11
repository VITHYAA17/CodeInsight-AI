terraform {
  required_version = ">= 1.5.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.20.0"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "~> 5.20.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.26.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6.0"
    }
  }

  # Uncomment for remote state storage on Google Cloud Storage (GCS)
  # backend "gcs" {
  #   bucket = "codeinsight-terraform-state"
  #   prefix = "terraform/state/prod"
  # }
}
