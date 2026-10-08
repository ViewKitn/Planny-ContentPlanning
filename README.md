# Planny

**Planny** เป็น Web Application สำหรับครีเอเตอร์ที่ต้องการจัดการ Content Lifecycle ตั้งแต่คิดไอเดีย เขียนเนื้อหา เตรียมงาน ไปจนถึงวางแผนและบันทึกการเผยแพร่ในที่เดียว

เหมาะสำหรับครีเอเตอร์คนเดียวที่ดูแลหนึ่งเพจและเผยแพร่คอนเทนต์เดียวกันหลายแพลตฟอร์ม ช่วยให้เห็นว่างานแต่ละชิ้นอยู่ขั้นตอนไหน ต้องโพสต์เมื่อไร และเตรียมข้อความของแต่ละช่องทางได้ครบก่อนเผยแพร่ ด้วย UI ภาษาไทยสไตล์ Modern SaaS ที่รองรับทั้งคอมพิวเตอร์และมือถือ

## Features หลัก

- **จัดการวงจรคอนเทนต์** — ติดตามสถานะ Idea → Draft → Ready → Planned → Published พร้อมมุมมองบอร์ดและรายการ
- **เขียน Brief และ Script** — Rich Text Editor พร้อมบันทึกอัตโนมัติและกู้คืนร่างที่ยังบันทึกไม่สำเร็จในเบราว์เซอร์เดิม
- **เตรียมคอนเทนต์หลายแพลตฟอร์ม** — ใช้สถานะและกำหนดเผยแพร่ร่วมกัน พร้อมแยก Caption ลิงก์ไฟล์ และลิงก์โพสต์ตามแพลตฟอร์ม
- **วางแผนบนปฏิทิน** — มุมมองเดือนและสัปดาห์ ลากเปลี่ยนวันเผยแพร่โดยคงเวลาเดิม
- **โปรไฟล์เพจ** — แสดงโลโก้ Facebook, Instagram, TikTok และ YouTube บันทึกลิงก์หน้าเพจและเปิดปลายทางในแท็บใหม่
- **จัดระเบียบงาน** — Tags การค้นหาและกรอง ทำสำเนาคอนเทนต์ คลังงาน และถังขยะพร้อมกู้คืนหรือลบถาวร
- **สำรองและกู้ข้อมูล** — Export/Import ไฟล์สำรอง พร้อมตรวจสอบข้อมูลก่อนบันทึกลง PostgreSQL

รุ่นปัจจุบันเป็นเครื่องมือส่วนตัวที่รันบนเครื่องผ่าน localhost โดยยังไม่มีระบบบัญชี การเผยแพร่จริงทำด้วยตนเอง แล้วบันทึกสถานะและลิงก์โพสต์กลับเข้า Planny โครงการเน้นให้ Core Features สมบูรณ์ก่อนเพิ่ม AI การโพสต์อัตโนมัติ และระบบสมัครสมาชิก/เข้าสู่ระบบในระยะถัดไป

## Tech Stack

| ส่วน | เทคโนโลยี |
| --- | --- |
| Frontend | Vue 3, TypeScript, Vite |
| Rich Text Editor | Tiptap |
| UI และ Icons | CSS, Lucide, Simple Icons |
| Backend | Spring Boot 3.5, Java 21, Spring JDBC |
| Database | PostgreSQL 18, JSONB |
| Database Migrations | Flyway |
| Tests | Vitest, JUnit / Spring Boot Test |
| Build และ Local Runtime | npm, Maven, Docker Compose หรือ PostgreSQL บนเครื่อง |

Vue ติดต่อ Spring Boot ผ่าน REST API และเก็บข้อมูลใน PostgreSQL เมื่อ build สำหรับใช้งาน จะรวม frontend ไว้ใน Spring Boot JAR เพื่อเปิดแอปได้จากโปรแกรมเดียว

## เริ่มใช้งานบน Windows

ต้องมี Node.js 22.12+ และ Java 21 และ PostgreSQL (หรือ Docker สำหรับฐานข้อมูล)

