# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt-get install docker-compose-plugin

# Create directory structure
mkdir -p ~/hrms-deployment/{data,configs,scripts,backups,logs}
mkdir -p ~/hrms-deployment/data/{postgres,mongo}

# Download deployment files (or SCP them)
# You'll need to manually copy:
# 1. docker-compose.yml
# 2. .env (with your passwords)
# 3. deploy.sh
# 4. setup-vps.sh (this file)

# Set permissions
chmod +x ~/hrms-deployment/*.sh

# Initialize Docker Compose
cd ~/hrms-deployment
docker-compose up -d

echo "✅ VPS setup complete!"
echo "🔑 Remember to update .env with proper passwords"
echo "🚀 Run './deploy.sh' for future updates"