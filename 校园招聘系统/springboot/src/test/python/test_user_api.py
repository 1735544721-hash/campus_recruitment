import pytest
import requests
import json
from datetime import datetime
import time

# API基础URL
BASE_URL = "http://localhost:8080"

# 测试数据
test_user = {
    "username": "testuser",
    "password": "Test123456",
    "email": f"test_{datetime.now().strftime('%Y%m%d%H%M%S')}@test.com",
    "name": "测试用户",
    "roleCode": "ROLE_STUDENT"
}

class TestUserAPI:
    def setup_class(self):
        """测试类初始化"""
        self.token = None
        self.user_id = None
        self.session = requests.Session()
        # 设置请求超时
        self.timeout = 10
        # 设置重试次数
        self.max_retries = 3
        
    def teardown_class(self):
        """测试类清理"""
        self.session.close()
        
    def request_with_retry(self, method, url, **kwargs):
        """带重试机制的请求方法"""
        for i in range(self.max_retries):
            try:
                kwargs['timeout'] = self.timeout
                response = self.session.request(method, url, **kwargs)
                return response
            except requests.exceptions.RequestException as e:
                if i == self.max_retries - 1:
                    raise
                time.sleep(1)  # 重试前等待1秒
    
    @pytest.mark.timeout(30)
    def test_01_register(self):
        """测试用户注册"""
        url = f"{BASE_URL}/user/add"
        response = self.request_with_retry('POST', url, json=test_user)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert result["msg"] == "创建成功"
    
    @pytest.mark.timeout(30)
    def test_02_login(self):
        """测试用户登录"""
        url = f"{BASE_URL}/user/login"
        login_data = {
            "username": test_user["username"],
            "password": test_user["password"]
        }
        response = self.request_with_retry('POST', url, json=login_data)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert "token" in result["data"]
        
        # 保存token和用户ID供后续测试使用
        self.token = result["data"]["token"]
        self.user_id = result["data"]["id"]
    
    @pytest.mark.timeout(30)
    def test_03_get_current_user(self):
        """测试获取当前用户信息"""
        url = f"{BASE_URL}/user/current"
        headers = {"Authorization": f"Bearer {self.token}"}
        response = self.request_with_retry('GET', url, headers=headers)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert result["data"]["username"] == test_user["username"]
    
    @pytest.mark.timeout(30)
    def test_04_update_user(self):
        """测试更新用户信息"""
        url = f"{BASE_URL}/user/{self.user_id}"
        headers = {"Authorization": f"Bearer {self.token}"}
        update_data = {
            "name": "更新后的测试用户",
            "email": test_user["email"]
        }
        response = self.request_with_retry('PUT', url, json=update_data, headers=headers)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert result["msg"] == "更新成功"
    
    @pytest.mark.timeout(30)
    def test_05_update_password(self):
        """测试修改密码"""
        url = f"{BASE_URL}/user/password/{self.user_id}"
        headers = {"Authorization": f"Bearer {self.token}"}
        password_data = {
            "oldPassword": test_user["password"],
            "newPassword": "NewTest123456",
            "confirmPassword": "NewTest123456"
        }
        response = self.request_with_retry('PUT', url, json=password_data, headers=headers)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert result["msg"] == "密码修改成功"
        
        # 更新测试数据中的密码
        test_user["password"] = "NewTest123456"
    
    @pytest.mark.timeout(30)
    def test_06_search_users(self):
        """测试搜索用户"""
        url = f"{BASE_URL}/user/search"
        headers = {"Authorization": f"Bearer {self.token}"}
        params = {"keyword": test_user["username"]}
        response = self.request_with_retry('GET', url, params=params, headers=headers)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert len(result["data"]) > 0
    
    @pytest.mark.timeout(30)
    def test_07_get_users_by_page(self):
        """测试分页获取用户列表"""
        url = f"{BASE_URL}/user/page"
        headers = {"Authorization": f"Bearer {self.token}"}
        params = {
            "currentPage": 1,
            "size": 10,
            "username": test_user["username"]
        }
        response = self.request_with_retry('GET', url, params=params, headers=headers)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert result["data"]["total"] > 0
    
    @pytest.mark.timeout(30)
    def test_08_update_status(self):
        """测试更新用户状态"""
        url = f"{BASE_URL}/user/status/{self.user_id}"
        headers = {"Authorization": f"Bearer {self.token}"}
        params = {"status": 0}  # 0-禁用，1-启用
        response = self.request_with_retry('PUT', url, params=params, headers=headers)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
    
    @pytest.mark.timeout(30)
    def test_09_delete_user(self):
        """测试删除用户"""
        url = f"{BASE_URL}/user/delete/{self.user_id}"
        headers = {"Authorization": f"Bearer {self.token}"}
        response = self.request_with_retry('DELETE', url, headers=headers)
        result = response.json()
        
        assert response.status_code == 200
        assert result["code"] == 200
        assert result["msg"] == "删除成功"

if __name__ == "__main__":
    pytest.main(["-v", "test_user_api.py", "--reruns", "3", "--reruns-delay", "1"]) 