<template>
  <div>
    <div class="flex items-center gap-1.5 py-1 px-1 hover:bg-gray-50 rounded cursor-pointer" @click="toggle">
      <span class="w-4 h-4 flex items-center justify-center flex-shrink-0">
        <svg v-if="hasChildren" class="w-3 h-3 text-gray-400 transition-transform duration-200" :class="{ 'rotate-90': expanded }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
      </span>
      <input type="checkbox" :checked="checked" :indeterminate="indeterminate" @click.stop="toggleCheck" class="w-4 h-4 rounded accent-[var(--color-primary)]"/>
      <span class="text-sm text-gray-700 select-none">{{ node[labelKey] }}</span>
    </div>
    <div v-if="hasChildren && expanded" class="ml-5">
      <TreeNode v-for="child in node.children" :key="child[nodeKey]" :node="child" :node-key="nodeKey" :label-key="labelKey" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)"/>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'

const props = defineProps({
  node: { type: Object, required: true },
  modelValue: { type: Array, default: () => [] },
  nodeKey: { type: String, default: 'id' },
  labelKey: { type: String, default: 'label' }
})

const emit = defineEmits(['update:modelValue'])

const expanded = ref(false)

const hasChildren = computed(() => props.node.children && props.node.children.length > 0)

const getAllKeys = (node) => {
  let keys = [node[props.nodeKey]]
  if (node.children) {
    node.children.forEach(c => { keys = keys.concat(getAllKeys(c)) })
  }
  return keys
}

const checked = computed(() => {
  const allKeys = getAllKeys(props.node)
  return allKeys.length > 0 && allKeys.every(k => props.modelValue.includes(k))
})

const indeterminate = computed(() => {
  if (checked.value) return false
  const allKeys = getAllKeys(props.node)
  return allKeys.some(k => props.modelValue.includes(k))
})

const toggleCheck = () => {
  const allKeys = getAllKeys(props.node)
  let newVal = [...props.modelValue]
  if (checked.value) {
    newVal = newVal.filter(k => !allKeys.includes(k))
  } else {
    allKeys.forEach(k => { if (!newVal.includes(k)) newVal.push(k) })
  }
  emit('update:modelValue', newVal)
}

const toggle = () => {
  if (hasChildren.value) expanded.value = !expanded.value
}
</script>
