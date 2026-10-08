<script setup lang="ts">
import {onMounted,onBeforeUnmount,ref} from 'vue';
import {RefreshCw,AlertCircle} from 'lucide-vue-next';
import WorkspaceApp from './WorkspaceApp.vue';
import LoginPage from './components/LoginPage.vue';
import {request} from './api';
const checking=ref(true),authenticated=ref(false),required=ref(true),error=ref(''),loggingOut=ref(false);
async function checkSession(){
  checking.value=true;error.value='';
  try{const session=await request<{required:boolean;authenticated:boolean}>('/auth/session');required.value=session.required;authenticated.value=session.authenticated;}
  catch(e){error.value=(e as Error).message;}finally{checking.value=false;}
}
function expired(){authenticated.value=false;error.value='';}
async function logout(){
  loggingOut.value=true;
  try{await request('/auth/logout',{method:'POST'});authenticated.value=false;}
  catch(e){error.value=(e as Error).message;}finally{loggingOut.value=false;}
}
onMounted(()=>{void checkSession();window.addEventListener('planny:session-expired',expired);});
onBeforeUnmount(()=>window.removeEventListener('planny:session-expired',expired));
</script>

<template>
  <main v-if="checking" class="auth-loading loading-state" aria-live="polite"><RefreshCw class="spin" :size="28"/><h1>กำลังเปิด Planny</h1><p>เตรียมพื้นที่สำหรับไอเดียของคุณ</p></main>
  <main v-else-if="error&&!authenticated" class="auth-loading connection-error"><AlertCircle :size="30"/><h1>ยังเชื่อมต่อ Planny ไม่ได้</h1><p>{{error}}</p><button class="button primary" @click="checkSession">ลองอีกครั้ง</button></main>
  <LoginPage v-else-if="!authenticated" @authenticated="authenticated=true"/>
  <template v-else>
    <div v-if="error" class="error-banner" role="alert">{{error}}<button class="button small" @click="error=''">ปิดข้อความ</button></div>
    <WorkspaceApp :can-logout="required" :logging-out="loggingOut" @logout="logout"/>
  </template>
</template>
