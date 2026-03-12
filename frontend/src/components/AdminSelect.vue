<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

const props = defineProps({
    modelValue: {
        type: [String, Number, Boolean, Object, null],
        default: ''
    },
    options: {
        type: Array,
        default: () => []
    },
    placeholder: {
        type: String,
        default: '请选择'
    },
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['update:modelValue', 'change']);

const rootRef = ref(null);
const open = ref(false);

const normalizedOptions = computed(() =>
    (props.options || []).map((option, index) => {
        if (option && typeof option === 'object' && Object.prototype.hasOwnProperty.call(option, 'value')) {
            return {
                label: String(option.label ?? option.value ?? ''),
                value: option.value,
                disabled: Boolean(option.disabled),
                key: `${String(option.value)}_${index}`
            };
        }
        return {
            label: String(option ?? ''),
            value: option,
            disabled: false,
            key: `${String(option)}_${index}`
        };
    })
);

const selectedOption = computed(() =>
    normalizedOptions.value.find((item) => Object.is(item.value, props.modelValue)) || null
);

const displayText = computed(() => selectedOption.value?.label || props.placeholder);

const toggle = () => {
    if (props.disabled) return;
    open.value = !open.value;
};

const close = () => {
    open.value = false;
};

const pick = (option) => {
    if (!option || option.disabled) return;
    emit('update:modelValue', option.value);
    emit('change', option.value);
    close();
};

const onDocumentClick = (event) => {
    const root = rootRef.value;
    if (!root) return;
    if (root.contains(event.target)) return;
    close();
};

const onDocumentKeydown = (event) => {
    if (event.key === 'Escape') {
        close();
    }
};

onMounted(() => {
    document.addEventListener('mousedown', onDocumentClick);
    document.addEventListener('keydown', onDocumentKeydown);
});

onBeforeUnmount(() => {
    document.removeEventListener('mousedown', onDocumentClick);
    document.removeEventListener('keydown', onDocumentKeydown);
});
</script>

<template>
    <div ref="rootRef" class="admin-select" :class="{ 'admin-select--disabled': disabled }">
        <button
            class="admin-select__trigger"
            type="button"
            :disabled="disabled"
            :aria-expanded="open ? 'true' : 'false'"
            @click="toggle"
        >
            <span class="admin-select__value" :class="{ 'admin-select__value--placeholder': !selectedOption }">
                {{ displayText }}
            </span>
            <span class="admin-select__arrow" :class="{ 'admin-select__arrow--open': open }"></span>
        </button>
        <transition name="admin-select-fade">
            <div v-if="open" class="admin-select__panel">
                <button
                    v-for="option in normalizedOptions"
                    :key="option.key"
                    class="admin-select__option"
                    :class="{ 'admin-select__option--active': Object.is(option.value, modelValue) }"
                    type="button"
                    :disabled="option.disabled"
                    @click="pick(option)"
                >
                    <span class="admin-select__check">{{ Object.is(option.value, modelValue) ? '✓' : '' }}</span>
                    <span class="admin-select__label">{{ option.label }}</span>
                </button>
            </div>
        </transition>
    </div>
</template>

<style scoped>
.admin-select {
    position: relative;
}

.admin-select__trigger {
    display: flex;
    width: 100%;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    border-radius: 10px;
    border: 1px solid #e2e8f0;
    background: #ffffff;
    padding: 8px 12px;
    color: #0f172a;
    font-size: 13px;
    line-height: 1.2;
    text-align: left;
    transition: border-color 0.2s, background-color 0.2s;
}

.admin-select__trigger:hover {
    border-color: #bfdbfe;
}

.admin-select__trigger:focus-visible {
    outline: 2px solid rgba(59, 130, 246, 0.3);
    outline-offset: 1px;
}

.admin-select__value {
    min-width: 0;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.admin-select__value--placeholder {
    color: #64748b;
}

.admin-select__arrow {
    width: 0;
    height: 0;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 6px solid #94a3b8;
    transition: transform 0.2s;
}

.admin-select__arrow--open {
    transform: rotate(180deg);
}

.admin-select__panel {
    position: absolute;
    left: 0;
    right: 0;
    top: calc(100% + 6px);
    z-index: 80;
    max-height: 260px;
    overflow-y: auto;
    border-radius: 10px;
    border: 1px solid #dbe2eb;
    background: #ffffff;
    box-shadow: 0 12px 24px rgba(15, 23, 42, 0.16);
}

.admin-select__option {
    display: flex;
    width: 100%;
    align-items: center;
    gap: 8px;
    border: none;
    background: transparent;
    padding: 9px 12px;
    color: #0f172a;
    text-align: left;
    font-size: 13px;
    line-height: 1.2;
}

.admin-select__option:hover {
    background: #f8fafc;
}

.admin-select__option--active {
    background: #e8f1ff;
    color: #1d4ed8;
}

.admin-select__check {
    width: 14px;
    text-align: center;
    color: #1d4ed8;
    font-weight: 700;
}

.admin-select__label {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.admin-select--disabled .admin-select__trigger {
    cursor: not-allowed;
    background: #f1f5f9;
    color: #94a3b8;
}

.admin-select-fade-enter-active,
.admin-select-fade-leave-active {
    transition: all 0.15s ease;
}

.admin-select-fade-enter-from,
.admin-select-fade-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}

[data-theme='dark'] .admin-select__trigger {
    border-color: var(--border-color);
    background: var(--surface-2);
    color: var(--text-primary);
}

[data-theme='dark'] .admin-select__value--placeholder {
    color: var(--text-muted-2);
}

[data-theme='dark'] .admin-select__panel {
    border-color: rgba(148, 163, 184, 0.3);
    background: var(--surface-1);
    box-shadow: 0 12px 24px rgba(2, 6, 23, 0.45);
}

[data-theme='dark'] .admin-select__option {
    color: var(--text-primary);
}

[data-theme='dark'] .admin-select__option:hover {
    background: rgba(148, 163, 184, 0.12);
}

[data-theme='dark'] .admin-select__option--active {
    background: rgba(96, 165, 250, 0.2);
    color: #93c5fd;
}

[data-theme='dark'] .admin-select--disabled .admin-select__trigger {
    background: var(--surface-3);
    color: var(--text-muted-2);
}
</style>
