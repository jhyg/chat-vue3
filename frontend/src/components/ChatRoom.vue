<script setup>
import { ref, reactive, onMounted, onUnmounted, inject, nextTick } from 'vue'
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

// 스크롤 관련 상태
const messageContainer = ref(null)
const isAtBottom = ref(true)
const newMessageNotification = ref(null)

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

// 스크롤 관련 함수
const scrollToBottom = () => {
  if (!messageContainer.value) return
  // nextTick을 사용하여 DOM 업데이트 후 스크롤 실행
  nextTick(() => {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  })
}

const checkIfAtBottom = () => {
  if (!messageContainer.value) return
  const { scrollTop, scrollHeight, clientHeight } = messageContainer.value
  // 여유값을 조금 더 늘려서 스크롤 감지를 더 관대하게 설정
  isAtBottom.value = Math.abs(scrollHeight - scrollTop - clientHeight) < 50
}

const handleScroll = () => {
  checkIfAtBottom()
  // 스크롤이 하단에 도달하면 알림 제거
  if (isAtBottom.value) {
    newMessageNotification.value = null
  }
}

// 스크롤 버튼 클릭 핸들러 수정
const scrollToBottomWithNotificationClear = () => {
  scrollToBottom()
  newMessageNotification.value = null
}

// 메시지 전송 함수 수정
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
    // 내가 보낸 메시지는 항상 스크롤
    nextTick(() => {
      scrollToBottom()
    })
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

// 메시지 구독 함수 수정
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
        
        nextTick(() => {
          // 메시지를 보낸 사람이 나인 경우 또는 스크롤이 하단에 있는 경우
          if (payload.new.user_id === userInfo.value.user_id || isAtBottom.value) {
            scrollToBottom()
          } else {
            // 다른 사람의 메시지이고 스크롤이 하단이 아닌 경우
            newMessageNotification.value = payload.new
          }
        })
      }
    })
    .subscribe()
}

// 라이프사이클 훅 수정
onMounted(() => {
  if (props.roomId) {
    loadMessages().then(() => {
      scrollToBottom()
      if (messageContainer.value) {
        messageContainer.value.addEventListener('scroll', handleScroll)
      }
    })
    subscribeToMessages()
  }
})

onUnmounted(() => {
  if (messageSubscription) {
    messageSubscription.unsubscribe()
  }
  if (messageContainer.value) {
    messageContainer.value.removeEventListener('scroll', handleScroll)
  }
})

// 텍스트 길이 제한 함수 추가
const truncateText = (text, maxLength = 90) => {
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}
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
              <div v-if="message.user_id != userInfo.user_id" class="message-user">{{ message.user_name }}</div>
              <div class="message-text">{{ message.content }}</div>
            </div>
            <div class="message-time">
              {{ new Date(message.timestamp).toLocaleTimeString('ko-KR', { 
                hour: 'numeric', 
                minute: '2-digit', 
                hour12: true 
              }) }}
            </div>
          </div>
        </div>

        <!-- 새 메시지 알림 수정 -->
        <div v-if="newMessageNotification" 
             class="new-message-notification"
             @click="scrollToBottomWithNotificationClear">
          <v-card
            class="notification-card d-flex align-center"
            flat
          >
            <div class="notification-content">
              <div class="notification-name">{{ newMessageNotification.user_name }}</div>
              <div class="notification-text">{{ truncateText(newMessageNotification.content) }}</div>
            </div>
            <v-icon color="grey lighten-1">mdi-chevron-down</v-icon>
          </v-card>
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
  align-items: flex-end;
  gap: 8px;
}

.message-own {
  flex-direction: row-reverse;
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
  white-space: nowrap;
}

.message-input {
  display: flex;
  gap: 8px;
}

.chat-form-container {
  padding: 16px;
}

.new-message-notification {
  position: absolute;
  bottom: 70px;
  width: 100%;
  cursor: pointer;
  z-index: 1;
  padding: 0;
  padding-right: 60px;
}

.notification-card {
  background-color: rgba(240, 240, 240, 0.85) !important;
  border: none !important;
  border-radius: 4px !important;
  box-shadow: none !important;
  padding: 8px 12px;
  transition: all 0.2s ease;
  backdrop-filter: blur(1px);
  margin: 0;
}

.notification-card:hover {
  background-color: rgba(245, 245, 245, 0.9) !important;
  transform: translateY(-1px);
}

.notification-content {
  flex-grow: 1;
  margin-right: 12px;
}

.notification-name {
  font-size: 0.8rem;
  color: rgba(0, 0, 0, 0.5);
  margin-bottom: 2px;
  font-weight: 500;
}

.notification-text {
  color: rgba(0, 0, 0, 0.75);
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100vw - 80px);
  line-height: 1.2;
}
</style>