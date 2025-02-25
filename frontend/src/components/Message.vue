<template>
    <div>
        <String label="MessageId" v-model="value.messageId" :editMode="editMode"/>
        <String label="RoomId" v-model="value.roomId" :editMode="editMode"/>
        <String label="UserId" v-model="value.userId" :editMode="editMode"/>
        <String label="Content" v-model="value.content" :editMode="editMode"/>
        <v-row class="ma-0 pa-0">
            <v-spacer></v-spacer>
            <v-btn
                width="64px"
                color="primary"
                @click="save"
            >
                저장
            </v-btn>
        </v-row>
    </div>
</template>


<script>
import BaseEntity from './base-ui/BaseEntity.vue'

export default {
    name: 'Message',
    mixins:[BaseEntity],
    components:{
    },
    
    data: () => ({
        path: "messages",
        value: {
        }
    }),
    created(){
    },
    computed:{
    },
    methods: {
    },
}
</script>
