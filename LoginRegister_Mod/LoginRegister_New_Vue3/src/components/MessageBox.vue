<template>
  <Transition name="fade">
    <div v-if="visible" class="message-overlay" @click="handleClose">
      <div class="message-box" @click.stop>
        <div class="message-icon" :class="type">
          <svg v-if="type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
          </svg>
          <svg v-else-if="type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <div class="message-content">
          <p>{{ message }}</p>
        </div>
        <button class="message-close" @click="handleClose">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  message: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'info', // success, error, info
    validator: (value) => ['success', 'error', 'info'].includes(value)
  },
  duration: {
    type: Number,
    default: 3000
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const visible = ref(props.show)

watch(() => props.show, (newVal) => {
  visible.value = newVal
  if (newVal && props.duration > 0) {
    setTimeout(() => {
      handleClose()
    }, props.duration)
  }
})

const handleClose = () => {
  visible.value = false
  emit('close')
}
</script>

<style scoped>
.message-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 20vh;
  z-index: 9999;
  backdrop-filter: blur(2px);
}

.message-box {
  background: white;
  border-radius: 12px;
  padding: 24px;
  min-width: 300px;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-icon svg {
  width: 24px;
  height: 24px;
}

.message-icon.success {
  background: #e8f5e9;
  color: #4caf50;
}

.message-icon.error {
  background: #ffebee;
  color: #f44336;
}

.message-icon.info {
  background: #e3f2fd;
  color: #2196f3;
}

.message-content {
  flex: 1;
  padding-top: 8px;
}

.message-content p {
  margin: 0;
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}

.message-close {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  width: 24px;
  height: 24px;
  padding: 0;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-close:hover {
  color: #333;
}

.message-close svg {
  width: 16px;
  height: 16px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

