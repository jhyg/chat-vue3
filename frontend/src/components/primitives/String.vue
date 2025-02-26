<template>
    <div>
        <div v-if="editMode">
            <div class="label-title" style="margin-left: 5px;">{{ label }}</div>
            <v-text-field 
                v-bind="$attrs"
                :model-value="modelValue"
                @update:model-value="updateValue"
                :label="label"
                outlined
                single-line
            />
        </div>
        <div v-else>
            {{ label }} : {{ modelValue }}
        </div>
    </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    editMode: {
        type: Boolean,
        default: false
    },
    label: {
        type: String,
        required: true
    }
})

const emit = defineEmits(['update:modelValue'])

const value = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
    value.value = newVal
})

const updateValue = (newValue) => {
    value.value = newValue
    emit('update:modelValue', newValue)
}
</script>

<style scoped>
.label-title {
    font-size: 0.875rem;
    color: rgba(0, 0, 0, 0.6);
    margin-bottom: 4px;
}
</style>