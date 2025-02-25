<script setup>
import {
  VerticalNavLink,
  VerticalNavSectionTitle,
} from '@layouts'
</script>

<template>
    <ul>
        <VerticalNavSectionTitle :item="{ heading: 'Chat' }" />
        <VerticalNavLink
            :item="{
                title: 'ChatRoom',
                to: '/chats/chatRooms',
            }"
        />
        <VerticalNavLink
            :item="{
                title: 'Message',
                to: '/chats/messages',
            }"
        />
    </ul>
</template>
