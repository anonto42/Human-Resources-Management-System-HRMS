# Stop only the hrms_backend container
docker compose stop hrms_backend

# Remove the container (keeps the image)
docker compose rm -f hrms_backend

# Rebuild with cache
docker compose build hrms_backend

# Start it fresh
docker compose up -d hrms_backend