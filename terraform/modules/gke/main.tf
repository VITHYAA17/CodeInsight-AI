# Dedicated Service Account for GKE Nodes
resource "google_service_account" "gke_nodes_sa" {
  account_id   = "codeinsight-gke-node-sa"
  display_name = "CodeInsight GKE Node Pool Service Account"
  project      = var.project_id
}

# IAM Role bindings for GKE Node Service Account (Least Privilege)
resource "google_project_iam_member" "node_log_writer" {
  project = var.project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.gke_nodes_sa.email}"
}

resource "google_project_iam_member" "node_metric_writer" {
  project = var.project_id
  role    = "roles/monitoring.metricWriter"
  member  = "serviceAccount:${google_service_account.gke_nodes_sa.email}"
}

resource "google_project_iam_member" "node_monitoring_viewer" {
  project = var.project_id
  role    = "roles/monitoring.viewer"
  member  = "serviceAccount:${google_service_account.gke_nodes_sa.email}"
}

resource "google_project_iam_member" "node_artifact_registry" {
  project = var.project_id
  role    = "roles/artifactregistry.reader"
  member  = "serviceAccount:${google_service_account.gke_nodes_sa.email}"
}

# Google Kubernetes Engine (GKE) Cluster
resource "google_container_cluster" "primary" {
  name     = "${var.cluster_name}-${var.environment}"
  location = var.region
  project  = var.project_id

  # Start with decoupled node pool
  remove_default_node_pool = true
  initial_node_count       = 1

  network    = var.network_id
  subnetwork = var.subnet_id

  # VPC-Native Cluster configuration (IP Aliasing)
  ip_allocation_policy {
    cluster_secondary_range_name  = var.pods_range_name
    services_secondary_range_name = var.services_range_name
  }

  # Security: Private Cluster with Private Nodes
  private_cluster_config {
    enable_private_nodes    = true
    enable_private_endpoint = false # Public endpoint with authorized networks for management
    master_ipv4_cidr_block  = "172.16.0.0/28"
  }

  # Workload Identity for secure GCP API access from Pods without secret keys
  workload_identity_config {
    workload_pool = "${var.project_id}.svc.id.goog"
  }

  # Addons Configuration
  addons_config {
    http_load_balancing {
      disabled = false
    }
    horizontal_pod_autoscaling {
      disabled = false
    }
    network_policy_config {
      disabled = false
    }
  }

  network_policy {
    enabled  = true
    provider = "CALICO"
  }

  maintenance_policy {
    daily_maintenance_window {
      start_time = "03:00"
    }
  }

  # Delete protection for production safeguards
  deletion_protection = false
}

# Autoscaling Custom Node Pool
resource "google_container_node_pool" "primary_nodes" {
  name       = "codeinsight-app-pool-${var.environment}"
  location   = var.region
  cluster    = google_container_cluster.primary.name
  project    = var.project_id

  autoscaling {
    min_node_count = var.node_count_min
    max_node_count = var.node_count_max
  }

  management {
    auto_repair  = true
    auto_upgrade = true
  }

  node_config {
    machine_type    = var.machine_type
    service_account = google_service_account.gke_nodes_sa.email
    disk_size_gb    = 50
    disk_type       = "pd-standard"

    oauth_scopes = [
      "https://www.googleapis.com/auth/cloud-platform"
    ]

    labels = {
      environment = var.environment
      role        = "application"
      app         = "codeinsight"
    }

    tags = ["gke-node", "codeinsight-node"]

    # Shielded Instance configurations
    shielded_instance_config {
      enable_secure_boot          = true
      enable_integrity_monitoring = true
    }
  }
}
