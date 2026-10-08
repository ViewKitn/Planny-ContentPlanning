export class ApiError extends Error {constructor(message:string,public status:number){super(message);}}
export async function request<T>(url:string, options:RequestInit={}):Promise<T>{
  let response:Response;
  try {response=await fetch(`/api${url}`,{...options,headers:{'Content-Type':'application/json',...options.headers},signal:AbortSignal.timeout(12000)});}catch{throw new ApiError('เชื่อมต่อ Planny ไม่สำเร็จ ตรวจว่า backend เปิดอยู่แล้วลองใหม่',0);}
  const body=await response.json().catch(()=>({message:'อ่านคำตอบจากระบบไม่สำเร็จ'}));
  if(!response.ok)throw new ApiError(body.message||'บันทึกไม่สำเร็จ กรุณาลองใหม่',response.status);
  return body as T;
}
