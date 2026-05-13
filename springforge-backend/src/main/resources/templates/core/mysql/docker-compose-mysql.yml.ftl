  mysql:
    image: mysql:8.0
    container_name: ${artifactId}-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${r"${MYSQL_ROOT_PASSWORD:-root}"}
      MYSQL_DATABASE: ${artifactId}
      MYSQL_USER: ${r"${MYSQL_USER:-appuser}"}
      MYSQL_PASSWORD: ${r"${MYSQL_PASSWORD:-apppass}"}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
