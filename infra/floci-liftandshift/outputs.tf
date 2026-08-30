output "instance_ids" {
  value = {
    db01  = aws_instance.db.id
    mc01  = aws_instance.cache.id
    rmq01 = aws_instance.mq.id
    app01 = aws_instance.app.id
    web01 = aws_instance.web.id
  }
}

output "private_ips" {
  value = {
    db01  = aws_instance.db.private_ip
    mc01  = aws_instance.cache.private_ip
    rmq01 = aws_instance.mq.private_ip
    app01 = aws_instance.app.private_ip
    web01 = aws_instance.web.private_ip
  }
}

output "security_group_id" {
  value = aws_security_group.vprofile.id
}

output "find_published_ports" {
  description = "Host ports Floci publishes for the security group's CIDR-sourced rules aren't predictable in advance -- find them here once apply finishes."
  value        = "docker logs vprofile-floci-floci-1 2>&1 | grep Published"
}
