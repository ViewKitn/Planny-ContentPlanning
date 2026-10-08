<script setup lang="ts">
import { watch, onBeforeUnmount } from 'vue';
import { useEditor, EditorContent } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import { Bold, Heading2, List, ListOrdered } from 'lucide-vue-next';
import type {RichDoc} from '../model';
const props=defineProps<{modelValue:RichDoc;label:string}>();
const emit=defineEmits<{ 'update:modelValue':[value:RichDoc] }>();
const editor=useEditor({extensions:[StarterKit.configure({blockquote:false,code:false,codeBlock:false,horizontalRule:false,italic:false,strike:false,link:false,underline:false,heading:{levels:[2,3]}})],content:props.modelValue,editorProps:{attributes:{'aria-label':props.label,role:'textbox','aria-multiline':'true'}},onUpdate:({editor})=>emit('update:modelValue',editor.getJSON() as RichDoc)});
watch(()=>props.modelValue,v=>{if(editor.value&&JSON.stringify(editor.value.getJSON())!==JSON.stringify(v))editor.value.commands.setContent(v,{emitUpdate:false});},{deep:true});
onBeforeUnmount(()=>editor.value?.destroy());
</script>
<template><div class="rich-editor"><div v-if="editor" class="editor-tools" :aria-label="`จัดรูปแบบ ${label}`"><button type="button" title="ตัวหนา" :aria-pressed="editor.isActive('bold')" @click="editor.chain().focus().toggleBold().run()"><Bold :size="16"/></button><button type="button" title="หัวข้อ" :aria-pressed="editor.isActive('heading')" @click="editor.chain().focus().toggleHeading({level:2}).run()"><Heading2 :size="17"/></button><button type="button" title="รายการ" :aria-pressed="editor.isActive('bulletList')" @click="editor.chain().focus().toggleBulletList().run()"><List :size="17"/></button><button type="button" title="รายการลำดับ" :aria-pressed="editor.isActive('orderedList')" @click="editor.chain().focus().toggleOrderedList().run()"><ListOrdered :size="17"/></button></div><EditorContent :editor="editor"/></div></template>