1. เปิดฐานข้อมูลแยกสำหรับโปรเจกต์นี้:

   ```powershell
   .\scripts\database.ps1 start
   ```

   หาก PostgreSQL อยู่ที่อื่น ใช้ `-PgBin 'C:\path\to\bin'` สคริปต์นี้สร้าง cluster ใหม่ใน `.local/postgres` ไม่ใช้ฐานข้อมูลเดิมบนเครื่อง ใช้ trust authentication เฉพาะ instance ส่วนตัวที่ bind กับ loopback ของเครื่อง หากใช้เครื่องร่วมกับผู้อื่นให้เปลี่ยนเป็นรหัสผ่าน

   หรือใช้ `docker compose up -d db` แทนสคริปต์ โดยเลือกอย่างใดอย่างหนึ่ง (ใช้ port เดียวกัน)

2. เปิด backend ใน terminal หนึ่ง:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   Maven จะถูกดาวน์โหลดใน `.tools` เมื่อใช้ wrapper ครั้งแรก ต้องตั้ง JAVA_HOME ให้ชี้ Java 21 หากยังไม่มี

3. เปิด frontend ในอีก terminal:

   ```powershell
   cd frontend
   npm.cmd install
   npm.cmd run dev
   ```

4. เปิด http://127.0.0.1:5173

ระบบเริ่มด้วยพื้นที่ว่าง ไม่มีข้อมูลตัวอย่างปะปนกับงานจริง API และ frontend bind กับ loopback เป็นเครื่องมือส่วนตัวที่ยังไม่มีระบบบัญชี

เมนู **โปรไฟล์เพจ** ใช้เพิ่มลิงก์หน้าเพจของแต่ละแพลตฟอร์ม กด “บันทึกลิงก์” แล้วใช้ “เปิดเพจ” เพื่อเปิดในแท็บใหม่ ลิงก์เก็บใน PostgreSQL และรวมในไฟล์สำรอง โลโก้ Facebook, Instagram, TikTok และ YouTube แสดงในคอนเทนต์และโปรไฟล์ด้วย

## ตรวจสอบและ build

```powershell
cd frontend
npm.cmd test
npm.cmd run build
```

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd package
```

backend integration tests ใช้ฐานข้อมูล `planny_test` เมื่อกำหนด `PLANNY_TEST_DB_URL` (รายละเอียดใน docs/verification.md) และไม่แตะข้อมูลใช้งานจริง

## เปิดจากไฟล์เดียว

เมื่อ build frontend ก่อน package backend แล้ว Vue จะรวมอยู่ใน jar:

```powershell
.\scripts\build.ps1
java -jar .\backend\target\planny-0.1.0.jar
```

เปิด http://127.0.0.1:18080 โดยไม่ต้องเปิด Vite ฐานข้อมูลต้องรันอยู่เหมือนเดิม

## การตั้งค่า backend

`PLANNY_DB_URL` (ค่าเริ่มต้น `jdbc:postgresql://127.0.0.1:55432/planny`), `PLANNY_DB_USER`, `PLANNY_DB_PASSWORD`, `PLANNY_PORT` ปรับได้ผ่าน environment

สำรองผ่านหน้า “การตั้งค่า” → Export ไฟล์สำรอง Import เป็นการแทนที่ข้อมูลทั้งหมดและต้องยืนยันก่อน ไฟล์มี schema version และ server ตรวจข้อมูลก่อนบันทึกแบบ transaction

ข้อมูลที่บันทึกอยู่ใน PostgreSQL; ร่างที่ยังไม่บันทึกอยู่เฉพาะเบราว์เซอร์เดิม การปิด/เปิดโปรแกรมไม่ล้างข้อมูล หยุดฐานข้อมูลได้ด้วย `.\scripts\database.ps1 stop`

## เอกสาร

- [สเปกที่ยืนยันแล้ว](docs/planny-spec.md)
- [คำศัพท์](GLOSSARY.md)
- [บันทึกการตัดสินใจ](docs/planning.md)

รุ่นนี้ไม่มี AI การโพสต์อัตโนมัติ ระบบบัญชี Brand ทีม อัปโหลดสื่อ Campaign Dark Mode หรือ Dashboard สถิติ
