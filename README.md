# 在 Spring Boot 中配置 TLS

## 生成密钥对

要启用 TLS，我们需要创建一个公钥/私钥对。为此，我们使用keytool。keytool命令随默认 Java
发行版一起提供。让我们使用keytool生成密钥对并将其存储在keystore.p12文件中：

```shell
keytool -genkeypair -alias "sca" -keyalg RSA -keysize 4096 \
-validity 3650 -dname "CN=localhost" -keypass "123456" -keystore keystore.p12 \
-storeType PKCS12 -storepass "123456"
```

密钥库文件可以采用不同的格式。最流行的两种格式是 Java KeyStore (JKS) 和 PKCS#12。JKS 特定于 Java，而 PKCS#12
是一种行业标准格式，属于公钥加密标准(PKCS) 下定义的标准系列。

## 在 spring 中配置TLS

我们先来配置单向 TLS。我们在application.yml文件中配置 TLS 相关的属性：

```yaml
server:
  ssl:
    # enable/disable https
    enabled: true
    # keystore format
    key-store-type: PKCS12
    # keystore location
    key-store: classpath:keystore/keystore.p12
    # keystore password
    key-store-password: 123456
    # 在配置 SSL 协议时，我们将使用 TLS 并告诉服务器使用 TLS 1.2
    protocol: TLS
    enabled-protocols: TLSv1.2
```

> : Tomcat started on port 41000 (https) with context path '/auth'

# 启动并测试

## 1. 发送请求，获取授权码

1. 基础
   https://127.0.0.1:41000/auth/oauth2/authorize?client_id=demo-client&response_type=code&scope=openid&redirect_uri=https://www.baidu.com

> https://www.baidu.com/?code=Ats1Af3vpxoOFcKkvwfga4s06ATL8JUJ-PuQXQv3H8pudS3Q0QGCukJ-IkYHK3PDf848MRMbaGEwIukWvKX-aADN8v7_uPCuyNDHiGIUr5Q0-b8t4qemER0Vi8zJxl5b

2. 升级使用

https://127.0.0.1:41000/auth/oauth2/authorize?response_type=code&client_id=client-id-1&scope=message.read&redirect_uri=https://127.0.0.1:41000/auth/login/oauth2/code/client-id-1-oidc

> https://127.0.0.1:41000/auth/login/oauth2/code/client-id-1-oidc?code=MVGxNkSF1lc2nZ1xYvlEFQUdna10Y-_iTcDvgsVfbYTDOdEOlAOraHbXM0mnjAjHZNu7dl_n8z2ZipFJORKOaHjh6basyEDzmaqP7kP0H3Bt9UdCpUKYwkiAN1Cat1K-

## 2. 根据授权码获取token

在 postman 里面

- 请求路径：/auth/oauth2/token
- auth：Basic Auth
    - username：demo-client
    - password：123456
- body: application/x-www-form-urlencoded
    - grant_type=authorization_code
    -
  code=Ats1Af3vpxoOFcKkvwfga4s06ATL8JUJ-PuQXQv3H8pudS3Q0QGCukJ-IkYHK3PDf848MRMbaGEwIukWvKX-aADN8v7_uPCuyNDHiGIUr5Q0-b8t4qemER0Vi8zJxl5b
    - redirect_uri=https%3A%2F%2Fwww.baidu.com

响应：

