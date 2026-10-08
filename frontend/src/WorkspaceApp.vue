<script setup lang="ts">
import {computed,ref,watch,onMounted,onBeforeUnmount,nextTick,defineAsyncComponent} from 'vue';
import {LayoutGrid,CalendarDays,Archive,Trash2,Settings,Plus,Search,List,ArrowLeft,ArrowRight,Check,Clock,Copy,Download,Upload,RotateCcw,X,ChevronRight,FileText,PanelLeftClose,PanelLeftOpen,AlertCircle,RefreshCw,Link2,CheckCircle2,UserRound,LogOut} from 'lucide-vue-next';
const RichEditor=defineAsyncComponent(()=>import('./components/RichEditor.vue'));
const CalendarView=defineAsyncComponent(()=>import('./components/CalendarView.vue'));
import {useWorkspace} from './useWorkspace';
import {statuses,labels,createContent,duplicate,richText,validateContent,localTime,instant,moveDay,overdue,type Content,type Workspace,type Status} from './model';
import {request} from './api';
import PlatformIcon from './components/PlatformIcon.vue';
import PlatformProfile from './components/PlatformProfile.vue';
const store=useWorkspace();
defineProps<{canLogout:boolean;loggingOut:boolean}>();
const emit=defineEmits<{logout:[]}>();
async function logout(){if(await leaveEditor())emit('logout');}
const {data,loading,error,busy,conflict}=store;
type View='board'|'calendar'|'archive'|'trash'|'settings'|'profile';
const view=ref<View>('board'),listMode=ref(false),sidebarOpen=ref(false);
const nav=[{key:'board' as View,label:'คอนเทนต์ของฉัน',icon:LayoutGrid},{key:'calendar' as View,label:'ปฏิทินเผยแพร่',icon:CalendarDays},{key:'archive' as View,label:'คลังงาน',icon:Archive},{key:'trash' as View,label:'ถังขยะ',icon:Trash2},{key:'profile' as View,label:'โปรไฟล์เพจ',icon:UserRound},{key:'settings' as View,label:'การตั้งค่า',icon:Settings}];
const query=ref(''),filterStatus=ref(''),filterPlatform=ref(''),filterTag=ref(''),fromDate=ref(''),toDate=ref('');
const form=ref<Content|null>(null),saving=ref(false),saveLabel=ref('บันทึกแล้ว'),editorError=ref(''),recoveryIssue=ref('');
const toast=ref(''),tagName=ref(''),platformName=ref(''),newTitle=ref(''),newDay=ref(''),creating=ref(false);
const newTag=ref('');
const draftPrefix='planny:draft:';
const confirmBox=ref<{title:string;message:string;accept:string;danger:boolean}|null>(null);
let resolveConfirm:((v:boolean)=>void)|null=null;
function ask(title:string,message:string,accept='ยืนยัน',danger=false){return new Promise<boolean>(resolve=>{confirmBox.value={title,message,accept,danger};resolveConfirm=resolve;});}
function answer(value:boolean){resolveConfirm?.(value);resolveConfirm=null;confirmBox.value=null;}
function notify(message:string){toast.value=message;setTimeout(()=>toast.value='',3500);}
const copy=<T,>(v:T):T=>JSON.parse(JSON.stringify(v));
const zone=computed(()=>data.value?.timezone||'Asia/Bangkok');
const currentNav=computed(()=>nav.find(n=>n.key===view.value)!);
const active=computed(()=>data.value?.contents.filter(c=>!c.deletedAt&&!c.archived)||[]);
const visible=computed(()=>{
  const base=(data.value?.contents||[]).filter(c=>view.value==='trash'?!!c.deletedAt:view.value==='archive'?c.archived&&!c.deletedAt:!c.archived&&!c.deletedAt);
  return base.filter(c=>{
    const text=[c.title,richText(c.brief),richText(c.script),...c.destinations.map(d=>d.caption)].join(' ').toLocaleLowerCase();
    const stamp=c.status==='Published'?c.publishedAt:c.plannedAt;
    const day=stamp?localTime(stamp,zone.value).slice(0,10):'';
    return (!query.value||text.includes(query.value.toLocaleLowerCase()))&&(!filterStatus.value||c.status===filterStatus.value)&&(!filterPlatform.value||c.destinations.some(d=>d.platformId===filterPlatform.value))&&(!filterTag.value||c.tagIds.includes(filterTag.value))&&(!fromDate.value||(!!day&&day>=fromDate.value))&&(!toDate.value||(!!day&&day<=toDate.value));
  }).sort((a,b)=>b.updatedAt.localeCompare(a.updatedAt));
});
const pending=computed(()=>!!form.value&&JSON.stringify(form.value)!==JSON.stringify(data.value?.contents.find(c=>c.id===form.value!.id)));
const lateCount=computed(()=>active.value.filter(overdue).length);
const platformLabel=(id:string)=>data.value?.platforms.find(p=>p.id===id)?.name||id;
function formatDate(value:string|null){return value?new Intl.DateTimeFormat('th-TH',{day:'numeric',month:'short',hour:'2-digit',minute:'2-digit',timeZone:zone.value}).format(new Date(value)):'ยังไม่กำหนดวัน';}
let saveTimer:ReturnType<typeof setTimeout>|undefined;
let editorQueue:Promise<unknown>=Promise.resolve();
let suppress=false;
function keepDraft(c:Content){try{localStorage.setItem(draftPrefix+c.id,JSON.stringify({content:c,savedAt:new Date().toISOString()}));recoveryIssue.value='';}catch{recoveryIssue.value='เบราว์เซอร์เก็บร่างไม่ได้ กรุณาคัดลอกเนื้อหาก่อนปิดหน้า';}}
watch(form,()=>{
  if(suppress||!form.value)return;
  keepDraft(form.value);saveLabel.value='มีการเปลี่ยนแปลง';
  clearTimeout(saveTimer);saveTimer=setTimeout(()=>{void saveEditor();},850);
},{deep:true});
async function saveEditor():Promise<boolean>{
  clearTimeout(saveTimer);
  if(!form.value)return true;
  const captured=copy(form.value);
  const validation=validateContent(captured);
  if(validation){editorError.value=validation;saveLabel.value='ยังบันทึกไม่ได้';keepDraft(captured);return false;}
  const task=editorQueue.catch(()=>{}).then(async()=>{
    const current=data.value?.contents.find(c=>c.id===captured.id);
    if(JSON.stringify(current)===JSON.stringify(captured)){saveLabel.value='บันทึกแล้ว';return true;}
    saving.value=true;saveLabel.value='กำลังบันทึก';editorError.value='';
    try{await store.commit(w=>{const index=w.contents.findIndex(c=>c.id===captured.id);if(index<0)throw Error('ไม่พบงานนี้ในข้อมูลล่าสุด กรุณาคัดลอกเนื้อหาและสร้างงานใหม่');w.contents[index]=captured;});
      if(form.value?.id===captured.id&&JSON.stringify(form.value)===JSON.stringify(captured)){
        localStorage.removeItem(draftPrefix+captured.id);saveLabel.value='บันทึกแล้ว';
      }return true;
    }catch(e){editorError.value=(e as Error).message;saveLabel.value='บันทึกไม่สำเร็จ';keepDraft(captured);return false;}finally{saving.value=false;}
  });editorQueue=task;return task;
}
async function leaveEditor(){if(!form.value)return true;const saved=await saveEditor();if(!saved&&!(await ask('ยังมีข้อมูลที่ไม่ได้บันทึก','กลับไปหน้ารายการโดยเก็บร่างไว้ในเบราว์เซอร์หรือไม่? ร่างจะไม่เขียนทับข้อมูลเดิม','กลับไปหน้ารายการ')))return false;suppress=true;form.value=null;await nextTick();suppress=false;return true;}
async function go(next:View){if(!(await leaveEditor()))return;view.value=next;sidebarOpen.value=false;query.value='';filterStatus.value='';filterPlatform.value='';filterTag.value='';fromDate.value='';toDate.value='';}
async function open(id:string){if(!(await leaveEditor()))return;const c=data.value?.contents.find(x=>x.id===id);if(!c)return;
  let chosen=copy(c);const raw=localStorage.getItem(draftPrefix+id);
  if(raw){try{const draft=JSON.parse(raw);if(draft.content?.id===id&&JSON.stringify(draft.content)!==JSON.stringify(c)&&!c.deletedAt){if(await ask('พบร่างที่ยังไม่ได้บันทึก','ใช้ร่างจากเบราว์เซอร์เพื่อแก้ไขต่อหรือไม่? จะยังไม่เขียนทับข้อมูลที่บันทึกไว้','กู้ร่าง'))chosen=draft.content;else localStorage.removeItem(draftPrefix+id);}}catch{localStorage.removeItem(draftPrefix+id);}}
  suppress=true;form.value=chosen;editorError.value='';saveLabel.value=JSON.stringify(chosen)===JSON.stringify(c)?'บันทึกแล้ว':'ร่างที่กู้คืน — กดบันทึกเพื่อลองใหม่';await nextTick();suppress=false;
}
async function mutate(fn:(w:Workspace)=>void,message=''){try{await store.commit(fn);if(message)notify(message);return true;}catch{return false;}}
function startCreate(day=''){newTitle.value='';newDay.value=day;creating.value=true;nextTick(()=>document.querySelector<HTMLInputElement>('#new-title')?.focus());}
async function create(){if(!newTitle.value.trim()||busy.value)return;const c=createContent(newTitle.value.trim());if(newDay.value)c.plannedAt=instant(`${newDay.value}T09:00`,zone.value);if(await mutate(w=>w.contents.unshift(c))){creating.value=false;await open(c.id);}}
function touch(){if(form.value)form.value.updatedAt=new Date().toISOString();}
async function changeStatus(event:Event){if(!form.value)return;const select=event.target as HTMLSelectElement;const next=select.value as Status;const c=form.value;
  if(c.status==='Published'&&next!=='Published'){
    select.value=c.status;
    if(!(await ask('ย้อนสถานะการเผยแพร่','เวลาเผยแพร่จริงและลิงก์โพสต์ทุกแพลตฟอร์มจะถูกล้าง เพื่อแก้ข้อมูลที่บันทึกผิด','ย้อนสถานะ',true)))return;
    c.publishedAt=null;c.destinations.forEach(d=>d.postLink='');
  }
  c.status=next;if(next==='Published'&&!c.publishedAt)c.publishedAt=new Date().toISOString();touch();
}
async function publish(){if(!form.value)return;if(!form.value.destinations.length){editorError.value='เลือกแพลตฟอร์มอย่างน้อยหนึ่งแห่งก่อน';return;}
  if(await ask('เผยแพร่ครบทุกแพลตฟอร์มแล้วใช่ไหม?',`บันทึกว่าเผยแพร่บน ${form.value.destinations.map(d=>platformLabel(d.platformId)).join(', ')} ครบแล้ว คุณแก้เวลาและเพิ่มลิงก์ภายหลังได้`,'บันทึก Published')){form.value.status='Published';form.value.publishedAt=new Date().toISOString();touch();await saveEditor();}}
