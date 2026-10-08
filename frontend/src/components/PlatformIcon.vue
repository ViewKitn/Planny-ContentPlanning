<script setup lang="ts">
import {computed} from 'vue';
import {Globe2} from 'lucide-vue-next';
import {siFacebook,siInstagram,siTiktok,siYoutube} from 'simple-icons';
const props=withDefaults(defineProps<{name:string;size?:number}>(),{size:18});
const icon=computed(()=>({facebook:siFacebook,instagram:siInstagram,tiktok:siTiktok,youtube:siYoutube})[props.name.trim().toLowerCase() as 'facebook'|'instagram'|'tiktok'|'youtube']);
</script>
<template><svg v-if="icon" class="platform-logo" :width="size" :height="size" viewBox="0 0 24 24" aria-hidden="true" focusable="false" :style="{color:`#${icon.hex}`}"><path :d="icon.path" fill="currentColor"/></svg><Globe2 v-else class="platform-logo custom-platform-logo" :size="size" aria-hidden="true"/></template>
