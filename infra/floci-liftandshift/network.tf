# One shared security group across all five instances, matching the original
# aws-LiftAndShift branch's flat topology -- this is a lift-and-shift of a
# training reference app, not a segmented production network.
#
# Every port is opened to 0.0.0.0/0 deliberately: Floci only auto-publishes a
# security-group port to the host's localhost when the rule's source is a
# CIDR block (confirmed against Floci's EC2 docs' "Security Group Port
# Publishing" section -- a rule referencing another security group or a
# prefix list is never published). Without wide-open CIDR rules here, none of
# these ports would be reachable from the Mac at all.
resource "aws_security_group" "vprofile" {
  name        = "vprofile-liftandshift"
  description = "Shared SG for the vprofile lift-and-shift topology on Floci"

  ingress {
    description = "nginx (web01)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "tomcat (app01)"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "mariadb (db01)"
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "memcached (mc01)"
    from_port   = 11211
    to_port     = 11211
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "rabbitmq (rmq01)"
    from_port   = 5672
    to_port     = 5672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "rabbitmq management UI (rmq01)"
    from_port   = 15672
    to_port     = 15672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
