# Five instances recreating devopshydclub/vprofile-project's aws-LiftAndShift
# branch topology, pointed at Floci instead of real AWS. userdata for each
# service does everything a real EC2 launch would (install, build, deploy)
# except start the process that needs to reach another instance by hostname
# (app01 needs db01/mc01/rmq01; web01 needs app01) -- see the chicken-and-egg
# note in README.md. null_resource.wire finishes that part once every
# instance's private IP actually exists.

resource "aws_instance" "db" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  vpc_security_group_ids = [aws_security_group.vprofile.id]

  user_data = templatefile("${path.module}/userdata/db.sh", {
    vprofile_repo_url    = var.vprofile_repo_url
    vprofile_repo_branch = var.vprofile_repo_branch
    mysql_database       = var.mysql_database
    mysql_user           = var.mysql_user
    mysql_password       = var.mysql_password
    mysql_root_password  = var.mysql_root_password
  })

  tags = { Name = "db01" }
}

resource "aws_instance" "cache" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  vpc_security_group_ids = [aws_security_group.vprofile.id]
  user_data              = file("${path.module}/userdata/cache.sh")

  tags = { Name = "mc01" }
}

resource "aws_instance" "mq" {
  ami                    = var.mq_ami_id       # Ubuntu, not AL2023 -- see variables.tf
  instance_type          = var.mq_instance_type # arm64 image needs a Graviton family -- see variables.tf
  vpc_security_group_ids = [aws_security_group.vprofile.id]

  user_data = templatefile("${path.module}/userdata/mq.sh", {
    rabbitmq_user     = var.rabbitmq_user
    rabbitmq_password = var.rabbitmq_password
  })

  tags = { Name = "rmq01" }
}

resource "aws_instance" "app" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  vpc_security_group_ids = [aws_security_group.vprofile.id]

  user_data = templatefile("${path.module}/userdata/app.sh", {
    vprofile_repo_url    = var.vprofile_repo_url
    vprofile_repo_branch = var.vprofile_repo_branch
    mysql_database       = var.mysql_database
    mysql_user           = var.mysql_user
    mysql_password       = var.mysql_password
    rabbitmq_user        = var.rabbitmq_user
    rabbitmq_password    = var.rabbitmq_password
  })

  tags = { Name = "app01" }

  depends_on = [aws_instance.db, aws_instance.cache, aws_instance.mq]
}

resource "aws_instance" "web" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  vpc_security_group_ids = [aws_security_group.vprofile.id]
  user_data              = file("${path.module}/userdata/web.sh")

  tags = { Name = "web01" }

  depends_on = [aws_instance.app]
}

resource "null_resource" "wire" {
  depends_on = [
    aws_instance.db,
    aws_instance.cache,
    aws_instance.mq,
    aws_instance.app,
    aws_instance.web,
  ]

  triggers = {
    db_ip    = aws_instance.db.private_ip
    cache_ip = aws_instance.cache.private_ip
    mq_ip    = aws_instance.mq.private_ip
    app_id   = aws_instance.app.id
    app_ip   = aws_instance.app.private_ip
    web_id   = aws_instance.web.id
  }

  provisioner "local-exec" {
    command = "${path.module}/scripts/wire.sh"

    environment = {
      FLOCI_ENDPOINT = var.floci_endpoint
      AWS_REGION     = var.aws_region
      DB_IP          = aws_instance.db.private_ip
      CACHE_IP       = aws_instance.cache.private_ip
      MQ_IP          = aws_instance.mq.private_ip
      APP_ID         = aws_instance.app.id
      APP_IP         = aws_instance.app.private_ip
      WEB_ID         = aws_instance.web.id
    }
  }
}