```json
{
  "access_token": "eyJraWQiOiIwYjFkYzYzMi05ZTY4LTQxMWQtOGZjMy03NmUyZjNiOWNhYWMiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ6aGFuZ3l1IiwiYXVkIjoiZGVtby1jbGllbnQiLCJuYmYiOjE3MTgxNzI3NjQsInNjb3BlIjpbIm9wZW5pZCJdLCJpc3MiOiJodHRwczovLzEyNy4wLjAuMTo0MTAwMC9hdXRoIiwiZXhwIjoxNzE4MTczMDY0LCJpYXQiOjE3MTgxNzI3NjQsImp0aSI6ImYxNjAyMTcyLTZkMGMtNDMzZC1hODMwLTIxMmE1ZGEzMjg2YiJ9.QR3vH0Ly-5toZjr-8xmqWxR_cE58ZrX5Zhlvq4g1cIlSqC1OBBG2gNzD1QJggzQjoxvVxrPvZ6mbCu1QAT1rCTujpIlfbffVSm6q4_B8EmsMRw1HBrVsYTzysjpFG_I6Vi8GZ019XN0iBf1wdfuSF_oAPdV8xmF0pLOUSyw4guuASK7gbCJUPqtUtNEfpJ-ZkJVBqiaNpLvzapEn6fmzAbqtGz_VIluqWw6BAdrYMSyeQx3ODl9Rz5F607k9Leh5yi6Pn3zLhbgZQAj1dsvP79-LgnL7bGJ59HE0thn83hZbhdY019x8Iw9mbfjOJNmHMTfLnyEh7wdZV5OfXP5W9Q",
  "refresh_token": "5dBTM4FsNclNpyKscnMEsH692wccJ_e-NQoN74sCDXAA2QXwi_MdeDAvxEQtZi9_HpY7tdNgU0YiIaaAFr-9ErwQPUwtU1uCvZuZsm1KnVqZLJ6MzVu5ZNO3iMz-iJzO",
  "scope": "openid",
  "id_token": "eyJraWQiOiIwYjFkYzYzMi05ZTY4LTQxMWQtOGZjMy03NmUyZjNiOWNhYWMiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ6aGFuZ3l1IiwiYXVkIjoiZGVtby1jbGllbnQiLCJhenAiOiJkZW1vLWNsaWVudCIsImF1dGhfdGltZSI6MTcxODE3Mjc0NywiaXNzIjoiaHR0cHM6Ly8xMjcuMC4wLjE6NDEwMDAvYXV0aCIsImV4cCI6MTcxODE3NDU2NCwiaWF0IjoxNzE4MTcyNzY0LCJqdGkiOiJkZDQ5ODc3Ny05ZmYzLTRiMWItOTIwOS0wMGJmMjIzNzdmMzkiLCJzaWQiOiI2N3k4M2pLX3ZZMG5KQnM5MExWTHZWWkw1empOQWJoemJXQU9vcXRrQ19VIn0.XWXW-zUCy2DtFd1V95KfIJMQPHsE2d1Z6mQ-7QgcHFxgRECtY3ZInjNvT5fmyo6Rd73FQouY9eRVPq71YbOZRUZJgokZTgefGa8wEN1IVdVJuz9uwZZTEc2sj2nwSTG0DP9FyzCfyf_1qyWsuwe7oRhf19QQdX7x-BY1cPZZz8VLW0p7T-BJjbUyGAf3e9DBUkdgRnWAzgWqBlS-64jxKeqzO4K6o3MwKjRe1mUVuwDZSG1RKQi6CzL-fMYy1SMMeiRb2oEcjLqK6yB02oxIlPMr7DX1EpdwfW-76DQlG6DDSNXa0rQQ_KumV_pYfUM39eS672XSP0hKkgqHjQaiyw",
  "token_type": "Bearer",
  "expires_in": 299
}
```

```json
{
  "access_token": "eyJraWQiOiJmMzcwYTI2Ny1hNmI0LTQ3MmMtYTNkMi05ODkwZGI2MTlhNDUiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ6aGFuZ3l1IiwiYXVkIjoiY2xpZW50LWlkLTEiLCJuYmYiOjE3MTkyMjQyMDQsInNjb3BlIjpbIm1lc3NhZ2UucmVhZCJdLCJpc3MiOiJodHRwczovLzEyNy4wLjAuMTo0MTAwMC9hdXRoIiwiZXhwIjoxNzE5MjMxNDA0LCJpYXQiOjE3MTkyMjQyMDQsImp0aSI6IjczNjk2ZmUyLTIyODYtNDcwMS1hMDBjLTdhYjE4YjI2NWRlNiJ9.auMQPEjtAGSbZv6cq7UjxuH1khYts5D9cYpvU82vaSnY8nrY8Nx4WbRBQIPs9DAEuSgJWtJb_WckeVeQqe4sKW8nTB3_UXgd-K9W1p5zQUDbbXRtTvB5k7BsbrEpGrStKxedM9loNjSCA0VOaUhx-us4uCfMSdPlJhU_RMBUNMpHDA2bMymUxXxEpJOpdYKq_HwNSEfXjDG4qskoRkQiZCjZv20rUMzDr9gPD78GbIeWeXKMPngRAimy-YLJAg8pyrgkFNFhX7oEcfNZqZO_xuL5jA2rPEkSNpcTaIJBuRao9xXjP30DlIGWuDrJn-XHdUUX5MBBYfCxsBiHkoq5iQ",
  "refresh_token": "EkTfpvj-Wwm6q3JBjmheLtDCS2nNCdqZvPUjD9zKm8xNIFSV7rD7g--k7qTYTgCln3Sy9xpT53VuL0h6p9rjceImSENZJfm6iDcjo11J2-iJ76sXjwkRak6M-JIkdmL6",
  "scope": "message.read",
  "token_type": "Bearer",
  "expires_in": 7199
}
```

