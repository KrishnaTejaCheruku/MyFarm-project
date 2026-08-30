# Base MariaDB image doesn't include Galera -- confirmed directly
# against MariaDB/mariadb-docker's own Dockerfile for this exact tag,
# which only installs mariadb-server/mariadb-backup/socat, nothing
# wsrep-related.
#
# Two ready-made alternatives were checked and ruled out before
# building this thin layer ourselves:
#   - bitnami/mariadb-galera: Bitnami discontinued free images for
#     most of its catalog on 2025-08-28, moving it behind Broadcom's
#     paid "Bitnami Secure Images" -- confirmed by querying Docker
#     Hub's own tags API for bitnami/mariadb-galera, which returned
#     zero tags.
#   - severalnines/mariadb: last published in 2018, and its own
#     README calls it an abandoned 2017 proof-of-concept, recommending
#     a Kubernetes operator instead.
#
# So: build on the exact same mariadb:11.4.13 image already pinned in
# services/api's TestcontainersConfiguration, keeping one MariaDB
# version across the whole project, under our own control instead of
# a third party's image lifecycle.
#
# galera-4 is the real, current package name for MariaDB 10.4+'s
# wsrep provider library (MariaDB 10.3 and earlier used "galera-3") --
# confirmed against MariaDB Corporation's own docs and quickstart
# guide, not assumed. The base image already configures MariaDB's own
# apt repository (see MariaDB/mariadb-docker's Dockerfile), so
# galera-4 comes from the same trusted source as mariadb-server
# itself, not a third-party repo.
FROM mariadb:11.4.13

RUN apt-get update \
    && apt-get install -y --no-install-recommends galera-4 rsync \
    && rm -rf /var/lib/apt/lists/*