async function cancelPlan(){if(!form.value)return;if(await ask('ยกเลิกกำหนดเผยแพร่','งานจะกลับเป็น Ready และล้างกำหนดเผยแพร่','ยกเลิกกำหนด')){form.value.status='Ready';form.value.plannedAt=null;touch();}}
function setDate(key:'plannedAt'|'publishedAt',event:Event){try{if(form.value){form.value[key]=instant((event.target as HTMLInputElement).value,zone.value);touch();}}catch{editorError.value='เวลานี้ไม่มีอยู่จริงหรือซ้ำในเขตเวลาที่เลือก กรุณาเลือกเวลาอื่น';}}
async function togglePlatform(id:string){if(!form.value)return;const c=form.value;const index=c.destinations.findIndex(d=>d.platformId===id);
  if(index<0){if(c.status==='Published'){editorError.value='หากต้องการเพิ่มแพลตฟอร์มหลังเผยแพร่ กรุณา Duplicate หรือย้อนสถานะก่อน';return;}c.destinations.push({platformId:id,caption:'',assetLinks:[],postLink:''});}
  else {if((c.status==='Planned'||c.status==='Published')&&c.destinations.length===1){editorError.value='สถานะนี้ต้องมีแพลตฟอร์มอย่างน้อยหนึ่งแห่ง';return;}
    if(!(await ask('เอาแพลตฟอร์มออก?',`caption และลิงก์ของ ${platformLabel(id)} ในงานนี้จะถูกเอาออก`,'เอาออก',true)))return;c.destinations.splice(index,1);}
  touch();
}
async function duplicateCurrent(){if(!form.value||!(await saveEditor()))return;const c=duplicate(copy(form.value));if(await mutate(w=>w.contents.unshift(c),'สร้างสำเนาแล้ว'))await open(c.id);}
async function archiveCurrent(){if(!form.value||!(await saveEditor()))return;const id=form.value.id;if(await mutate(w=>{w.contents.find(c=>c.id===id)!.archived=true;},'เก็บเข้าคลังแล้ว')){suppress=true;form.value=null;await nextTick();suppress=false;}}
async function trashCurrent(){if(!form.value)return;if(!(await ask('ย้ายคอนเทนต์ลงถังขยะ?','คุณกู้คืนเนื้อหาและข้อมูลแพลตฟอร์มทั้งหมดได้ภายหลัง','ย้ายลงถังขยะ',true)))return;if(!(await saveEditor()))return;const id=form.value.id;if(await mutate(w=>{w.contents.find(c=>c.id===id)!.deletedAt=new Date().toISOString();},'ย้ายลงถังขยะแล้ว')){localStorage.removeItem(draftPrefix+id);suppress=true;form.value=null;await nextTick();suppress=false;}}
async function restore(c:Content){await mutate(w=>{const target=w.contents.find(x=>x.id===c.id)!;if(target.deletedAt)target.deletedAt=null;else target.archived=false;},'คืนงานแล้ว');}
async function purge(c:Content){if(await ask('ลบถาวร?',`“${c.title}” และข้อมูลที่เกี่ยวข้องจะกู้คืนไม่ได้`,'ลบถาวร',true))if(await mutate(w=>{w.contents=w.contents.filter(x=>x.id!==c.id);},'ลบถาวรแล้ว'))localStorage.removeItem(draftPrefix+c.id);}
async function calendarMove(id:string,day:string){const c=data.value?.contents.find(x=>x.id===id);if(!c)return;try{await mutate(w=>{const i=w.contents.findIndex(x=>x.id===id);w.contents[i]=moveDay(c,day,zone.value);},'เลื่อนกำหนดแล้ว');}catch(e){notify((e as Error).message);}}
async function addTag(editor=false){const name=(editor?newTag.value:tagName.value).trim();if(!name)return;const existing=data.value?.tags.find(t=>t.name.toLowerCase()===name.toLowerCase());if(existing){if(editor&&form.value&&!form.value.tagIds.includes(existing.id)){form.value.tagIds.push(existing.id);touch();}else notify('มี Tag นี้แล้ว');newTag.value='';return;}
  const tag={id:crypto.randomUUID(),name};if(await mutate(w=>w.tags.push(tag))){if(editor&&form.value){form.value.tagIds.push(tag.id);touch();}tagName.value='';newTag.value='';}}
