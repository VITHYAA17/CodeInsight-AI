resource "google_redis_instance" "cache" {
  name           = "codeinsight-redis-${var.environment}"
  tier           = var.environment == "prod" ? "STANDARD_HA" : "BASIC"
  memory_size_gb = var.memory_size_gb
  region         = var.region
  project        = var.project_id

  location_id             = "${var.region}-a"
  alternative_location_id = var.environment == "prod" ? "${var.region}-b" : null

  authorized_network = var.network_id
  connect_mode       = "DIRECT_PEERING"

  redis_version = "REDIS_7_0"
  display_name  = "CodeInsight Redis Cache & Rate Limiting Instance"

  labels = {
    environment = var.environment
    app         = "codeinsight"
  }
}
