from django.db import models

class rank(models.Model):                               #创建一个rank类，用于存储id、分数、时间
      name = models.CharField(max_length=11)              #姓名属性，数据类型为Char，最大长度为11
      score = models.IntegerField()                       #分数属性，数据类型为Integer
      time = models.DateTimeField(auto_now_add=True)      #时间属性，数据类型为DateTime，自动设置为当前时间

class Img(models.Model):
      img_url = models.ImageField(upload_to='data/',blank=True,null=True) #指定图片上传路径，即data