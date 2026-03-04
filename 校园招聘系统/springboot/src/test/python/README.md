# 用户模块自动化测试

本测试套件用于测试校园招聘系统的用户模块API接口。

## 环境要求

- Python 3.8+
- pip（Python包管理器）

## 安装步骤

### 1. 配置pip（解决SSL问题）

选择以下方法之一：

#### 方法1：使用环境变量（推荐）

在命令行中执行：
```bash
set PYTHONHTTPSVERIFY=0
```

#### 方法2：使用pip配置文件

1. 将 `pip.ini` 文件复制到以下位置之一：
   - Windows: `%APPDATA%\pip\pip.ini` 或 `%HOME%\pip\pip.ini`
   - Linux/macOS: `$HOME/.pip/pip.conf`

### 2. 更新pip

```bash
python -m pip install --upgrade pip
```

### 3. 安装依赖

选择以下方法之一：

#### 方法1：使用国内镜像源（推荐）

```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

#### 方法2：标准安装

```bash
pip install -r requirements.txt
```

## 运行测试

1. 确保后端服务已启动并运行在 http://localhost:8080

2. 运行所有测试：
```bash
pytest -v test_user_api.py
```

3. 生成HTML测试报告：
```bash
pytest -v test_user_api.py --html=report.html
```

## 测试用例说明

测试用例按照以下顺序执行：

1. test_01_register - 测试用户注册
2. test_02_login - 测试用户登录
3. test_03_get_current_user - 测试获取当前用户信息
4. test_04_update_user - 测试更新用户信息
5. test_05_update_password - 测试修改密码
6. test_06_search_users - 测试搜索用户
7. test_07_get_users_by_page - 测试分页获取用户列表
8. test_08_update_status - 测试更新用户状态
9. test_09_delete_user - 测试删除用户

## 常见问题解决

### 1. SSL证书错误

如果遇到SSL证书错误，请按照以下步骤处理：

1. 确保已经设置了环境变量：
   ```bash
   set PYTHONHTTPSVERIFY=0
   ```

2. 或者使用了正确的pip配置文件

3. 如果仍然有问题，可以尝试：
   ```bash
   pip install --trusted-host pypi.org --trusted-host files.pythonhosted.org -r requirements.txt
   ```

### 2. 依赖安装失败

1. 清除pip缓存：
   ```bash
   pip cache purge
   ```

2. 使用国内镜像源：
   ```bash
   pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
   ```

### 3. 测试执行超时

1. 检查后端服务是否正常运行
2. 适当增加测试超时时间：
   ```bash
   pytest -v test_user_api.py --timeout=30
   ```

### 4. 代理设置问题

如果您在使用代理，请设置以下环境变量：
```bash
set HTTP_PROXY=http://your-proxy:port
set HTTPS_PROXY=http://your-proxy:port
```

## 注意事项

1. 测试用例之间有依赖关系，请按顺序执行
2. 每次运行测试会创建新的测试用户
3. 测试完成后会自动清理创建的测试数据
4. 如果测试失败，请检查：
   - 后端服务是否正常运行
   - 数据库连接是否正常
   - API接口地址是否正确
   - 网络连接是否正常 