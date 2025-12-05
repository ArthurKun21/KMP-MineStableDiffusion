<div align="center">

# Mine Diffusion 🐯

**Kotlin Multiplatform Stable Diffusion**  
_Only supports VulKun 1.1 or above_

<img src="./docs/splash.jpg" alt="splash" width="800">

</div>


## About this Repo

This project is **open sourced and free**. Hope you enjoy it!

## Build

- 安装Vulkan SDK,并验证好环境变量


## Other

### Tip

- 由于Windows系统对文件路径有260字符的限制,注意路径要尽可能短,要不然会导致CMake构建失败
- Vulkan 1.2 required: ErrorFeatureNotPresent,报这个错说明设备配置太低了

### gradle cache dir 

- GRADLE_USER_HOME
- KONAN_DATA_DIR
- KOTLIN_NATIVE_HOME
- PUB_CACHE 