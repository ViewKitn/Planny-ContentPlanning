import {ref} from 'vue';
import {request,ApiError} from './api';
import type {Workspace,Snapshot} from './model';
export function useWorkspace(){
  const data=ref<Workspace|null>(null),revision=ref(0),loading=ref(true),error=ref(''),busy=ref(false),conflict=ref(false);
  let queue:Promise<unknown>=Promise.resolve();
  async function load(){loading.value=true;try{const s=await request<Snapshot>('/state');data.value=s.data;revision.value=s.revision;error.value='';conflict.value=false;}catch(e){error.value=(e as Error).message;}finally{loading.value=false;}}
  function commit(change:(next:Workspace)=>void){
    const work=queue.catch(()=>{}).then(async()=>{
      if(!data.value)throw Error('ยังไม่ได้โหลดข้อมูล');
      if(conflict.value)throw Error('ข้อมูลเปลี่ยนจากหน้าต่างอื่น โหลดข้อมูลล่าสุดก่อนลองใหม่');
      busy.value=true;
      try{const next=structuredClone(JSON.parse(JSON.stringify(data.value))) as Workspace;change(next);const s=await request<Snapshot>('/state',{method:'PUT',body:JSON.stringify({revision:revision.value,data:next})});data.value=s.data;revision.value=s.revision;error.value='';}
      catch(e){error.value=(e as Error).message;if(e instanceof ApiError && e.status===409)conflict.value=true;throw e;}
      finally{busy.value=false;}
    });queue=work;return work;
  }
  async function importBackup(backup:unknown){await queue.catch(()=>{});busy.value=true;try{const s=await request<Snapshot>('/import',{method:'POST',body:JSON.stringify({revision:revision.value,backup})});data.value=s.data;revision.value=s.revision;error.value='';conflict.value=false;}catch(e){error.value=(e as Error).message;throw e;}finally{busy.value=false;}}
  return {data,revision,loading,error,busy,conflict,load,commit,importBackup};
}