async function removeTag(id:string){if(await ask('ลบ Tag?','เอาป้ายนี้ออกจากคอนเทนต์ทั้งหมด โดยไม่ลบคอนเทนต์','ลบ Tag',true))await mutate(w=>{w.tags=w.tags.filter(t=>t.id!==id);w.contents.forEach(c=>c.tagIds=c.tagIds.filter(t=>t!==id));},'ลบ Tag แล้ว');}
async function addPlatform(){const name=platformName.value.trim();if(!name)return;if(data.value?.platforms.some(p=>p.name.toLowerCase()===name.toLowerCase())){notify('มีแพลตฟอร์มนี้แล้ว');return;}if(await mutate(w=>w.platforms.push({id:crypto.randomUUID(),name,active:true,pageUrl:''})))platformName.value='';}
async function savePlatformUrl(id:string,url:string){return mutate(w=>{const platform=w.platforms.find(p=>p.id===id);if(!platform)throw Error('ไม่พบแพลตฟอร์ม');platform.pageUrl=url;},'บันทึกลิงก์เพจแล้ว');}
async function exportBackup(){if(busy.value)return;try{const backup=await request('/backup');const url=URL.createObjectURL(new Blob([JSON.stringify(backup,null,2)],{type:'application/json'}));const a=document.createElement('a');a.href=url;a.download=`planny-backup-${new Date().toISOString().slice(0,10)}.json`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);notify('ดาวน์โหลดไฟล์สำรองแล้ว');}catch(e){notify((e as Error).message);}}
async function importFile(event:Event){const input=event.target as HTMLInputElement;const file=input.files?.[0];input.value='';if(!file)return;if(file.size>8_000_000){notify('ไฟล์ใหญ่เกิน 8 MB');return;}
  try{const backup=JSON.parse(await file.text());if(backup.schemaVersion!==1||!backup.data||!Array.isArray(backup.data.contents))throw Error('ไฟล์สำรองไม่ถูกต้องหรือเวอร์ชันไม่รองรับ');
    if(!(await ask('นำเข้าข้อมูลแทนที่ทั้งหมด?',`ไฟล์มี ${backup.data.contents.length} คอนเทนต์ ข้อมูลปัจจุบันและร่างที่เก็บในเบราว์เซอร์จะถูกแทนที่ กรุณาสำรองข้อมูลเดิมก่อน`,'นำเข้าและแทนที่',true)))return;
    await store.importBackup(backup);Object.keys(localStorage).filter(k=>k.startsWith(draftPrefix)).forEach(k=>localStorage.removeItem(k));notify('นำเข้าข้อมูลแล้ว');
  }catch(e){notify((e as Error).message);}}
