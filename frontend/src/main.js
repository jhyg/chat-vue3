/* eslint-disable import/order */
import '@/@iconify/icons-bundle'
import App from '@/App.vue'
import vuetify from '@/plugins/vuetify'
import { loadFonts } from '@/plugins/webfontloader'
import router from '@/router'
import '@/styles/styles.scss'
import '@core/scss/index.scss'
import { createPinia } from 'pinia'
import { createApp } from 'vue'
import axios from 'axios'
import { Icon } from '@iconify/vue'
import { createClient } from '@supabase/supabase-js'

loadFonts()

// Create vue app
const app = createApp(App)

// Setting Config
axios.defaults.baseURL = ''
app.config.globalProperties.$axios = axios



const supabaseUrl = 'http://127.0.0.1:54321'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0'

const supabase = createClient(supabaseUrl, supabaseAnonKey)

// 전역으로 supabase 인스턴스 제공
app.provide('supabase', supabase)
// 편의를 위해 globalProperties에도 추가
app.config.globalProperties.$supabase = supabase

// Component
app.component('Icon',Icon)

// Use plugins
app.use(vuetify)
app.use(createPinia())
app.use(router)

// Mount vue app
app.mount('#app')
