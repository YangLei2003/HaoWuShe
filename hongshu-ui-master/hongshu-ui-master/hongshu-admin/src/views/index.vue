<template>
  <div class="app-container home">
    <div class="welcome-section">
      <el-row :gutter="40" flex align="middle">
        <el-col :sm="24" :lg="12">
          <div class="intro-card">
            <h2 class="main-title"><b>好物社后台管理系统</b></h2>
            <div class="brand-line"></div>
            <p class="description">
              本项目基于 <span>SpringBoot 3.0</span> + <span>Vue 3.0</span> 搭建的前后端分离系统。
              <br /><br />
              管理系统基于若依框架搭建，整体布局和配色风格深度借鉴了 <strong>"小红书"</strong> 平台。
              <br /><br />
              <i class="ai-thanks">前端代码特此感谢 DeepSeek 和 Gemini 的鼎力相助。</i>
              <br /><br />
              后端使用 SpringBoot + Mybatis-plus 进行开发，辅以 ElasticSearch 全文检索与 Redis 高性能缓存。
            </p>
          </div>
        </el-col>

        <el-col :sm="24" :lg="12">
          <div class="tech-card">
            <h2 class="tech-title"><b>技术选型</b></h2>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="tech-group">
                  <h4>后端技术</h4>
                  <ul>
                    <li><el-tag size="small" effect="plain" type="danger">SpringBoot</el-tag></li>
                    <li><el-tag size="small" effect="plain" type="danger">Redis</el-tag></li>
                    <li><el-tag size="small" effect="plain" type="danger">MyBatis</el-tag></li>
                    <li><el-tag size="small" effect="plain" type="danger">MyBatis-Plus</el-tag></li>
                    <li><el-tag size="small" effect="plain" type="danger">ElasticSearch</el-tag></li>
                  </ul>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="tech-group">
                  <h4>前端技术</h4>
                  <ul>
                    <li><el-tag size="small" effect="plain">Vue 3.0</el-tag></li>
                    <li><el-tag size="small" effect="plain">Element Plus</el-tag></li>
                    <li><el-tag size="small" effect="plain">Sass</el-tag></li>
                  </ul>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-divider />

    <el-row :gutter="20">
    </el-row>
  </div>
</template>

<script setup name="Index">
import { ref } from 'vue'; // 确保引入了 ref
const version = ref("1.0.0");

function goTarget(url) {
  window.open(url, "__blank");
}
</script>

<style scoped lang="scss">
/* 小红书品牌色 */
$xhs-red: #ff2442;
$text-main: #333;
$text-sub: #666;
$bg-card: #ffffff;

.home {
  padding: 30px;
  background-color: #f9f9f9; /* 浅灰色背景衬托白色卡片 */
  min-height: 100vh;
  font-family: -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;

  .welcome-section {
    max-width: 1400px; /* 适配 24 寸大屏，限制最大宽度防止太散 */
    margin: 0 auto;
  }

  /* 通用卡片样式 */
  .intro-card, .tech-card {
    background: $bg-card;
    border-radius: 16px;
    padding: 40px;
    height: 100%;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
    transition: transform 0.3s ease;

    &:hover {
      transform: translateY(-5px);
    }
  }

  /* 标题美化 */
  .main-title, .tech-title {
    font-size: 28px;
    color: $text-main;
    margin-bottom: 15px;
    font-weight: 700 !important;
  }

  .brand-line {
    width: 60px;
    height: 4px;
    background-color: $xhs-red;
    border-radius: 2px;
    margin-bottom: 25px;
  }

  /* 文字排版 */
  .description {
    font-size: 16px;
    line-height: 1.8;
    color: $text-sub;

    span {
      color: $xhs-red;
      font-weight: 600;
    }

    .ai-thanks {
      font-size: 14px;
      color: #999;
      border-left: 3px solid #eee;
      padding-left: 10px;
      margin-top: 10px;
      display: inline-block;
    }
  }

  /* 技术列表美化 */
  .tech-group {
    h4 {
      font-size: 18px;
      color: $text-main;
      margin-bottom: 20px;
      border-bottom: 1px solid #f0f0f0;
      padding-bottom: 10px;
    }

    ul {
      list-style: none;
      padding: 0;
      display: flex;
      flex-wrap: wrap;
      gap: 10px; /* 标签间距 */

      li {
        margin-bottom: 5px;
      }
    }
  }

  /* 覆盖 Element Divider 颜色 */
  :deep(.el-divider--horizontal) {
    margin: 40px 0;
    background-color: #eee;
  }
}
</style>