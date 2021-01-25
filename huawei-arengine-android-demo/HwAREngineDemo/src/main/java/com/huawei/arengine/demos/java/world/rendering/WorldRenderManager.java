/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.arengine.demos.java.world.rendering;
import com.huawei.arengine.demos.java.world.EndActivity;
import com.huawei.hiar.ARBody;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.huawei.arengine.demos.R;
import com.huawei.arengine.demos.common.ArDemoRuntimeException;
import com.huawei.arengine.demos.common.DisplayRotationManager;
import com.huawei.arengine.demos.common.TextDisplay;
import com.huawei.arengine.demos.common.TextureDisplay;
import com.huawei.arengine.demos.java.world.GestureEvent;
import com.huawei.arengine.demos.java.world.VirtualObject;
import com.huawei.hiar.ARCamera;
import com.huawei.hiar.ARFrame;
import com.huawei.hiar.ARHitResult;
import com.huawei.hiar.ARLightEstimate;
import com.huawei.hiar.ARPlane;
import com.huawei.hiar.ARPoint;
import com.huawei.hiar.ARPose;
import com.huawei.hiar.ARSession;
import com.huawei.hiar.ARTrackable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;  //导入随机函数所需要的包
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class provides rendering management related to the world scene, including
 * label rendering and virtual object rendering management.
 *
 * @author HW
 * @since 2020-03-21
 */
public class WorldRenderManager implements GLSurfaceView.Renderer {
    private static final String TAG = WorldRenderManager.class.getSimpleName();

    private static final int PROJ_MATRIX_OFFSET = 0;

    private static final float PROJ_MATRIX_NEAR = 0.1f;

    private static final float PROJ_MATRIX_FAR = 100.0f;

    private static final float MATRIX_SCALE_SX = -1.0f;

    private static final float MATRIX_SCALE_SY = -1.0f;

    private static final float[] BLUE_COLORS = new float[] {66.0f, 133.0f, 244.0f, 255.0f};

    private static final float[] GREEN_COLORS = new float[] {66.0f, 133.0f, 244.0f, 255.0f};

    private ARSession mSession;  //由WorldAcivity.java传入

    private Activity mActivity; //由WorldAcivity.java传入

    private Context mContext;  //由WorldAcivity.java传入

    private TextView mTextView;   //UI,显示帧数

    private TextView mSearchingTextView;  //UI,显示提示

    private int frames = 0;

    private long lastInterval;

    private float fps;   //帧数

    private TextureDisplay mTextureDisplay = new TextureDisplay();

    private TextDisplay mTextDisplay = new TextDisplay();

    private LabelDisplay mLabelDisplay = new LabelDisplay();

    private ObjectDisplay mObjectDisplay = new ObjectDisplay();  //虚拟模型类

    private DisplayRotationManager mDisplayRotationManager; //管理虚拟模型的旋转

    private ArrayBlockingQueue<GestureEvent> mQueuedSingleTaps;

    private VirtualObject mSelectedObj = null;   //选中虚拟模型

    private ArrayList<VirtualObject> mVirtualObjects = new ArrayList<>();//虚拟模型类List

    int obj_now= -1;  //添加全局变量obj_now，方便调用及修改，-1为初值，即不存在该姿态
    int[] objs={1,2,3,4,5,6};//存储模型名称，最终生成的模型为(obj_now+1).obj
    int objs_length=6; //模型数量+1
    private ArrayList<BodyRelatedDisplay> mBodyRelatedDisplays = new ArrayList<>();//声明表mBodyRelatedDisplays
    long timeflag=0;  //记录第一帧动作匹配的时间戳
    int score=0; //变量，计分
    int time=60;  //变量，游戏总时间，共计60s
    long timestart;//变量，游戏开始时间戳
    private TextView timeTextView;//时间显示UI组件
    private TextView scoreTextView;//变量，分数显示UI组件

