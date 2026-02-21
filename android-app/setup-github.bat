@echo off
setlocal

echo ========================================
echo   Web Terminal Android - GitHub Setup
echo ========================================
echo.

REM Check if git is installed
where git >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: git is not installed.
    echo Please install git first: https://git-scm.com/downloads
    exit /b 1
)

REM Check if already a git repo
if exist ".git" (
    echo Git repository already exists.
) else (
    echo Initializing git repository...
    git init
    git branch -M main
)

REM Add all files
echo Adding files...
git add .

REM Commit
echo Creating commit...
git commit -m "Initial commit - Web Terminal Android App"

echo.
echo ========================================
echo   Next Steps
echo ========================================
echo.
echo 1. Create a new repository on GitHub:
echo    https://github.com/new
echo.
echo 2. Add remote and push:
echo    git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
echo    git push -u origin main
echo.
echo 3. Go to Actions tab to see build progress
echo.
echo 4. Download APK from Actions ^> Artifacts
echo.
echo ========================================

endlocal
