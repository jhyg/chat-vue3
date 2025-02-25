import { createRouter, createWebHashHistory } from 'vue-router';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../components/pages/Index.vue'),
    },
    {
      path: '/chats/chatRooms',
      component: () => import('../components/ui/ChatRoomGrid.vue'),
    },
    {
      path: '/chats/messages',
      component: () => import('../components/ui/MessageGrid.vue'),
    },
  ],
})

export default router;
