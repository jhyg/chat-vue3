<template>
  <v-card outlined class="enter-chat-dialog">
    <v-card-title class="chat-dialog-header">
      <div class="d-flex align-center">
        <v-icon color="primary" class="mr-3">mdi-lock</v-icon>
        비밀번호 확인
      </div>
      <v-btn
        icon
        variant="plain"
        @click="close"
        class="close-button"
      >
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>

    <v-card-text class="pt-5">
      <v-form ref="form" v-model="isFormValid">
        <v-text-field
          v-model="password"
          label="방 비밀번호"
          :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
          :type="showPassword ? 'text' : 'password'"
          @click:append="showPassword = !showPassword"
          :rules="[v => !!v || '비밀번호를 입력해주세요']"
          outlined
          dense
          class="mb-3"
          @keyup.enter="validatePassword"
        ></v-text-field>
      </v-form>
    </v-card-text>

    <v-divider></v-divider>

    <v-card-actions class="pa-4">
      <v-spacer></v-spacer>
      <v-btn
        text
        @click="close"
        class="mr-2"
      >
        취소
      </v-btn>
      <v-btn
        color="primary"
        :disabled="!isFormValid"
        @click="validatePassword"
        :loading="loading"
      >
        확인
      </v-btn>
    </v-card-actions>

    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
      top
    >
      {{ snackbar.text }}
      <template v-slot:action="{ attrs }">
        <v-btn
          text
          v-bind="attrs"
          @click="snackbar.show = false"
        >
          닫기
        </v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { inject } from 'vue'

const props = defineProps({
  chatRoomInfo: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['closeDialog', 'passwordValidated'])

const supabase = inject('supabase')
const form = ref(null)
const password = ref('')
const isFormValid = ref(false)
const showPassword = ref(false)
const loading = ref(false)

const snackbar = reactive({
  show: false,
  text: '',
  color: 'error',
  timeout: 3000
})

// 비밀번호 검증
const validatePassword = async () => {
  if (!form.value?.validate()) return

  loading.value = true
  try {
    if (password.value === props.chatRoomInfo.room_pw) {
      emit('passwordValidated')
    } else {
      snackbar.text = '비밀번호가 일치하지 않습니다.'
      snackbar.show = true
      password.value = ''
    }
  } catch (error) {
    console.error('Error validating password:', error)
    snackbar.text = '오류가 발생했습니다.'
    snackbar.show = true
  } finally {
    loading.value = false
  }
}

const close = () => {
  password.value = ''
  emit('closeDialog')
}
</script>

<style scoped>
.enter-chat-dialog {
  border-radius: 8px;
}

.chat-dialog-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  padding: 16px;
  position: relative;
  display: flex;
  align-items: center;
}

.close-button {
  position: absolute;
  right: 8px;
  top: 8px;
}

/* 다크 모드 지원 */
:deep(.v-theme--dark) .chat-dialog-header {
  background-color: #1e1e1e;
}
</style>

