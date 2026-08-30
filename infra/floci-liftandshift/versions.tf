terraform {
  required_version = ">= 1.7"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Floci is a LocalStack-compatible local AWS emulator: one HTTP endpoint
# (default :4566) fronts every AWS service, confirmed against Floci's own
# "Migrating from LocalStack" guide, which describes it as a drop-in
# replacement using the same port/credential/SDK pattern. Dummy
# credentials + the usual LocalStack-style skip_* flags are what make the
# real hashicorp/aws provider talk to it instead of real AWS.
provider "aws" {
  region                      = var.aws_region
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    ec2 = var.floci_endpoint
    ssm = var.floci_endpoint
  }
}
