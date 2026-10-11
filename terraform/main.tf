provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

provider "google-beta" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

# 1. VPC Network & Subnets
module "vpc" {
  source      = "./modules/vpc"
  project_id  = var.project_id
  region      = var.region
  environment = var.environment
}

# 2. Artifact Registry for Container Images
module "artifact_registry" {
  source      = "./modules/artifact_registry"
  project_id  = var.project_id
  region      = var.region
  environment = var.environment
}

# 3. Google Kubernetes Engine (GKE) Cluster & Autoscaling Node Pool
module "gke" {
  source              = "./modules/gke"
  project_id          = var.project_id
  region              = var.region
  zone                = var.zone
  environment         = var.environment
  cluster_name        = var.cluster_name
  network_id          = module.vpc.network_id
  subnet_id           = module.vpc.subnet_id
  pods_range_name     = module.vpc.pods_range_name
  services_range_name = module.vpc.services_range_name
  node_count_min      = var.gke_node_count_min
  node_count_max      = var.gke_node_count_max
  machine_type        = var.gke_machine_type
}

# 4. Cloud SQL PostgreSQL Instance with pgvector support
module "cloud_sql" {
  source                 = "./modules/cloud_sql"
  project_id             = var.project_id
  region                 = var.region
  environment            = var.environment
  network_id             = module.vpc.network_id
  private_vpc_connection = module.vpc.private_vpc_connection
  tier                   = var.db_instance_tier
  db_name                = var.db_name
  db_username            = var.db_username
  db_password            = var.db_password
}

# 5. Cloud Memorystore for Redis (Cache & Bucket4j Rate Limiting)
module "redis" {
  source         = "./modules/redis"
  project_id     = var.project_id
  region         = var.region
  environment    = var.environment
  network_id     = module.vpc.network_id
  memory_size_gb = var.redis_memory_size_gb
}
