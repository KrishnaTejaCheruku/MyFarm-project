variable "floci_endpoint" {
  description = "Floci's single AWS-compatible endpoint. Must match the port the floci service publishes in legacy/vprofile/compose.yaml (4566)."
  type        = string
  default     = "http://localhost:4566"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "ami_id" {
  description = <<-DESC
    Floci EC2 image-catalog alias, not a real AMI ID. ami-amazonlinux2023
    maps to public.ecr.aws/amazonlinux/amazonlinux:2023 with Floci's plain
    "tail -f /dev/null" container lifecycle (no systemd). Deliberately NOT
    ami-ubuntu2404-cloud -- Floci's own docs flag that variant experimental.
    Confirmed against Floci's EC2 service docs (floci.io/floci/services/ec2/).
  DESC
  type    = string
  default = "ami-amazonlinux2023"
}

variable "instance_type" {
  description = "Not billed under Floci -- kept only for parity with a real aws_instance block."
  type        = string
  default     = "t3.micro"
}

variable "vprofile_repo_url" {
  description = "Repo each instance clones at boot to get legacy/vprofile's source, db_backup.sql, and pom.xml -- this MyFarm-project repo, not the upstream devopshydclub/vprofile-project, so the EC2 topology always matches what compose.yaml already runs locally."
  type        = string
  default     = "https://github.com/KrishnaTejaCheruku/MyFarm-project.git"
}

variable "vprofile_repo_branch" {
  type    = string
  default = "floci-complete"
}

variable "mysql_database" {
  type    = string
  default = "accounts"
}

variable "mysql_user" {
  type    = string
  default = "admin"
}

variable "mysql_password" {
  type      = string
  default   = "admin123"
  sensitive = true
}

variable "mysql_root_password" {
  type      = string
  default   = "root123"
  sensitive = true
}

variable "rabbitmq_user" {
  type    = string
  default = "test"
}

variable "rabbitmq_password" {
  type      = string
  default   = "test"
  sensitive = true
}

variable "mq_instance_type" {
  description = <<-DESC
    t3.micro (the default instance_type) is an Intel/AMD-only family in
    real AWS's own EC2 instance type catalog -- Floci enforces that same
    architecture rule. The Ubuntu image mq_ami_id resolves to on an
    Apple Silicon Mac is arm64, so it needs a Graviton (arm64) family
    instead: t4g.micro. Real error hit: "InvalidParameterValue: The
    architecture 'arm64' of the specified image does not match the
    architecture supported by instance type 't3.micro'." db/cache/app/web
    stay on the default t3.micro since ami-amazonlinux2023 resolved x86_64
    on the same host.
  DESC
  type    = string
  default = "t4g.micro"
}

variable "mq_ami_id" {
  description = <<-DESC
    RabbitMQ's instance deliberately uses a different image-catalog alias than
    the rest of the fleet: Amazon Linux 2023 has no working Erlang/RabbitMQ
    package path (confirmed -- rabbitmq/erlang-rpm's own issue tracker has an
    open "Investigate if Amazon Linux 2023 can be supported" issue, and a
    separate discussion titled "Erlang Does Not Build on ARM Based Amazon
    Linux 2023"). ami-ubuntu2404 maps to Floci's plain
    public.ecr.aws/docker/library/ubuntu:24.04 catalog image (still the
    non-systemd "tail -f /dev/null" lifecycle, not the experimental
    ami-ubuntu2404-cloud systemd variant), where "apt-get install -y
    rabbitmq-server" pulls a compatible Erlang automatically as a normal
    dependency.
  DESC
  type    = string
  default = "ami-ubuntu2404"
}
