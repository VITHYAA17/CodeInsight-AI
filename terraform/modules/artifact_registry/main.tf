resource "google_artifact_registry_repository" "docker_repo" {
  provider      = google-beta
  project       = var.project_id
  location      = var.region
  repository_id = "codeinsight-docker-${var.environment}"
  description   = "Docker repository for CodeInsight AI Backend & Frontend container images"
  format        = "DOCKER"

  labels = {
    environment = var.environment
    app         = "codeinsight"
  }
}
