<script setup lang="ts">
import {computed,ref} from 'vue';
import {Temporal} from '@js-temporal/polyfill';
import {ChevronLeft,ChevronRight,Plus} from 'lucide-vue-next';
import {dayKey,eventTime,localTime,overdue,type Content} from '../model';
const props=defineProps<{contents:Content[];zone:string}>();
const emit=defineEmits<{open:[id:string];move:[id:string,day:string];create:[day:string]}>();
const cursor=ref(Temporal.Now.plainDateISO(props.zone)),mode=ref<'month'|'week'>('month'),dragId=ref('');
const first=computed(()=>{const d=mode.value==='month'?cursor.value.with({day:1}):cursor.value;return d.subtract({days:d.dayOfWeek-1});});
const days=computed(()=>Array.from({length:mode.value==='month'?42:7},(_,i)=>first.value.add({days:i})));
const heading=computed(()=>new Intl.DateTimeFormat('th-TH',{month:'long',year:'numeric',timeZone:'UTC'}).format(new Date(`${cursor.value}T12:00:00Z`)));
const today=computed(()=>Temporal.Now.plainDateISO(props.zone).toString());
function events(day:string){return props.contents.filter(c=>{const time=eventTime(c);return time&&dayKey(time,props.zone)===day;}).sort((a,b)=>(eventTime(a)||'').localeCompare(eventTime(b)||''));}
function navigate(n:number){cursor.value=mode.value==='month'?cursor.value.add({months:n}):cursor.value.add({days:n*7});}
function drop(day:string){if(dragId.value)emit('move',dragId.value,day);dragId.value='';}
</script>
<template><section class="calendar"><div class="calendar-toolbar"><div class="month-heading"><button class="icon-button" aria-label="ช่วงก่อนหน้า" @click="navigate(-1)"><ChevronLeft :size="18"/></button><h2>{{heading}}</h2><button class="icon-button" aria-label="ช่วงถัดไป" @click="navigate(1)"><ChevronRight :size="18"/></button><button class="button subtle" @click="cursor=Temporal.Now.plainDateISO(zone)">วันนี้</button></div><div class="segmented"><button :class="{active:mode==='month'}" @click="mode='month'">เดือน</button><button :class="{active:mode==='week'}" @click="mode='week'">สัปดาห์</button></div></div><p class="helper">ลากงาน Planned เพื่อเลื่อนวัน · งาน Published แสดงตามวันที่เผยแพร่จริง</p><div class="calendar-grid weekday"><span v-for="d in ['จ.','อ.','พ.','พฤ.','ศ.','ส.','อา.']" :key="d">{{d}}</span></div><div class="calendar-grid" :class="{week:mode==='week'}"><div v-for="day in days" :key="day.toString()" class="calendar-day" :class="{outside:day.month!==cursor.month,today:day.toString()===today}" @dragover.prevent @drop.prevent="drop(day.toString())"><div class="day-head"><span>{{day.day}}</span><button class="icon-button" :aria-label="`สร้างคอนเทนต์วันที่ ${day}`" @click="emit('create',day.toString())"><Plus :size="14"/></button></div><button v-for="c in events(day.toString())" :key="c.id" class="calendar-event" :class="[c.status.toLowerCase(),{late:overdue(c)}]" :draggable="c.status==='Planned'" @dragstart="dragId=c.id" @dragend="dragId=''" @click="emit('open',c.id)"><span class="event-time">{{localTime(eventTime(c),zone).slice(11)}} · {{c.status}}</span><strong>{{c.title}}</strong><span v-if="overdue(c)">เลยกำหนด</span></button></div></div></section></template>
