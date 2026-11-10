import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElLoading } from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/variables.css'
import '@/styles/global.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
// 按需引入后 v-loading 指令需手动注册
app.directive('loading', ElLoading.directive)

app.mount('#app')