async function clipboard(text:string){try{await navigator.clipboard.writeText(text);notify('คัดลอกแล้ว');}catch{notify('คัดลอกไม่สำเร็จ กรุณาเลือกข้อความแล้วคัดลอกเอง');}}
function clearFilters(){query.value='';filterStatus.value='';filterPlatform.value='';filterTag.value='';fromDate.value='';toDate.value='';}
const filtered=computed(()=>!!(query.value||filterStatus.value||filterPlatform.value||filterTag.value||fromDate.value||toDate.value));
function lines(event:Event){return (event.target as HTMLTextAreaElement).value.split('\n');}
function beforeUnload(e:BeforeUnloadEvent){if(pending.value||saving.value){e.preventDefault();}}
let lastFocus:HTMLElement|null=null;
watch(()=>!!(creating.value||confirmBox.value),async open=>{
  if(open){lastFocus=document.activeElement as HTMLElement;await nextTick();document.querySelector<HTMLElement>(creating.value?'#new-title':'.dialog button')?.focus();}
  else {await nextTick();lastFocus?.focus();}
});
function onKey(e:KeyboardEvent){
  if(e.key==='Escape'){if(confirmBox.value)answer(false);else if(creating.value)creating.value=false;}
  if(e.key==='Tab'&&(creating.value||confirmBox.value)){
    const controls=[...document.querySelectorAll<HTMLElement>('.dialog button:not(:disabled), .dialog input:not(:disabled), .dialog [tabindex="0"]')];
    const first=controls[0],last=controls.at(-1);
    if(e.shiftKey&&document.activeElement===first){e.preventDefault();last?.focus();}
    else if(!e.shiftKey&&document.activeElement===last){e.preventDefault();first?.focus();}
  }
  if((e.ctrlKey||e.metaKey)&&e.key==='s'){e.preventDefault();void saveEditor();}
}
onMounted(()=>{void store.load();window.addEventListener('beforeunload',beforeUnload);window.addEventListener('keydown',onKey);});
onBeforeUnmount(()=>{clearTimeout(saveTimer);window.removeEventListener('beforeunload',beforeUnload);window.removeEventListener('keydown',onKey);});
</script>

