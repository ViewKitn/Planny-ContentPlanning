<script setup lang="ts">
import PlatformProfileRow from './PlatformProfileRow.vue';
import type {Platform} from '../model';
defineProps<{platforms:Platform[];save:(id:string,url:string)=>Promise<boolean>}>();
</script>
<template><div class="profile-layout"><section class="profile-intro"><div class="profile-avatar">P</div><h2>เพจของฉัน</h2><p>รวมลิงก์หน้าเพจที่คุณดูแลไว้ในที่เดียว เปิดไปยังแต่ละแพลตฟอร์มได้จากโปรไฟล์นี้</p><p class="helper">เพิ่มหรือปิดใช้แพลตฟอร์มได้ในเมนูการตั้งค่า</p></section><div class="profile-platforms"><PlatformProfileRow v-for="platform in platforms" :key="platform.id" :platform="platform" :save="save"/><p v-if="!platforms.length" class="empty-inline">ยังไม่มีแพลตฟอร์ม เพิ่มได้จากการตั้งค่า</p></div></div></template>