    /**
     * The constructor passes context and activity. This method will be called when {@link Activity#onCreate}.
     *
     * @param activity Activity
     * @param context Context
     */
    public WorldRenderManager(Activity activity, Context context) { //赋值mContext与mActivity，并绑定UI组件
        mActivity = activity;
        mContext = context;
        mTextView = activity.findViewById(R.id.wordTextView);
        mSearchingTextView = activity.findViewById(R.id.searchingTextView);
        timeTextView=activity.findViewById(R.id.time);//关联显示时间UI组件
        scoreTextView=activity.findViewById(R.id.score);//关联显示分时UI组件
    }

    /**
     * Set ARSession, which will update and obtain the latest data in OnDrawFrame.
     *
     * @param arSession ARSession.
     */
    public void setArSession(ARSession arSession) {  //赋值mSession
        if (arSession == null) {
            Log.e(TAG, "setSession error, arSession is null!");
            return;
        }
        mSession = arSession;
    }

    /**
     * Set a gesture type queue.
     *
     * @param queuedSingleTaps Gesture type queue.
     */
    public void setQueuedSingleTaps(ArrayBlockingQueue<GestureEvent> queuedSingleTaps) {
        if (queuedSingleTaps == null) {
            Log.e(TAG, "setSession error, arSession is null!");
            return;
        }
        mQueuedSingleTaps = queuedSingleTaps;
    }

    /**
     * Set the DisplayRotationManage object, which will be used in onSurfaceChanged and onDrawFrame.
     *
     * @param displayRotationManager DisplayRotationManage is a customized object.
     */
    public void setDisplayRotationManage(DisplayRotationManager displayRotationManager) { //模型旋转
        if (displayRotationManager == null) {
            Log.e(TAG, "SetDisplayRotationManage error, displayRotationManage is null!");
            return;
        }
        mDisplayRotationManager = displayRotationManager;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) { //UI刷新
        // Set the window color.
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        for (BodyRelatedDisplay bodyRelatedDisplay : mBodyRelatedDisplays) {  //识别人像
            bodyRelatedDisplay.init();
        }
        mTextureDisplay.init();
        mTextDisplay.setListener(new TextDisplay.OnTextInfoChangeListener() {
            @Override
            public void textInfoChanged(String text, float positionX, float positionY) {
                showWorldTypeTextView(text, positionX, positionY);
            }
        });

        mLabelDisplay.init(getPlaneBitmaps());

        mObjectDisplay.init(mContext,obj_now); //传入obj_now参数
    }

