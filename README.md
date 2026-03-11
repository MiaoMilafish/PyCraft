# PyCraft - Minecraft与Python WebSocket通信模组

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green) ![NeoForge](https://img.shields.io/badge/NeoForge-21.1.219-blue) ![Java](https://img.shields.io/badge/Java-21-orange) ![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-lightgrey)

## 项目简介

PyCraft 是一个创新的Minecraft模组，它通过WebSocket协议为外部Python程序提供与游戏世界的实时双向通信能力。开发者可以通过Python脚本查询游戏状态、控制游戏实体、甚至自动化游戏操作。

### 主要特性

- **WebSocket通信**：基于标准的WebSocket协议，支持实时双向通信
- **游戏状态查询**：获取玩家位置、物品栏、生物信息等游戏数据
- **游戏控制**：通过外部程序控制玩家移动、物品使用等操作
- **Python友好**：提供简洁的Python客户端库，易于集成
- **开发者友好**：面向没有模组开发经验的开发者，提供详细的使用指南

## 快速开始

### 环境要求

- **Java 21** 或更高版本
- **Minecraft 1.21.1**
- **NeoForge 21.1.219**
- **IntelliJ IDEA**（推荐）

### 在IntelliJ IDEA中运行游戏

#### 步骤1：导入项目

1. 下载、安装并打开IntelliJ IDEA
2. 复制本仓库的URL
3. 选择 "文件" → "新建" → "来自版本控制的项目"
4. 黏贴URL并选择项目目录
5. 等待项目配置完成，这个过程需要合适的网络环境

#### 步骤2：首次运行游戏

1. 在IDEA右边栏找到Gradle任务栏(大象图标)
2. 找到Tasks → mod development → runClient
3. 双击，等待Gradle构建完成，Minecraft客户端将自动启动


#### 步骤3：运行游戏

1. 在IDEA上栏选择[runClient]配置
2. 点击绿色三角形运行按钮或使用快捷键 `Shift+F10`
3. 等待Gradle构建完成，Minecraft客户端将自动启动