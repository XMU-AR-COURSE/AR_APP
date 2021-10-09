# AR_APP

***Server***文件夹中是服务器代码<br/>
**运行环境**：Pyhton3.7、Django3.0（Python3.7下命令安装Django即可）<br/>
**目录结构**：<br/>
![Image text](https://raw.githubusercontent.com/XMU-AR-COURSE/Img/main/Server%E7%9B%AE%E5%BD%95.png)<br/>
*ar_server*：应用的容器。<br/>
*ar_server/migrations*：数据库迁移文件夹。<br/>
*ar_server/admin.py*：后台admin配置文件。<br/>
*ar_server/models.py*：数据库模型文件。<br/>
*ar_server/views.py*：应用的视图函数处理文件。<br/>
*Server*：项目的容器。<br/>
*Server/asgi.py*：ASGI兼容的Web服务器的入口，以便运行项目。<br/>
*Server/setting.py*：项目配置文件。<br/>
*Server/urls.py*：该Django项目的URL声明，用于管理访问地址。<br/>
*Server/wsgi.py*：WSGI兼容的Web服务器的入口，以便运行项目。<br/>
*manage.py*：命令行工具，可以让开发者以各种方式与Django项目进行交互。<br/>
**运行步骤**：<br/>
1.在Server目录下打开命令行；<br/>
2.数据库迁移命令：<br/> 
```python manage.py makemigrations```<br/>
```python manage.py migrate```<br/> 
3.运行后，服务器与Android需处于同一局域网才能通信：<br/> 
```python manage.py runserver 0.0.0.0:8080```<br/> 
****
***huawei-arengine-android-demo***文件夹中是手机端的Android studoi项目<br/> 
**运行环境**：Java1.8、Android Studio4.0<br/> 
**目录结构**：<br/>
![Image text](https://raw.githubusercontent.com/XMU-AR-COURSE/Img/main/Android%20Demo%E7%9B%AE%E5%BD%95.png)<br/>
*world*存放使用ARWorldTracking开发的环境识别Demo，包含平面检测、虚拟物体放置、平面语义识别等功能。本样例代码是基于该Demo实现的<br/>
*body3d*存放使用ARBodyTracking开发的骨胳识别Demo，包含人体关节点和骨骼识别能力，可以输出四肢端点、身体姿态、人体骨骼等人体特征。<br/>
*face*存放使用ARFaceTracking开发的人脸Mesh绘制Demo，并提取人脸跟踪的数据，包含人脸位置、姿态、人脸模型。<br/>
*hand*存放使用ARHandTracking开发的手部识别Demo，包含手部骨骼坐标数据、手势识别结果等功能。<br/>
*health*存放使用ARHandTracking开发的人脸健康检测Demo，包含健康检测进度和检测状态、 健康检测各项参数等功能。<br/>
*common*存放上述五个Demo所需要的共同java文件。<br/>
**运行步骤**：<br/> 
1.在Android Studio导入工程文件；<br/> 
2.手机接入Android Studio，并运行。<br/> 
****
**objs**文件夹中是Android中所需的模型文件，在目录huawei-arengine-android-demo/HwAREngineDemo/src/main/assets/下 
