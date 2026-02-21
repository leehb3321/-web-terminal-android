#!/bin/bash

# Quick setup script to push to GitHub and trigger build

echo "========================================"
echo "  Web Terminal Android - GitHub Setup"
echo "========================================"
echo ""

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "Error: git is not installed."
    echo "Please install git first: https://git-scm.com/downloads"
    exit 1
fi

# Check if already a git repo
if [ -d ".git" ]; then
    echo "Git repository already exists."
else
    echo "Initializing git repository..."
    git init
    git branch -M main
fi

# Add all files
echo "Adding files..."
git add .

# Commit
echo "Creating commit..."
git commit -m "Initial commit - Web Terminal Android App"

echo ""
echo "========================================"
echo "  Next Steps"
echo "========================================"
echo ""
echo "1. Create a new repository on GitHub:"
echo "   https://github.com/new"
echo ""
echo "2. Add remote and push:"
echo "   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git"
echo "   git push -u origin main"
echo ""
echo "3. Go to Actions tab to see build progress"
echo ""
echo "4. Download APK from Actions > Artifacts"
echo ""
echo "========================================"
