import json

from PIL import Image
from django.shortcuts import render
from django.core import serializers
from django.http import JsonResponse
from django.http import HttpResponse
from ar_server import models
from .models import Img

#python manage.py runserver 0.0.0.0:8080
def upload_score(request):
    if request.method == 'GET':  #传输方式为 GET
        player_name = request.GET.get('name')  #获取字段为“name”的值
        player_score = request.GET.get('score') #获取字段为“score”的值
        models.rank.objects.create(name=player_name, score=player_score)   #添加一条数据仅进rank表
        num=models.rank.objects.count()  #查询rank表中数据的条数
        num = num if num <10 else 10     #取前10条，不足10条就取全部
        models_list=models.rank.objects.all().order_by('-score','time')[:num]  #查询rank表中所有数据,并按score降序、time升序
        rank = serializers.serialize('json', models_list)  #list转化为json 1
        ranks = json.loads(rank)#list转化为json 2
    return JsonResponse(ranks,safe=False)   #返回json数据

def img_with_mask(request):
    if request.method == 'POST':
        img = request.FILES.get('img')
        mask = request.FILES.get('mask')
        with open('data/img.png', 'wb+') as f:
            f.write(img.read())
        with open('data/mask.png', 'wb+') as m:
            m.write(mask.read())
    return HttpResponse('OK')