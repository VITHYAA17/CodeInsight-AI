resource "random_id" "db_suffix" {
  byte_length = 4
}

# Cloud SQL PostgreSQL Instance with pgvector extension enabled for RAG
resource "google_sql_database_instance" "postgres" {
  name             = "codeinsight-pg-${var.environment}-${random_id.db_suffix.hex}"
  database_version = "POSTGRES_15"
  region           = var.region
  project          = var.project_id

  depends_on = [var.private_vpc_connection]

  settings {
    tier              = var.tier
    availability_type = var.environment == "prod" ? "REGIONAL" : "ZONAL"
    disk_size         = 30
    disk_type         = "PD_SSD"
    disk_autoresize   = true

    ip_configuration {
      ipv4_enabled                                  = false # Private IP only for enterprise security
      private_network                               = var.network_id
      enable_private_path_for_google_cloud_services = true
    }

    backup_configuration {
      enabled                        = true
      start_time                     = "02:00"
      point_in_time_recovery_enabled = true
      transaction_log_retention_days = 7
    }

    # Cloud SQL database flags: Enable pgvector for AI semantic similarity search
    database_flags {
      name  = "cloudsql.enable_pgvector"
      value = "on"
    }

    database_flags {
      name  = "max_connections"
      value = "200"
    }

    insights_config {
      query_insights_enabled  = true
      query_string_length     = 1024
      record_application_tags = true
    }
  }

  deletion_protection = false
}

# Application Database
resource "google_sql_database" "database" {
  name     = var.db_name
  instance = google_sql_database_instance.postgres.name
  project  = var.project_id
}

# Application User
resource "google_sql_user" "db_user" {
  name     = var.db_username
  instance = google_sql_database_instance.postgres.name
  password = var.db_password
  project  = var.project_id
}
