<template>
  <div class="grok-home">
    <div class="hero-container">
      <div class="logo-area">
        <h1 class="grok-title">Mobile Classroom</h1>
        <p class="grok-subtitle">Intelligent Learning Management System</p>
      </div>

      <div class="search-wrapper">
        <div class="search-box">
          <i class="el-icon-search search-icon"></i>
          <input 
            type="text" 
            placeholder="Search for courses, students, or resources..." 
            class="grok-input"
          />
          <div class="action-icon">
            <i class="el-icon-right"></i>
          </div>
        </div>
      </div>

      <div class="quick-cards">
        <div class="card" @click="$router.push('/proj_myx/Attendance')">
          <div class="card-icon blue"><i class="el-icon-location"></i></div>
          <h3>Attendance</h3>
          <p>Manage class check-ins</p>
        </div>
        <div class="card" @click="$router.push('/proj_myx/Vote')">
          <div class="card-icon green"><i class="el-icon-data-analysis"></i></div>
          <h3>Voting</h3>
          <p>Create polls & surveys</p>
        </div>
        <div class="card" @click="$router.push('/proj_myx/RandomPick')">
          <div class="card-icon purple"><i class="el-icon-magic-stick"></i></div>
          <h3>Random Pick</h3>
          <p>Select students randomly</p>
        </div>
        <div class="card" @click="$router.push('/system/user')">
          <div class="card-icon orange"><i class="el-icon-user"></i></div>
          <h3>Users</h3>
          <p>Manage system users</p>
        </div>
      </div>
    </div>
    
    <div class="footer-info">
      <p>Mobile Classroom v3.8.9 | Powered by RuoYi-Vue</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      version: "3.8.9"
    };
  },
  methods: {
    goTarget(url) {
      window.open(url, "__blank");
    },
  },
};
</script>

<style scoped lang="scss">
.grok-home {
  min-height: calc(100vh - 84px);
  background-color: #ffffff; /* Grok Light Background */
  color: #000000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;

  /* Subtle background gradient/glow */
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle at center, rgba(59, 130, 246, 0.05) 0%, transparent 50%);
    z-index: 0;
    pointer-events: none;
  }
}

.hero-container {
  z-index: 1;
  text-align: center;
  max-width: 800px;
  width: 100%;
}

.grok-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: -0.02em;
  background: linear-gradient(to right, #111827, #4b5563);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.grok-subtitle {
  font-size: 20px;
  color: #6b7280;
  margin-bottom: 48px;
  font-weight: 400;
}

.search-wrapper {
  margin-bottom: 60px;
  position: relative;
}

.search-box {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);

  &:hover, &:focus-within {
    border-color: #d1d5db;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025);
  }
}

.search-icon {
  font-size: 20px;
  color: #9ca3af;
  margin-right: 16px;
}

.grok-input {
  background: transparent;
  border: none;
  color: #111827;
  font-size: 18px;
  width: 100%;
  outline: none;
  
  &::placeholder {
    color: #9ca3af;
  }
}

.action-icon {
  background: #f3f4f6;
  color: #374151;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #e5e7eb;
    transform: scale(1.05);
  }
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 20px;
  width: 100%;
}

.card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  text-align: left;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);

  &:hover {
    border-color: #d1d5db;
    transform: translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025);
  }

  h3 {
    margin: 16px 0 8px 0;
    font-size: 16px;
    font-weight: 600;
    color: #111827;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: #6b7280;
  }
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 16px;

  &.blue { background: #eff6ff; color: #3b82f6; }
  &.green { background: #ecfdf5; color: #10b981; }
  &.purple { background: #f5f3ff; color: #8b5cf6; }
  &.orange { background: #fffbeb; color: #f59e0b; }
}

.footer-info {
  position: absolute;
  bottom: 20px;
  color: #9ca3af;
  font-size: 12px;
}
</style>
