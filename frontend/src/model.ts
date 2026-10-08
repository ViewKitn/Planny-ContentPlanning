import { Temporal } from '@js-temporal/polyfill';
export const statuses = ['Idea','Draft','Ready','Planned','Published'] as const;
export type Status = typeof statuses[number];
export const labels: Record<Status,string> = {Idea:'เก็บไอเดีย',Draft:'กำลังเขียน',Ready:'พร้อมเผยแพร่',Planned:'วางแผนแล้ว',Published:'เผยแพร่แล้ว'};
export interface RichDoc {type:string; content?:RichDoc[]; text?:string; marks?:{type:string}[]; attrs?:Record<string,unknown>}
export interface Destination {platformId:string;caption:string;assetLinks:string[];postLink:string}
export interface Content {id:string; title:string; status:Status; brief:RichDoc; script:RichDoc;referenceLinks:string[];tagIds:string[];destinations:Destination[];plannedAt:string|null;publishedAt:string|null;archived:boolean;deletedAt:string|null;createdAt:string;updatedAt:string}
export interface Platform {id:string;name:string;active:boolean;pageUrl?:string|null}
export interface Workspace {contents:Content[]; tags:{id:string;name:string}[]; platforms:Platform[];timezone:string}
export interface Snapshot {revision:number;data:Workspace}
export const blankDoc = ():RichDoc => ({type:'doc',content:[{type:'paragraph'}]});
export function createContent(title:string):Content { const now=new Date().toISOString();return {id:crypto.randomUUID(), title, status:'Idea',brief:blankDoc(),script:blankDoc(),referenceLinks:[],tagIds:[],destinations:[],plannedAt:null,publishedAt:null,archived:false,deletedAt:null,createdAt:now,updatedAt:now}; }
export function duplicate(c:Content):Content {return {...structuredClone(c),id:crypto.randomUUID(),title:`${c.title} (สำเนา)`,status:'Draft',plannedAt:null,publishedAt:null,archived:false,deletedAt:null,destinations:c.destinations.map(d=>({...structuredClone(d),postLink:''})),createdAt:new Date().toISOString(),updatedAt:new Date().toISOString()};}
export function richText(doc:RichDoc):string {return [doc.text||'',...(doc.content||[]).map(richText)].join(' ');}
export function validateContent(c:Content):string|null {
  if(!c.title.trim()) return 'กรุณาใส่ชื่อคอนเทนต์';
  if(c.title.length>240)return 'ชื่อคอนเทนต์ยาวได้ไม่เกิน 240 ตัวอักษร';
  if(c.status==='Planned' && (!c.destinations.length||!c.plannedAt))return 'Planned ต้องมีแพลตฟอร์มและกำหนดเผยแพร่';
  if(c.status==='Published' && (!c.destinations.length||!c.publishedAt))return 'Published ต้องมีแพลตฟอร์มและเวลาเผยแพร่จริง';
  const links=[...c.referenceLinks,...c.destinations.flatMap(d=>[...d.assetLinks,d.postLink])];
  for(const link of links.filter(Boolean)){try{const u=new URL(link);if(!['http:','https:'].includes(u.protocol))throw Error();}catch{return 'ลิงก์ต้องเป็น URL แบบ http หรือ https';}}
  return null;
}
export function dayKey(iso:string,zone:string):string {return Temporal.Instant.from(iso).toZonedDateTimeISO(zone).toPlainDate().toString();}
export function localTime(iso:string|null,zone:string):string {return iso?Temporal.Instant.from(iso).toZonedDateTimeISO(zone).toPlainDateTime().toString({smallestUnit:'minute'}):'';}
export function instant(local:string,zone:string):string|null {return local?Temporal.PlainDateTime.from(local).toZonedDateTime(zone,{disambiguation:'reject'}).toInstant().toString():null;}
export function moveDay(c:Content,day:string,zone:string):Content {if(c.status!=='Planned'||!c.plannedAt)return c;const old=localTime(c.plannedAt,zone);return {...c,plannedAt:instant(`${day}T${old.slice(11)}`,zone),updatedAt:new Date().toISOString()};}
export const eventTime=(c:Content)=>c.status==='Published'?c.publishedAt:c.status==='Planned'?c.plannedAt:null;
export const overdue=(c:Content)=>c.status==='Planned'&&!!c.plannedAt&&Date.parse(c.plannedAt)<Date.now();
