# Custom VPC Network
resource "google_compute_network" "vpc" {
  name                    = "codeinsight-vpc-${var.environment}"
  auto_create_subnetworks = false
  project                 = var.project_id
}

# Subnet for GKE Nodes with Secondary Ranges for Pods & Services
resource "google_compute_subnetwork" "subnet" {
  name                     = "codeinsight-k8s-subnet-${var.environment}"
  ip_cidr_range            = var.subnet_cidr
  region                   = var.region
  network                  = google_compute_network.vpc.id
  project                  = var.project_id
  private_ip_google_access = true

  secondary_ip_range {
    range_name    = "k8s-pods"
    ip_cidr_range = var.pods_cidr
  }

  secondary_ip_range {
    range_name    = "k8s-services"
    ip_cidr_range = var.services_cidr
  }
}

# Cloud Router for NAT Gateway (enables private nodes to reach external APIs)
resource "google_compute_router" "router" {
  name    = "codeinsight-router-${var.environment}"
  region  = var.region
  network = google_compute_network.vpc.id
  project = var.project_id
}

# Cloud NAT for outbound internet connectivity
resource "google_compute_router_nat" "nat" {
  name                               = "codeinsight-nat-${var.environment}"
  router                             = google_compute_router.router.name
  region                             = var.region
  project                            = var.project_id
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

  log_config {
    enable = true
    filter = "ERRORS_ONLY"
  }
}

# Reserved Internal IP range for Cloud SQL Private Service Access (VPC Peering)
resource "google_compute_global_address" "private_ip_address" {
  name          = "codeinsight-db-private-ip-${var.environment}"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.vpc.id
  project       = var.project_id
}

# Private Service Connection for Cloud SQL
resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.vpc.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}

# Firewall Rule: Allow GCP Load Balancer Health Checks
resource "google_compute_firewall" "allow_health_checks" {
  name    = "codeinsight-allow-health-checks-${var.environment}"
  network = google_compute_network.vpc.name
  project = var.project_id

  allow {
    protocol = "tcp"
    ports    = ["80", "443", "8080"]
  }

  source_ranges = ["130.211.0.0/22", "35.191.0.0/16"]
  target_tags   = ["gke-node"]
}

# Firewall Rule: Allow internal communication across VPC
resource "google_compute_firewall" "allow_internal" {
  name    = "codeinsight-allow-internal-${var.environment}"
  network = google_compute_network.vpc.name
  project = var.project_id

  allow {
    protocol = "icmp"
  }
  allow {
    protocol = "tcp"
    ports    = ["0-65535"]
  }
  allow {
    protocol = "udp"
    ports    = ["0-65535"]
  }

  source_ranges = [var.subnet_cidr, var.pods_cidr, var.services_cidr]
}
