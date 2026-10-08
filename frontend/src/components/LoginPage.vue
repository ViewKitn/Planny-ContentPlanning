<script setup lang="ts">
import {ref,nextTick} from 'vue';
import {Eye,EyeOff,ArrowRight,RefreshCw,AlertCircle} from 'lucide-vue-next';
import {request} from '../api';
const emit=defineEmits<{authenticated:[]}>();
const username=ref(''),password=ref(''),showPassword=ref(false),busy=ref(false),error=ref('');
const passwordInput=ref<HTMLInputElement|null>(null);
async function login(){
  if(busy.value)return;
  error.value='';busy.value=true;
  try{await request('/auth/login',{method:'POST',body:JSON.stringify({username:username.value.trim(),password:password.value})});password.value='';emit('authenticated');}
  catch(e){error.value=(e as Error).message;await nextTick();passwordInput.value?.focus();}
  finally{busy.value=false;}
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel" aria-labelledby="login-title">
      <div class="wordmark login-wordmark"><span class="brand-symbol" aria-hidden="true"><span></span><span></span><span></span></span>planny<span class="brand-period">.</span></div>
      <div class="login-heading"><h1 id="login-title">กลับมาทำไอเดียให้เป็นจริง</h1><p>เข้าสู่ระบบเพื่อจัดการคอนเทนต์<br/>และวางแผนเผยแพร่ในพื้นที่ของคุณ</p></div>
      <form class="login-form" @submit.prevent="login">
        <label class="field" for="login-username"><span>ชื่อผู้ใช้</span><input id="login-username" v-model="username" name="username" autocomplete="username" autocapitalize="none" spellcheck="false" required :readonly="busy" :aria-invalid="!!error" placeholder="ชื่อผู้ใช้ของคุณ"/></label>
        <label class="field" for="login-password"><span>รหัสผ่าน</span></label>
        <div class="login-password-field"><input id="login-password" ref="passwordInput" v-model="password" name="password" :type="showPassword?'text':'password'" autocomplete="current-password" required :readonly="busy" :aria-invalid="!!error" :aria-describedby="error?'login-error':undefined" placeholder="รหัสผ่านของคุณ"/><button type="button" class="icon-button" :aria-label="showPassword?'ซ่อนรหัสผ่าน':'แสดงรหัสผ่าน'" :aria-pressed="showPassword" @click="showPassword=!showPassword"><component :is="showPassword?EyeOff:Eye" :size="19"/></button></div>
        <p v-if="error" id="login-error" class="login-error" role="alert"><AlertCircle :size="17"/><span>{{error}}</span></p>
        <button class="button primary login-submit" :disabled="busy"><RefreshCw v-if="busy" class="spin" :size="18"/><span>{{busy?'กำลังเข้าสู่ระบบ…':'เข้าสู่ระบบ'}}</span><ArrowRight v-if="!busy" :size="18"/></button>
      </form>
      <p class="login-footer">พื้นที่ส่วนตัวสำหรับทุกไอเดียของคุณ</p>
    </section>
  </main>
</template>
