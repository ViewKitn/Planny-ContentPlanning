import {describe,it,expect} from 'vitest';
import {createContent,duplicate,validateContent,instant,localTime,moveDay,dayKey,eventTime} from './model';
describe('content lifecycle',()=>{
  it('requires a destination and schedule for Planned',()=>{const c=createContent('ชื่อ');c.status='Planned';expect(validateContent(c)).toContain('กำหนด');c.destinations.push({platformId:'facebook',caption:'',assetLinks:[],postLink:''});c.plannedAt='2026-10-09T02:00:00Z';expect(validateContent(c)).toBeNull();});
  it('supports direct Published without a planned date or link',()=>{const c=createContent('ชื่อ');c.status='Published';c.publishedAt='2026-10-09T02:00:00Z';c.destinations.push({platformId:'facebook',caption:'',assetLinks:[],postLink:''});expect(validateContent(c)).toBeNull();});
  it('duplicates into Draft without sharing mutable data or old publication metadata',()=>{const c=createContent('งาน');c.status='Published';c.plannedAt='2026-10-08T02:00:00Z';c.publishedAt='2026-10-09T02:00:00Z';c.destinations=[{platformId:'facebook',caption:'ต้นฉบับ',assetLinks:['https://example.com/file'],postLink:'https://example.com/post'}];c.archived=true;const d=duplicate(c);expect(d.status).toBe('Draft');expect(d.plannedAt).toBeNull();expect(d.publishedAt).toBeNull();expect(d.destinations[0].postLink).toBe('');expect(d.archived).toBe(false);d.destinations[0].caption='ใหม่';expect(c.destinations[0].caption).toBe('ต้นฉบับ');});
  it('rejects executable link schemes',()=>{const c=createContent('งาน');c.referenceLinks=['javascript:alert(1)'];expect(validateContent(c)).toContain('http');});
});
describe('calendar timezone',()=>{
  it('round-trips Bangkok wall time',()=>{const value=instant('2026-10-09T09:30','Asia/Bangkok');expect(value).toBe('2026-10-09T02:30:00Z');expect(localTime(value,'Asia/Bangkok')).toBe('2026-10-09T09:30');});
  it('moves only Planned and preserves local time across a DST boundary',()=>{const c=createContent('งาน');c.status='Planned';c.plannedAt=instant('2026-03-07T09:30','America/New_York');const moved=moveDay(c,'2026-03-09','America/New_York');expect(localTime(moved.plannedAt,'America/New_York')).toBe('2026-03-09T09:30');expect(moved.plannedAt).toBe('2026-03-09T13:30:00Z');c.status='Published';expect(moveDay(c,'2026-03-09','America/New_York')).toBe(c);});
  it('rejects a nonexistent wall time',()=>{expect(()=>instant('2026-03-08T02:30','America/New_York')).toThrow();});
  it('uses actual publication day for Published',()=>{const c=createContent('งาน');c.status='Published';c.plannedAt='2026-10-08T02:00:00Z';c.publishedAt='2026-10-09T19:00:00Z';expect(dayKey(eventTime(c)!,'Asia/Bangkok')).toBe('2026-10-10');});
});
