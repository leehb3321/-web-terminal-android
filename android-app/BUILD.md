# 快速构建 APK 指南

## 步骤 1: 创建 GitHub 仓库

1. 访问 https://github.com/new
2. 创建新仓库（例如：`web-terminal-android`）
3. **不要**勾选 "Add a README file"

## 步骤 2: 上传代码

在 `android-app` 目录下执行：

```bash
# 方式 A: 使用脚本（推荐）
./setup-github.sh

# 然后添加远程仓库
git remote add origin https://github.com/YOUR_USERNAME/web-terminal-android.git
git push -u origin main
```

或者手动执行：

```bash
git init
git branch -M main
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/web-terminal-android.git
git push -u origin main
```

## 步骤 3: 等待构建完成

1. 访问你的 GitHub 仓库
2. 点击 **Actions** 标签
3. 等待构建完成（约 3-5 分钟）

## 步骤 4: 下载 APK

1. 在 Actions 页面点击完成的 workflow
2. 滚动到底部 **Artifacts** 区域
3. 下载 `app-debug`

## 步骤 5: 安装到手机

1. 将 APK 传输到手机
2. 点击 APK 文件安装
3. 如果提示"未知来源"，请在设置中允许

---

## 手动触发构建

如果你想重新构建：

1. 访问 Actions 标签
2. 选择 "Build Android APK" workflow
3. 点击 "Run workflow"
4. 选择分支并运行

## 发布版本（可选）

创建 tag 来触发 release：

```bash
git tag v1.0.0
git push origin v1.0.0
```

这会自动创建 GitHub Release 并附上 APK。

---

## 目录结构

```
android-app/
├── .github/workflows/
│   └── build.yml          # GitHub Actions 配置
├── app/
│   ├── src/main/          # 源代码
│   └── build.gradle.kts   # 构建配置
├── gradle/wrapper/        # Gradle 包装器
├── setup-github.sh        # Linux/Mac 设置脚本
├── setup-github.bat       # Windows 设置脚本
└── README.md              # 项目说明
```