<template>
<div class="app-shell" :class="{'nav-open':sidebarOpen}">
  <aside class="sidebar"><a class="wordmark" href="#" @click.prevent="go('board')"><span class="brand-symbol"><span></span><span></span><span></span></span>planny<span class="brand-period">.</span></a><div class="workspace-label">พื้นที่ส่วนตัว</div><nav aria-label="เมนูหลัก"><button v-for="item in nav" :key="item.key" :class="{selected:view===item.key}" @click="go(item.key)"><component :is="item.icon" :size="19"/><span>{{item.label}}</span><span v-if="item.key==='board'" class="nav-count">{{active.length}}</span></button></nav><button class="sidebar-foot profile-shortcut" @click="go('profile')"><div class="personal-mark">P</div><div><strong>โปรไฟล์เพจ</strong><span>ลิงก์เพจแต่ละแพลตฟอร์ม</span></div><ChevronRight :size="15"/></button></aside>
  <main class="main-workspace"><div class="topbar"><button class="icon-button mobile-toggle" aria-label="เปิด/ปิดเมนู" @click="sidebarOpen=!sidebarOpen"><PanelLeftOpen :size="20"/></button><span>พื้นที่ส่วนตัว <ChevronRight :size="13"/> {{form?'รายละเอียดคอนเทนต์':currentNav.label}}</span><span class="topbar-note"><span class="connection-dot" :class="{offline:error}"></span>{{error?'เชื่อมต่อไม่สำเร็จ':'พื้นที่สำหรับทุกไอเดียของคุณ'}}</span><button v-if="canLogout" class="button subtle small logout-button" :disabled="loggingOut||busy||saving" @click="logout"><LogOut :size="16"/><span>{{loggingOut?'กำลังออก…':'ออกจากระบบ'}}</span></button></div>
  <div v-if="loading" class="loading-state"><RefreshCw class="spin" :size="28"/><h2>กำลังเปิดพื้นที่ของคุณ</h2><p>โหลดคอนเทนต์และแผนเผยแพร่</p></div>
  <div v-else-if="!data" class="connection-error"><AlertCircle :size="32"/><h1>ยังเปิดพื้นที่ไม่ได้</h1><p>{{error}}</p><button class="button primary" @click="store.load()"><RefreshCw :size="16"/>ลองเชื่อมต่อใหม่</button></div>
  <template v-else>
    <div v-if="error" class="error-banner" role="alert"><AlertCircle :size="18"/><span>{{error}}</span><button class="button small" @click="store.load()">โหลดข้อมูลล่าสุด</button></div>
    <section v-if="form" class="editor-page">
      <div class="editor-heading"><button class="button subtle" @click="leaveEditor()"><ArrowLeft :size="16"/>กลับไป{{currentNav.label}}</button><div class="save-indicator" :class="{failed:editorError}" role="status"><RefreshCw v-if="saving" class="spin" :size="14"/><Check v-else-if="!pending&&!editorError" :size="14"/><Clock v-else :size="14"/>{{saveLabel}}</div><button class="button small" :disabled="saving||busy" @click="saveEditor()">บันทึก</button></div>
      <div v-if="editorError||recoveryIssue" class="error-banner" role="alert"><AlertCircle :size="18"/><span>{{editorError||recoveryIssue}}</span><button v-if="conflict" class="button small" @click="store.load()">โหลดข้อมูลล่าสุด</button></div>
      <div class="editor-layout"><div class="writing-surface"><input v-model="form.title" class="title-input" aria-label="ชื่อคอนเทนต์" maxlength="240" placeholder="ชื่อคอนเทนต์ของคุณ" @input="touch()"/><p class="helper">จากไอเดียเล็ก ๆ สู่คอนเทนต์ชิ้นถัดไป</p>
        <section class="writing-section"><div class="section-title"><h2>ไอเดีย &amp; Brief</h2><span>เป้าหมาย · ประเด็น · มุมเล่า</span></div><RichEditor :key="`${form.id}-brief`" v-model="form.brief" label="ไอเดียและ Brief" @update:model-value="touch()"/></section>
        <section class="writing-section"><div class="section-title"><h2>สคริปต์</h2><button class="button subtle small" @click="clipboard(richText(form.script))"><Copy :size="14"/>คัดลอก</button></div><RichEditor :key="`${form.id}-script`" v-model="form.script" label="สคริปต์" @update:model-value="touch()"/></section>
        <section class="writing-section"><h2>ลิงก์อ้างอิง</h2><label class="field"><span>หนึ่งลิงก์ต่อบรรทัด</span><textarea :value="form.referenceLinks.join('\n')" placeholder="https://…" rows="2" @input="form.referenceLinks=lines($event);touch()"></textarea></label></section>
        <section class="writing-section"><div class="section-title"><h2>ข้อความแต่ละแพลตฟอร์ม</h2><span>{{form.destinations.length}} แพลตฟอร์ม</span></div><p v-if="!form.destinations.length" class="empty-inline">เลือกแพลตฟอร์มด้านข้าง แล้วเตรียม caption ได้ที่นี่</p><div v-for="destination in form.destinations" :key="destination.platformId" class="destination-editor"><div class="section-title"><h3 class="platform-name"><PlatformIcon :name="platformLabel(destination.platformId)"/>{{platformLabel(destination.platformId)}}</h3><button class="button subtle small" @click="clipboard(destination.caption)"><Copy :size="14"/>คัดลอก caption</button></div><label class="field"><span>Caption</span><textarea v-model="destination.caption" rows="5" placeholder="เล่าเรื่องในแบบที่เหมาะกับแพลตฟอร์มนี้…" @input="touch()"></textarea></label><label class="field"><span>ลิงก์ไฟล์ที่ใช้จริง · หนึ่งลิงก์ต่อบรรทัด</span><textarea :value="destination.assetLinks.join('\n')" rows="2" placeholder="https://…" @input="destination.assetLinks=lines($event);touch()"></textarea></label><label v-if="form.status==='Published'" class="field"><span>ลิงก์โพสต์ที่เผยแพร่แล้ว</span><input v-model="destination.postLink" type="url" placeholder="เพิ่มภายหลังได้" @input="touch()"/></label></div></section>
        <p v-if="form.status==='Published'" class="published-note"><AlertCircle :size="16"/>การแก้ข้อมูลใน Planny ไม่เปลี่ยนโพสต์จริงบนแพลตฟอร์ม</p>
      </div><aside class="content-inspector"><section><h2>เส้นทางของคอนเทนต์</h2><label class="field"><span>สถานะ</span><select :value="form.status" aria-label="สถานะคอนเทนต์" @change="changeStatus"><option v-for="status in statuses" :key="status" :value="status">{{status}} — {{labels[status]}}</option></select></label><div class="lifecycle-track"><span v-for="s in statuses" :key="s" :class="{reached:statuses.indexOf(s)<=statuses.indexOf(form.status)}" :title="labels[s]"></span></div><label class="field"><span>กำหนดเผยแพร่</span><input type="datetime-local" :value="localTime(form.plannedAt,zone)" @change="setDate('plannedAt',$event)"/></label><label v-if="form.status==='Published'" class="field"><span>เวลาเผยแพร่จริง</span><input type="datetime-local" :value="localTime(form.publishedAt,zone)" @change="setDate('publishedAt',$event)"/></label><p class="helper">เขตเวลา {{zone}}</p><button v-if="form.status==='Planned'" class="button subtle small" @click="cancelPlan()">ยกเลิกกำหนด</button><button v-if="form.status!=='Published'" class="button primary full" @click="publish()"><CheckCircle2 :size="16"/>บันทึกเผยแพร่แล้ว</button></section>
        <section><h2>แพลตฟอร์ม</h2><p class="helper">ใช้สถานะและวันเผยแพร่ร่วมกัน</p><label v-for="p in data.platforms.filter(p=>p.active||form!.destinations.some(d=>d.platformId===p.id))" :key="p.id" class="platform-check"><input type="checkbox" :checked="form.destinations.some(d=>d.platformId===p.id)" :disabled="!p.active&&!form.destinations.some(d=>d.platformId===p.id)" @change="togglePlatform(p.id)"/><PlatformIcon :name="p.name" :size="17"/><span>{{p.name}} <small v-if="!p.active">(ปิดใช้)</small></span></label></section>
        <section><h2>Tags</h2><div class="tag-picker"><label v-for="tag in data.tags" :key="tag.id" class="tag-option"><input v-model="form.tagIds" type="checkbox" :value="tag.id" @change="touch()"/>{{tag.name}}</label></div><form class="inline-form" @submit.prevent="addTag(true)"><input v-model="newTag" aria-label="สร้าง Tag ในคอนเทนต์" placeholder="เพิ่ม Tag" maxlength="80"/><button class="icon-button" aria-label="เพิ่ม Tag" :disabled="busy"><Plus :size="16"/></button></form></section>
        <section class="inspector-actions"><button class="button subtle full" @click="duplicateCurrent()"><Copy :size="16"/>ทำสำเนาเป็น Draft</button><button class="button subtle full" @click="archiveCurrent()"><Archive :size="16"/>เก็บเข้าคลัง</button><button class="button danger-text full" @click="trashCurrent()"><Trash2 :size="16"/>ย้ายลงถังขยะ</button></section></aside></div>
    </section>
    <section v-else class="page-content"><header class="page-heading"><div><h1>{{currentNav.label}}</h1><p>{{view==='board'?'เก็บไอเดีย เตรียมเนื้อหา แล้วค่อย ๆ ทำให้เป็นจริง':view==='calendar'?'มองเห็นแผนข้างหน้า และพื้นที่สำหรับคอนเทนต์ชิ้นถัดไป':view==='archive'?'งานที่เก็บไว้ ยังกลับมาเปิดอ่านและทำต่อได้':view==='trash'?'งานที่ลบยังอยู่ที่นี่ จนกว่าคุณจะเลือกยืนยันลบถาวร':view==='profile'?'รวมแพลตฟอร์มและลิงก์หน้าเพจของคุณ':'จัดพื้นที่ทำงานให้เหมาะกับวิธีทำคอนเทนต์ของคุณ'}}</p></div><button v-if="view==='board'||view==='calendar'" class="button primary" @click="startCreate()"><Plus :size="18"/>สร้างคอนเทนต์</button></header>
      <PlatformProfile v-if="view==='profile'" :platforms="data.platforms" :save="savePlatformUrl"/>
      <template v-else-if="view!=='settings'">
        <div class="filter-bar"><label class="search-field"><Search :size="17"/><input v-model="query" aria-label="ค้นหาคอนเทนต์" placeholder="ค้นหาคอนเทนต์หรือข้อความ…"/></label><select v-model="filterStatus" aria-label="กรองสถานะ"><option value="">ทุกสถานะ</option><option v-for="s in statuses" :key="s">{{s}}</option></select><select v-model="filterPlatform" aria-label="กรองแพลตฟอร์ม"><option value="">ทุกแพลตฟอร์ม</option><option v-for="p in data.platforms" :key="p.id" :value="p.id">{{p.name}}</option></select><select v-model="filterTag" aria-label="กรอง Tag"><option value="">ทุก Tag</option><option v-for="t in data.tags" :key="t.id" :value="t.id">{{t.name}}</option></select><div v-if="view==='board'" class="segmented view-switch"><button :class="{active:!listMode}" aria-label="มุมมองบอร์ด" @click="listMode=false"><LayoutGrid :size="16"/></button><button :class="{active:listMode}" aria-label="มุมมองรายการ" @click="listMode=true"><List :size="17"/></button></div></div>
        <div class="filter-details"><span v-if="view==='board'">{{visible.length}} คอนเทนต์ <span v-if="lateCount" class="late-text">· {{lateCount}} เลยกำหนด</span></span><label>ช่วงวันเผยแพร่ <input v-model="fromDate" type="date" aria-label="วันเผยแพร่เริ่มต้น"/></label><span>ถึง</span><input v-model="toDate" type="date" aria-label="วันเผยแพร่สิ้นสุด"/><button v-if="filtered" class="button subtle small" @click="clearFilters()"><X :size="13"/>ล้างตัวกรอง</button></div>
        <CalendarView v-if="view==='calendar'" :contents="visible" :zone="zone" @open="open" @move="calendarMove" @create="startCreate"/>
        <div v-else-if="view==='board'&&!listMode" class="board"><section v-for="status in statuses" :key="status" class="board-column" :class="status.toLowerCase()"><div class="column-heading"><span class="status-dot"></span><h2>{{status}}</h2><span class="column-count">{{visible.filter(c=>c.status===status).length}}</span></div><p class="column-subtitle">{{labels[status]}}</p><div class="column-cards"><button v-for="c in visible.filter(c=>c.status===status)" :key="c.id" class="content-card" @click="open(c.id)"><div class="card-platforms"><span v-for="d in c.destinations" :key="d.platformId"><PlatformIcon :name="platformLabel(d.platformId)" :size="12"/>{{platformLabel(d.platformId)}}</span><span v-if="!c.destinations.length" class="muted">ยังไม่เลือกแพลตฟอร์ม</span></div><h3>{{c.title}}</h3><p v-if="richText(c.brief).trim()" class="card-excerpt">{{richText(c.brief)}}</p><div v-if="c.tagIds.length" class="card-tags"><span v-for="id in c.tagIds.slice(0,3)" :key="id">{{data.tags.find(t=>t.id===id)?.name}}</span></div><footer><span :class="{'late-text':overdue(c)}"><Clock :size="13"/>{{overdue(c)?'เลยกำหนด · ':''}}{{formatDate(c.status==='Published'?c.publishedAt:c.plannedAt)}}</span><ChevronRight :size="14"/></footer></button><div v-if="!visible.some(c=>c.status===status)" class="column-empty"><span class="empty-stage-mark"><FileText :size="20"/></span><span>{{filtered?'ไม่พบงานที่ตรงตัวกรอง':'ยังไม่มีคอนเทนต์ในขั้นนี้'}}</span></div></div><button v-if="status==='Idea'" class="add-idea" @click="startCreate()"><Plus :size="16"/>เก็บไอเดียใหม่</button></section></div>
        <div v-else class="list-surface"><div v-if="!visible.length" class="empty-state"><component :is="view==='trash'?Trash2:view==='archive'?Archive:FileText" :size="32"/><h2>{{filtered?'ไม่พบคอนเทนต์ที่ตรงตัวกรอง':view==='trash'?'ถังขยะยังว่าง':view==='archive'?'ยังไม่มีงานในคลัง':'เริ่มจากไอเดียแรกของคุณ'}}</h2><p>{{filtered?'ลองเปลี่ยนคำค้นหาหรือล้างตัวกรอง':'พื้นที่นี้จะค่อย ๆ เติบโตไปพร้อมคอนเทนต์ของคุณ'}}</p><button v-if="filtered" class="button" @click="clearFilters()">ล้างตัวกรอง</button><button v-else-if="view==='board'" class="button primary" @click="startCreate()">สร้างคอนเทนต์</button></div><table v-else><thead><tr><th>คอนเทนต์</th><th>สถานะ</th><th>แพลตฟอร์ม</th><th>วันเผยแพร่</th><th><span class="sr-only">การจัดการ</span></th></tr></thead><tbody><tr v-for="c in visible" :key="c.id"><td><button class="title-link" @click="view==='trash'?undefined:open(c.id)">{{c.title}}</button></td><td><span class="status-badge" :class="c.status.toLowerCase()">{{c.status}}</span></td><td><div class="list-platforms"><span v-for="d in c.destinations" :key="d.platformId" class="platform-name"><PlatformIcon :name="platformLabel(d.platformId)" :size="15"/>{{platformLabel(d.platformId)}}</span><span v-if="!c.destinations.length">—</span></div></td><td :class="{'late-text':overdue(c)}">{{formatDate(c.status==='Published'?c.publishedAt:c.plannedAt)}}</td><td class="row-actions"><template v-if="view==='trash'||view==='archive'"><button class="button small" @click="restore(c)"><RotateCcw :size="14"/>{{view==='trash'?'กู้คืน':'คืนงาน'}}</button><button v-if="view==='trash'" class="icon-button danger-text" aria-label="ลบถาวร" @click="purge(c)"><Trash2 :size="16"/></button></template><button v-else class="icon-button" aria-label="เปิดคอนเทนต์" @click="open(c.id)"><ArrowRight :size="17"/></button></td></tr></tbody></table></div>
      </template>
      <div v-else class="settings-layout"><section class="settings-section"><h2>วันเวลาในพื้นที่ของคุณ</h2><p>วันเผยแพร่ทั้งหมดจะแสดงตามเขตเวลานี้</p><label class="field"><span>เขตเวลา</span><select :value="zone" @change="mutate(w=>w.timezone=($event.target as HTMLSelectElement).value,'เปลี่ยนเขตเวลาแล้ว')"><option v-for="z in ['Asia/Bangkok','Asia/Tokyo','Asia/Singapore','UTC','Europe/London','America/New_York','America/Los_Angeles']" :key="z">{{z}}</option></select></label></section>
        <section class="settings-section"><h2>แพลตฟอร์ม</h2><p>ปิดใช้เพื่อซ่อนจากงานใหม่ ข้อมูลงานเดิมจะยังอยู่</p><div v-for="p in data.platforms" :key="p.id" class="setting-row"><span class="platform-name"><PlatformIcon :name="p.name"/>{{p.name}}</span><button class="button small" :disabled="busy" :aria-pressed="p.active" @click="mutate(w=>{w.platforms.find(x=>x.id===p.id)!.active=!p.active})">{{p.active?'ปิดใช้':'เปิดใช้'}}</button></div><form class="inline-form" @submit.prevent="addPlatform()"><input v-model="platformName" aria-label="ชื่อแพลตฟอร์มใหม่" placeholder="ชื่อแพลตฟอร์มอื่น" maxlength="80"/><button class="button" :disabled="busy||!platformName.trim()"><Plus :size="16"/>เพิ่ม</button></form></section>
        <section class="settings-section"><h2>Tags</h2><p>จัดหมวดหมู่คอนเทนต์ให้ค้นหาง่ายขึ้น</p><div v-for="tag in data.tags" :key="tag.id" class="setting-row"><span>{{tag.name}}</span><button class="icon-button danger-text" :aria-label="`ลบ Tag ${tag.name}`" @click="removeTag(tag.id)"><Trash2 :size="15"/></button></div><form class="inline-form" @submit.prevent="addTag()"><input v-model="tagName" aria-label="ชื่อ Tag ใหม่" placeholder="เช่น รีวิวสินค้า หรือเบื้องหลัง" maxlength="80"/><button class="button" :disabled="busy||!tagName.trim()"><Plus :size="16"/>เพิ่ม</button></form></section>
        <section class="settings-section backup-section"><h2>สำรองข้อมูล</h2><p>เก็บคอนเทนต์ Tags แพลตฟอร์ม คลัง และถังขยะไว้ในไฟล์เดียว</p><button class="button" :disabled="busy" @click="exportBackup()"><Download :size="16"/>Export ไฟล์สำรอง</button><hr/><h3>นำเข้าข้อมูล</h3><p>Import จะแทนที่ข้อมูลทั้งหมด ควร Export ข้อมูลเดิมก่อน</p><label class="button import-button" :class="{disabled:busy}"><Upload :size="16"/>เลือกไฟล์สำรอง<input type="file" accept="application/json,.json" :disabled="busy" @change="importFile"/></label></section>
      </div>
    </section>
  </template></main>
  <div v-if="toast" class="toast" role="status"><Check :size="16"/>{{toast}}</div>
  <div v-if="creating" class="dialog-backdrop" @click.self="creating=false"><form class="dialog" role="dialog" aria-modal="true" aria-labelledby="create-heading" @submit.prevent="create()"><div class="dialog-heading"><h2 id="create-heading">เริ่มคอนเทนต์ใหม่</h2><button type="button" class="icon-button" aria-label="ปิด" @click="creating=false"><X :size="18"/></button></div><p>ยังไม่ต้องคิดทุกอย่างให้ครบ เริ่มจากชื่อหรือไอเดียสั้น ๆ</p><label class="field"><span>ชื่อคอนเทนต์</span><input id="new-title" v-model="newTitle" maxlength="240" required placeholder="คอนเทนต์ชิ้นถัดไปจะเล่าเรื่องอะไร?"/></label><div class="dialog-actions"><button type="button" class="button" @click="creating=false">ยกเลิก</button><button class="button primary" :disabled="busy||!newTitle.trim()">{{busy?'กำลังสร้าง…':'สร้างไอเดีย'}}</button></div></form></div>
  <div v-if="confirmBox" class="dialog-backdrop" @click.self="answer(false)"><section class="dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-heading"><h2 id="confirm-heading">{{confirmBox.title}}</h2><p>{{confirmBox.message}}</p><div class="dialog-actions"><button class="button" autofocus @click="answer(false)">ยกเลิก</button><button class="button" :class="confirmBox.danger?'danger':'primary'" @click="answer(true)">{{confirmBox.accept}}</button></div></section></div>
</div>
</template>