## 3. 授权码模式刷新令牌

- grant_type：refresh_token 表示刷新令牌
- refresh_token：即前面获取到的 refresh_token 的值

Auth 信息与前面一致

```json
{
  "access_token": "eyJraWQiOiJmMzcwYTI2Ny1hNmI0LTQ3MmMtYTNkMi05ODkwZGI2MTlhNDUiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ6aGFuZ3l1IiwiYXVkIjoiY2xpZW50LWlkLTEiLCJuYmYiOjE3MTkyMjQ1MDMsInNjb3BlIjpbIm1lc3NhZ2UucmVhZCJdLCJpc3MiOiJodHRwczovLzEyNy4wLjAuMTo0MTAwMC9hdXRoIiwiZXhwIjoxNzE5MjMxNzAzLCJpYXQiOjE3MTkyMjQ1MDMsImp0aSI6IjFiNDEwZjFkLTQ1OTktNDdlNC1hZTkzLWFlMDU3YTdjMzUxMyJ9.EDiuRD27_Ivt33a_2yuq_-uneEQ-Z8HvNEJMgEVwMz-Gqgn8SZi3sScyuCzh0tjwRLPjNvky0e294L7Mbby4GBVxJAfYG-zyNm3w_PecCPMGlBuoCNDS5jw-N9QSPZApXUR44vPqAHNXennw1ckBIU3hhryuuM8b5Lwbge-u933CwLnvfhjvHaGtAnDQ8xfo7TJk8E7-R9dr_uRtpJ8dyodKY2rAFeHt4b0UWBenCVUO13yBVIAGt7h6TClakJvfkZq_zOCQ5Ks9t3if2q2Ae8PP6ZEgM7uQk-UmVemalRNxA657yDkBWDhfRMKDyfyeKyVVi20SNuZRYdJL5qdAEA",
  "refresh_token": "EkTfpvj-Wwm6q3JBjmheLtDCS2nNCdqZvPUjD9zKm8xNIFSV7rD7g--k7qTYTgCln3Sy9xpT53VuL0h6p9rjceImSENZJfm6iDcjo11J2-iJ76sXjwkRak6M-JIkdmL6",
  "scope": "message.read",
  "token_type": "Bearer",
  "expires_in": 7199
}
```

> refresh_token 使用一次就会失效

## 4. 客户端模式

同样使用表单格式，grant_type的值为client_credentials

- 请求

```shell
curl --location 'https://127.0.0.1:41000/auth/oauth2/token' \ 
--header 'Authorization: Basic Y2xpZW50LWlkLTE6Y2xpZW50LWlkLTE=' \
--form 'grant_type="client_credentials"'
```

- 响应：

```json
{
  "access_token": "eyJraWQiOiJjNDQ4MjkyZC0yMjc3LTRkNGYtYmQwNi1iZmM2YzI4NmQxOWUiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJjbGllbnQtaWQtMSIsImF1ZCI6ImNsaWVudC1pZC0xIiwibmJmIjoxNzE5Mzg2NTA2LCJpc3MiOiJodHRwczovLzEyNy4wLjAuMTo0MTAwMC9hdXRoIiwiZXhwIjoxNzE5MzkzNzA2LCJpYXQiOjE3MTkzODY1MDYsImp0aSI6ImY2NGI4ZDVjLTY0NzEtNDA1MS05ODQxLTMyNDMxMDE4NjEyMiJ9.j3hyxLj0NEntyYV8E1hWcZRgP9wuWw1rBS_Yhxj2c7H5z90cWcbRNqv-48CxcNwZqgOoGsWfi6dIOR_dwBL2DZNp71-gPL_kPH4C1mBaSyTHxHSoFdb8FkYqOhLC8XzzEhF_DjdWdWSVj0GebRHKsfHDc354HJ950ebh2TOfV2a04HXchZ-4ePQF8G5P4KHPyrKjd55X5MU3gGpFhKoDAxEe2930Gnn2Vd8bBYRLCdvwJZMLi3Ty46BzTrenLwjbSqFxceP1Tmalk8F8ZMvr4-0Qn1R_cKrfPRwcPXMy2fIOknkyJFDxh7Gi3q6Nn-PVuQ1aoMvcDWZPwNrEri-SMA",
  "token_type": "Bearer",
  "expires_in": 7199
}
```