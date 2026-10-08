<script setup lang="ts">
import {ref,watch,computed} from 'vue';
import {ExternalLink,Check,RefreshCw} from 'lucide-vue-next';
import PlatformIcon from './PlatformIcon.vue';
import type {Platform} from '../model';
const props=defineProps<{platform:Platform;save:(id:string,url:string)=>Promise<boolean>}>();
const url=ref(props.platform.pageUrl||''),saving=ref(false),error=ref(''),saved=ref(false);
const dirty=computed(()=>url.value.trim()!==(props.platform.pageUrl||''));
watch(()=>props.platform.pageUrl,v=>{url.value=v||'';});
const safeUrl=computed(()=>{try{const parsed=new URL(props.platform.pageUrl||'');return ['http:','https:'].includes(parsed.protocol)?parsed.href:null;}catch{return null;}});
async function submit(){
  const value=url.value.trim();error.value='';saved.value=false;
  if(value){try{const parsed=new URL(value);if(!['http:','https:'].includes(parsed.protocol)||!parsed.hostname)throw Error();}catch{error.value='ใส่ลิงก์หน้าเพจแบบ https:// หรือ http://';return;}}
  saving.value=true;
  try{if(await props.save(props.platform.id,value)){saved.value=true;}else error.value='บันทึกไม่สำเร็จ กรุณาลองใหม่';}finally{saving.value=false;}
}
</script>
<template><section class="profile-platform"><div class="profile-platform-heading"><span class="profile-logo-box"><PlatformIcon :name="platform.name" :size="27"/></span><div><h2>{{platform.name}}</h2><span v-if="!platform.active" class="profile-status">ปิดใช้สำหรับคอนเทนต์ใหม่</span><span v-else class="profile-status">หน้าเพจของคุณ</span></div><a v-if="safeUrl" class="button small profile-visit" :href="safeUrl" target="_blank" rel="noopener noreferrer" :aria-label="`เปิดเพจ ${platform.name} ในแท็บใหม่`"><ExternalLink :size="15"/>เปิดเพจ</a></div><form @submit.prevent="submit" novalidate><label class="field" :for="`page-url-${platform.id}`"><span>ลิงก์เพจ {{platform.name}}</span></label><div class="profile-link-input"><input :id="`page-url-${platform.id}`" v-model="url" type="url" inputmode="url" placeholder="https://…" maxlength="2048" :aria-invalid="!!error" :aria-describedby="error?`page-error-${platform.id}`:undefined" @input="saved=false;error=''"/><button class="button" :disabled="saving||!dirty"><RefreshCw v-if="saving" class="spin" :size="15"/><Check v-else-if="saved&&!dirty" :size="15"/>{{saving?'กำลังบันทึก':saved&&!dirty?'บันทึกแล้ว':'บันทึกลิงก์'}}</button></div><p v-if="error" :id="`page-error-${platform.id}`" class="profile-link-error" role="alert">{{error}}</p><p v-else-if="!safeUrl" class="helper">เพิ่มลิงก์แล้วจะเปิดหน้าเพจจากโปรไฟล์นี้ได้ทันที</p><a v-else class="profile-url" :href="safeUrl" target="_blank" rel="noopener noreferrer">{{platform.pageUrl}}</a></form></section></template>