    /**
     * Create a thread for text display in the UI thread. This thread will be called back in TextureDisplay.
     *
     * @param text Gesture information displayed on the screen
     * @param positionX The left padding in pixels.
     * @param positionY The right padding in pixels.
     */
    private void showWorldTypeTextView(final String text, final float positionX, final float positionY) { //UI设置
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setTextColor(Color.WHITE);

                // Set the font size to be displayed on the screen.
                mTextView.setTextSize(10f);
                if (text != null) {
                    mTextView.setText(text);
                    mTextView.setPadding((int) positionX, (int) positionY, 0, 0);
                } else {
                    mTextView.setText("");
                }
                time = (int) (60 - (System.currentTimeMillis() - timestart) / 1000); //计算剩余时间
                if (time == 0 || objs_length == 0) { //60s时间结束或者完成7组动作，即为游戏结束
                    score = score + time;  //剩余时间算上额外分数
                    mSession.stop(); //结束ARSession
                    Intent intent = new Intent(mActivity, EndActivity.class);
                    intent.putExtra("sc", String.valueOf(score));//将分数作为参数，传至结算页面
                    mContext.startActivity(intent); //转至结算页面
                }
                timeTextView.setText("时间：" + time + "S"); //UI更新倒计时
                scoreTextView.setText("得分：" + score); //UI更新分数
            }
        });
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {  //UI更新
        mTextureDisplay.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
        mDisplayRotationManager.updateViewportRotation(width, height);
        mObjectDisplay.setSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {   //绘制帧
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mSession == null) {
            return;
        }
        if (mDisplayRotationManager.getDeviceRotation()) {
            mDisplayRotationManager.updateArSessionDisplayGeometry(mSession);
        }

        try {
            mSession.setCameraTextureName(mTextureDisplay.getExternalTextureId());
            ARFrame arFrame = mSession.update();
            ARCamera arCamera = arFrame.getCamera();

            // The size of the projection matrix is 4 * 4.
            float[] projectionMatrix = new float[16];

            arCamera.getProjectionMatrix(projectionMatrix, PROJ_MATRIX_OFFSET, PROJ_MATRIX_NEAR, PROJ_MATRIX_FAR);
            mTextureDisplay.onDrawFrame(arFrame);
            StringBuilder sb = new StringBuilder();
            updateMessageData(sb);
            mTextDisplay.onDrawFrame(sb);

            // The size of ViewMatrix is 4 * 4.
            float[] viewMatrix = new float[16];
            arCamera.getViewMatrix(viewMatrix, 0);
            for (ARPlane plane : mSession.getAllTrackables(ARPlane.class)) {
                if (plane.getType() != ARPlane.PlaneType.UNKNOWN_FACING
                    && plane.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                    hideLoadingMessage();
                    break;
                }
            }
            mLabelDisplay.onDrawFrame(mSession.getAllTrackables(ARPlane.class), arCamera.getDisplayOrientedPose(),
                projectionMatrix);
            handleGestureEvent(arFrame, arCamera, projectionMatrix, viewMatrix);
            ARLightEstimate lightEstimate = arFrame.getLightEstimate();
            float lightPixelIntensity = 1;
            if (lightEstimate.getState() != ARLightEstimate.State.NOT_VALID) {
                lightPixelIntensity = lightEstimate.getPixelIntensity();
            }
            /*
             *接收人像数据，需import java.util.Collection;
           */
           Collection<ARBody> bodies = mSession.getAllTrackables(ARBody.class);
           if (bodies.size() == 0) {
                 mTextDisplay.onDrawFrame(null);
                 return;
           }
            for (ARBody body : bodies) { //分析人像数据，最多两人，bodies.size()=2
                if (body.getTrackingState() != ARTrackable.TrackingState.TRACKING) {
                    continue;
                }
                if (obj_now != -1&&body.getBodyAction() == objs[obj_now] && timeflag == 0) { //初次动作正确记录时间戳
                    timeflag = System.currentTimeMillis();//记录当前时间戳
                } else if (body.getBodyAction() == objs[obj_now]  && (System.currentTimeMillis() - timeflag) / 1000 >= 3) {
                    //动作坚持3秒即算成功
                    /*系统提示音，若是手机震动或静音则不发声*/
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(mContext, notification);
                    r.play();
                    /*震动*/
                    Vibrator vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 10));//震动时间0.1秒，震动强度（1-255）为10

                    _ChangeObj(1);//完成一组动作，随机选择下一组模型
                    score = score + 10; //完成一组动作，总分加10分
                    timeflag = 0;//时间戳重置
                }
                if (body.getBodyAction()  != objs[obj_now]) { //动作不同，时间戳重置为0
                    timeflag = 0;
                }
            }
          drawAllObjects(projectionMatrix, viewMatrix, lightPixelIntensity);
        } catch (ArDemoRuntimeException e) {
            Log.e(TAG, "Exception on the ArDemoRuntimeException!");
        } catch (Throwable t) {
            // This prevents the app from crashing due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread: ", t);
        }
    }

    private void drawAllObjects(float[] projectionMatrix, float[] viewMatrix, float lightPixelIntensity) {  //绘制虚拟模型
        Iterator<VirtualObject> ite = mVirtualObjects.iterator();
        while (ite.hasNext()) {
            VirtualObject obj = ite.next();
            if (obj.getAnchor().getTrackingState() == ARTrackable.TrackingState.STOPPED) {
                ite.remove();
            }
            if (obj.getAnchor().getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                mObjectDisplay.onDrawFrame(viewMatrix, projectionMatrix, lightPixelIntensity, obj);
            }
        }
    }

    private ArrayList<Bitmap> getPlaneBitmaps() {   //更新环境标签
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(getPlaneBitmap(R.id.plane_other));
        bitmaps.add(getPlaneBitmap(R.id.plane_wall));
        bitmaps.add(getPlaneBitmap(R.id.plane_floor));
        bitmaps.add(getPlaneBitmap(R.id.plane_seat));
        bitmaps.add(getPlaneBitmap(R.id.plane_table));
        bitmaps.add(getPlaneBitmap(R.id.plane_ceiling));
        bitmaps.add(getPlaneBitmap(R.id.plane_door));
        bitmaps.add(getPlaneBitmap(R.id.plane_window));
        bitmaps.add(getPlaneBitmap(R.id.plane_bed));
        return bitmaps;
    }

    private Bitmap getPlaneBitmap(int id) {
        TextView view = mActivity.findViewById(id);
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = view.getDrawingCache();
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.setScale(MATRIX_SCALE_SX, MATRIX_SCALE_SY);
        if (bitmap != null) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    /**
     * Update the information to be displayed on the screen.
     *
     * @param sb String buffer.
     */
    private void updateMessageData(StringBuilder sb) {  //更新UI信息
        float fpsResult = doFpsCalculate();
        sb.append("FPS=").append(fpsResult).append(System.lineSeparator());
    }

    private float doFpsCalculate() {  //计算fps
        ++frames;
        long timeNow = System.currentTimeMillis();

        // Convert millisecond to second.
        if (((timeNow - lastInterval) / 1000.0f) > 0.5f) {
            fps = frames / ((timeNow - lastInterval) / 1000.0f);
            frames = 0;
            lastInterval = timeNow;
        }
        return fps;
    }

    private void hideLoadingMessage() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSearchingTextView != null) {
                    mSearchingTextView.setVisibility(View.GONE);
                    mSearchingTextView = null;
                }
            }
        });
    }

    private void handleGestureEvent(ARFrame arFrame, ARCamera arCamera, float[] projectionMatrix, float[] viewMatrix) {
        GestureEvent event = mQueuedSingleTaps.poll();
        if (event == null) {
            return;
        }

        // Do not perform anything when the object is not tracked.
        if (arCamera.getTrackingState() != ARTrackable.TrackingState.TRACKING) {
            return;
        }

        int eventType = event.getType();
        switch (eventType) {
            case GestureEvent.GESTURE_EVENT_TYPE_DOUBLETAP: {
                doWhenEventTypeDoubleTap(viewMatrix, projectionMatrix, event);
                break;
            }
            case GestureEvent.GESTURE_EVENT_TYPE_SCROLL: {
                if (mSelectedObj == null) {
                    break;
                }
                ARHitResult hitResult = hitTest4Result(arFrame, arCamera, event.getEventSecond());
                if (hitResult != null) {
                    mSelectedObj.setAnchor(hitResult.createAnchor());
                }
                break;
            }
            case GestureEvent.GESTURE_EVENT_TYPE_SINGLETAPCONFIRMED: {
                // Do not perform anything when an object is selected.
                if (mSelectedObj != null) {
                    mSelectedObj.setIsSelected(false);
                    mSelectedObj = null;
                }

                MotionEvent tap = event.getEventFirst();
                ARHitResult hitResult = null;

                hitResult = hitTest4Result(arFrame, arCamera, tap);

                if (hitResult == null) {
                    break;
                }
                doWhenEventTypeSingleTap(hitResult);
                break;
            }
            default: {
                Log.e(TAG, "Unknown motion event type, and do nothing.");
            }
        }
    }

    private void doWhenEventTypeDoubleTap(float[] viewMatrix, float[] projectionMatrix, GestureEvent event) {
        if (mSelectedObj != null) {
            mSelectedObj.setIsSelected(false);
            mSelectedObj = null;
        }
        for (VirtualObject obj : mVirtualObjects) {
            if (mObjectDisplay.hitTest(viewMatrix, projectionMatrix, obj, event.getEventFirst())) {
                obj.setIsSelected(true);
                mSelectedObj = obj;
                break;
            }
        }
    }

    private void doWhenEventTypeSingleTap(ARHitResult hitResult) {
        // The hit results are sorted by distance. Only the nearest hit point is valid.
        // Set the number of stored objects to 10 to avoid the overload of rendering and AR Engine.
        if (mVirtualObjects.size() >= 1) { //修改为1，最多出现一个模型
            mVirtualObjects.get(0).getAnchor().detach();
            mVirtualObjects.remove(0);
        }

        ARTrackable currentTrackable = hitResult.getTrackable();
        if (currentTrackable instanceof ARPoint) {
            mVirtualObjects.add(new VirtualObject(hitResult.createAnchor(), BLUE_COLORS));
        } else if (currentTrackable instanceof ARPlane) {
            if(obj_now==-1){  //开始游戏
                timeTextView.setVisibility(View.VISIBLE);//显示倒计时
                scoreTextView.setVisibility(View.VISIBLE);//显示分数
                timestart=System.currentTimeMillis();//记录开始游戏时间戳
            }
            _ChangeObj(0); ///随机生成模型，第一次生成或用户主动跳过当前模型，传参0
            mVirtualObjects.add(new VirtualObject(hitResult.createAnchor(), GREEN_COLORS));
        } else {
            Log.i(TAG, "Hit result is not plane or point.");
        }
    }

    private ARHitResult hitTest4Result(ARFrame frame, ARCamera camera, MotionEvent event) {   //操作屏幕
        ARHitResult hitResult = null;
        List<ARHitResult> hitTestResults = frame.hitTest(event);

        for (int i = 0; i < hitTestResults.size(); i++) {
            // Determine whether the hit point is within the plane polygon.
            ARHitResult hitResultTemp = hitTestResults.get(i);
            if (hitResultTemp == null) {
                continue;
            }
            ARTrackable trackable = hitResultTemp.getTrackable();

            boolean isPlanHitJudge =
                trackable instanceof ARPlane && ((ARPlane) trackable).isPoseInPolygon(hitResultTemp.getHitPose())
                    && (calculateDistanceToPlane(hitResultTemp.getHitPose(), camera.getPose()) > 0);

            // Determine whether the point cloud is clicked and whether the point faces the camera.
            boolean isPointHitJudge = trackable instanceof ARPoint
                && ((ARPoint) trackable).getOrientationMode() == ARPoint.OrientationMode.ESTIMATED_SURFACE_NORMAL;

            // Select points on the plane preferentially.
            if (isPlanHitJudge || isPointHitJudge) {
                hitResult = hitResultTemp;
                if (trackable instanceof ARPlane) {
                    break;
                }
            }
        }
        return hitResult;
    }

    /**
     * Calculate the distance between a point in a space and a plane. This method is used
     * to calculate the distance between a camera in a space and a specified plane.
     *
     * @param planePose ARPose of a plane.
     * @param cameraPose ARPose of a camera.
     * @return Calculation results.
     */
    private static float calculateDistanceToPlane(ARPose planePose, ARPose cameraPose) {
        // The dimension of the direction vector is 3.
        float[] normals = new float[3];

        // Obtain the unit coordinate vector of a normal vector of a plane.
        planePose.getTransformedAxis(1, 1.0f, normals, 0);

        // Calculate the distance based on projection.
        return (cameraPose.tx() - planePose.tx()) * normals[0] // 0:x
            + (cameraPose.ty() - planePose.ty()) * normals[1] // 1:y
            + (cameraPose.tz() - planePose.tz()) * normals[2]; // 2:z
    }
    private void _ChangeObj(int f){ //随机修改三维模型
        objs_length=objs_length-f;  //每次完成，则减少1个
        if(f==1){    //每次完成，将对应的obj设置为-1，即当做不存在
            objs[obj_now]=-1;
        }
        obj_now=new Random().nextInt(6); //模型数量+1，下标随机从0-5改为0-7
        while (objs[obj_now]==-1&&objs_length>0){//随机查找未完成的模型
            obj_now=new Random().nextInt(6); //模型数量+1，下标随机从0-5改为0-7
        }
        mObjectDisplay.init(mContext,objs[obj_now]);//生成模型
    }
}