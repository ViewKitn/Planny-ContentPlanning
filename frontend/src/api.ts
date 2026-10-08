export class ApiError extends Error {constructor(message:string,public status:number){super(message);}}
export async function request<T>(url:string, options:RequestInit={}):Promise<T>{
  let response:Response;
  const local = ['localhost','127.0.0.1','[::1]'].includes(window.location.hostname);
  try {response=await fetch(`/api${url}`,{...options,credentials:'same-origin',headers:{'Content-Type':'application/json',...options.headers},signal:AbortSignal.timeout(local ? 12000 : 60000)});}catch{throw new ApiError('เชื่อมต่อ Planny ไม่สำเร็จ ตรวจการเชื่อมต่อแล้วลองใหม่',0);}
  const body=await response.json().catch(()=>({message:'อ่านคำตอบจากระบบไม่สำเร็จ'}));
  if(!response.ok){if(response.status===401&&!url.startsWith('/auth/'))window.dispatchEvent(new Event('planny:session-expired'));throw new ApiError(body.message||'บันทึกไม่สำเร็จ กรุณาลองใหม่',response.status);}
  return body as T;
}
