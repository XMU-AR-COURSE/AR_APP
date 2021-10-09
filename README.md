# AR_APP

**Server**文件夹中是服务器代码<br/>
***运行环境***：Pyhton3.7、Django3.0（Python3.7下命令安装Django即可）<br/>
***目录结构***：<br/>
***运行步骤***：<br/>
1.在Server目录下打开命令行；<br/>
2.数据库迁移命令：<br/> 
```python manage.py makemigrations```<br/>
```python manage.py migrate```<br/> 
3.运行后，服务器与Android需处于同一局域网才能通信：<br/> 
```python manage.py runserver 0.0.0.0:8080```<br/> 
****
**huawei-arengine-android-demo**文件夹中是手机端的Android studoi项目<br/> 
***运行环境***：Java1.8、Android Studio4.0<br/> 
***目录结构***：<br/>
***运行步骤***：<br/> 
1.在Android Studio导入工程文件；<br/> 
2.手机接入Android Studio，并运行。<br/> 
****
**objs**文件夹中是Android中所需的模型文件，在目录huawei-arengine-android-demo/HwAREngineDemo/src/main/assets/下 
