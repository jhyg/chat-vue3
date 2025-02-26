<script setup>
import { ref, reactive, onMounted, onUnmounted, inject } from 'vue'
import String from './primitives/String.vue'

const props = defineProps({
  roomId: {
    type: String,
    required: false
  },
  roomName: {
    type: String,
    required: false
  },
  modelValue: {
    type: Object,
    default: () => ({
      roomId: '',
      roomPw: '',
      roomName: '',
      is_private: false
    })
  },
  editMode: {
    type: Boolean,
    default: false
  },
  isNew: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['back', 'update:modelValue', 'add', 'edit'])

const supabase = inject('supabase')
const form = ref(null)
const localValue = ref({ ...props.modelValue })

// 채팅 관련 상태
const messages = ref([])
const newMessage = ref('')
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{"user_id": "1", "user_name": "John Doe"}'))
let messageSubscription

// 폼 관련 상태
const state = reactive({
  isFormValid: false,
  isPrivate: props.modelValue.is_private,
  snackbar: {
    show: false,
    text: '',
    color: 'error',
    timeout: 3000
  }
})

// UUID 생성 함수
const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

// 채팅방 생성/수정 관련 함수
const save = async () => {
  try {
    const roomData = {
      room_id: localValue.value.roomId,
      room_pw: localValue.value.roomPw,
      room_name: localValue.value.roomName,
      is_private: state.isPrivate
    }

    const { error } = await supabase
      .from('chat_rooms')
      .upsert([roomData])

    if (error) throw error

    if (props.isNew) {
      emit('add', localValue.value)
      // 폼 초기화
      localValue.value = {
        roomId: '',
        roomPw: '',
        roomName: '',
        is_private: false
      }
      state.isPrivate = false
      form.value?.reset()
    } else {
      emit('edit', localValue.value)
    }
    emit('back')
  } catch (e) {
    state.snackbar.text = '저장에 실패했습니다.'
    state.snackbar.show = true
  }
}

// 채팅 관련 함수
const sendMessage = async () => {
  if (!newMessage.value.trim() || !userInfo.value) return

  try {
    const { error } = await supabase
      .from('messages')
      .insert([{
        message_id: generateUUID(),
        room_id: props.roomId,
        user_id: userInfo.value.user_id,
        user_name: userInfo.value.user_name,
        content: newMessage.value,
        timestamp: new Date().toLocaleString('en-US', { timeZone: 'Asia/Seoul' })
      }])

    if (error) throw error
    newMessage.value = ''
  } catch (e) {
    state.snackbar.text = '메시지 전송에 실패했습니다.'
    state.snackbar.show = true
  }
}

const loadMessages = async () => {
  const { data, error } = await supabase
    .from('messages')
    .select('*')
    .eq('room_id', props.roomId)
    .order('timestamp', { ascending: true })

  if (error) {
    console.error('Error loading messages:', error)
    return
  }
  messages.value = data
}

const subscribeToMessages = () => {
  messageSubscription = supabase
    .channel('messages')
    .on('postgres_changes', {
      event: '*',
      schema: 'public',
      table: 'messages',
      filter: `room_id=eq.${props.roomId}`
    }, (payload) => {
      if (payload.eventType === 'INSERT') {
        messages.value.push(payload.new)
      }
    })
    .subscribe()
}

// 라이프사이클 훅
onMounted(() => {
  if (props.roomId) {
    loadMessages()
    subscribeToMessages()
  }
})

onUnmounted(() => {
  if (messageSubscription) {
    messageSubscription.unsubscribe()
  }
})
</script>

<template>
  <div>
    <!-- 채팅방 생성/수정 모드 -->
    <div v-if="!roomId" class="chat-form-container">
      <v-form ref="form" v-model="state.isFormValid">
        <!-- <String label="방 ID" v-model="localValue.roomId" :editMode="editMode"/> -->
        <String label="방 이름" v-model="localValue.roomName" :editMode="editMode"/>
        
        <v-switch
          v-model="state.isPrivate"
          label="비공개 방"
          :disabled="!editMode"
        />
        <String 
          v-if="state.isPrivate" 
          label="비밀번호" 
          v-model="localValue.roomPw" 
          :editMode="editMode"
        />

        <v-btn
          color="primary"
          @click="save"
          :disabled="!state.isFormValid"
          class="mt-4 float-right"
        >
          저장
        </v-btn>
      </v-form>
    </div>

    <!-- 채팅방 대화 모드 -->
    <v-card v-else class="chat-room">
      <v-card-title class="chat-header">
        <div class="d-flex align-center">
          <v-btn icon @click="$emit('back')">
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
          <div>
            <v-list-item-title class="text-h6 mb-1" style="margin-left: 10px;">
              {{ props.roomName }}
            </v-list-item-title>
            <v-list-item-subtitle style="margin-left: 10px;">
              {{ props.roomId }}
            </v-list-item-subtitle>
          </div>
        </div>
      </v-card-title>

      <v-card-text class="chat-content">
        <div class="messages" ref="messageContainer" style="height: 60vh; overflow-y: scroll;">
          <div v-for="message in messages" 
               :key="message.message_id" 
               :class="['message', { 'message-own': message.user_id === userInfo.user_id }]">
            <div class="message-content">
              <div class="message-user">{{ message.user_name }}</div>
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">
                {{ new Date(message.timestamp).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', timeZone: 'Asia/Seoul' }) }}
              </div>
            </div>
          </div>
        </div>

        <div class="message-input">
          <v-text-field
            v-model="newMessage"
            placeholder="메시지를 입력하세요."
            @keyup.enter="sendMessage"
            dense
            outlined
          />
          <v-btn color="primary" @click="sendMessage">
            전송
          </v-btn>
        </div>
      </v-card-text>
    </v-card>

    <!-- 스낵바 -->
    <v-snackbar
      v-model="state.snackbar.show"
      :color="state.snackbar.color"
      :timeout="state.snackbar.timeout"
    >
      {{ state.snackbar.text }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="state.snackbar.show = false">
          닫기
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<style scoped>
.chat-room {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.chat-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.messages {
  flex-grow: 1;
  overflow-y: auto;
  margin-bottom: 16px;
  margin-top: 10px;
}

.message {
  margin-bottom: 12px;
  display: flex;
}

.message-own {
  justify-content: flex-end;
  margin-right: 10px;
}

.message-content {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.message-own .message-content {
  background-color: #e3f2fd;
}

.message-user {
  font-size: 0.8rem;
  color: #666;
  margin-bottom: 4px;
  font-weight: bold;
}

.message-time {
  font-size: 0.7rem;
  color: #999;
  text-align: right;
  margin-top: 4px;
}

.message-input {
  display: flex;
  gap: 8px;
}

.chat-form-container {
  padding: 16px;
}
</style>