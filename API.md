# <font color="pink">所有API的curl以及response</font>
## <font color="purple">用户增删改查</font>
### 1 注册用户
```cmd
curl -X POST http://localhost:5000/auth/register -H "Content-Type: application/json" -d "{\"username\":\"testuser1\",\"password\":\"123456\"}"
```
```cmd
{"user_id":9,"message":"Registration successful"}
```
### 2 登录（保存 Session）
```cmd
curl -X POST http://localhost:5000/auth/login -H "Content-Type: application/json" -c cookies.txt -d "{\"username\":\"testuser1\",\"password\":\"123456\"}"
```
```cmd
{"user_id":9,"message":"Login successful"}
```
### 3 查询用户资料
```cmd
curl -X GET http://localhost:5000/auth/user -b cookies.txt
```
```cmd
{"qq":null,"user_id":9,"phone":null,"wechat":null,"email":null,"point":0,"username":"testuser1"}
```
### 4 修改资料
```cmd
curl -X PATCH http://localhost:5000/auth/user -H "Content-Type: application/json" -b cookies.txt -d "{\"qq\":\"123456789\"}"
```
```cmd
{"message":"Profile updated successfully","updated":["qq"]}
```
### 删除账号
```cmd
curl -X POST http://localhost:5000/auth/delete_account -H "Content-Type: application/json" -b cookies.txt -d "{\"password\":\"123456\"}"
```
```cmd
{"message":"Account deleted successfully"}
```

## <font color="purple">文件增删改查</font>
### 1 创建并上传文件
```cmd
echo Hello Upload Test > hello.txt
```
```cmd
curl -X POST http://localhost:5000/download/upload -b cookies.txt -F "file=@hello.txt" -F "file_permission=private" -F "description=测试文件"
```
文件存在时
```cmd
{"error":"file already exists","_status":400}
```
文件不存在时
```cmd
{"file_hash":"d00c020ff18ee3797493bacc855758283b8b6daf39033a0c552bb0e5ed9b5606","uploaded_at":"2025-09-02T13:07:56.031358200Z","file_name":"hello.txt","file_id":5,"file_permission":"private","description":"测试文件","message":"Upload successful","_status":201,"file_size":20}
```

### 2 列出文件
```cmd
curl -X GET http://localhost:5000/download/files -b cookies.txt
```
```cmd
[{"file_id":5,"file_name":"hello.txt","updated_at":"2025-09-02T21:07:56Z","description":"测试文件","file_permission":"private","file_hash":"d00c020ff18ee3797493bacc855758283b8b6daf39033a0c552bb0e5ed9b5606","file_size":20}]
```

### 3 下载文件
```cmd
curl -X GET http://localhost:5000/download/download/4 -b cookies.txt -o downloaded.txt
```
```cmd
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    26  100    26    0     0   1681      0 --:--:-- --:--:-- --:--:--  1733
```
### 4 修改文件信息 
```cmd
curl -X PUT http://localhost:5000/download/file/4 -H "Content-Type: application/json" -b cookies.txt -d "{\"file_name\":\"new_hello.txt\",\"file_permission\":\"public\"}"
```
```cmd
{"message":"Update successful"}
```
### 5 删除文件

```cmd
curl -X DELETE http://localhost:5000/download/file/4 -b cookies.txt
```
```cmd
{"message":"Delete successful"}
```