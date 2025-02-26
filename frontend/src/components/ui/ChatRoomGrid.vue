<template>
  <v-container fluid>
    <div v-if="selectedRoomId">
        <ChatRoom
          :roomId="selectedRoomId"
          :roomName="selectedRoomName"
          @back="selectedRoomId = null, selectedRoomName = null"
        />
    </div>
    <div v-else>

        <!-- 채팅방 생성 버튼 -->
        <v-row class="mb-4">
          <v-col>
            <v-btn 
              color="primary" 
              @click="openCreateDialog" 
              prepend-icon="mdi-plus"
            >
              새 채팅방
            </v-btn>
          </v-col>
        </v-row>
    
        <!-- 채팅방 목록 -->
        <v-row>
          <v-col>
            <v-card flat>
              <v-list lines="two">
                <v-list-item
                  v-for="room in chatRooms"
                  :key="room.room_id"
                  :value="room"
                  :active="selectedRow?.room_id === room.room_id"
                  :class="{ 'selected-room': selectedRow?.room_id === room.room_id }"
                  @click="selectedRow = room"
                >
                  <!-- 채팅방 정보 -->
                  <template v-slot:prepend>
                    <v-avatar color="primary" class="mr-3">
                      <v-icon color="white">{{ room.is_private ? 'mdi-lock' : 'mdi-chat' }}</v-icon>
                    </v-avatar>
                  </template>
    
                  <v-list-item-title class="text-h6 mb-1">
                    {{ room.room_name }}
                  </v-list-item-title>
                  
                  <v-list-item-subtitle>
                    ID: {{ room.room_id }}
                  </v-list-item-subtitle>
    
                  <!-- 선택된 방에만 표시되는 작업 버튼들 -->
                  <template v-slot:append>
                    <div v-if="selectedRow?.room_id === room.room_id" class="d-flex align-center">
                      <v-btn
                        variant="text"
                        class="mr-2"
                        @click.stop="openEditDialog(room)"
                      >
                        <v-icon>mdi-pencil</v-icon>
                        <v-tooltip activator="parent" location="top">수정</v-tooltip>
                      </v-btn>
                      <v-btn
                        variant="text"
                        class="mr-2"
                        @click.stop="handleEnterRoom(room)"
                      >
                        <v-icon>mdi-login</v-icon>
                        <v-tooltip activator="parent" location="top">입장</v-tooltip>
                      </v-btn>
                      <v-btn
                        variant="text"
                        @click.stop="deleteRoom(room)"
                      >
                        <v-icon>mdi-delete</v-icon>
                        <v-tooltip activator="parent" location="top">삭제</v-tooltip>
                      </v-btn>
                    </div>
                  </template>
                </v-list-item>
              </v-list>
            </v-card>
          </v-col>
        </v-row>
    
        <!-- 채팅방 생성/수정 다이얼로그 -->
        <v-dialog v-model="openDialog" max-width="500px">
          <v-card>
            <v-toolbar color="primary" class="text-white">
              <v-toolbar-title>{{ selectedRow ? '채팅방 수정' : '새 채팅방' }}</v-toolbar-title>
              <v-spacer></v-spacer>
              <v-btn icon @click="openDialog = false" color="white">
                <v-icon>mdi-close</v-icon>
              </v-btn>
            </v-toolbar>
    
            <v-card-text class="pt-4">
              <ChatRoom
                :isNew="!selectedRow"
                :editMode="true"
                v-model="newChatRoom"
                @add="handleRoomSaved"
                @edit="handleRoomSaved"
                @back="openDialog = false"
              />
            </v-card-text>
          </v-card>
        </v-dialog>
    
        <!-- 비밀번호 확인 다이얼로그 -->
        <v-dialog v-model="enterChatRoomDialog" max-width="500px">
          <EnterChatRoom
            :chatRoomInfo="selectedRow"
            @closeDialog="enterChatRoomDialog = false"
            @passwordValidated="onPasswordValidated"
          />
        </v-dialog>
    </div>

    <!-- 스낵바 -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
    >
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn text @click="snackbar.show = false">닫기</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { inject } from 'vue'
import ChatRoom from '../ChatRoom.vue'
import EnterChatRoom from '../EnterChatRoom.vue'

const supabase = inject('supabase')
const selectedRow = ref(null)
const enterChatRoomDialog = ref(false)
const openDialog = ref(false)
const selectedRoomId = ref(null)
const selectedRoomName = ref(null)
const chatRooms = ref([])

// 새 채팅방 생성을 위한 초기값
const newChatRoom = ref({
  roomId: '',
  roomPw: '',
  roomName: '',
  is_private: false
})

const snackbar = reactive({
  show: false,
  text: '',
  color: 'error',
  timeout: 3000
})

// 채팅방 목록 로드
const loadChatRooms = async () => {
  const { data, error } = await supabase
    .from('chat_rooms')
    .select('*')
  
  if (error) {
    console.error('Error loading chat rooms:', error)
    return
  }
  chatRooms.value = data
}

onMounted(() => {
  loadChatRooms()
})

// UUID 생성 함수
const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

// 채팅방 생성 다이얼로그 열기
const openCreateDialog = () => {
  selectedRow.value = null
  newChatRoom.value = {
    roomId: generateUUID(),
    roomPw: '',
    roomName: '',
    is_private: false
  }
  openDialog.value = true
}

// 채팅방 수정 다이얼로그 열기
const openEditDialog = (room) => {
  selectedRow.value = room
  newChatRoom.value = {
    roomId: room.room_id,
    roomPw: room.room_pw || '',
    roomName: room.room_name,
    is_private: room.is_private
  }
  openDialog.value = true
}

// 채팅방 삭제
const deleteRoom = async (room) => {
  try {
    const { error } = await supabase
      .from('chat_rooms')
      .delete()
      .eq('room_id', room.room_id)

    if (error) throw error
    
    await loadChatRooms()
    snackbar.text = '채팅방이 삭제되었습니다.'
    snackbar.color = 'success'
    snackbar.show = true
  } catch (e) {
    console.error('Error deleting chat room:', e)
    snackbar.text = '삭제에 실패했습니다.'
    snackbar.show = true
  }
}

// 채팅방 입장 처리
const handleEnterRoom = (room) => {
  if (room.is_private) {
    selectedRow.value = room
    enterChatRoomDialog.value = true
  } else {
    selectedRoomId.value = room.room_id
    selectedRoomName.value = room.room_name
  }
}

// 비밀번호 검증 성공 후 채팅방 입장
const onPasswordValidated = () => {
  selectedRoomId.value = selectedRow.value.room_id
  selectedRoomName.value = selectedRow.value.room_name
  enterChatRoomDialog.value = false
}

// 채팅방 생성/수정 완료 처리
const handleRoomSaved = () => {
  openDialog.value = false
  loadChatRooms()
  snackbar.text = selectedRow.value ? '채팅방이 수정되었습니다.' : '채팅방이 생성되었습니다.'
  snackbar.color = 'success'
  snackbar.show = true
}
</script>

<style scoped>
.selected-room {
  background-color: rgb(var(--v-theme-primary), 0.1);
}

.v-list-item {
  border-radius: 8px;
  margin: 4px 0;
  transition: all 0.2s ease;
}

.v-list-item:hover {
  background-color: rgb(var(--v-theme-primary), 0.05);
}

.v-btn {
  text-transform: none;
}
</style>