from django.contrib import admin
from django.urls import path
from ar_server import views

urlpatterns = [
    path('admin/', admin.site.urls),
    path(r'upload_score',views.upload_score,name='upload_score'), #注册地址
    path(r'img_with_mask',views.img_with_mask,name='img_with_mask'), #注册地址
]