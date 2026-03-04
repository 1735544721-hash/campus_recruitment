<template>
  <div class="error-container">
    <!-- 左侧品牌宣传区域 -->
    <div class="error-brand">
      <div class="brand-content">
        <div class="logo">
          <span>聘</span>
        </div>
        <h1 class="brand-title">校园招聘系统</h1>
        <div class="brand-image">
          <img src="@/assets/auth-illustration.svg" alt="校园招聘" />
        </div>
        <p class="brand-slogan">连接校园与企业，成就职业未来</p>
      </div>
    </div>
    
    <!-- 右侧错误信息区域 -->
    <div class="error-info-container">
      <div class="error-box">
        <div class="error-code">404</div>
        <h2 class="error-title">页面未找到</h2>
        <p class="error-message">抱歉，您访问的页面不存在</p>
        <router-link to="/" class="back-home">
          返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
@import '@/styles/auth.scss';

.error-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  
  // 左侧品牌区样式
  .error-brand {
    flex: 1;
    background: linear-gradient(135deg, #4a6fa1 0%, #2c4b7c 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .brand-content {
      text-align: center;
      padding: 2rem;
      
      .logo {
        display: inline-block;
        background-color: white;
        color: #2c4b7c;
        width: 80px;
        height: 80px;
        border-radius: 50%;
        line-height: 80px;
        font-size: 40px;
        margin-bottom: 1.5rem;
        
        span {
          font-family: "Songti SC", "STSong", serif;
          writing-mode: vertical-rl;
          text-orientation: upright;
          letter-spacing: 0.5em;
        }
      }
      
      .brand-title {
        font-family: "Songti SC", "STSong", serif;
        font-size: 2.5rem;
        margin-bottom: 2rem;
      }
      
      .brand-image {
        max-width: 80%;
        margin: 0 auto 2rem;
        
        img {
          max-width: 100%;
        }
      }
      
      .brand-slogan {
        font-family: "Songti SC", "STSong", serif;
        font-size: 1.2rem;
        opacity: 0.9;
      }
    }
  }
  
  // 右侧错误信息区样式
  .error-info-container {
    flex: 1;
    background-color: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .error-box {
      width: 80%;
      max-width: 400px;
      padding: 2rem;
      text-align: center;
      
      .error-code {
        font-family: "Songti SC", "STSong", serif;
        font-size: 120px;
        font-weight: 700;
        color: #2c4b7c;
        line-height: 1;
        margin-bottom: 1rem;
      }
      
      .error-title {
        font-family: "Songti SC", "STSong", serif;
        font-size: 30px;
        margin: 20px 0;
        color: #333;
      }
      
      .error-message {
        font-family: "Songti SC", "STSong", serif;
        color: #666;
        margin-bottom: 30px;
      }
      
      .back-home {
        display: inline-block;
        padding: 10px 30px;
        background: #2c4b7c;
        color: white;
        text-decoration: none;
        border-radius: 4px;
        transition: background 0.3s;
        font-family: "Songti SC", "STSong", serif;
        letter-spacing: 0.1em;
        
        &:hover {
          background: #4a6fa1;
        }
      }
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .error-container {
    flex-direction: column;
    
    .error-brand {
      flex: none;
      height: 40vh;
      
      .brand-content {
        padding: 1rem;
        
        .logo {
          width: 60px;
          height: 60px;
          line-height: 60px;
          font-size: 30px;
          margin-bottom: 1rem;
        }
        
        .brand-title {
          font-size: 1.8rem;
          margin-bottom: 1rem;
        }
        
        .brand-image {
          display: none;
        }
      }
    }
    
    .error-info-container {
      flex: none;
      height: 60vh;
    }
  }
}
</style> 